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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Predicate;

import static net.minecraft.core.BlockPos.*;

import net.minecraft.core.BlockPos.MutableBlockPos;

public final class CollisionHandler {

	public static VoxelShape getCollisionShape(BlockState state, BlockGetter reader, BlockPos blockPos, CollisionContext context) {
		boolean canCollide = state.getBlock().hasCollision;
		try {
			return getCollisionShapeOrThrow(canCollide, state, reader, blockPos, (EntityCollisionContext) context);
		} catch (Throwable t) {
			if (!ModUtil.IS_DEVELOPER_WORKSPACE.get())
				throw t;
			return canCollide ? Shapes.block() : Shapes.empty();
		}
	}

	// TODO: Why is the 'cache' of every blockstate storing an empty VoxelShape... this is causing issues like
	//  grass paths turning to dirt causing a crash because dirt's VoxelShape is empty
	//  and not being able to place snow anywhere ('Block.doesSideFillSquare' is returning false for a flat area of stone)
	public static VoxelShape getCollisionShapeOrThrow(boolean canCollide, BlockState state, BlockGetter reader, BlockPos blockPos, EntityCollisionContext context) {
		if (!canCollide)
			return Shapes.empty();
		assert NoCubesConfig.Server.collisionsEnabled;
		assert NoCubes.smoothableHandler.isSmoothable(state);

//		if (context.getEntity() instanceof PlayerEntity)
//			// Noclip for debugging
//			return VoxelShapes.empty();

		var entity = context.getEntity();
		if (entity instanceof FallingBlockEntity || // Stop sand etc. breaking when it falls
			// Stop grass path turning to dirt causing a crash from trying to turn an empty VoxelShape into an AABB
			entity == null || reader.getBlockState(blockPos) != state
		)
			return state.getShape(reader, blockPos);

		var generator = NoCubesConfig.Server.meshGenerator;
		var ref = new VoxelShape[]{Shapes.empty()};
		if (reader instanceof Level)
			((Level) reader).getProfiler().push("NoCubes collisions");
		try (var area = new Area(reader, blockPos, ModUtil.VEC_ONE, generator)) {
			// See MeshGenerator#translateToMeshStart for an explanation of this
			var dx = MeshGenerator.validateMeshOffset(area.start.getX() - blockPos.getX());
			var dy = MeshGenerator.validateMeshOffset(area.start.getY() - blockPos.getY());
			var dz = MeshGenerator.validateMeshOffset(area.start.getZ() - blockPos.getZ());
			generate(area, generator, (x0, y0, z0, x1, y1, z1) -> {
				var shape = Shapes.box(
					dx + x0, dy + y0, dz + z0,
					dx + x1, dy + y1, dz + z1
				);
				ref[0] = Shapes.joinUnoptimized(ref[0], shape, BooleanOp.OR);
				return true;
			});
		} finally {
			if (reader instanceof Level)
				((Level) reader).getProfiler().pop();
		}
		return ref[0];//.optimize();
	}

	// region indev

	public static Deque<VoxelShape> createNoCubesIntersectingCollisionList(CollisionGetter world, AABB area, MutableBlockPos pos) {
		Deque<VoxelShape> shapes = new ArrayDeque<>();
		int minX = Mth.floor(area.minX - 1.0E-7D) - 1;
		int maxX = Mth.floor(area.maxX + 1.0E-7D) + 1;
		int minY = Mth.floor(area.minY - 1.0E-7D) - 1;
		int maxY = Mth.floor(area.maxY + 1.0E-7D) + 1;
		int minZ = Mth.floor(area.minZ - 1.0E-7D) - 1;
		int maxZ = Mth.floor(area.maxZ + 1.0E-7D) + 1;
		forEachCollisionRelativeToStart(world, pos, minX, maxX, minY, maxY, minZ, maxZ, (x0, y0, z0, x1, y1, z1) -> {
			x0 += minX;
			x1 += minX;
			y0 += minY;
			y1 += minY;
			z0 += minZ;
			z1 += minZ;
			if (area.intersects(x0, y0, z0, x1, y1, z1))
				shapes.add(Shapes.box(x0, y0, z0, x1, y1, z1));
			return true;
		});
		return shapes;
	}

	public static double collideAxisInArea(
		AABB aabb, LevelReader world, double motion, CollisionContext ctx,
		AxisCycle rotation, AxisCycle inverseRotation, MutableBlockPos pos,
		int minX, int maxX, int minY, int maxY, int minZ, int maxZ
	) {
		if (world instanceof Level)
			((Level) world).getProfiler().push("NoCubes collisions");
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
			if (world instanceof Level)
				((Level) world).getProfiler().pop();
		}
	}

	public static void forEachCollisionShapeRelativeToStart(CollisionGetter world, MutableBlockPos pos, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Predicate<VoxelShape> predicate) {
		forEachCollisionRelativeToStart(world, pos, minX, maxX, minY, maxY, minZ, maxZ,
			(x0, y0, z0, x1, y1, z1) -> predicate.test(Shapes.box(x0, y0, z0, x1, y1, z1))
		);
	}

	public static void forEachCollisionRelativeToStart(CollisionGetter world, MutableBlockPos pos, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, IShapeConsumer consumer) {
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
				dx + x0, dy + y0, dz + z0,
				dx + x1, dy + y1, dz + z1
			));
		}
	}

	// endregion indev

	public static void generate(Area area, MeshGenerator generator, IShapeConsumer consumer) {
		Face vertexNormals = new Face();
		Vec faceNormal = new Vec();
		Vec centre = new Vec();
		Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
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

//			if (isSmoothable.test(area.getBlockState(pos))) {
//				int x0 = pos.getX();
//				int y0 = pos.getY();
//				int z0 = pos.getZ();
//				return consumer.accept(
//					x0, y0, z0,
//					x0 + 1, y0 + 1, z0 + 1
//				);
//			}

			if (!generateShape(centre, faceNormal, consumer, face.v0))
				return false;
			if (!generateShape(centre, faceNormal, consumer, face.v1))
				return false;
			if (!generateShape(centre, faceNormal, consumer, face.v2))
				return false;
			if (!generateShape(centre, faceNormal, consumer, face.v3))
				return false;
			return true;
		});
	}

	private static boolean generateShape(Vec centre, Vec faceNormal, IShapeConsumer consumer, Vec v) {
		float vX = v.x;
		float vY = v.y;
		float vZ = v.z;
		float extX = centre.x + faceNormal.x;
		float extY = centre.y + faceNormal.y;
		float extZ = centre.z + faceNormal.z;
		return consumer.accept(
			Math.min(vX, extX), Math.min(vY, extY), Math.min(vZ, extZ),
			Math.max(vX, extX), Math.max(vY, extY), Math.max(vZ, extZ)
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
