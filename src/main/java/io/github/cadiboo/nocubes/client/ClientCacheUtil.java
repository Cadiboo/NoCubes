package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.StateCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class ClientCacheUtil {

	public static PackedLightCache generatePackedLightCache(
			final int startPosX, final int startPosY, final int startPosZ,
			@Nonnull StateCache stateCache,
			@Nonnull final IBlockAccess blockAccess,
			@Nonnull BlockPos.PooledMutableBlockPos pooledMutableBlockPos
	) {
		final int cacheSizeX = stateCache.sizeX;
		final int cacheSizeY = stateCache.sizeY;
		final int cacheSizeZ = stateCache.sizeZ;

		final PackedLightCache packedLightCache = PackedLightCache.retain(cacheSizeX, cacheSizeY, cacheSizeZ);
		final int[] packedLightCacheArray = packedLightCache.getPackedLightCache();

		int index = 0;
		for (int z = 0; z < cacheSizeZ; z++) {
			for (int y = 0; y < cacheSizeY; y++) {
				for (int x = 0; x < cacheSizeX; x++) {
					pooledMutableBlockPos.setPos(startPosX + x, startPosY + y, startPosZ + z);
					packedLightCacheArray[index] = stateCache.getStateCache()[stateCache.getIndex(x, y, z)].getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
					index++;
				}
			}
		}
		return packedLightCache;
	}

}
