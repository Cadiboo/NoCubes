package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.Mesher;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Collisions sanity check:
 * - Entities shouldn't fall through the floor ({@link io.github.cadiboo.nocubes.mixin.BlockCollisionsMixin#nocubes_computeNext})
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
// TODO: Improve method names and cleanup
public final class SmoothShapes {

	// TODO: Incorporate hacks for FallingBlocks and tempMobCollisionsDisabled

	// Not for use by the collisions system, simply
	// public static VoxelShape createNoCubesVoxelShape(BlockGetter world, BlockPos pos)

	public static VoxelShape shapeForSmoothBlock(BlockState state, BlockGetter reader, BlockPos blockPos, CollisionContext context) {
		boolean canCollide = state.getBlock().hasCollision;
		try {
			return getCollisionShapeOrThrow(canCollide, state, reader, blockPos, (EntityCollisionContext) context);
		} catch (Throwable t) {
			Util.pauseInIde(t);
			throw t;
		}
	}

	// TODO: Why is the 'cache' of every blockstate storing an empty VoxelShape... this is causing issues like
	//  grass paths turning to dirt causing a crash because dirt's VoxelShape is empty
	//  and not being able to place snow anywhere ('Block.doesSideFillSquare' is returning false for a flat area of stone)
	public static VoxelShape getCollisionShapeOrThrow(boolean canCollide, BlockState state, BlockGetter reader, BlockPos blockPos, EntityCollisionContext context) {
		if (!canCollide)
			return Shapes.empty();
		// assert NoCubesConfig.Server.collisionsEnabled; // This is called from debug code & the ScreenEffectRenderer
		assert NoCubes.smoothableHandler.isSmoothable(state);

//		if (context.getEntity() instanceof PlayerEntity)
//			// Noclip for debugging
//			return VoxelShapes.empty();

		var entity = context.getEntity();
		if (entity instanceof FallingBlockEntity || // Stop sand etc. breaking when it falls
			(NoCubesConfig.Server.tempMobCollisionsDisabled && !(entity instanceof Player)) ||
			// Stop grass path turning to dirt causing a crash from trying to turn an empty VoxelShape into an AABB
			(entity == null && reader.getBlockState(blockPos) != state)
		)
			return state.getShape(reader, blockPos);

		var mesher = NoCubesConfig.Server.mesher;
		var ref = new VoxelShape[]{Shapes.empty()};
		if (reader instanceof Level)
			((Level) reader).getProfiler().push("NoCubes collisions");
		try (var area = new Area(reader, blockPos, ModUtil.VEC_ONE, mesher)) {
			// See Mesher#translateToMeshStart for an explanation of this
			var dx = Mesher.validateMeshOffset(area.start.getX() - blockPos.getX());
			var dy = Mesher.validateMeshOffset(area.start.getY() - blockPos.getY());
			var dz = Mesher.validateMeshOffset(area.start.getZ() - blockPos.getZ());
			generate(area, mesher, (x0, y0, z0, x1, y1, z1) -> {
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

	public static <T> Deque<T> createNoCubesIntersectingCollisionList(CollisionGetter world, AABB area, MutableBlockPos pos, boolean onlySuffocatingBlocks, BiFunction<MutableBlockPos, VoxelShape, T> resultProvider) {
		Deque<T> shapes = new ArrayDeque<T>();
		if (onlySuffocatingBlocks) {
			// Stops grass path collisions being broken.
			// How? I don't really know:
			// It's got something to do with {@link Player#moveTowardsClosestSpace} only checking suffocating blocks.
			// Returning false from 'isSuffocating' stops smooth blocks being included in the blocks checked by
			// {@link BlockCollisions#computeNext()} when called by the Player method.
			//
			// @implNote We don't change the base {@link BlockState#isSuffocating(BlockGetter, BlockPos)} method because it's used
			// in {@link Player#freeAt} and {@link net.minecraft.world.entity.Entity#isInWall} which we don't want to mess up.
			return shapes; // TODO: WHY WHY WHY DOES EVERYTHING BREAK IF THIS ISN'T HERE (read javadoc on deleted nocubes_isSuffocating method)
		}

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
				shapes.add(resultProvider.apply(pos.set(x0, y0, z0), Shapes.box(x0, y0, z0, x1, y1, z1)));
			return true;
		});
		return shapes;
	}

	public static void forEachCollisionShapeRelativeToStart(CollisionGetter world, MutableBlockPos pos, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Predicate<VoxelShape> predicate) {
		forEachCollisionRelativeToStart(world, pos, minX, maxX, minY, maxY, minZ, maxZ,
			(x0, y0, z0, x1, y1, z1) -> predicate.test(Shapes.box(x0, y0, z0, x1, y1, z1))
		);
	}

	public static void forEachCollisionRelativeToStart(CollisionGetter world, MutableBlockPos pos, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, ShapeConsumer consumer) {
		var mesher = NoCubesConfig.Server.mesher;

		var start = new BlockPos(minX, minY, minZ);
		// Size is mutable and only correct until the Area constructor call
		var size = pos.set(
			maxX - minX,
			maxY - minY,
			maxZ - minZ
		);
		try (var area = new Area(world, start, size, mesher)) {
			// See Mesher#translateToMeshStart for an explanation of this
			var dx = Mesher.validateMeshOffset(area.start.getX() - start.getX());
			var dy = Mesher.validateMeshOffset(area.start.getY() - start.getY());
			var dz = Mesher.validateMeshOffset(area.start.getZ() - start.getZ());
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
		if (!SmoothShapes.generateShape(centre, faceNormal, consumer, face.v0))
			return false;
		if (!SmoothShapes.generateShape(centre, faceNormal, consumer, face.v1))
			return false;
		if (!SmoothShapes.generateShape(centre, faceNormal, consumer, face.v2))
			return false;
		if (!SmoothShapes.generateShape(centre, faceNormal, consumer, face.v3))
			return false;
		return true;
	}

	private static boolean generateShape(Vec centre, Vec faceNormal, ShapeConsumer consumer, Vec v) {
		var vX = v.x;
		var vY = v.y;
		var vZ = v.z;
		var extX = centre.x + faceNormal.x;
		var extY = centre.y + faceNormal.y;
		var extZ = centre.z + faceNormal.z;
		return consumer.accept(
			Math.min(vX, extX), Math.min(vY, extY), Math.min(vZ, extZ),
			Math.max(vX, extX), Math.max(vY, extY), Math.max(vZ, extZ)
		);
	}

}
