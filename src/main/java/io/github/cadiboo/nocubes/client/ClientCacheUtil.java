package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper.ColorResolver;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class ClientCacheUtil {

	public static LazyPackedLightCache generateLazyPackedLightCache(
			final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final IBlockAccess cache
	) {
		return LazyPackedLightCache.retain(
				cache,
				stateCache,
				renderChunkPosX, renderChunkPosY, renderChunkPosZ
		);
	}

	public static LazyBlockColorCache generateLazyBlockColorCache(
			final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ,
			@Nonnull final IBlockAccess cache,
			@Nonnull final ColorResolver resolver
	) {
		return LazyBlockColorCache.retain(
				cache, resolver,
				renderChunkPosX, renderChunkPosY, renderChunkPosZ
		);
	}

}
