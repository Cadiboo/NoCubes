package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper.ColorResolver;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class ClientCacheUtil {

	public static LazyPackedLightCache generatePackedLightCache(
			final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final IBlockAccess cache
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
			@Nonnull final IBlockAccess cache,
			@Nonnull final ColorResolver resolver
	) {
		return LazyBlockColorCache.retain(
				//From -2 to +2
				20, 20, 20,
				cache, resolver,
				renderChunkPosX, renderChunkPosY, renderChunkPosZ
		);
	}

}
