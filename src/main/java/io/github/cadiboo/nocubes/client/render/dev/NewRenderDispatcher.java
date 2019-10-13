package io.github.cadiboo.nocubes.client.render.dev;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.client.optifine.proxy.OptiFine;
import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import java.util.Random;

import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES;
import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES_LENGTH;

/**
 * @author Cadiboo
 */
public final class NewRenderDispatcher {

	public static void renderChunkVanilla(
			@Nonnull final ChunkRender chunkRender,
			@Nonnull final BlockPos chunkRenderPos,
			@Nonnull final ChunkRenderTask chunkRenderTask,
			@Nonnull final CompiledChunk compiledChunk,
			// Use World for eagerly generated caches
			@Nonnull final IWorld world,
			// Use RenderChunkCache for lazily generated caches
			@Nonnull final IEnviromentBlockReader chunkRenderCache,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final Random random,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher
	) {
		final int posX = chunkRenderPos.getX();
		final int posY = chunkRenderPos.getY();
		final int posZ = chunkRenderPos.getZ();

		final RegionRenderCacheBuilder builders = chunkRenderTask.getRegionRenderCacheBuilder();
		final OptiFine optiFine = OptiFineCompatibility.get();

		try (PooledMutableBlockPos pos = PooledMutableBlockPos.retain()) {
			for (int z = 0; z < 16; ++z) {
				for (int y = 0; y < 16; ++y) {
					for (int x = 0; x < 16; ++x) {
						pos.setPos(posX + x, posY + y, posZ + z);

						final BlockState blockState = chunkRenderCache.getBlockState(pos);
						final IFluidState fluidState = chunkRenderCache.getFluidState(pos);

						boolean forgeRenderLayerChanged = false;
						IModelData modelData = null;

						for (int i = BLOCK_RENDER_LAYER_VALUES_LENGTH - 1; i >= 0; i--) {
							final BlockRenderLayer initial = BLOCK_RENDER_LAYER_VALUES[i];

							final boolean renderFluid = !fluidState.isEmpty() && fluidState.canRenderInLayer(initial);
							final boolean renderBlock = !Hooks.canBlockStateRender(blockState) && blockState.getRenderType() != BlockRenderType.INVISIBLE;

							int layerOrdinal = 0;
							BlockRenderLayer layer = null;
							BufferBuilder bufferBuilder = null;

							if (renderFluid || renderBlock) {
								layerOrdinal = ClientUtil.getCorrectRenderLayer(i);
								layer = BLOCK_RENDER_LAYER_VALUES[layerOrdinal];
								ForgeHooksClient.setRenderLayer(layer);
								forgeRenderLayerChanged = true;
								bufferBuilder = builders.getBuilder(layerOrdinal);
								if (modelData == null)
									modelData = chunkRenderTask.getModelData(pos);
							}
							if (renderFluid) {
//								Object renderEnv = optiFine.preRenderBlock(bufferBuilder, layer, fluidState, pos, builders, chunkRenderCache);
								Object renderEnv = optiFine.preRenderBlock(bufferBuilder, layer, blockState, pos, builders, chunkRenderCache);
								ClientUtil.startOrContinueBufferBuilder(compiledChunk, layer, chunkRender, chunkRenderPos, bufferBuilder);
								usedBlockRenderLayers[layerOrdinal] |= blockRendererDispatcher.renderFluid(pos, chunkRenderCache, bufferBuilder, fluidState);
								optiFine.postRenderBlock(renderEnv, chunkRender, builders, compiledChunk, usedBlockRenderLayers);
							}
							if (renderBlock) {
								Object renderEnv = optiFine.preRenderBlock(bufferBuilder, layer, blockState, pos, builders, chunkRenderCache);
								ClientUtil.startOrContinueBufferBuilder(compiledChunk, layer, chunkRender, chunkRenderPos, bufferBuilder);
								usedBlockRenderLayers[layerOrdinal] |= blockRendererDispatcher.renderBlock(blockState, pos, chunkRenderCache, bufferBuilder, random, modelData);
								optiFine.postRenderBlock(renderEnv, chunkRender, builders, compiledChunk, usedBlockRenderLayers);
							}
						}
						if (forgeRenderLayerChanged)
							ForgeHooksClient.setRenderLayer(null);
					}
				}
			}
		}
	}

	public static void renderChunk(
			@Nonnull final ChunkRender chunkRender,
			@Nonnull final BlockPos chunkRenderPos,
			@Nonnull final ChunkRenderTask chunkRenderTask,
			@Nonnull final CompiledChunk compiledChunk,
			// Use World for eagerly generated caches
			@Nonnull final IWorld world,
			// Use RenderChunkCache for lazily generated caches
			@Nonnull final IEnviromentBlockReader chunkRenderCache,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final Random random,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher
	) {
		MarchingCubesRenderer.renderChunk(chunkRender, chunkRenderPos, chunkRenderTask, compiledChunk, world, chunkRenderCache, usedBlockRenderLayers, random, blockRendererDispatcher);
	}

	public static void renderSmoothBlockDamage(
			final Tessellator tessellatorIn,
			final BufferBuilder bufferBuilderIn,
			final BlockPos blockpos,
			final BlockState iblockstate,
			final IEnviromentBlockReader world,
			final TextureAtlasSprite textureatlassprite
	) {

	}

}
