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
public final class CacheUtil {

	/**
	 * Generates a {@link StateCache}
	 */
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

	/**
	 * Generates a {@link SmoothableCache} from a {@link StateCache}
	 */
	public static SmoothableCache generateSmoothableCache(
			@Nonnull final StateCache stateCache,
			@Nonnull final IsSmoothable isStateSmoothable
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
						smoothableCacheArray[index] = isStateSmoothable.apply(stateCacheArray[index]);
					}
				}
			}

			return smoothableCache;
		}
	}

	/**
	 * Generates a {@link DensityCache} from a {@link StateCache} and a {@link SmoothableCache}
	 */
	public static DensityCache generateDensityCache(
			final int densityCacheSizeX, final int densityCacheSizeY, final int densityCacheSizeZ,
			final int addX, final int addY, final int addZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final SmoothableCache smoothableCache
	) {
		try (ModProfiler ignored = ModProfiler.get().start("generate densityCache")) {
			final DensityCache densityCache = DensityCache.retain(densityCacheSizeX, densityCacheSizeY, densityCacheSizeZ);
			final float[] densityCacheArray = densityCache.getDensityCache();

			int index = 0;
			for (int z = 0; z < densityCacheSizeZ; ++z) {
				for (int y = 0; y < densityCacheSizeY; ++y) {
					for (int x = 0; x < densityCacheSizeX; ++x, ++index) {
						densityCacheArray[index] = getBlockDensity(
								x + addX, y + addY, z + addZ,
								stateCache, smoothableCache
						);
					}
				}
			}
			return densityCache;
		}
	}

	/**
	 * Gets the density for a block (between -8 and 8) based on the smoothability of itself and its 7 neighbours in negative directions
	 */
	private static float getBlockDensity(
			final int posX, final int posY, final int posZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final SmoothableCache smoothableCache
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
					density += ModUtil.getIndividualBlockDensity(smoothableCacheArray[index], stateCacheArray[index]);
				}
			}
		}
		return density;
	}

}
