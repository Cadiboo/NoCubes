package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesBlockType;
import io.github.cadiboo.nocubes.mesh.Mesher;
import io.github.cadiboo.nocubes.mixin.client.ScreenEffectRendererMixin;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Predicate;

/**
 * Collisions sanity check:
 * - Entities shouldn't fall through the floor ({@link io.github.cadiboo.nocubes.mixin.BlockStateBaseMixin#noCubes$getCollisionShape})
 * - Player should be able to walk into and out of single block holes ({@link io.github.cadiboo.nocubes.mixin.BlockStateBaseMixin#noCubes$hasLargeCollisionShape})
 * - Player should be able to walk up slopes made out of normally solid blocks like stone/dirt/grass/sand ({@link io.github.cadiboo.nocubes.mixin.BlockStateBaseMixin#noCubes$hasLargeCollisionShape})
 * - Player should be able to swim through smooth blocks underwater without suffocating (e.g. near the shore) ({@link io.github.cadiboo.nocubes.mixin.BlockCollisionsMixin#noCubes$isSuffocating})
 * - Player should not suffocate when inside the voxel of a smooth block but not inside its new shape (same as above)
 * - Player should suffocate when inside the voxel of a smooth block and inside its new shape (same as above)
 * - Suffocation overlay should only show when inside the voxel and new shape of a smooth block ({@link ScreenEffectRendererMixin#noCubes$isViewBlocking})
 * - 3rd person camera should not be super zoomed-in when only partially inside a smooth voxel ({@link io.github.cadiboo.nocubes.mixin.BlockStateBaseMixin#noCubes$getVisualShape})
 * - Dirt path/Farmland turning to dirt should not crash the game
 * - Falling blocks (sand/gravel) should not break when they fall
 * - Player should be able to place redstone on slopes of smooth blocks
 * - Player should be able to place snow on slopes of smooth blocks (currently broken)
 * - Player should not suffocate when {@link NoCubesConfig.Server#tempMobCollisionsDisabled} is false
 * - Player should not be able to sleep in a bed obstructed by smooth blocks
 */
public final class CollisionHandler {

	static final int OLD_COLLISIONS_CORNERS = 1;
	static final int OLD_COLLISIONS_EDGES = OLD_COLLISIONS_CORNERS + 1;
	static final int OLD_COLLISIONS_CORNER_EDGES_INTERP = OLD_COLLISIONS_EDGES + 1;
	static final int OLD_COLLISIONS_CENTER_CORNER_INTERP = OLD_COLLISIONS_CORNER_EDGES_INTERP + 1;
	public static final int OLD_COLLISIONS_ENHANCEMENT_LEVEL_MAX = OLD_COLLISIONS_CENTER_CORNER_INTERP;

	public static VoxelShape getShapeOfSmoothBlock(BlockState state, BlockGetter reader, BlockPos blockPos, CollisionContext context) {
		var canCollide = ((INoCubesBlockType) state.getBlock()).noCubes$hasCollision();
		try {
			return getShapeOfSmoothBlockOrThrow(canCollide, state, reader, blockPos, (EntityCollisionContext) context);
		} catch (Throwable t) {
			Util.pauseInIde(t);
			throw t;
		}
	}

	// TODO: Why is the 'cache' of every blockstate storing an empty VoxelShape... this is causing issues like
	//  grass paths turning to dirt causing a crash because dirt's VoxelShape is empty
	//  and not being able to place snow anywhere ('Block.doesSideFillSquare' is returning false for a flat area of stone)
	public static VoxelShape getShapeOfSmoothBlockOrThrow(boolean canCollide, BlockState state, BlockGetter reader, BlockPos blockPos, EntityCollisionContext context) {
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
		var ref = new VoxelShape[] {Shapes.empty()};
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
			Util.pauseInIde(t);
			throw t;
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
		var mut = faceNormal;
		var mutOldX = mut.x;
		var mutOldY = mut.y;
		var mutOldZ = mut.z;
		try {
			if (!generateShapesOld(consumer, face, centre, mut, Double.MAX_VALUE, 0.125F, NoCubesConfig.Server.oldStyleCollisionsEnhancementLevel))
				return false;
		} finally {
			mut.set(
				mutOldX,
				mutOldY,
				mutOldZ
			);
		}

		if (!NoCubesConfig.Server.onlyOldStyleCollisions) {
			if (!CollisionHandler.generateShape(centre, faceNormal, consumer, face.v0))
				return false;
			if (!CollisionHandler.generateShape(centre, faceNormal, consumer, face.v1))
				return false;
			if (!CollisionHandler.generateShape(centre, faceNormal, consumer, face.v2))
				return false;
			if (!CollisionHandler.generateShape(centre, faceNormal, consumer, face.v3))
				return false;
		}
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

	private static boolean generateShapesOld(ShapeConsumer consumer, Face face, Vec centre, Vec mut, double maxY, float boxRadius, int level) {
		var v0 = face.v0;
		var v1 = face.v1;
		var v2 = face.v2;
		var v3 = face.v3;

		//0___3
		//_____
		//__c__
		//_____
		//1___2
		if (level < OLD_COLLISIONS_CORNERS)
			return true;
		if (!acceptVoxelShapeForVertex(consumer, v0, boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, v1, boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, v2, boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, v3, boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, centre, boxRadius, maxY)
		)
			return false;

		//0_*_3
		//_____
		//*_c_*
		//_____
		//1_*_2
		if (level < OLD_COLLISIONS_EDGES)
			return true;
		if (!acceptVoxelShapeForVertex(consumer, interp(v0, v1, 0.5F, mut), boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, interp(v1, v2, 0.5F, mut), boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, interp(v2, v3, 0.5F, mut), boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, interp(v3, v0, 0.5F, mut), boxRadius, maxY)
		)
			return false;

		//0x*x3
		//x___x
		//*_c_*
		//x___x
		//1x*x2
		if (level < OLD_COLLISIONS_CORNER_EDGES_INTERP)
			return true;
		if (!acceptVoxelShapeForVertex(consumer, interp(v0, v1, 0.25F, mut), boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, interp(v0, v1, 0.75F, mut), boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, interp(v1, v2, 0.25F, mut), boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, interp(v1, v2, 0.75F, mut), boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, interp(v2, v3, 0.25F, mut), boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, interp(v2, v3, 0.75F, mut), boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, interp(v3, v0, 0.25F, mut), boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, interp(v3, v0, 0.75F, mut), boxRadius, maxY)
		)
			return false;

		//0x*x3
		//x†_†x
		//*_c_*
		//x†_†x
		//1x*x2
		if (level < OLD_COLLISIONS_CENTER_CORNER_INTERP)
			return true;
		if (!acceptVoxelShapeForVertex(consumer, interp(v0, centre, 0.5F, mut), boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, interp(v1, centre, 0.5F, mut), boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, interp(v2, centre, 0.5F, mut), boxRadius, maxY) ||
			!acceptVoxelShapeForVertex(consumer, interp(v3, centre, 0.5F, mut), boxRadius, maxY)
		)
			return false;

		return true;
	}

	private static Vec interp(Vec v0, Vec v1, float t, Vec toUse) {
		return toUse.set(
			v0.x + t * (v1.x - v0.x),
			v0.y + t * (v1.y - v0.y),
			v0.z + t * (v1.z - v0.z)
		);

	}

	private static boolean acceptVoxelShapeForVertex(ShapeConsumer consumer, Vec v, float boxRadius, double maxY) {
		final double topX = v.x + boxRadius;
		final double topY = Math.min(v.y + boxRadius, maxY);
		final double topZ = v.z + boxRadius;

		return consumer.accept(
			//min
			topX - boxRadius * 2,
			topY - boxRadius * 2,
			topZ - boxRadius * 2,
			//max
			topX,
			topY,
			topZ
		);
	}

}
