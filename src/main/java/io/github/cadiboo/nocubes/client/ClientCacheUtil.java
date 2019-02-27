package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class ClientCacheUtil {

	public static PackedLightCache generatePackedLightCache(
			final int startPosX, final int startPosY, final int startPosZ,
			final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ,
			@Nonnull final IWorldReader cache,
			@Nonnull BlockPos.PooledMutableBlockPos pooledMutableBlockPos
	) {
		NoCubes.getProfiler().start("generate pooledPackedLightCache");
		final PackedLightCache pooledPackedLightCache = PackedLightCache.retain(cacheSizeX, cacheSizeY, cacheSizeZ);
		final int[] packedLightCache = pooledPackedLightCache.getPackedLightCache();
		int index = 0;
		for (int z = 0; z < cacheSizeZ; z++) {
			for (int y = 0; y < cacheSizeY; y++) {
				for (int x = 0; x < cacheSizeX; x++) {
					packedLightCache[index] = cache.getBlockState(pooledMutableBlockPos.setPos(startPosX + x, startPosY + y, startPosZ + z)).getPackedLightmapCoords(cache, pooledMutableBlockPos);
					index++;
				}
			}
		}
		NoCubes.getProfiler().end();
		return pooledPackedLightCache;
	}

}
