package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.ModProfiler;
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
		try (ModProfiler ignored = NoCubes.getProfiler().start("generate packedLightCache")) {
			final PackedLightCache packedLightCache = PackedLightCache.retain(cacheSizeX, cacheSizeY, cacheSizeZ);
			final int[] packedLightCacheArray = packedLightCache.getPackedLightCache();

			int index = 0;
			for (int z = 0; z < cacheSizeZ; z++) {
				for (int y = 0; y < cacheSizeY; y++) {
					for (int x = 0; x < cacheSizeX; x++) {
						packedLightCacheArray[index] = cache.getBlockState(pooledMutableBlockPos.setPos(startPosX + x, startPosY + y, startPosZ + z)).getPackedLightmapCoords(cache, pooledMutableBlockPos);
						index++;
					}
				}
			}
			return packedLightCache;
		}
	}

}
