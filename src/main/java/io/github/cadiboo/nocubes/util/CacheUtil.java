package io.github.cadiboo.nocubes.util;

import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

//TODO: javadocs
public final class CacheUtil {

	public static PooledStateCache generateStateCache(
			final int startPosX, final int startPosY, final int startPosZ,
			final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ,
			@Nonnull final IBlockAccess cache,
			@Nonnull PooledMutableBlockPos pooledMutableBlockPos
	) {
		final PooledStateCache stateCache = PooledStateCache.retain(cacheSizeX * cacheSizeY * cacheSizeZ);
		int index = 0;
		for (int z = 0; z < cacheSizeZ; z++) {
			for (int y = 0; y < cacheSizeY; y++) {
				for (int x = 0; x < cacheSizeX; x++) {
					stateCache.getStateCache()[index] = cache.getBlockState(pooledMutableBlockPos.setPos(startPosX + x, startPosY + y, startPosZ + z));
					index++;
				}
			}
		}
		return stateCache;
	}

	public static PooledSmoothableCache generateSmoothableCache(
			final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ,
			@Nonnull final PooledStateCache stateCache,
			@Nonnull final IIsSmoothable isStateSmoothable
	) {
		final PooledSmoothableCache smoothableCache = PooledSmoothableCache.retain(cacheSizeX * cacheSizeY * cacheSizeZ);
		int index = 0;
		for (int z = 0; z < cacheSizeZ; z++) {
			for (int y = 0; y < cacheSizeY; y++) {
				for (int x = 0; x < cacheSizeX; x++) {
					smoothableCache.getSmoothableCache()[index] = isStateSmoothable.isSmoothable(stateCache.getStateCache()[index]);
					index++;
				}
			}
		}
		return smoothableCache;
	}

	public static PooledDensityCache generateDensityCache(
			final int startPosX, final int startPosY, final int startPosZ,
			final int densityCacheSizeX, final int densityCacheSizeY, final int densityCacheSizeZ,
			@Nonnull final PooledStateCache stateCache,
			@Nonnull final PooledSmoothableCache smoothableCache,
			final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ,
			@Nonnull final IBlockAccess cache,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos
	) {
		final PooledDensityCache densityCache = PooledDensityCache.retain(densityCacheSizeX * densityCacheSizeY * densityCacheSizeZ);
		int index = 0;
		for (int z = 0; z < densityCacheSizeZ; z++) {
			for (int y = 0; y < densityCacheSizeY; y++) {
				for (int x = 0; x < densityCacheSizeX; x++) {
					densityCache.getDensityCache()[index] = getBlockDensity(startPosX, startPosY, startPosZ, x, y, z, stateCache, smoothableCache, cacheSizeX, cacheSizeY, cacheSizeZ, cache, pooledMutableBlockPos);
					index++;
				}
			}
		}
		return densityCache;
	}

	private static float getBlockDensity(
			final int startPosX, final int startPosY, final int startPosZ,
			final int posX, final int posY, final int posZ,
			@Nonnull final PooledStateCache stateCache,
			@Nonnull final PooledSmoothableCache smoothableCache,
			final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ,
			@Nonnull final IBlockAccess cache,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos
	) {
		float density = 0;
		// why pre-Increment? We don't know but it works
		for (int zOffset = 0; zOffset < 2; ++zOffset) {
			for (int yOffset = 0; yOffset < 2; ++yOffset) {
				for (int xOffset = 0; xOffset < 2; ++xOffset) {

					// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
					final int index = (posX + xOffset) + cacheSizeX * ((posY + yOffset) + cacheSizeY * (posZ + zOffset));

					pooledMutableBlockPos.setPos(
							startPosX + posX - xOffset,
							startPosY + posY - yOffset,
							startPosZ + posZ - zOffset
					);

					density += ModUtil.getIndividualBlockDensity(smoothableCache.getSmoothableCache()[index], stateCache.getStateCache()[index], cache, pooledMutableBlockPos);
				}
			}
		}
		return density;
	}

}
