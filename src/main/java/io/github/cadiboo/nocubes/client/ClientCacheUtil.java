package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nonnull;

import static net.minecraft.world.biome.BiomeColors.IColorResolver;

/**
 * @author Cadiboo
 */
public final class ClientCacheUtil {

	public static LazyPackedLightCache generatePackedLightCache(
			final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final IEnviromentBlockReader cache
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
			@Nonnull final IEnviromentBlockReader cache,
			@Nonnull final IColorResolver resolver
	) {
		return LazyBlockColorCache.retain(
				//From -2 to +2
				20, 20, 20,
				cache, resolver,
				renderChunkPosX, renderChunkPosY, renderChunkPosZ
		);
	}

}
