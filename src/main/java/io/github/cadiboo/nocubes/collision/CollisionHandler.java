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
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.function.Predicate;

public final class CollisionHandler {

	public static VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos blockPos, ISelectionContext context) {
		boolean canCollide = state.getBlock().hasCollision;
		try {
			return getCollisionShapeOrThrow(canCollide, state, reader, blockPos, context);
		} catch (Throwable t) {
			if (!ModUtil.IS_DEVELOPER_WORKSPACE.get())
				throw t;
			return canCollide ? VoxelShapes.block() : VoxelShapes.empty();
		}
	}

	// TODO: Why is the 'cache' of every blockstate storing an empty VoxelShape... this is causing issues like
	//  grass paths turning to dirt causing a crash because dirt's VoxelShape is empty
	//  and not being able to place snow anywhere ('Block.doesSideFillSquare' is returning false for a flat area of stone)
	public static VoxelShape getCollisionShapeOrThrow(boolean canCollide, BlockState state, IBlockReader reader, BlockPos blockPos, ISelectionContext context) {
		if (!canCollide)
			return VoxelShapes.empty();
		if (!NoCubesConfig.Server.collisionsEnabled || !NoCubes.smoothableHandler.isSmoothable(state))
			return state.getShape(reader, blockPos);
		Entity entity = context.getEntity();
//		if (entity instanceof PlayerEntity)
//			// Noclip for debugging
//			return VoxelShapes.empty();
		if (entity instanceof FallingBlockEntity)
			// Stop sand etc. breaking when it falls
			return state.getShape(reader, blockPos);
		if (entity == null || reader.getBlockState(blockPos) != state)
			// Stop grass path turning to dirt causing a crash from trying to turn an empty VoxelShape into an AABB
			return state.getShape(reader, blockPos);

		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;
		VoxelShape[] ref = {VoxelShapes.empty()};
		if (reader instanceof World)
			((World) reader).getProfiler().push("NoCubes collisions");
		try (Area area = new Area(reader, blockPos, ModUtil.VEC_ONE, generator)) {
			// See MeshGenerator#translateToMeshStart for an explanation of this
			float dx = MeshGenerator.validateMeshOffset(area.start.getX() - blockPos.getX());
			float dy = MeshGenerator.validateMeshOffset(area.start.getY() - blockPos.getY());
			float dz = MeshGenerator.validateMeshOffset(area.start.getZ() - blockPos.getZ());
			generate(area, generator, (x0, y0, z0, x1, y1, z1) -> {
				VoxelShape shape = VoxelShapes.box(x0 + dx, y0 + dy, z0 + dz, x1 + dx, y1 + dy, z1 + dz);
				ref[0] = VoxelShapes.joinUnoptimized(ref[0], shape, IBooleanFunction.OR);
			});
		} finally {
			if (reader instanceof World)
				((World) reader).getProfiler().pop();
		}
		return ref[0];//.optimize();
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
				if (generator instanceof SurfaceNets) {
					// Pretty disgusting, see the comments in SurfaceNets about densities and corners for why this offset exists
					x0 += 0.5F;
					y0 += 0.5F;
					z0 += 0.5F;
				}
				consumer.accept(
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

			generateShape(centre, faceNormal, consumer, face.v0);
			generateShape(centre, faceNormal, consumer, face.v1);
			generateShape(centre, faceNormal, consumer, face.v2);
			generateShape(centre, faceNormal, consumer, face.v3);
			return true;
		});
	}

	private static void generateShape(Vec centre, Vec faceNormal, IShapeConsumer consumer, Vec v) {
		consumer.accept(
			v.x, v.y, v.z,
			centre.x + faceNormal.x, centre.y + faceNormal.y, centre.z + faceNormal.z
		);
	}

	public interface IShapeConsumer {

		void accept(
			float x0, float y0, float z0,
			float x1, float y1, float z1
		);

	}

}
