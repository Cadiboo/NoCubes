package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.util.pooled.cache.DensityCache;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.BlockState;
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
			// from position
			final int fromX, final int fromY, final int fromZ,
			// to position
			final int toX, final int toY, final int toZ,
			// the difference between the chunkRenderPosition and from position. Always positive
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			@Nonnull final IBlockReader cache,
			@Nonnull PooledMutableBlockPos pooledMutableBlockPos
	) {
		final int cacheSizeX = Math.abs(toX - fromX);
		final int cacheSizeY = Math.abs(toY - fromY);
		final int cacheSizeZ = Math.abs(toZ - fromZ);
		final StateCache stateCache = StateCache.retain(
				startPaddingX, startPaddingY, startPaddingZ,
				cacheSizeX, cacheSizeY, cacheSizeZ
		);
		try (ModProfiler ignored = ModProfiler.get().start("generate stateCache")) {
			final BlockState[] blockStates = stateCache.getBlockStates();
			final IFluidState[] fluidStates = stateCache.getFluidStates();

			int index = 0;
			for (int z = 0; z < cacheSizeZ; ++z) {
				for (int y = 0; y < cacheSizeY; ++y) {
					for (int x = 0; x < cacheSizeX; ++x, ++index) {
						blockStates[index] = cache.getBlockState(pooledMutableBlockPos.setPos(fromX + x, fromY + y, fromZ + z));
						fluidStates[index] = cache.getFluidState(pooledMutableBlockPos);
					}
				}
			}

			return stateCache;
		} catch (final Exception e) {
			// getBlockState/getFluidState can throw an error if its trying to get a region for a chunk out of bounds
			// close the state cache to prevent errors about it already being in use
			stateCache.close();
			throw e;
		}
	}

	/**
	 * Generates a {@link SmoothableCache} from a {@link StateCache}
	 */
	public static SmoothableCache generateSmoothableCache(
			// from position
			final int fromX, final int fromY, final int fromZ,
			// to position
			final int toX, final int toY, final int toZ,
			// the difference between the chunkRenderPosition and from position. Always positive
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final IsSmoothable isStateSmoothable
	) {
		final int cacheSizeX = Math.abs(toX - fromX);
		final int cacheSizeY = Math.abs(toY - fromY);
		final int cacheSizeZ = Math.abs(toZ - fromZ);
		try (ModProfiler ignored = ModProfiler.get().start("generate smoothableCache")) {
			final SmoothableCache smoothableCache = SmoothableCache.retain(startPaddingX, startPaddingY, startPaddingZ, cacheSizeX, cacheSizeY, cacheSizeZ);
			final boolean[] smoothableCacheArray = smoothableCache.getSmoothableCache();

			final int stateCacheSizeX = stateCache.sizeX;
			final int stateCacheSizeY = stateCache.sizeY;
			final int diffX = stateCache.startPaddingX - startPaddingX;
			final int diffY = stateCache.startPaddingY - startPaddingY;
			final int diffZ = stateCache.startPaddingZ - startPaddingZ;
			final BlockState[] blockStateArray = stateCache.getBlockStates();

			int smoothableIndex = 0;
			for (int z = 0; z < cacheSizeZ; ++z) {
				for (int y = 0; y < cacheSizeY; ++y) {
					for (int x = 0; x < cacheSizeX; ++x, ++smoothableIndex) {
						smoothableCacheArray[smoothableIndex] = isStateSmoothable.apply(
								blockStateArray[stateCache.getIndex(
										x + diffX,
										y + diffY,
										z + diffZ,
										stateCacheSizeX, stateCacheSizeY
								)]
						);
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
			// from position
			final int fromX, final int fromY, final int fromZ,
			// to position
			final int toX, final int toY, final int toZ,
			// the difference between the chunkRenderPosition and from position. Always positive
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final SmoothableCache smoothableCache
	) {
		final int cacheSizeX = Math.abs(toX - fromX);
		final int cacheSizeY = Math.abs(toY - fromY);
		final int cacheSizeZ = Math.abs(toZ - fromZ);
		try (ModProfiler ignored = ModProfiler.get().start("generate densityCache")) {
			final DensityCache densityCache = DensityCache.retain(startPaddingX, startPaddingY, startPaddingZ, cacheSizeX, cacheSizeY, cacheSizeZ);
			final float[] densityCacheArray = densityCache.getDensityCache();

			int index = 0;
			for (int z = 0; z < cacheSizeZ; ++z) {
				for (int y = 0; y < cacheSizeY; ++y) {
					for (int x = 0; x < cacheSizeX; ++x, ++index) {
						densityCacheArray[index] = getBlockDensity(
								x, y, z,
								startPaddingX, startPaddingY, startPaddingZ,
								stateCache, smoothableCache
						);
					}
				}
			}
			return densityCache;
		}
	}

	/**
	 * Gets the density for a block (between -8 and 8) based on the smoothability of itself and its 7 neighbours in positive directions
	 */
	private static float getBlockDensity(
			final int posX, final int posY, final int posZ,
			// the difference between the chunkRenderPosition and the density cache's from position. Always positive
			final int densityCacheStartPaddingX, final int densityCacheStartPaddingY, final int densityCacheStartPaddingZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final SmoothableCache smoothableCache
	) {
		final int stateCacheDiffX = stateCache.startPaddingX - densityCacheStartPaddingX;
		final int stateCacheDiffY = stateCache.startPaddingY - densityCacheStartPaddingY;
		final int stateCacheDiffZ = stateCache.startPaddingZ - densityCacheStartPaddingZ;

		final int stateCacheSizeX = stateCache.sizeX;
		final int stateCacheSizeY = stateCache.sizeY;
		final BlockState[] stateCacheArray = stateCache.getBlockStates();

		final int smoothableCacheSizeX = smoothableCache.sizeX;
		final int smoothableCacheSizeY = smoothableCache.sizeY;
		final boolean[] smoothableCacheArray = smoothableCache.getSmoothableCache();

		float density = 0;
		for (int zOffset = 0; zOffset < 2; ++zOffset) {
			for (int yOffset = 0; yOffset < 2; ++yOffset) {
				for (int xOffset = 0; xOffset < 2; ++xOffset) {
					density += ModUtil.getIndividualBlockDensity(
							smoothableCacheArray[smoothableCache.getIndex(
									posX + xOffset,
									posY + yOffset,
									posZ + zOffset,
									smoothableCacheSizeX, smoothableCacheSizeY
							)],
							stateCacheArray[stateCache.getIndex(
									posX + xOffset + stateCacheDiffX,
									posY + yOffset + stateCacheDiffY,
									posZ + zOffset + stateCacheDiffZ,
									stateCacheSizeX, stateCacheSizeY
							)]
					);
				}
			}
		}
		return density;
	}

}
