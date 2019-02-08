package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.OptifineCompatibility;
import io.github.cadiboo.nocubes.client.PooledPackedLightCache;
import io.github.cadiboo.nocubes.util.PooledSmoothableCache;
import io.github.cadiboo.nocubes.util.PooledStateCache;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class ExtendedLiquidChunkRenderer {

	public static boolean isLiquidSource(final IBlockState state) {
		return state.getBlock() instanceof BlockLiquid && state.getValue(BlockLiquid.LEVEL) == 0;
	}

	public static void renderChunk(
			@Nonnull final RenderChunk renderChunk,
			@Nonnull final ChunkCompileTaskGenerator generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			@Nonnull final IBlockAccess blockAccess,
			@Nonnull final BlockPos.PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final PooledStateCache pooledStateCache, @Nonnull final PooledSmoothableCache pooledSmoothableCache, @Nonnull final PooledPackedLightCache pooledPackedLightCache,
			final int cachesSizeX, final int cachesSizeY, final int cachesSizeZ
	) {

		final IBlockState[] stateCache = pooledStateCache.getStateCache();

		final int stateCacheLength = stateCache.length;

		final boolean[] isLiquid = new boolean[stateCacheLength];
		for (int i = 0; i < stateCacheLength; i++) {
			isLiquid[i] = isLiquidSource(stateCache[i]);
		}

		final boolean[] isSmoothable = pooledSmoothableCache.getSmoothableCache();

		final Minecraft minecraft = Minecraft.getMinecraft();
		final TextureMap textureMap = minecraft.getTextureMapBlocks();
		final BlockColors blockColors = minecraft.getBlockColors();

		final int cacheAddX = 1;
		final int cacheAddY = 1;
		final int cacheAddZ = 1;

		for (int z = 0; z < 16; z++) {
			for (int y = 0; y < 16; y++) {
				for (int x = 0; x < 16; x++) {

					//TODO: set the index in the loop with whatever black magic mikelasko uses
					final int smoothableIndex = (x + cacheAddX) + cachesSizeX * (y + cacheAddY + cachesSizeY * (z + cacheAddZ));

					if (!isSmoothable[smoothableIndex]) {
						continue;
					}

					// For offset = -2 to offset = 2;
					OFFSET:
					for (int xOffset = -2; xOffset < 4; ++xOffset) {
						for (int zOffset = -2; zOffset < 4; ++zOffset) {

							//no point in checking myself
							if (xOffset == 0 && zOffset == 0) {
								continue;
							}

							// Add 1 to account for offset=-1
							// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
							final int liquidStateIndex = (x + xOffset + cacheAddX) + cachesSizeX * (y + cacheAddY + cachesSizeY * (z + zOffset + cacheAddZ));
							if (!isLiquid[liquidStateIndex]) {
								continue;
							}

							// only render if block up is air/not a normal cube
							final int upStateIndex = (x + xOffset + cacheAddX) + cachesSizeX * (y + cacheAddY + 1 + cachesSizeY * (z + zOffset + cacheAddZ));
							if (stateCache[upStateIndex].isNormalCube()) {
								continue;
							}

							final IBlockState liquidState = stateCache[liquidStateIndex];

							final BlockRenderLayer blockRenderLayer = ClientUtil.getRenderLayer(liquidState);
							final int blockRenderLayerOrdinal = blockRenderLayer.ordinal();

							final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(generator, blockRenderLayerOrdinal, compiledChunk, blockRenderLayer, renderChunk, renderChunkPosition);
							OptifineCompatibility.pushShaderThing(liquidState, pooledMutableBlockPos.setPos(
									renderChunkPositionX + x,
									renderChunkPositionY + y,
									renderChunkPositionZ + z
							), blockAccess, bufferBuilder);
							//TODO lighting?
							usedBlockRenderLayers[blockRenderLayerOrdinal] |= ExtendedLiquidBlockRenderer.renderExtendedLiquid(
									textureMap, blockColors,
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
