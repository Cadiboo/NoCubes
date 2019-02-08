package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.OptifineCompatibility;
import io.github.cadiboo.nocubes.client.PooledPackedLightCache;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.PooledStateCache;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
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

	private static final int liquidCacheAddX = 1;
	private static final int liquidCacheAddY = 1;
	private static final int liquidCacheAddZ = 1;
	private static final int liquidCacheSizeX = 16 + liquidCacheAddX + liquidCacheAddX;
	private static final int liquidCacheSizeY = 16 + liquidCacheAddY + liquidCacheAddY;
	private static final int liquidCacheSizeZ = 16 + liquidCacheAddZ + liquidCacheAddZ;

	private static final ThreadLocal<PooledStateCache> LIQUID_CACHE = new ThreadLocal<>();

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
			@Nonnull final PooledStateCache pooledStateCache, @Nonnull final PooledPackedLightCache pooledPackedLightCache,
			final int cachesSizeX, final int cachesSizeY, final int cachesSizeZ
	) {

		final IBlockState[] stateCache = pooledStateCache.getStateCache();

		final int stateCacheLength = stateCache.length;

		final boolean[] isLiquid = new boolean[stateCacheLength];
		for (int i = 0; i < stateCacheLength; i++) {
			isLiquid[i] = isLiquidSource(stateCache[i]);
		}

		final boolean[] isSmoothable = new boolean[stateCacheLength];
		for (int i = 0; i < stateCacheLength; i++) {
			isSmoothable[i] = ModUtil.TERRAIN_SMOOTHABLE.isSmoothable(stateCache[i]) || ModUtil.LEAVES_SMOOTHABLE.isSmoothable(stateCache[i]);
		}

		final Minecraft minecraft = Minecraft.getMinecraft();
		final TextureMap textureMap = minecraft.getTextureMapBlocks();
		final BlockColors blockColors = minecraft.getBlockColors();

		for (int z = 0; z < 16; z++) {
			for (int y = 0; y < 16; y++) {
				for (int x = 0; x < 16; x++) {

					//TODO: set the index in the loop with whatever black magic mikelasko uses
					final int smoothableIndex = (x + 1) + cachesSizeX * (y + 1 + cachesSizeY * (z + 1));

					if (!isSmoothable[smoothableIndex]) {
						continue;
					}

					// For offset = -1, offset = 1;
					OFFSET:
					for (int xOffset = -1; xOffset < 2; ++xOffset) {
						for (int zOffset = -1; zOffset < 2; ++zOffset) {

							//no point in checking myself
							if (xOffset == 0 && zOffset == 0) {
								continue;
							}

							// Add 1 to account for offset=-1
							// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
							final int liquidStateIndex = (x + xOffset + 1) + cachesSizeX * (y + 1 + cachesSizeY * (z + zOffset + 1));
							if (!isLiquid[liquidStateIndex]) {
								continue;
							}

							// only render if block up is air/not a normal cube
							final int upStateIndex = (x + xOffset + 1) + cachesSizeX * (y + 1 + 1 + cachesSizeY * (z + zOffset + 1));
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
