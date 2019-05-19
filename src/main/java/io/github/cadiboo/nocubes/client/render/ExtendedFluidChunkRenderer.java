package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.LazyPackedLightCache;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class ExtendedFluidChunkRenderer {

	public static void renderChunk(
			@Nonnull final RenderChunk renderChunk,
			@Nonnull final ChunkRenderTask generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			@Nonnull final IWorldReader blockAccess,
			@Nonnull final BlockPos.PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final StateCache stateCache,
			@Nonnull final SmoothableCache smoothableCache,
			@Nonnull final LazyPackedLightCache packedLightCache
	) {

		try (final ModProfiler ignored = ModProfiler.get().start("render extended fluid chunk")) {
			final IBlockState[] blockCacheArray = stateCache.getBlockStates();
			final IFluidState[] fluidCacheArray = stateCache.getFluidStates();

			final int fluidCacheLength = fluidCacheArray.length;

			final boolean[] isFluidSource = new boolean[fluidCacheLength];
			for (int i = 0; i < fluidCacheLength; i++) {
				isFluidSource[i] = fluidCacheArray[i].isSource();
			}

			final boolean[] isSmoothable = smoothableCache.getSmoothableCache();

			final int extendRange = Config.extendFluidsRange.getRange();

			final int cacheAddX = 2;
			final int cacheAddY = 2;
			final int cacheAddZ = 2;

			// For offset = -1 or -2 to offset = 1 or 2;
			final int maxXOffset = extendRange;
			final int maxZOffset = extendRange;

			for (int z = 0; z < 16; ++z) {
				for (int y = 0; y < 16; ++y) {
					for (int x = 0; x < 16; ++x) {

						if (!isSmoothable[smoothableCache.getIndex(x + cacheAddX, y + cacheAddY, z + cacheAddZ)]) {
							continue;
						}
						if (!fluidCacheArray[stateCache.getIndex(x + cacheAddX, y + cacheAddY, z + cacheAddZ)].isEmpty()) {
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
								final int fluidStateIndex = stateCache.getIndex(x + xOffset + cacheAddX, y + cacheAddY, z + zOffset + cacheAddZ);
								if (!isFluidSource[fluidStateIndex]) {
									continue;
								}

								// only render if block up is not solid
								if (blockCacheArray[stateCache.getIndex(x + cacheAddX, y + cacheAddY + 1, z + cacheAddZ)].isSolid()) {
									continue;
								}

								final IFluidState fluidState = fluidCacheArray[fluidStateIndex];

								final BlockRenderLayer blockRenderLayer = ClientUtil.getCorrectRenderLayer(fluidState);
								final int blockRenderLayerOrdinal = blockRenderLayer.ordinal();

								final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(generator, blockRenderLayerOrdinal, compiledChunk, blockRenderLayer, renderChunk, renderChunkPosition);
								OptiFineCompatibility.pushShaderThing(fluidState, pooledMutableBlockPos.setPos(
										renderChunkPositionX + x,
										renderChunkPositionY + y,
										renderChunkPositionZ + z
								), blockAccess, bufferBuilder);
								try {
									usedBlockRenderLayers[blockRenderLayerOrdinal] |= ExtendedFluidBlockRenderer.renderExtendedFluid(
											renderChunkPositionX + x,
											renderChunkPositionY + y,
											renderChunkPositionZ + z,
											pooledMutableBlockPos.setPos(
													renderChunkPositionX + x + xOffset,
													renderChunkPositionY + y,
													renderChunkPositionZ + z + zOffset
											),
											blockAccess,
											blockCacheArray[stateCache.getIndex(x + xOffset + cacheAddX, y + cacheAddY, z + zOffset + cacheAddZ)],
											fluidState,
											bufferBuilder,
											packedLightCache
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
