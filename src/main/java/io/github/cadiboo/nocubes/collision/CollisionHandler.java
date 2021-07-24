package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.mesh.OldNoCubes;
import io.github.cadiboo.nocubes.mesh.SurfaceNets;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.util.AxisRotation;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.ICollisionReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.function.Predicate;

import static net.minecraft.util.math.BlockPos.*;

public final class CollisionHandler {

	public static double collideAxisInArea(
		AxisAlignedBB aabb, IWorldReader world, double motion, ISelectionContext ctx,
		AxisRotation rotation, AxisRotation inverseRotation, Mutable pos,
		int minX, int maxX, int minY, int maxY, int minZ, int maxZ
	) {
		if (world instanceof World)
			((World) world).getProfiler().push("NoCubes collisions");
		try {
			double[] motionRef = {motion};
			Axis axis = inverseRotation.cycle(Axis.Z);
			Predicate<VoxelShape> predicate = shape -> {
				assert Math.abs(motionRef[0]) >= 1.0E-7D;
				motionRef[0] = shape.collide(axis, aabb, motionRef[0]);
				if (Math.abs(motionRef[0]) < 1.0E-7D) {
					motionRef[0] = 0;
					return false;
				}
				return true;
			};

			// NB: minZ and maxZ may be swapped depending on if the motion is positive or not
			forEachCollisionShapeRelativeToStart(world, pos, minX, maxX, minY, maxY, Math.min(minZ, maxZ), Math.max(minZ, maxZ), predicate);
			return motionRef[0];
		} catch (Throwable t) {
			if (!ModUtil.IS_DEVELOPER_WORKSPACE.get())
				throw t;
			return motion;
		} finally {
			if (world instanceof World)
				((World) world).getProfiler().pop();
		}
	}

	public static void forEachCollisionShapeRelativeToStart(ICollisionReader world, Mutable pos, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Predicate<VoxelShape> predicate) {
		forEachCollisionRelativeToStart(world, pos, minX, maxX, minY, maxY, minZ, maxZ,
			(x0, y0, z0, x1, y1, z1) -> predicate.test(VoxelShapes.box(x0, y0, z0, x1, y1, z1))
		);
	}

	public static void forEachCollisionRelativeToStart(ICollisionReader world, Mutable pos, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, IShapeConsumer consumer) {
		assert NoCubesConfig.Server.collisionsEnabled;
		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;

		BlockPos start = new BlockPos(minX, minY, minZ);
		// Size is mutable and only correct until the Area constructor call
		BlockPos size = pos.set(
			maxX - minX,
			maxY - minY,
			maxZ - minZ
		);
		try (Area area = new Area(world, start, size, generator)) {
			// See MeshGenerator#translateToMeshStart for an explanation of this
			double dx = MeshGenerator.validateMeshOffset(area.start.getX() - start.getX());
			double dy = MeshGenerator.validateMeshOffset(area.start.getY() - start.getY());
			double dz = MeshGenerator.validateMeshOffset(area.start.getZ() - start.getZ());
			generate(area, generator, (x0, y0, z0, x1, y1, z1) -> consumer.accept(
				x0 + dx, y0 + dy, z0 + dz,
				x1 + dx, y1 + dy, z1 + dz
			));
		}
	}

	public static void generate(Area area, MeshGenerator generator, IShapeConsumer consumer) {
		Face vertexNormals = new Face();
		Vec faceNormal = new Vec();
		Vec centre = new Vec();
		Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable; // + || isLeavesSmoothable
		generator.generate(area, isSmoothable, (pos, amount) -> {
			// Generate collisions for blocks that are fully inside the isosurface
			// The face handler will generate collisions for the surface
			if (amount == 1) {
				float x0 = pos.getX();
				float y0 = pos.getY();
				float z0 = pos.getZ();
				if (!NoCubesConfig.Server.extraSmoothMesh && generator instanceof SurfaceNets) {
					// Pretty disgusting, see the comments in SurfaceNets about densities and corners for why this offset exists
					x0 += 0.5F;
					y0 += 0.5F;
					z0 += 0.5F;
				}
				return consumer.accept(
					x0, y0, z0,
					x0 + 1, y0 + 1, z0 + 1
				);
			}
			return true;
		}, (pos, face) -> {
			face.assignAverageTo(centre);
			face.assignNormalTo(vertexNormals);
			vertexNormals.assignAverageTo(faceNormal);
			if (generator instanceof OldNoCubes)
				// Keeps flat surfaces collidable but also allows super rough terrain
				faceNormal.multiply(0.00001F);

			if (isSmoothable.test(area.getBlockState(pos))) {
				int x0 = pos.getX();
				int y0 = pos.getY();
				int z0 = pos.getZ();
				return consumer.accept(
					x0, y0, z0,
					x0 + 1, y0 + 1, z0 + 1
				);
			}

//			if (!generateShape(centre, faceNormal, consumer, face.v0))
//				return false;
//			if (!generateShape(centre, faceNormal, consumer, face.v1))
//				return false;
//			if (!generateShape(centre, faceNormal, consumer, face.v2))
//				return false;
//			if (!generateShape(centre, faceNormal, consumer, face.v3))
//				return false;
			return true;
		});
	}

	private static boolean generateShape(Vec centre, Vec faceNormal, IShapeConsumer consumer, Vec v) {
		return consumer.accept(
			v.x, v.y, v.z,
			centre.x + faceNormal.x, centre.y + faceNormal.y, centre.z + faceNormal.z
		);
	}

	public interface IShapeConsumer {

		/**
		 * Return if more shapes should be generated.
		 */
		boolean accept(
			double x0, double y0, double z0,
			double x1, double y1, double z1
		);

	}

}
