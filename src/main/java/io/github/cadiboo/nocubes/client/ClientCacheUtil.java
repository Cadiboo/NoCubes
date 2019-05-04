package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.biome.BiomeColors;

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

	public static LazyBlockColorCache generateLazyBlockColorCache(
			final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ,
			@Nonnull final IWorldReaderBase cache,
			@Nonnull final BiomeColors.ColorResolver resolver
	) {
		return LazyBlockColorCache.retain(
				//From -2 to +2
				20, 20, 20,
				cache, resolver,
				renderChunkPosX, renderChunkPosY, renderChunkPosZ
		);
	}

}
