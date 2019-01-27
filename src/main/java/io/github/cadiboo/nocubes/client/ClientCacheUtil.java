package io.github.cadiboo.nocubes.client;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class ClientCacheUtil {

	public static PooledPackedLightCache generatePackedLightCache(
			final int startPosX, final int startPosY, final int startPosZ,
			final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ,
			@Nonnull final IBlockAccess cache,
			@Nonnull BlockPos.PooledMutableBlockPos pooledMutableBlockPos
	) {
		final PooledPackedLightCache pooledPackedLightCache = PooledPackedLightCache.retain(cacheSizeX * cacheSizeY * cacheSizeZ);
		int index = 0;
		for (int z = 0; z < cacheSizeZ; z++) {
			for (int y = 0; y < cacheSizeY; y++) {
				for (int x = 0; x < cacheSizeX; x++) {
					pooledPackedLightCache.getPackedLightCache()[index] = cache.getBlockState(pooledMutableBlockPos.setPos(startPosX + x, startPosY + y, startPosZ + z)).getPackedLightmapCoords(cache, pooledMutableBlockPos);
					index++;
				}
			}
		}
		return pooledPackedLightCache;
	}

}
