package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.mesh.MeshDispatcher;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;
import io.github.cadiboo.nocubes.util.pooled.cache.DensityCache;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.CubeCoordinateIterator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.chunk.IChunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.ModUtil.getMeshSizeX;
import static io.github.cadiboo.nocubes.util.ModUtil.getMeshSizeY;
import static io.github.cadiboo.nocubes.util.ModUtil.getMeshSizeZ;
import static net.minecraft.util.math.MathHelper.clamp;

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

	private static void addIntersectingFaceShapesToList(
			final List<VoxelShape> outShapes,
			final Face face,
			final ModProfiler profiler,
			final double maxYLevel,
			final float shapeRadius,
			final Predicate<VoxelShape> doesShapeIntersect,
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

		try (ModProfiler ignored = profiler.start("interpolate")) {
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
//		final VoxelShape v0shape;
//		final VoxelShape v1shape;
//		final VoxelShape v2shape;
//		final VoxelShape v3shape;
		//0_*_3
		//_____
		//*___*
		//_____
		//1_*_2
		final VoxelShape v0v1shape;
		final VoxelShape v1v2shape;
		final VoxelShape v2v3shape;
		final VoxelShape v3v0shape;
//		//0x*x3
//		//x___x
//		//*___*
//		//x___x
//		//1x*x2
//		final VoxelShape v0v1v0shape;
//		final VoxelShape v0v1v1shape;
//		final VoxelShape v1v2v1shape;
//		final VoxelShape v1v2v2shape;
//		final VoxelShape v2v3v2shape;
//		final VoxelShape v2v3v3shape;
//		final VoxelShape v3v0v3shape;
//		final VoxelShape v3v0v0shape;
		//0x*x3
		//xa_ax
		//*___*
		//xa_ax
		//1x*x2
		final VoxelShape v0v1v1v2shape;
		final VoxelShape v1v2v2v3shape;
		final VoxelShape v2v3v3v0shape;
		final VoxelShape v3v0v0v1shape;
//		//0x*x3
//		//xabax
//		//*b_b*
//		//xabax
//		//1x*x2
//		final VoxelShape v0v1v1v2v1v2v2v3shape;
//		final VoxelShape v1v2v2v3v2v3v3v0shape;
//		final VoxelShape v2v3v3v0v3v0v0v1shape;
//		final VoxelShape v3v0v0v1v0v1v1v2shape;
//		//0x*x3
//		//xabax
//		//*bcb*
//		//xabax
//		//1x*x2
//		final VoxelShape v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1shape;
//		final VoxelShape v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2shape;

		try (ModProfiler ignored = profiler.start("createVoxelShapes")) {
//			v0shape = createVoxelShapeForVertex(v0, shapeRadius, maxYLevel);
//			v1shape = createVoxelShapeForVertex(v1, shapeRadius, maxYLevel);
//			v2shape = createVoxelShapeForVertex(v2, shapeRadius, maxYLevel);
//			v3shape = createVoxelShapeForVertex(v3, shapeRadius, maxYLevel);
			v0v1shape = createVoxelShapeForVertex(v0v1, shapeRadius, maxYLevel);
			v1v2shape = createVoxelShapeForVertex(v1v2, shapeRadius, maxYLevel);
			v2v3shape = createVoxelShapeForVertex(v2v3, shapeRadius, maxYLevel);
			v3v0shape = createVoxelShapeForVertex(v3v0, shapeRadius, maxYLevel);
//			v0v1v0shape = createVoxelShapeForVertex(v0v1v0, shapeRadius, maxYLevel);
//			v0v1v1shape = createVoxelShapeForVertex(v0v1v1, shapeRadius, maxYLevel);
//			v1v2v1shape = createVoxelShapeForVertex(v1v2v1, shapeRadius, maxYLevel);
//			v1v2v2shape = createVoxelShapeForVertex(v1v2v2, shapeRadius, maxYLevel);
//			v2v3v2shape = createVoxelShapeForVertex(v2v3v2, shapeRadius, maxYLevel);
//			v2v3v3shape = createVoxelShapeForVertex(v2v3v3, shapeRadius, maxYLevel);
//			v3v0v3shape = createVoxelShapeForVertex(v3v0v3, shapeRadius, maxYLevel);
//			v3v0v0shape = createVoxelShapeForVertex(v3v0v0, shapeRadius, maxYLevel);
			v0v1v1v2shape = createVoxelShapeForVertex(v0v1v1v2, shapeRadius, maxYLevel);
			v1v2v2v3shape = createVoxelShapeForVertex(v1v2v2v3, shapeRadius, maxYLevel);
			v2v3v3v0shape = createVoxelShapeForVertex(v2v3v3v0, shapeRadius, maxYLevel);
			v3v0v0v1shape = createVoxelShapeForVertex(v3v0v0v1, shapeRadius, maxYLevel);
//			v0v1v1v2v1v2v2v3shape = createVoxelShapeForVertex(v0v1v1v2v1v2v2v3, shapeRadius, maxYLevel);
//			v1v2v2v3v2v3v3v0shape = createVoxelShapeForVertex(v1v2v2v3v2v3v3v0, shapeRadius, maxYLevel);
//			v2v3v3v0v3v0v0v1shape = createVoxelShapeForVertex(v2v3v3v0v3v0v0v1, shapeRadius, maxYLevel);
//			v3v0v0v1v0v1v1v2shape = createVoxelShapeForVertex(v3v0v0v1v0v1v1v2, shapeRadius, maxYLevel);
//			v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1shape = createVoxelShapeForVertex(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1, shapeRadius, maxYLevel);
//			v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2shape = createVoxelShapeForVertex(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2, shapeRadius, maxYLevel);
		}

		try (ModProfiler ignored = profiler.start("addVoxelShapes")) {
//			addCollisionShapeToList(outShapes, v0shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v1shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v2shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v3shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v0v1shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v1v2shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v2v3shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v3v0shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v0v1v0shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v0v1v1shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v1v2v1shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v1v2v2shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v2v3v2shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v2v3v3shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v3v0v3shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v3v0v0shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v0v1v1v2shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v1v2v2v3shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v2v3v3v0shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v3v0v0v1shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v0v1v1v2v1v2v2v3shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v1v2v2v3v2v3v3v0shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v2v3v3v0v3v0v0v1shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v3v0v0v1v0v1v1v2shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2shape, doesShapeIntersect, ignoreIntersects);
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
			final Predicate<VoxelShape> doesShapeIntersect,
			final boolean ignoreIntersects
	) {
		if (ignoreIntersects || doesShapeIntersect.test(shape)) {
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
			final int minXm1, final int maxXp1,
			final int minYm1, final int maxYp1,
			final int minZm1, final int maxZp1,
			final ISelectionContext context
	) {
		try (PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()) {
			final MeshGenerator meshGenerator = Config.terrainMeshGenerator.getMeshGenerator();

			final byte meshSizeX = getMeshSizeX(maxXp1 - minXm1, meshGenerator);
			final byte meshSizeY = getMeshSizeY(maxYp1 - minYm1, meshGenerator);
			final byte meshSizeZ = getMeshSizeZ(maxZp1 - minZm1, meshGenerator);

			// DensityCache needs -1 on each NEGATIVE axis
			final int startPosX = minXm1 - 1;
			final int startPosY = minYm1 - 1;
			final int startPosZ = minZm1 - 1;

			// StateCache needs +1 on each POSITIVE axis
			final int endPosX = maxXp1 + 1;
			final int endPosY = maxYp1 + 1;
			final int endPosZ = maxZp1 + 1;

			if (!_this.isAreaLoaded(
					startPosX, startPosY, startPosZ,
					endPosX, endPosY, endPosZ
			)) {
				return getFallbackMeshCollisions(_this, entity, aabb, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, context);
			}

			final ModProfiler profiler = ModProfiler.get();
			try (
					// DensityCache needs -1 on each NEGATIVE axis
					// StateCache needs +1 on each POSITIVE axis
					// Density calculation needs +1 on ALL axis, 1+1=2
					StateCache stateCache = CacheUtil.generateStateCache(
							startPosX, startPosY, startPosZ,
							endPosX, endPosY, endPosZ,
							1, 1, 1,
							_this, pooledMutableBlockPos
					);
					SmoothableCache smoothableCache = CacheUtil.generateSmoothableCache(
							startPosX, startPosY, startPosZ,
							// StateCache needs +1 on each POSITIVE axis
							endPosX, endPosY, endPosZ,
							1, 1, 1,
							stateCache, TERRAIN_SMOOTHABLE
					);
					DensityCache densityCache = CacheUtil.generateDensityCache(
							startPosX, startPosY, startPosZ,
							// DensityCache needs -1 on each NEGATIVE axis (not +1 on each positive axis as well)
							endPosX - 1, endPosY - 1, endPosZ - 1,
							1, 1, 1,
							stateCache, smoothableCache
					)
			) {

				final HashMap<Vec3b, FaceList> meshData;
				try (ModProfiler ignored = profiler.start("Calculate collisions mesh")) {
					if (Config.terrainMeshGenerator == MeshGeneratorType.OldNoCubes) {
						// TODO: Remove
						meshData = new HashMap<>();
						meshData.put(
								Vec3b.retain((byte) 0, (byte) 0, (byte) 0),
								OldNoCubes.generateBlock(new BlockPos(minXm1 + 1, minYm1 + 1, minZm1 + 1), _this, TERRAIN_SMOOTHABLE, pooledMutableBlockPos)
						);
					} else {
						meshData = meshGenerator.generateChunk(densityCache.getDensityCache(), new byte[]{meshSizeX, meshSizeY, meshSizeZ});
					}
				}

				try (ModProfiler ignored = profiler.start("Offset collisions mesh")) {
					MeshDispatcher.offsetMesh(minXm1, minYm1, minZm1, meshData);
				}

				try (FaceList finalFaces = FaceList.retain()) {

					try (ModProfiler ignored = profiler.start("Combine collisions faces")) {
						for (final FaceList generatedFaceList : meshData.values()) {
							finalFaces.addAll(generatedFaceList);
							generatedFaceList.close();
						}
						for (final Vec3b vec3b : meshData.keySet()) {
							vec3b.close();
						}
					}

					final List<VoxelShape> collidingShapes = new ArrayList<>();

					final VoxelShape aabbShape = VoxelShapes.create(aabb);

					final BlockState[] blocksArray = stateCache.getBlockStates();

					final int stateCacheSizeX = stateCache.sizeX;
					final int stateCacheSizeY = stateCache.sizeY;

					for (int i = 0, finalFacesSize = finalFaces.size(); i < finalFacesSize; ++i) {
						try (
								final Face face = finalFaces.get(i);
								final Vec3 v0 = face.getVertex0();
								final Vec3 v1 = face.getVertex1();
								final Vec3 v2 = face.getVertex2();
								final Vec3 v3 = face.getVertex3()
						) {
							final double maxY;
							try (ModProfiler ignored = profiler.start("Snap collisions to original")) {
								// Snap collision VoxelShapes max Y to max Y VoxelShapes of original block at pos if smaller than original
								// To stop players falling down through the world when they enable collisions
								// (Only works on flat or near-flat surfaces)
								// TODO: remove
								final int approximateX = clamp(floorAvg(v0.x, v1.x, v2.x, v3.x), startPosX, endPosX);
								final int approximateY = clamp(floorAvg(v0.y - 0.5, v1.y - 0.5, v2.y - 0.5, v3.y - 0.5), startPosY, endPosY);
								final int approximateZ = clamp(floorAvg(v0.z, v1.z, v2.z, v3.z), startPosZ, endPosZ);
								final BlockState state = blocksArray[stateCache.getIndex(
										approximateX - startPosX,
										approximateY - startPosY,
										approximateZ - startPosZ,
										stateCacheSizeX, stateCacheSizeY
								)];
								final VoxelShape originalCollisionShape = state.getCollisionShape(_this, pooledMutableBlockPos.setPos(
										approximateX, approximateY, approximateZ
								));
								maxY = approximateY + originalCollisionShape.getEnd(Axis.Y);
							}
//							addIntersectingFaceShapesToList(collidingShapes, face, profiler, maxY, 0.15F, aabb::intersects, false);
							addIntersectingFaceShapesToList(collidingShapes, face, profiler, maxY, 0.15F, checkShape -> VoxelShapes.compare(aabbShape, checkShape, IBooleanFunction.AND), false);
						}
					}
					return collidingShapes.stream();
				}
			}
		}
	}

	private static Stream<VoxelShape> getFallbackMeshCollisions(
			final IWorldReader _this,
			final Entity entity, final AxisAlignedBB aabb,
			final int minXm1, final int maxXp1,
			final int minYm1, final int maxYp1,
			final int minZm1, final int maxZp1,
			final ISelectionContext context
	) {
		return getVanillaCollisions(_this, entity, aabb, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, context);
	}

	public static Stream<VoxelShape> getReposeCollisions(
			final IWorldReader _this,
			final Entity entity, final AxisAlignedBB aabb,
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
			final int minXm1, final int maxXp1,
			final int minYm1, final int maxYp1,
			final int minZm1, final int maxZp1,
			final ISelectionContext context
	) {
		final CubeCoordinateIterator cubecoordinateiterator = new CubeCoordinateIterator(
				minXm1, minYm1, minZm1,
				maxXp1, maxYp1, maxZp1
		);
		final BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
		final VoxelShape voxelshape = VoxelShapes.create(aabb);
		return StreamSupport.stream(new AbstractSpliterator<VoxelShape>(Long.MAX_VALUE, 0x500) { // NONNULL | IMMUTABLE
			boolean isEntityNull = entity == null;

			public boolean tryAdvance(Consumer<? super VoxelShape> p_tryAdvance_1_) {
				if (!this.isEntityNull) {
					this.isEntityNull = true;
					VoxelShape voxelshape1 = _this.getWorldBorder().getShape();
					boolean flag = VoxelShapes.compare(voxelshape1, VoxelShapes.create(entity.getBoundingBox().shrink(1.0E-7D)), IBooleanFunction.AND);
					boolean flag1 = VoxelShapes.compare(voxelshape1, VoxelShapes.create(entity.getBoundingBox().grow(1.0E-7D)), IBooleanFunction.AND);
					if (!flag && flag1) {
						p_tryAdvance_1_.accept(voxelshape1);
						return true;
					}
				}

				VoxelShape voxelshape3;
				while (true) {
					if (!cubecoordinateiterator.hasNext()) {
						return false;
					}

					int j2 = cubecoordinateiterator.getX();
					int k2 = cubecoordinateiterator.getY();
					int l2 = cubecoordinateiterator.getZ();
					int k1 = cubecoordinateiterator.func_223473_e();
					if (k1 != 3) {
						int l1 = j2 >> 4;
						int i2 = l2 >> 4;
						IChunk ichunk = _this.getChunk(l1, i2, _this.getChunkStatus(), false);
						if (ichunk != null) {
							blockpos$mutableblockpos.setPos(j2, k2, l2);
							BlockState blockstate = ichunk.getBlockState(blockpos$mutableblockpos);
							if ((k1 != 1 || blockstate.func_215704_f()) && (k1 != 2 || blockstate.getBlock() == Blocks.MOVING_PISTON)) {
								VoxelShape voxelshape2 = blockstate.getCollisionShape(_this, blockpos$mutableblockpos, context);
								voxelshape3 = voxelshape2.withOffset((double) j2, (double) k2, (double) l2);
								if (VoxelShapes.compare(voxelshape, voxelshape3, IBooleanFunction.AND)) {
									break;
								}
							}
						}
					}
				}

				p_tryAdvance_1_.accept(voxelshape3);
				return true;
			}
		}, false);
	}

	@Nonnull
	public static Stream<VoxelShape> getCollisionShapes(
			final IWorldReader _this,
			final Entity entity, final AxisAlignedBB aabb,
			final int minXm1, final int maxXp1,
			final int minYm1, final int maxYp1,
			final int minZm1, final int maxZp1,
			final ISelectionContext context
	) {
		if (!Config.terrainCollisions) {
			return getVanillaCollisions(_this, entity, aabb, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, context);
		} else if (shouldApplyMeshCollisions(entity)) {
			return getMeshCollisions(_this, entity, aabb, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, context);
		} else if (shouldApplyReposeCollisions(entity)) {
			return getReposeCollisions(_this, entity, aabb, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, context);
		} else {
			return getVanillaCollisions(_this, entity, aabb, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, context);
		}
	}

}
