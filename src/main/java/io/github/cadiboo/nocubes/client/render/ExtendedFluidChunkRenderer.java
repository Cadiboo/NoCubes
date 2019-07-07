package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.LazyPackedLightCache;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class ExtendedFluidChunkRenderer {

	private static final ThreadLocal<boolean[]> IS_FLUID_SOURCE_THREAD_LOCAL = ThreadLocal.withInitial(() -> new boolean[0]);

	public static void renderChunk(
			@Nonnull final ChunkRender chunkRender,
			@Nonnull final ChunkRenderTask generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			@Nonnull final IWorldReader reader,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final StateCache stateCache,
			@Nonnull final SmoothableCache smoothableCache,
			@Nonnull final LazyPackedLightCache lazyPackedLightCache
	) {
		try (final ModProfiler ignored = ModProfiler.get().start("Render extended fluid chunk")) {
			final BlockState[] blockCacheArray = stateCache.getBlockStates();
			final IFluidState[] fluidCacheArray = stateCache.getFluidStates();

			final int fluidCacheLength = blockCacheArray.length;

			final int stateCacheSizeX = stateCache.sizeX;
			final int stateCacheSizeY = stateCache.sizeY;
			final int stateCacheStartPaddingX = stateCache.startPaddingX;
			final int stateCacheStartPaddingY = stateCache.startPaddingY;
			final int stateCacheStartPaddingZ = stateCache.startPaddingZ;

			boolean[] isFluidSource = IS_FLUID_SOURCE_THREAD_LOCAL.get();
			// TODO: shouldn't really be the same size as the state cache
			if (isFluidSource.length < fluidCacheLength) {
				isFluidSource = new boolean[fluidCacheLength];
				IS_FLUID_SOURCE_THREAD_LOCAL.set(isFluidSource);
			}
			for (int i = 0; i < fluidCacheLength; ++i) {
				isFluidSource[i] = fluidCacheArray[i].isSource();
			}

			final boolean[] isSmoothable = smoothableCache.getSmoothableCache();

			final int smoothableCacheSizeX = smoothableCache.sizeX;
			final int smoothableCacheSizeY = smoothableCache.sizeY;
			final int smoothableCacheStartPaddingX = smoothableCache.startPaddingX;
			final int smoothableCacheStartPaddingY = smoothableCache.startPaddingY;
			final int smoothableCacheStartPaddingZ = smoothableCache.startPaddingZ;

			final int extendRange = Config.extendFluidsRange.getRange();

			// For offset = -1 or -2 to offset = 1 or 2;
			final int maxXOffset = extendRange;
			final int maxZOffset = extendRange;

			for (int z = 0; z < 16; ++z) {
				for (int y = 0; y < 16; ++y) {
					for (int x = 0; x < 16; ++x) {

						final int stateCacheOffsetX = stateCacheStartPaddingX + x;
						final int stateCacheOffsetY = stateCacheStartPaddingY + y;
						final int stateCacheOffsetZ = stateCacheStartPaddingZ + z;

						if (!isSmoothable[smoothableCache.getIndex(
								smoothableCacheStartPaddingX + x,
								smoothableCacheStartPaddingY + y,
								smoothableCacheStartPaddingZ + z,
								smoothableCacheSizeX, smoothableCacheSizeY
						)]) {
							continue;
						}
						if (!fluidCacheArray[stateCache.getIndex(
								stateCacheOffsetX,
								stateCacheOffsetY,
								stateCacheOffsetZ,
								stateCacheSizeX, stateCacheSizeY
						)].isEmpty()) {
							continue;
						}

						OFFSET:
						for (int xOffset = -maxXOffset; xOffset <= maxXOffset; ++xOffset) {
							for (int zOffset = -maxZOffset; zOffset <= maxZOffset; ++zOffset) {

								//no point in checking myself
								if (xOffset == 0 && zOffset == 0) {
									continue;
								}

								// Add 1 or 2 to account for offset=-1 or -2
								final int fluidStateIndex = stateCache.getIndex(
										stateCacheOffsetX + xOffset,
										stateCacheOffsetY,
										stateCacheOffsetZ + zOffset,
										stateCacheSizeX, stateCacheSizeY
								);
								if (!isFluidSource[fluidStateIndex]) {
									continue;
								}

								// Only render if block up is not solid
								if (blockCacheArray[stateCache.getIndex(
										stateCacheOffsetX + xOffset,
										stateCacheOffsetY + 1,
										stateCacheOffsetZ + zOffset,
										stateCacheSizeX, stateCacheSizeY
								)].isSolid()) {
									continue;
								}

								final IFluidState fluidState = fluidCacheArray[fluidStateIndex];

								final BlockRenderLayer blockRenderLayer = ClientUtil.getCorrectRenderLayer(fluidState);
								final int blockRenderLayerOrdinal = blockRenderLayer.ordinal();

								final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(generator, blockRenderLayerOrdinal, compiledChunk, blockRenderLayer, chunkRender, renderChunkPosition);
								final int worldX = renderChunkPositionX + x;
								final int worldY = renderChunkPositionY + y;
								final int worldZ = renderChunkPositionZ + z;
								OptiFineCompatibility.pushShaderThing(fluidState, pooledMutableBlockPos.setPos(
										worldX,
										worldY,
										worldZ
								), reader, bufferBuilder);
								try {
									usedBlockRenderLayers[blockRenderLayerOrdinal] |= ExtendedFluidBlockRenderer.renderExtendedFluid(
											worldX,
											worldY,
											worldZ,
											pooledMutableBlockPos.setPos(
													worldX + xOffset,
													worldY,
													worldZ + zOffset
											),
											reader,
											fluidState,
											bufferBuilder,
											blockRendererDispatcher,
											lazyPackedLightCache
									);
								} finally {
									OptiFineCompatibility.popShaderThing(bufferBuilder);
								}

								break OFFSET;
							}
						}

					}
				}
			}
		}
	}

}
