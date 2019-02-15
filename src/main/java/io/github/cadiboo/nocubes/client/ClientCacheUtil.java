package io.github.cadiboo.nocubes.client;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExtendedBlockView;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class ClientCacheUtil {

	public static PackedLightCache generatePackedLightCache(
			final int startPosX, final int startPosY, final int startPosZ,
			final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ,
			@Nonnull final ExtendedBlockView cache,
			@Nonnull BlockPos.PooledMutable pooledMutableBlockPos
	) {
		final PackedLightCache pooledPackedLightCache = PackedLightCache.retain(cacheSizeX, cacheSizeY, cacheSizeZ);
		int index = 0;
		for (int z = 0; z < cacheSizeZ; z++) {
			for (int y = 0; y < cacheSizeY; y++) {
				for (int x = 0; x < cacheSizeX; x++) {
					pooledPackedLightCache.getPackedLightCache()[index] = cache.getBlockState(pooledMutableBlockPos.set(startPosX + x, startPosY + y, startPosZ + z)).getLightSubtracted(cache, pooledMutableBlockPos);
					index++;
				}
			}
		}
		return pooledPackedLightCache;
	}

}
