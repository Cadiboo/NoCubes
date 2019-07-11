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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
		return entity instanceof EntityPlayer;
	}

	public static boolean shouldApplyReposeCollisions(@Nullable final Entity entity) {
		return entity instanceof EntityItem || entity instanceof EntityLivingBase;
	}

	public static boolean getCollisionBoxes(
			final World _this,
			final Entity entityIn,
			final AxisAlignedBB aabb,
			final boolean p_191504_3_,
			final List<AxisAlignedBB> outList,
			final int minXm1, final int maxXp1,
			final int minYm1, final int maxYp1,
			final int minZm1, final int maxZp1,
			final WorldBorder worldborder,
			final boolean flag,
			final boolean flag1
	) {
		if (!Config.terrainCollisions) {
			return getVanillaCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
		} else if (shouldApplyMeshCollisions(entityIn)) {
			return getMeshCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
		} else if (shouldApplyReposeCollisions(entityIn)) {
			return getReposeCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
		} else {
			return getVanillaCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
		}
	}

	private static boolean getVanillaCollisions(final World _this, final Entity entityIn, final AxisAlignedBB aabb, final boolean p_191504_3_, final List<AxisAlignedBB> outList, final int i, final int j, final int k, final int l, final int i1, final int j1, final WorldBorder worldborder, final boolean flag, final boolean flag1) {
		IBlockState iblockstate = Blocks.STONE.getDefaultState();
		BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

		// NoCubes: fix forge not closing pooled mutable block pos
		try {
			if (p_191504_3_ && !net.minecraftforge.event.ForgeEventFactory.gatherCollisionBoxes(_this, entityIn, aabb, outList))
				return true;
			for (int k1 = i; k1 < j; ++k1) {
				for (int l1 = i1; l1 < j1; ++l1) {
					boolean flag2 = k1 == i || k1 == j - 1;
					boolean flag3 = l1 == i1 || l1 == j1 - 1;

					if ((!flag2 || !flag3) && _this.isBlockLoaded(blockpos$pooledmutableblockpos.setPos(k1, 64, l1))) {
						for (int i2 = k; i2 < l; ++i2) {
							if (!flag2 && !flag3 || i2 != l - 1) {
								if (p_191504_3_) {
									if (k1 < -30000000 || k1 >= 30000000 || l1 < -30000000 || l1 >= 30000000) {
										return true;
									}
								} else if (entityIn != null && flag == flag1) {
									entityIn.setOutsideBorder(!flag1);
								}

								blockpos$pooledmutableblockpos.setPos(k1, i2, l1);
								IBlockState iblockstate1;

								if (!p_191504_3_ && !worldborder.contains(blockpos$pooledmutableblockpos) && flag1) {
									iblockstate1 = iblockstate;
								} else {
									iblockstate1 = _this.getBlockState(blockpos$pooledmutableblockpos);
								}

								iblockstate1.addCollisionBoxToList(_this, blockpos$pooledmutableblockpos, aabb, outList, entityIn, false);

								if (p_191504_3_ && !net.minecraftforge.event.ForgeEventFactory.gatherCollisionBoxes(_this, entityIn, aabb, outList)) {
									return true;
								}
							}
						}
					}
				}
			}
		} finally {
			blockpos$pooledmutableblockpos.release();
		}

		return !outList.isEmpty();
	}

	private static boolean getReposeCollisions(final World _this, final Entity entityIn, final AxisAlignedBB aabb, final boolean p_191504_3_, final List<AxisAlignedBB> outList, final int startX, final int j, final int k, final int l, final int startZ, final int maxZ, final WorldBorder worldborder, final boolean flag, final boolean flag1) {
		final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
		try {
			if (p_191504_3_ && !net.minecraftforge.event.ForgeEventFactory.gatherCollisionBoxes(_this, entityIn, aabb, outList))
				return true;
			for (int posX = startX; posX < j; ++posX) {
				for (int posZ = startZ; posZ < maxZ; ++posZ) {
					boolean flag2 = posX == startX || posX == j - 1;
					boolean flag3 = posZ == startZ || posZ == maxZ - 1;

					if ((!flag2 || !flag3) && _this.isBlockLoaded(pooledMutableBlockPos.setPos(posX, 64, posZ))) {
						for (int i2 = k; i2 < l; ++i2) {
							if (!flag2 && !flag3 || i2 != l - 1) {
								if (p_191504_3_) {
									if (posX < -30000000 || posX >= 30000000 || posZ < -30000000 || posZ >= 30000000) {
										return true;
									}
								} else if (entityIn != null && flag == flag1) {
									entityIn.setOutsideBorder(!flag1);
								}

								pooledMutableBlockPos.setPos(posX, i2, posZ);
								final IBlockState state;

								if (!p_191504_3_ && !worldborder.contains(pooledMutableBlockPos) && flag1) {
									state = Blocks.STONE.getDefaultState();
								} else {
									state = _this.getBlockState(pooledMutableBlockPos);
								}

								if (TERRAIN_SMOOTHABLE.apply(state)) {
									StolenReposeCode.addCollisionBoxToList(state, _this, pooledMutableBlockPos, aabb, outList, entityIn, false);
								} else {
									state.addCollisionBoxToList(_this, pooledMutableBlockPos, aabb, outList, entityIn, false);
								}

								if (p_191504_3_ && !net.minecraftforge.event.ForgeEventFactory.gatherCollisionBoxes(_this, entityIn, aabb, outList)) {
									return true;
								}
							}
						}
					}
				}
			}
		} finally {
			pooledMutableBlockPos.release();
		}
		return !outList.isEmpty();
	}

	private static boolean getMeshCollisions(
			final World _this,
			final Entity entityIn,
			final AxisAlignedBB aabb,
			final boolean p_191504_3_,
			final List<AxisAlignedBB> outList,
			final int minXm1, final int maxXp1,
			final int minYm1, final int maxYp1,
			final int minZm1, final int maxZp1,
			final WorldBorder worldborder,
			final boolean flag,
			final boolean flag1
	) {
		final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
		try {
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
					new StructureBoundingBox(
							startPosX, startPosY, startPosZ,
							endPosX, endPosY, endPosZ
					),
					true
			)) {
				return getFallbackMeshCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
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

				final List<AxisAlignedBB> collidingShapes = new ArrayList<>();

				final float[] densityCacheArray = densityCache.getDensityCache();

				final IBlockState[] blockStateArray = stateCache.getBlockStates();

				final int stateOffsetX = stateCache.startPaddingX;
				final int stateOffsetY = stateCache.startPaddingY;
				final int stateOffsetZ = stateCache.startPaddingZ;
				final int stateCacheSizeX = stateCache.sizeX;
				final int stateCacheSizeY = stateCache.sizeY;

//				final VoxelShape aabbShape = VoxelShapes.create(aabb);

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
								final IBlockState blockState = blockStateArray[stateCache.getIndex(
										stateOffsetX + x,
										stateOffsetY + y,
										stateOffsetZ + z,
										stateCacheSizeX, stateCacheSizeY
								)];
								if (!blockState.nocubes_isTerrainSmoothable()
										||
										densityCacheArray[densityCache.getIndex(
												densityOffsetX + x,
												densityOffsetY + y,
												densityOffsetZ + z,
												densityCacheSizeX, densityCacheSizeY
										)] < -6 // -6 is very likely to be inside the isosurface (-8 is entirely inside)
								) {
									blockState.addCollisionBoxToList(_this, pooledMutableBlockPos.setPos(minXm1 + x, minYm1 + y, minZm1 + z), aabb, collidingShapes, entityIn, false);
								}
							}
						}
					}
				}

				final HashMap<Vec3b, FaceList> meshData;
				try (final ModProfiler ignored = profiler.start("Calculate collisions mesh")) {
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

				try (final ModProfiler ignored = profiler.start("Offset collisions mesh")) {
					MeshDispatcher.offsetMesh(minXm1, minYm1, minZm1, meshData);
				}

				try (FaceList finalFaces = FaceList.retain()) {

					try (final ModProfiler ignored = profiler.start("Combine collisions faces")) {
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
							try (final ModProfiler ignored = profiler.start("Snap collisions to original")) {
								// Snap collision AxisAlignedBBs max Y to max Y AxisAlignedBBs of original block at pos if smaller than original
								// To stop players falling down through the world when they enable collisions
								// (Only works on flat or near-flat surfaces)
								// TODO: remove
								final int approximateX = clamp(floorAvg(v0.x, v1.x, v2.x, v3.x), startPosX, endPosX);
								final int approximateY = clamp(floorAvg(v0.y - 0.5, v1.y - 0.5, v2.y - 0.5, v3.y - 0.5), startPosY, endPosY);
								final int approximateZ = clamp(floorAvg(v0.z, v1.z, v2.z, v3.z), startPosZ, endPosZ);
								final IBlockState state = blockStateArray[stateCache.getIndex(
										approximateX - startPosX,
										approximateY - startPosY,
										approximateZ - startPosZ,
										stateCacheSizeX, stateCacheSizeY
								)];
								final AxisAlignedBB originalCollisionShape = state.getCollisionBoundingBox(_this, pooledMutableBlockPos.setPos(
										approximateX, approximateY, approximateZ
								));
								maxY = originalCollisionShape == null ? approximateY : approximateY + originalCollisionShape.maxY;
							}
							MeshCollisionUtil.addIntersectingFaceShapesToList(collidingShapes, face, profiler, maxY, 0.15F, aabb::intersects, false);
						}
					}

					outList.addAll(collidingShapes);

					if (p_191504_3_ && !net.minecraftforge.event.ForgeEventFactory.gatherCollisionBoxes(_this, entityIn, aabb, outList))
						return true;
					for (int posX = minXm1; posX < maxXp1; ++posX) {
						for (int posZ = minZm1; posZ < maxZp1; ++posZ) {
							boolean flag2 = posX == minXm1 || posX == maxXp1 - 1;
							boolean flag3 = posZ == minZm1 || posZ == maxZp1 - 1;

							if ((!flag2 || !flag3) && _this.isBlockLoaded(pooledMutableBlockPos.setPos(posX, 64, posZ))) {
								for (int posY = minYm1; posY < maxYp1; ++posY) {
									if (!flag2 && !flag3 || posY != maxYp1 - 1) {
										if (p_191504_3_) {
											if (posX < -30000000 || posX >= 30000000 || posZ < -30000000 || posZ >= 30000000) {
												return true;
											}
										} else if (entityIn != null && flag == flag1) {
											entityIn.setOutsideBorder(!flag1);
										}

										pooledMutableBlockPos.setPos(posX, posY, posZ);
										final IBlockState state;

										if (!p_191504_3_ && !worldborder.contains(pooledMutableBlockPos) && flag1) {
											state = Blocks.STONE.getDefaultState();
										} else {
											state = _this.getBlockState(pooledMutableBlockPos);
										}

										if (!state.nocubes_isTerrainSmoothable()) {
											state.addCollisionBoxToList(_this, pooledMutableBlockPos, aabb, outList, entityIn, false);
										}

										if (p_191504_3_ && !net.minecraftforge.event.ForgeEventFactory.gatherCollisionBoxes(_this, entityIn, aabb, outList)) {
											return true;
										}
									}
								}
							}
						}
					}

				}
			}
		} finally {
			pooledMutableBlockPos.release();
		}
		return !outList.isEmpty();
	}

	private static boolean getFallbackMeshCollisions(
			final World _this,
			final Entity entityIn,
			final AxisAlignedBB aabb,
			final boolean p_191504_3_,
			final List<AxisAlignedBB> outList,
			final int minXm1, final int maxXp1,
			final int minYm1, final int maxYp1,
			final int minZm1, final int maxZp1,
			final WorldBorder worldborder,
			final boolean flag,
			final boolean flag1
	) {
		return getVanillaCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
	}

}
