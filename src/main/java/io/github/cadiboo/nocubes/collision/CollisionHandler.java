package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.Mesher;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ICollisionReader;
import net.minecraft.world.World;

import java.util.function.Predicate;

/**
 * Collisions sanity check:
 * - Entities shouldn't fall through the floor ({@link io.github.cadiboo.nocubes.mixin.BlockStateBaseMixin#nocubes_getCollisionShape})
 * - Player should be able to walk into and out of single block holes ({@link io.github.cadiboo.nocubes.mixin.BlockStateBaseMixin#nocubes_hasLargeCollisionShape})
 * - Player should be able to walk up slopes made out of normally solid blocks like stone/dirt/grass/sand ({@link io.github.cadiboo.nocubes.mixin.BlockStateBaseMixin#nocubes_hasLargeCollisionShape})
 * - Player should be able to swim through smooth blocks underwater without suffocating (e.g. near the shore) ({@link io.github.cadiboo.nocubes.mixin.BlockCollisionsMixin#nocubes_isSuffocating})
 * - Player should not suffocate when inside the voxel of a smooth block but not inside its new shape (same as above)
 * - Player should suffocate when inside the voxel of a smooth block and inside its new shape (same as above)
 * - Suffocation overlay should only show when inside the voxel and new shape of a smooth block ({@link io.github.cadiboo.nocubes.mixin.ScreenEffectRendererMixin#nocubes_isViewBlocking})
 * - 3rd person camera should not be super zoomed-in when only partially inside a smooth voxel ({@link io.github.cadiboo.nocubes.mixin.BlockStateBaseMixin#nocubes_getVisualShape})
 * - Dirt path/Farmland turning to dirt should not crash the game
 * - Falling blocks (sand/gravel) should not break when they fall
 * - Player should be able to place redstone on slopes of smooth blocks
 * - Player should be able to place snow on slopes of smooth blocks (currently broken)
 */
public final class CollisionHandler {

	public static VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos blockPos, ISelectionContext context) {
		boolean canCollide = state.getBlock().hasCollision;
		try {
			return getCollisionShapeOrThrow(canCollide, state, reader, blockPos, (EntitySelectionContext) context);
		} catch (Throwable t) {
			Util.pauseInIde(t);
			throw t;
		}
	}

	// TODO: Why is the 'cache' of every blockstate storing an empty VoxelShape... this is causing issues like
	//  grass paths turning to dirt causing a crash because dirt's VoxelShape is empty
	//  and not being able to place snow anywhere ('Block.doesSideFillSquare' is returning false for a flat area of stone)
	public static VoxelShape getCollisionShapeOrThrow(boolean canCollide, BlockState state, IBlockReader reader, BlockPos blockPos, EntitySelectionContext context) {
		if (!canCollide)
			return VoxelShapes.empty();
		// assert NoCubesConfig.Server.collisionsEnabled; // This is called from debug code & the ScreenEffectRenderer
		assert NoCubes.smoothableHandler.isSmoothable(state);

//		if (context.getEntity() instanceof PlayerEntity)
//			// Noclip for debugging
//			return VoxelShapes.empty();

		Entity entity = context.getEntity();
		if (entity instanceof FallingBlockEntity || // Stop sand etc. breaking when it falls
			(NoCubesConfig.Server.tempMobCollisionsDisabled && !(entity instanceof PlayerEntity)) ||
			// Stop grass path turning to dirt causing a crash from trying to turn an empty VoxelShape into an AABB
			(entity == null && reader.getBlockState(blockPos) != state)
		)
			return state.getShape(reader, blockPos);

		Mesher mesher = NoCubesConfig.Server.mesher;
		VoxelShape[] ref = new VoxelShape[]{VoxelShapes.empty()};
		if (reader instanceof World)
			((World) reader).getProfiler().push("NoCubes collisions");
		try (Area area = new Area(reader, blockPos, ModUtil.VEC_ONE, mesher)) {
			// See Mesher#translateToMeshStart for an explanation of this
			float dx = Mesher.validateMeshOffset(area.start.getX() - blockPos.getX());
			float dy = Mesher.validateMeshOffset(area.start.getY() - blockPos.getY());
			float dz = Mesher.validateMeshOffset(area.start.getZ() - blockPos.getZ());
			generate(area, mesher, (x0, y0, z0, x1, y1, z1) -> {
				VoxelShape shape = VoxelShapes.box(
					dx + x0, dy + y0, dz + z0,
					dx + x1, dy + y1, dz + z1
				);
				ref[0] = VoxelShapes.joinUnoptimized(ref[0], shape, IBooleanFunction.OR);
				return true;
			});
		} finally {
			if (reader instanceof World)
				((World) reader).getProfiler().pop();
		}
		return ref[0];//.optimize();
	}

	// region indev

	public static void forEachCollisionShapeRelativeToStart(ICollisionReader world, BlockPos.Mutable pos, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Predicate<VoxelShape> predicate) {
		forEachCollisionRelativeToStart(world, pos, minX, maxX, minY, maxY, minZ, maxZ,
			(x0, y0, z0, x1, y1, z1) -> predicate.test(VoxelShapes.box(x0, y0, z0, x1, y1, z1))
		);
	}

	public static void forEachCollisionRelativeToStart(ICollisionReader world, BlockPos.Mutable pos, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, ShapeConsumer consumer) {
		Mesher mesher = NoCubesConfig.Server.mesher;

		BlockPos start = new BlockPos(minX, minY, minZ);
		// Size is mutable and only correct until the Area constructor call
		BlockPos.Mutable size = pos.set(
			maxX - minX,
			maxY - minY,
			maxZ - minZ
		);
		try (Area area = new Area(world, start, size, mesher)) {
			// See Mesher#translateToMeshStart for an explanation of this
			float dx = Mesher.validateMeshOffset(area.start.getX() - start.getX());
			float dy = Mesher.validateMeshOffset(area.start.getY() - start.getY());
			float dz = Mesher.validateMeshOffset(area.start.getZ() - start.getZ());
			generate(area, mesher, (x0, y0, z0, x1, y1, z1) -> consumer.accept(
				dx + x0, dy + y0, dz + z0,
				dx + x1, dy + y1, dz + z1
			));
		}
	}

	// endregion indev


	public static void generate(Area area, Mesher mesher, ShapeConsumer consumer) {
		mesher.generateCollisions(area, NoCubes.smoothableHandler::isSmoothable, consumer);
	}

	public static boolean generateShapes(Vec centre, Vec faceNormal, ShapeConsumer consumer, Face face) {
		if (!CollisionHandler.generateShape(centre, faceNormal, consumer, face.v0))
			return false;
		if (!CollisionHandler.generateShape(centre, faceNormal, consumer, face.v1))
			return false;
		if (!CollisionHandler.generateShape(centre, faceNormal, consumer, face.v2))
			return false;
		if (!CollisionHandler.generateShape(centre, faceNormal, consumer, face.v3))
			return false;
		return true;
	}

	private static boolean generateShape(Vec centre, Vec faceNormal, ShapeConsumer consumer, Vec v) {
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

}
