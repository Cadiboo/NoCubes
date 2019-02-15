package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.OptifineCompatibility;
import io.github.cadiboo.nocubes.util.SmoothableCache;
import io.github.cadiboo.nocubes.util.StateCache;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.block.BlockColorMap;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.ChunkRenderData;
import net.minecraft.client.render.chunk.ChunkRenderTask;
import net.minecraft.client.render.chunk.ChunkRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExtendedBlockView;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class ExtendedLiquidChunkRenderer {

	public static boolean isLiquidSource(final BlockState state) {
		return state.getBlock() instanceof FluidBlock && state.get(FluidBlock.field_11278) == 0;
	}

	public static void renderChunk(
			@Nonnull final ChunkRenderer renderChunk,
			@Nonnull final ChunkRenderTask generator,
			@Nonnull final ChunkRenderData compiledChunk,
			@Nonnull final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			@Nonnull final ExtendedBlockView blockAccess,
			@Nonnull final BlockPos.PooledMutable pooledMutableBlockPos,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final BlockRenderManager blockRendererDispatcher,
			@Nonnull final StateCache stateCache, @Nonnull final SmoothableCache smoothableCache
	) {

		final BlockState[] stateCacheArray = stateCache.getStateCache();

		final int stateCacheLength = stateCacheArray.length;

		final boolean[] isLiquid = new boolean[stateCacheLength];
		for (int i = 0; i < stateCacheLength; i++) {
			isLiquid[i] = isLiquidSource(stateCacheArray[i]);
		}

		final boolean[] isSmoothable = smoothableCache.getSmoothableCache();

		final MinecraftClient minecraft = MinecraftClient.getInstance();
		final SpriteAtlasTexture textureMap = minecraft.getSpriteAtlas();
		final BlockColorMap blockColors = minecraft.getBlockColorMap();

		final int extendRange = ClientUtil.getExtendLiquidsRange();

		final int cacheAddX = extendRange;
		final int cacheAddY = 0;
		final int cacheAddZ = extendRange;

		// For offset = -1 or -2 to offset = 1 or 2;
		final int maxXOffset = extendRange;
		final int maxZOffset = extendRange;

		for (int z = 0; z < 16; z++) {
			for (int y = 0; y < 16; y++) {
				for (int x = 0; x < 16; x++) {

					if (!isSmoothable[smoothableCache.getIndex(x + cacheAddX, y + cacheAddY, z + cacheAddZ)]) {
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
							final int liquidStateIndex = stateCache.getIndex(x + xOffset + cacheAddX, y + cacheAddY, z + zOffset + cacheAddZ);
							if (!isLiquid[liquidStateIndex]) {
								continue;
							}

							// only render if block up is air/not a normal cube
							if (stateCacheArray[stateCache.getIndex(x + xOffset + cacheAddX, y + cacheAddY + 1, z + zOffset + cacheAddZ)].isFullBoundsCubeForCulling()) {
								continue;
							}

							final BlockState liquidState = stateCacheArray[liquidStateIndex];

							final BlockRenderLayer blockRenderLayer = ClientUtil.getRenderLayer(liquidState);
							final int blockRenderLayerOrdinal = blockRenderLayer.ordinal();

							final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(generator, blockRenderLayerOrdinal, compiledChunk, blockRenderLayer, renderChunk, renderChunkPosition);
							OptifineCompatibility.pushShaderThing(liquidState, pooledMutableBlockPos.set(
									renderChunkPositionX + x,
									renderChunkPositionY + y,
									renderChunkPositionZ + z
							), blockAccess, bufferBuilder);

							//TODO smooth lighting?
							usedBlockRenderLayers[blockRenderLayerOrdinal] |= ExtendedLiquidBlockRenderer.renderExtendedLiquid(
									textureMap, blockColors,
									renderChunkPositionX + x,
									renderChunkPositionY + y,
									renderChunkPositionZ + z,
									pooledMutableBlockPos.set(
											renderChunkPositionX + x + xOffset,
											renderChunkPositionY + y,
											renderChunkPositionZ + z + zOffset
									),
									blockAccess,
									blockAccess.getFluidState(pooledMutableBlockPos),
									bufferBuilder
							);
							OptifineCompatibility.popShaderThing(bufferBuilder);

							break OFFSET;
						}
					}

				}
			}
		}

	}

}
