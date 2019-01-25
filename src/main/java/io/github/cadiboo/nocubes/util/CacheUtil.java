package io.github.cadiboo.nocubes.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.function.Function;

public final class CacheUtil {

////	/**
////	 * start must be < end
////	 */
//	public static PooledDensityCache generateDensityCache(final int startPosX, final int startPosY, final int startPosZ, final int endPosX, final int endPosY, final int endPosZ, final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ, final PooledSmoothableCache smoothableCache, final PooledStateCache stateCache, final IBlockAccess cache, final PooledMutableBlockPos pooledMutableBlockPos) {
//		final int densityCacheSizeX = endPosX - startPosX;
//		final int densityCacheSizeY = endPosY - startPosY;
//		final int densityCacheSizeZ = endPosZ - startPosZ;
////		return generateDensityCache(startPosX, startPosY, startPosZ, densityCacheSizeX, densityCacheSizeY, densityCacheSizeZ, stateCache, smoothableCache, cacheSizeX, cacheSizeY, cacheSizeZ, cache, pooledMutableBlockPos);
//
//		// Density takes +1 block on every negative axis into account so we need to start at -1 block
//		final int cacheStartPosX = startPosX - 1;
//		final int cacheStartPosY = startPosY - 1;
//		final int cacheStartPosZ = startPosZ - 1;
//
//		// Density takes +1 block on every negative axis into account so we need bigger caches
//		final int cacheSizeX2 = densityCacheSizeX + 1;
//		final int cacheSizeY2 = densityCacheSizeY + 1;
//		final int cacheSizeZ2 = densityCacheSizeZ + 1;
//
//		try (final PooledStateCache stateCache2 = generateStateCache(cacheStartPosX, cacheStartPosY, cacheStartPosZ, cacheSizeX2, cacheSizeY2, cacheSizeZ2, cache, pooledMutableBlockPos);
//		     final PooledSmoothableCache smoothableCache2 = generateSmoothableCache(cacheSizeX2, cacheSizeY2, cacheSizeZ2, stateCache, ModUtil::shouldSmooth);
//		) {
//			return generateDensityCache(startPosX, startPosY, startPosZ, densityCacheSizeX, densityCacheSizeY, densityCacheSizeZ, stateCache, smoothableCache, cacheSizeX2, cacheSizeY2, cacheSizeZ2, cache, pooledMutableBlockPos);
//		}
//
////		return generateDensityCache(startPosX, startPosY, startPosZ, densityCacheSizeX, densityCacheSizeY, densityCacheSizeZ, stateCache, smoothableCache, cacheSizeX, cacheSizeY, cacheSizeZ, cache, pooledMutableBlockPos);
//	}

	public static PooledStateCache generateStateCache(final int startPosX, final int startPosY, final int startPosZ, final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ, final IBlockAccess cache, PooledMutableBlockPos pooledMutableBlockPos) {
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

	public static PooledSmoothableCache generateSmoothableCache(final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ, final PooledStateCache stateCache, Function<IBlockState, Boolean> isStateSmoothable) {
		final PooledSmoothableCache smoothableCache = PooledSmoothableCache.retain(cacheSizeX * cacheSizeY * cacheSizeZ);
		int index = 0;
		for (int z = 0; z < cacheSizeZ; z++) {
			for (int y = 0; y < cacheSizeY; y++) {
				for (int x = 0; x < cacheSizeX; x++) {
					smoothableCache.getSmoothableCache()[index] = isStateSmoothable.apply(stateCache.getStateCache()[index]);
					index++;
				}
			}
		}
		return smoothableCache;
	}

	private static PooledDensityCache generateDensityCache(final int startPosX, final int startPosY, final int startPosZ, final int densityCacheSizeX, final int densityCacheSizeY, final int densityCacheSizeZ, final PooledStateCache stateCache, final PooledSmoothableCache smoothableCache, final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ, final IBlockAccess cache, final PooledMutableBlockPos pooledMutableBlockPos) {
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

	private static float getBlockDensity(final int startPosX, final int startPosY, final int startPosZ, final int posX, final int posY, final int posZ, final PooledStateCache stateCache, final PooledSmoothableCache smoothableCache, final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ, final IBlockAccess cache, final PooledMutableBlockPos pooledMutableBlockPos) {
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

//	public static PooledDensityCache generateDensityCache(final BlockPos renderChunkPosition, final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ, final PooledStateCache stateCache, final PooledSmoothableCache smoothableCache, final IBlockAccess cache, final PooledMutableBlockPos pooledMutableBlockPos) {
//
//		final int renderChunkPositionX = renderChunkPosition.getX();
//		final int renderChunkPositionY = renderChunkPosition.getY();
//		final int renderChunkPositionZ = renderChunkPosition.getZ();
//
//		return CacheUtil.generateDensityCache(
//				renderChunkPositionX,
//				renderChunkPositionY,
//				renderChunkPositionZ,
//				renderChunkPositionX + cacheSizeX,
//				renderChunkPositionY + cacheSizeY,
//				renderChunkPositionZ + cacheSizeZ,
//				cacheSizeX, cacheSizeY, cacheSizeZ,
//				smoothableCache, stateCache, cache, pooledMutableBlockPos
//		);
//
//	}

	public static PooledDensityCache generateDensityCache(
			@Nonnull final BlockPos renderChunkPosition,
			final int densityCacheSizeX, final int densityCacheSizeY, final int densityCacheSizeZ,
			@Nonnull final Function<IBlockState, Boolean> isStateSmoothable,
			@Nonnull final IBlockAccess cache,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos) {

		final int renderChunkPositionX = renderChunkPosition.getX();
		final int renderChunkPositionY = renderChunkPosition.getY();
		final int renderChunkPositionZ = renderChunkPosition.getZ();

		// Density takes +1 block on every negative axis into account so we need to start at -1 block
		final int cachesStartPosX = renderChunkPositionX - 1;
		final int cachesStartPosY = renderChunkPositionY - 1;
		final int cachesStartPosZ = renderChunkPositionZ - 1;

		// Density takes +1 block on every negative axis into account so we need bigger caches
		final int cachesSizeX = densityCacheSizeX + 1;
		final int cachesSizeY = densityCacheSizeY + 1;
		final int cachesSizeZ = densityCacheSizeZ + 1;

		try (final PooledStateCache stateCache = generateStateCache(cachesStartPosX, cachesStartPosY, cachesStartPosZ, cachesSizeX, cachesSizeY, cachesSizeZ, cache, pooledMutableBlockPos);
		     final PooledSmoothableCache smoothableCache = generateSmoothableCache(cachesSizeX, cachesSizeY, cachesSizeZ, stateCache, isStateSmoothable);
		) {

			return CacheUtil.generateDensityCache(
					renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
					densityCacheSizeX, densityCacheSizeY, densityCacheSizeZ,
					stateCache, smoothableCache,
					cachesSizeX, cachesSizeY, cachesSizeZ,
					cache,
					pooledMutableBlockPos
			);
		}

	}

}
