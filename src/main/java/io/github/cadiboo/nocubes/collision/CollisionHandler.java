package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Cadiboo
 */
public final class CollisionHandler {

	private static int roundAvg(double d0, double d1, double d2, double d3) {
		return (int) ((Math.round(d0) + Math.round(d1) + Math.round(d2) + Math.round(d3)) / 4D);
	}

	//hmmm
	private static int floorAvg(double d0, double d1, double d2, double d3) {
		return MathHelper.floor((d0 + d1 + d2 + d3) / 4D);
	}

	//hmmm
	private static int average(final double d0, final double d1, final double d2, final double d3) {
		return (int) ((d0 + d1 + d2 + d3) / 4);
	}

	private static void addIntersectingFaceBoxesToList(
			final List<VoxelShape> outBoxes,
			final Face face,
			final ModProfiler profiler,
			final double maxYLevel,
			final float boxRadius,
			final Predicate<VoxelShape> predicate,
			final boolean ignoreIntersects
	) {

		//0___3
		//_____
		//_____
		//_____
		//1___2
		final Vec3 v0;
		final Vec3 v1;
		final Vec3 v2;
		final Vec3 v3;
		//0_*_3
		//_____
		//*___*
		//_____
		//1_*_2
		final Vec3 v0v1;
		final Vec3 v1v2;
		final Vec3 v2v3;
		final Vec3 v3v0;
//		//0x*x3
//		//x___x
//		//*___*
//		//x___x
//		//1x*x2
//		final Vec3 v0v1v0;
//		final Vec3 v0v1v1;
//		final Vec3 v1v2v1;
//		final Vec3 v1v2v2;
//		final Vec3 v2v3v2;
//		final Vec3 v2v3v3;
//		final Vec3 v3v0v3;
//		final Vec3 v3v0v0;
		//0x*x3
		//xa_ax
		//*___*
		//xa_ax
		//1x*x2
		final Vec3 v0v1v1v2;
		final Vec3 v1v2v2v3;
		final Vec3 v2v3v3v0;
		final Vec3 v3v0v0v1;
//		//0x*x3
//		//xabax
//		//*b_b*
//		//xabax
//		//1x*x2
//		final Vec3 v0v1v1v2v1v2v2v3;
//		final Vec3 v1v2v2v3v2v3v3v0;
//		final Vec3 v2v3v3v0v3v0v0v1;
//		final Vec3 v3v0v0v1v0v1v1v2;
//		//0x*x3
//		//xabax
//		//*bcb*
//		//xabax
//		//1x*x2
//		final Vec3 v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1;
//		final Vec3 v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2;

		try (final ModProfiler ignored = profiler.start("interpolate")) {
			v0 = face.getVertex0();
			v1 = face.getVertex1();
			v2 = face.getVertex2();
			v3 = face.getVertex3();
			v0v1 = interp(v0, v1, 0.5F);
			v1v2 = interp(v1, v2, 0.5F);
			v2v3 = interp(v2, v3, 0.5F);
			v3v0 = interp(v3, v0, 0.5F);
//			v0v1v0 = interp(v0v1, v0, 0.5F);
//			v0v1v1 = interp(v0v1, v1, 0.5F);
//			v1v2v1 = interp(v1v2, v1, 0.5F);
//			v1v2v2 = interp(v1v2, v2, 0.5F);
//			v2v3v2 = interp(v2v3, v2, 0.5F);
//			v2v3v3 = interp(v2v3, v3, 0.5F);
//			v3v0v3 = interp(v3v0, v3, 0.5F);
//			v3v0v0 = interp(v3v0, v0, 0.5F);
			v0v1v1v2 = interp(v0v1, v1v2, 0.5F);
			v1v2v2v3 = interp(v1v2, v2v3, 0.5F);
			v2v3v3v0 = interp(v2v3, v3v0, 0.5F);
			v3v0v0v1 = interp(v3v0, v0v1, 0.5F);
//			v0v1v1v2v1v2v2v3 = interp(v0v1v1v2, v1v2v2v3, 0.5F);
//			v1v2v2v3v2v3v3v0 = interp(v1v2v2v3, v2v3v3v0, 0.5F);
//			v2v3v3v0v3v0v0v1 = interp(v2v3v3v0, v3v0v0v1, 0.5F);
//			v3v0v0v1v0v1v1v2 = interp(v3v0v0v1, v0v1v1v2, 0.5F);
//			v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1 = interp(v0v1v1v2v1v2v2v3, v2v3v3v0v3v0v0v1, 0.5F);
//			v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2 = interp(v1v2v2v3v2v3v3v0, v3v0v0v1v0v1v1v2, 0.5F);
		}

		//0___3
		//_____
		//_____
		//_____
		//1___2
//		final VoxelShape v0box;
//		final VoxelShape v1box;
//		final VoxelShape v2box;
//		final VoxelShape v3box;
		//0_*_3
		//_____
		//*___*
		//_____
		//1_*_2
		final VoxelShape v0v1box;
		final VoxelShape v1v2box;
		final VoxelShape v2v3box;
		final VoxelShape v3v0box;
//		//0x*x3
//		//x___x
//		//*___*
//		//x___x
//		//1x*x2
//		final VoxelShape v0v1v0box;
//		final VoxelShape v0v1v1box;
//		final VoxelShape v1v2v1box;
//		final VoxelShape v1v2v2box;
//		final VoxelShape v2v3v2box;
//		final VoxelShape v2v3v3box;
//		final VoxelShape v3v0v3box;
//		final VoxelShape v3v0v0box;
		//0x*x3
		//xa_ax
		//*___*
		//xa_ax
		//1x*x2
		final VoxelShape v0v1v1v2box;
		final VoxelShape v1v2v2v3box;
		final VoxelShape v2v3v3v0box;
		final VoxelShape v3v0v0v1box;
//		//0x*x3
//		//xabax
//		//*b_b*
//		//xabax
//		//1x*x2
//		final VoxelShape v0v1v1v2v1v2v2v3box;
//		final VoxelShape v1v2v2v3v2v3v3v0box;
//		final VoxelShape v2v3v3v0v3v0v0v1box;
//		final VoxelShape v3v0v0v1v0v1v1v2box;
//		//0x*x3
//		//xabax
//		//*bcb*
//		//xabax
//		//1x*x2
//		final VoxelShape v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box;
//		final VoxelShape v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box;

		try (final ModProfiler ignored = profiler.start("createBoxes")) {
//			v0box = createVoxelShapeForVertex(v0, boxRadius, maxYLevel);
//			v1box = createVoxelShapeForVertex(v1, boxRadius, maxYLevel);
//			v2box = createVoxelShapeForVertex(v2, boxRadius, maxYLevel);
//			v3box = createVoxelShapeForVertex(v3, boxRadius, maxYLevel);
			v0v1box = createVoxelShapeForVertex(v0v1, boxRadius, maxYLevel);
			v1v2box = createVoxelShapeForVertex(v1v2, boxRadius, maxYLevel);
			v2v3box = createVoxelShapeForVertex(v2v3, boxRadius, maxYLevel);
			v3v0box = createVoxelShapeForVertex(v3v0, boxRadius, maxYLevel);
//			v0v1v0box = createVoxelShapeForVertex(v0v1v0, boxRadius, originalBoxOffset);
//			v0v1v1box = createVoxelShapeForVertex(v0v1v1, boxRadius, originalBoxOffset);
//			v1v2v1box = createVoxelShapeForVertex(v1v2v1, boxRadius, originalBoxOffset);
//			v1v2v2box = createVoxelShapeForVertex(v1v2v2, boxRadius, originalBoxOffset);
//			v2v3v2box = createVoxelShapeForVertex(v2v3v2, boxRadius, originalBoxOffset);
//			v2v3v3box = createVoxelShapeForVertex(v2v3v3, boxRadius, originalBoxOffset);
//			v3v0v3box = createVoxelShapeForVertex(v3v0v3, boxRadius, originalBoxOffset);
//			v3v0v0box = createVoxelShapeForVertex(v3v0v0, boxRadius, originalBoxOffset);
			v0v1v1v2box = createVoxelShapeForVertex(v0v1v1v2, boxRadius, maxYLevel);
			v1v2v2v3box = createVoxelShapeForVertex(v1v2v2v3, boxRadius, maxYLevel);
			v2v3v3v0box = createVoxelShapeForVertex(v2v3v3v0, boxRadius, maxYLevel);
			v3v0v0v1box = createVoxelShapeForVertex(v3v0v0v1, boxRadius, maxYLevel);
//			v0v1v1v2v1v2v2v3box = createVoxelShapeForVertex(v0v1v1v2v1v2v2v3, boxRadius, originalBoxOffset);
//			v1v2v2v3v2v3v3v0box = createVoxelShapeForVertex(v1v2v2v3v2v3v3v0, boxRadius, originalBoxOffset);
//			v2v3v3v0v3v0v0v1box = createVoxelShapeForVertex(v2v3v3v0v3v0v0v1, boxRadius, originalBoxOffset);
//			v3v0v0v1v0v1v1v2box = createVoxelShapeForVertex(v3v0v0v1v0v1v1v2, boxRadius, originalBoxOffset);
//			v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box = createVoxelShapeForVertex(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1, boxRadius, originalBoxOffset);
//			v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box = createVoxelShapeForVertex(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2, boxRadius, originalBoxOffset);
		}

		try (final ModProfiler ignored = profiler.start("addBoxes")) {
//			addCollisionBoxToList(outBoxes, v0box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v1box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v2box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v3box, predicate, ignoreIntersects);
			addCollisionShapeToList(outBoxes, v0v1box, predicate, ignoreIntersects);
			addCollisionShapeToList(outBoxes, v1v2box, predicate, ignoreIntersects);
			addCollisionShapeToList(outBoxes, v2v3box, predicate, ignoreIntersects);
			addCollisionShapeToList(outBoxes, v3v0box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v0v1v0box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v0v1v1box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v1v2v1box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v1v2v2box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v2v3v2box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v2v3v3box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v3v0v3box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v3v0v0box, predicate, ignoreIntersects);
			addCollisionShapeToList(outBoxes, v0v1v1v2box, predicate, ignoreIntersects);
			addCollisionShapeToList(outBoxes, v1v2v2v3box, predicate, ignoreIntersects);
			addCollisionShapeToList(outBoxes, v2v3v3v0box, predicate, ignoreIntersects);
			addCollisionShapeToList(outBoxes, v3v0v0v1box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v0v1v1v2v1v2v2v3box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v1v2v2v3v2v3v3v0box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v2v3v3v0v3v0v0v1box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v3v0v0v1v0v1v1v2box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box, predicate, ignoreIntersects);
		}

		//DO NOT CLOSE original face vectors
		{
//			v0.close();
//			v1.close();
//			v2.close();
//			v3.close();
		}
		v0v1.close();
		v1v2.close();
		v2v3.close();
		v3v0.close();
//		v0v1v0.close();
//		v0v1v1.close();
//		v1v2v1.close();
//		v1v2v2.close();
//		v2v3v2.close();
//		v2v3v3.close();
//		v3v0v3.close();
//		v3v0v0.close();
		v0v1v1v2.close();
		v1v2v2v3.close();
		v2v3v3v0.close();
		v3v0v0v1.close();
//		v0v1v1v2v1v2v2v3.close();
//		v1v2v2v3v2v3v3v0.close();
//		v2v3v3v0v3v0v0v1.close();
//		v3v0v0v1v0v1v1v2.close();
//		v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1.close();
//		v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2.close();

	}

	private static void addCollisionShapeToList(
			final List<VoxelShape> collidingShapes,
			final VoxelShape shape,
			final Predicate<VoxelShape> predicate,
			final boolean ignoreIntersects
	) {
		if (ignoreIntersects || predicate.test(shape)) {
			collidingShapes.add(shape);
		}
	}

	private static Vec3 interp(final Vec3 v0, final Vec3 v1, final float t) {
		return Vec3.retain(
				v0.x + t * (v1.x - v0.x),
				v0.y + t * (v1.y - v0.y),
				v0.z + t * (v1.z - v0.z)
		);
	}

	private static VoxelShape createVoxelShapeForVertex(
			final Vec3 vec3,
			final float boxRadius,
			final double maxY
	) {
		final double vy = vec3.y;
		final double vx = vec3.x;
		final double vz = vec3.z;

		final boolean isOverMax = vy + boxRadius > maxY;
		return VoxelShapes.create(
				//min
				vx - boxRadius,
				isOverMax ? vy - boxRadius - boxRadius : vy - boxRadius,
				vz - boxRadius,
				//max
				vx + boxRadius,
				isOverMax ? vy : vy + boxRadius,
				vz + boxRadius
		);
	}

	public static boolean shouldApplyMeshCollisions(@Nullable final Entity entity) {
		return entity instanceof PlayerEntity;
	}

	public static boolean shouldApplyReposeCollisions(@Nullable final Entity entity) {
		return entity instanceof ItemEntity || entity instanceof LivingEntity;
	}

	public static Stream<VoxelShape> getMeshCollisions(
			final IWorldReader _this,
			final Entity entity, final AxisAlignedBB aabb,
			final Set<Entity> entitiesToIgnore, final VoxelShape voxelShape,
			final int minXm1, final int maxXp1,
			final int minYm1, final int maxYp1,
			final int minZm1, final int maxZp1,
			final ISelectionContext context
	) {
		return Stream.of();
	}

	public static Stream<VoxelShape> getReposeCollisions(
			final IWorldReader _this,
			final Entity entity, final AxisAlignedBB aabb,
			final Set<Entity> entitiesToIgnore, final VoxelShape voxelShape,
			final int minXm1, final int maxXp1,
			final int minYm1, final int maxYp1,
			final int minZm1, final int maxZp1,
			final ISelectionContext context
	) {
		return Stream.of();
	}

	public static Stream<VoxelShape> getVanillaCollisions(
			final IWorldReader _this,
			final Entity entity, final AxisAlignedBB aabb,
			final Set<Entity> entitiesToIgnore, final VoxelShape voxelShape,
			final int minXm1, final int maxXp1,
			final int minYm1, final int maxYp1,
			final int minZm1, final int maxZp1,
			final ISelectionContext context
	) {
		return Stream.of(VoxelShapes.empty());
	}

	@Nonnull
	public static Stream<VoxelShape> getCollisionShapes(
			final IWorldReader _this,
			final Entity entity, final AxisAlignedBB aabb,
			final Set<Entity> entitiesToIgnore, final VoxelShape voxelShape,
			final int minXm1, final int maxXp1,
			final int minYm1, final int maxYp1,
			final int minZm1, final int maxZp1,
			final ISelectionContext context
	) {
		if (shouldApplyMeshCollisions(entity)) {
			return getMeshCollisions(_this, entity, aabb, entitiesToIgnore, voxelShape, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, context);
		} else if (shouldApplyReposeCollisions(entity)) {
			return getReposeCollisions(_this, entity, aabb, entitiesToIgnore, voxelShape, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, context);
		} else {
			return getVanillaCollisions(_this, entity, aabb, entitiesToIgnore, voxelShape, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, context);
		}
	}

}
