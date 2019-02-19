package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.OptifineCompatibility;
import io.github.cadiboo.nocubes.util.SmoothableCache;
import io.github.cadiboo.nocubes.util.StateCache;
import net.minecraft.block.BlockFlowingFluid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
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
public class ExtendedLiquidChunkRenderer {

	public static boolean isLiquidSource(final IBlockState state) {
		return state.getBlock() instanceof BlockFlowingFluid && state.get(BlockFlowingFluid.LEVEL) == 0;
	}

	//TODO new caches for IFluidStates
	public static void renderChunk(
			@Nonnull final RenderChunk renderChunk,
			@Nonnull final ChunkRenderTask generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			@Nonnull final IWorldReader blockAccess,
			@Nonnull final BlockPos.PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final StateCache stateCache, @Nonnull final SmoothableCache smoothableCache
	) {

		final IBlockState[] stateCacheArray = stateCache.getStateCache();

		final int stateCacheLength = stateCacheArray.length;

		final boolean[] isLiquid = new boolean[stateCacheLength];
		for (int i = 0; i < stateCacheLength; i++) {
			isLiquid[i] = isLiquidSource(stateCacheArray[i]);
		}

		final boolean[] isSmoothable = smoothableCache.getSmoothableCache();

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
							if (stateCacheArray[stateCache.getIndex(x + xOffset + cacheAddX, y + cacheAddY + 1, z + zOffset + cacheAddZ)].isNormalCube()) {
								continue;
							}

							final IBlockState liquidState = stateCacheArray[liquidStateIndex];
							final IFluidState fluidState = liquidState.getFluidState();

							final BlockRenderLayer blockRenderLayer = ClientUtil.getRenderLayer(fluidState);
							final int blockRenderLayerOrdinal = blockRenderLayer.ordinal();

							final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(generator, blockRenderLayerOrdinal, compiledChunk, blockRenderLayer, renderChunk, renderChunkPosition);
							OptifineCompatibility.pushShaderThing(liquidState, pooledMutableBlockPos.setPos(
									renderChunkPositionX + x,
									renderChunkPositionY + y,
									renderChunkPositionZ + z
							), blockAccess, bufferBuilder);

							//TODO smooth lighting?
							usedBlockRenderLayers[blockRenderLayerOrdinal] |= ExtendedLiquidBlockRenderer.renderExtendedLiquid(
									renderChunkPositionX + x,
									renderChunkPositionY + y,
									renderChunkPositionZ + z,
									pooledMutableBlockPos.setPos(
											renderChunkPositionX + x + xOffset,
											renderChunkPositionY + y,
											renderChunkPositionZ + z + zOffset
									),
									blockAccess,
									liquidState,
									fluidState,
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
