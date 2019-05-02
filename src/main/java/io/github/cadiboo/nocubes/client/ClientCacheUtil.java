package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class ClientCacheUtil {

	public static PackedLightCache generatePackedLightCache(
			final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final IWorldReader cache,
			@Nonnull BlockPos.PooledMutableBlockPos pooledMutableBlockPos
	) {
		try (ModProfiler ignored = ModProfiler.get().start("generate packedLightCache")) {
			final PackedLightCache packedLightCache = PackedLightCache.retain(
					//From -2 to +2
					20, 20, 20
			);
			final int[] packedLightCacheArray = packedLightCache.getPackedLightCache();
			final IBlockState[] stateCacheArray = stateCache.getBlockStates();

			int index = 0;
			for (int z = 0; z < 20; ++z) {
				for (int y = 0; y < 20; ++y) {
					for (int x = 0; x < 20; ++x, ++index) {
						packedLightCacheArray[index] = stateCacheArray[stateCache.getIndex(x, y, z)].getPackedLightmapCoords(
								cache,
								pooledMutableBlockPos.setPos(
										// -2 because offset
										renderChunkPosX + x - 2,
										renderChunkPosY + y - 2,
										renderChunkPosZ + z - 2
								)
						);
					}
				}
			}
			return packedLightCache;
		}
	}

	public static LazyBiomeGrassColorCache generateBiomeGrassColorCacheCache(
			final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ,
			@Nonnull final IWorldReader cache
	) {
		return LazyBiomeGrassColorCache.retain(
				//From -2 to +2
				20, 20, 20,
				cache,
				renderChunkPosX, renderChunkPosY, renderChunkPosZ
		);
	}

}
