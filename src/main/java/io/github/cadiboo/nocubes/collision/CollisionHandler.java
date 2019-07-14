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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.github.cadiboo.nocubes.collision.MeshCollisionUtil.floorAvg;
import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.ModUtil.getMeshSizeX;
import static io.github.cadiboo.nocubes.util.ModUtil.getMeshSizeY;
import static io.github.cadiboo.nocubes.util.ModUtil.getMeshSizeZ;
import static net.minecraft.util.math.MathHelper.clamp;

/**
 * @author Cadiboo
 */
public final class CollisionHandler {

	public static boolean shouldApplyMeshCollisions(@Nullable final Entity entity) {
		return entity instanceof PlayerEntity;
	}

	public static boolean shouldApplyReposeCollisions(@Nullable final Entity entity) {
		return entity instanceof ItemEntity || entity instanceof LivingEntity;
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
//		if (!Config.terrainCollisions) {
			return getVanillaCollisions(_this, entity, aabb, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, context);
//		} else if (shouldApplyMeshCollisions(entity)) {
//			return getMeshCollisions(_this, entity, aabb, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, context);
//		} else if (shouldApplyReposeCollisions(entity)) {
//			return getReposeCollisions(_this, entity, aabb, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, context);
//		} else {
//			return getVanillaCollisions(_this, entity, aabb, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, context);
//		}
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

				final List<VoxelShape> collidingShapes = new ArrayList<>();

				final float[] densityCacheArray = densityCache.getDensityCache();

				final BlockState[] blockStateArray = stateCache.getBlockStates();

				final int stateOffsetX = stateCache.startPaddingX;
				final int stateOffsetY = stateCache.startPaddingY;
				final int stateOffsetZ = stateCache.startPaddingZ;
				final int stateCacheSizeX = stateCache.sizeX;
				final int stateCacheSizeY = stateCache.sizeY;

				final VoxelShape aabbShape = VoxelShapes.create(aabb);

				// Get vanilla collisions (taking density into account)
				{
					final int sizeX = maxXp1 - minXm1;
					final int sizeY = maxYp1 - minYm1;
					final int sizeZ = maxZp1 - minZm1;

					final int densityOffsetX = densityCache.startPaddingX;
					final int densityOffsetY = densityCache.startPaddingY;
					final int densityOffsetZ = densityCache.startPaddingZ;
					final int densityCacheSizeX = densityCache.sizeX;
					final int densityCacheSizeY = densityCache.sizeY;

					for (int z = 0; z < sizeZ; ++z) {
						for (int y = 0; y < sizeY; ++y) {
							for (int x = 0; x < sizeX; ++x) {
								final BlockState blockState = blockStateArray[stateCache.getIndex(
										stateOffsetX + x,
										stateOffsetY + y,
										stateOffsetZ + z,
										stateCacheSizeX, stateCacheSizeY
								)];
								if (!blockState.nocubes_isTerrainSmoothable
										||
										densityCacheArray[densityCache.getIndex(
												densityOffsetX + x,
												densityOffsetY + y,
												densityOffsetZ + z,
												densityCacheSizeX, densityCacheSizeY
										)] < -6 // -6 is very likely to be inside the isosurface (-8 is entirely inside)
								) {
									final int posX = minXm1 + x;
									final int posY = minYm1 + y;
									final int posZ = minZm1 + z;
									pooledMutableBlockPos.setPos(posX, posY, posZ);
									final VoxelShape offsetCollisionShape = blockState.getCollisionShape(_this, pooledMutableBlockPos).withOffset(posX, posY, posZ);
									if (VoxelShapes.compare(aabbShape, offsetCollisionShape, IBooleanFunction.AND)) {
										collidingShapes.add(offsetCollisionShape);
									}
								}
							}
						}
					}
				}

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
						meshData = meshGenerator.generateChunk(densityCacheArray, new byte[]{meshSizeX, meshSizeY, meshSizeZ});
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
								final BlockState state = blockStateArray[stateCache.getIndex(
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
//							MeshCollisionUtil.addIntersectingFaceShapesToList(collidingShapes, face, profiler, maxY, 0.15F, aabb::intersects, false);
							MeshCollisionUtil.addIntersectingFaceShapesToList(collidingShapes, face, profiler, maxY, 0.15F, checkShape -> VoxelShapes.compare(aabbShape, checkShape, IBooleanFunction.AND), false);
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

	private static Stream<VoxelShape> getReposeCollisions(
			final IWorldReader _this,
			final Entity entity, final AxisAlignedBB aabb,
			final int minXm1, final int maxXp1,
			final int minYm1, final int maxYp1,
			final int minZm1, final int maxZp1,
			final ISelectionContext context
	) {
		final List<VoxelShape> collidingShapes = new ArrayList<>();
		final VoxelShape aabbShape = VoxelShapes.create(aabb);

		try (PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()) {
			for (int z = minZm1; z < maxZp1; ++z) {
				for (int y = minYm1; y < maxYp1; ++y) {
					for (int x = minXm1; x < maxXp1; ++x) {
						pooledMutableBlockPos.setPos(x, y, z);
						final BlockState blockState = _this.getBlockState(pooledMutableBlockPos);
						final VoxelShape offsetCollisionShape;
						if (blockState.nocubes_isTerrainSmoothable) {
							offsetCollisionShape = StolenReposeCode.getCollisionShape(blockState, _this, pooledMutableBlockPos);
						} else {
							offsetCollisionShape = blockState.getCollisionShape(_this, pooledMutableBlockPos).withOffset(x, y, z);
						}
						if (VoxelShapes.compare(aabbShape, offsetCollisionShape, IBooleanFunction.AND)) {
							collidingShapes.add(offsetCollisionShape);
						}
					}
				}
			}
		}
		return collidingShapes.stream();
	}

	private static Stream<VoxelShape> getVanillaCollisions(
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

}
