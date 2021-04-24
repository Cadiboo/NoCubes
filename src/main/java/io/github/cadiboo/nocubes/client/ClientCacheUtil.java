package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper.ColorResolver;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * @author Cadiboo
 */
public final class ClientCacheUtil {

	public static LazyPackedLightCache generateLazyPackedLightCache(
		// from position
		final int fromX, final int fromY, final int fromZ,
		// to position
		final int toX, final int toY, final int toZ,
		// the difference between the chunkRenderPosition and from position. Always positive
		final int startPaddingX, final int startPaddingY, final int startPaddingZ,
		final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ,
		@Nonnull final StateCache stateCache,
		@Nonnull final IBlockAccess cache
	) {
		final int cacheSizeX = Math.abs(toX - fromX);
		final int cacheSizeY = Math.abs(toY - fromY);
		final int cacheSizeZ = Math.abs(toZ - fromZ);
		return LazyPackedLightCache.retain(
			startPaddingX, startPaddingY, startPaddingZ,
			cacheSizeX, cacheSizeY, cacheSizeZ,
			cache,
			stateCache,
			renderChunkPosX, renderChunkPosY, renderChunkPosZ
		);
	}

	public static LazyBlockColorCache generateLazyBlockColorCache(
		BlockPos startInclusive, BlockPos endExclusive,
		IBlockAccess world,
		ColorResolver resolver,
		Predicate<IBlockState> shouldApply
	) {
		BlockPos size = endExclusive.subtract(startInclusive);
		return LazyBlockColorCache.retain(
			startPaddingX, startPaddingY, startPaddingZ,
			cacheSizeX, cacheSizeY, cacheSizeZ,
			cache, resolver, shouldApply,
			renderChunkPosX, renderChunkPosY, renderChunkPosZ
		);
	}

}
