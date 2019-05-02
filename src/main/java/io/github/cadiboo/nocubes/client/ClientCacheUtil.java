package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class ClientCacheUtil {

	public static LazyPackedLightCache generatePackedLightCache(
			final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final IWorldReader cache
	) {
		return LazyPackedLightCache.retain(
				//From -2 to +2
				20, 20, 20,
				cache,
				stateCache,
				renderChunkPosX, renderChunkPosY, renderChunkPosZ
		);
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
