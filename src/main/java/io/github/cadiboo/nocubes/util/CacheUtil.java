package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.util.pooled.cache.DensityCache;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
//TODO: javadocs
public final class CacheUtil {

	public static StateCache generateStateCache(
			final int startPosX, final int startPosY, final int startPosZ,
			final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ,
			@Nonnull final IBlockReader cache,
			@Nonnull PooledMutableBlockPos pooledMutableBlockPos
	) {
		try (ModProfiler ignored = ModProfiler.get().start("generate stateCache")) {
			final StateCache stateCache = StateCache.retain(cacheSizeX, cacheSizeY, cacheSizeZ);
			final IBlockState[] blockStates = stateCache.getBlockStates();
			final IFluidState[] fluidStates = stateCache.getFluidStates();

			int index = 0;
			for (int z = 0; z < cacheSizeZ; ++z) {
				for (int y = 0; y < cacheSizeY; ++y) {
					for (int x = 0; x < cacheSizeX; ++x, ++index) {
						blockStates[index] = cache.getBlockState(pooledMutableBlockPos.setPos(startPosX + x, startPosY + y, startPosZ + z));
						fluidStates[index] = cache.getFluidState(pooledMutableBlockPos);
					}
				}
			}

			return stateCache;
		}
	}

	public static SmoothableCache generateSmoothableCache(
			@Nonnull final StateCache stateCache,
			@Nonnull final IIsSmoothable isStateSmoothable
	) {
		try (ModProfiler ignored = ModProfiler.get().start("generate smoothableCache")) {
			final int cacheSizeX = stateCache.sizeX;
			final int cacheSizeY = stateCache.sizeY;
			final int cacheSizeZ = stateCache.sizeZ;

			final SmoothableCache smoothableCache = SmoothableCache.retain(cacheSizeX, cacheSizeY, cacheSizeZ);
			final boolean[] smoothableCacheArray = smoothableCache.getSmoothableCache();

			final IBlockState[] stateCacheArray = stateCache.getBlockStates();

			int index = 0;
			for (int z = 0; z < cacheSizeZ; ++z) {
				for (int y = 0; y < cacheSizeY; ++y) {
					for (int x = 0; x < cacheSizeX; ++x, ++index) {
						smoothableCacheArray[index] = isStateSmoothable.isSmoothable(stateCacheArray[index]);
					}
				}
			}

			return smoothableCache;
		}
	}

	public static DensityCache generateDensityCache(
			final int startPosX, final int startPosY, final int startPosZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final SmoothableCache smoothableCache,
			@Nonnull final IBlockReader blockAccess,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos
	) {
		try (ModProfiler ignored = ModProfiler.get().start("generate densityCache")) {
			final int densityCacheSizeX = stateCache.sizeX - 1;
			final int densityCacheSizeY = stateCache.sizeY - 1;
			final int densityCacheSizeZ = stateCache.sizeZ - 1;

			final DensityCache densityCache = DensityCache.retain(densityCacheSizeX, densityCacheSizeY, densityCacheSizeZ);
			final float[] densityCacheArray = densityCache.getDensityCache();

			int index = 0;
			for (int z = 0; z < densityCacheSizeZ; ++z) {
				for (int y = 0; y < densityCacheSizeY; ++y) {
					for (int x = 0; x < densityCacheSizeX; ++x, ++index) {
						densityCacheArray[index] = getBlockDensity(
								startPosX, startPosY, startPosZ,
								x, y, z,
								stateCache, smoothableCache,
								blockAccess, pooledMutableBlockPos
						);
					}
				}
			}
			return densityCache;
		}
	}

	private static float getBlockDensity(
			final int startPosX, final int startPosY, final int startPosZ,
			final int posX, final int posY, final int posZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final SmoothableCache smoothableCache,
			@Nonnull final IBlockReader cache,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos
	) {
		final int cacheSizeX = smoothableCache.sizeX;
		final int cacheSizeY = smoothableCache.sizeY;
		final boolean[] smoothableCacheArray = smoothableCache.getSmoothableCache();
		final IBlockState[] stateCacheArray = stateCache.getBlockStates();

		float density = 0;
		for (int zOffset = 0; zOffset < 2; ++zOffset) {
			for (int yOffset = 0; yOffset < 2; ++yOffset) {
				for (int xOffset = 0; xOffset < 2; ++xOffset) {

					// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
					final int index = (posX + xOffset) + cacheSizeX * ((posY + yOffset) + cacheSizeY * (posZ + zOffset));

//					pooledMutableBlockPos.setPos(
//							startPosX + posX - xOffset,
//							startPosY + posY - yOffset,
//							startPosZ + posZ - zOffset
//					);

					density += ModUtil.getIndividualBlockDensity(smoothableCacheArray[index], stateCacheArray[index]);
				}
			}
		}
		return density;
	}

}
