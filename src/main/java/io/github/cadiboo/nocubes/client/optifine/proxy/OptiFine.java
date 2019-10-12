package io.github.cadiboo.nocubes.client.optifine.proxy;

import io.github.cadiboo.nocubes.client.ClientUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;

import java.util.List;

/**
 * @author Cadiboo
 */
public interface OptiFine {

	/**
	 * Called to handle OptiFine logic right before a block is rendered.
	 *
	 * @param bufferBuilder The buffer builder. May not be started {@link ClientUtil#startOrContinueBufferBuilder(ChunkRenderTask, int, CompiledChunk, BlockRenderLayer, ChunkRender, BlockPos)}
	 * @return the RenderEnv
	 */
	Object preRenderBlock(final BufferBuilder bufferBuilder, final BlockRenderLayer blockRenderLayer, final BlockState blockState, final BlockPos pos, final RegionRenderCacheBuilder builders, final IEnviromentBlockReader blockAccess);

	/**
	 * Called to handle OptiFine logic right after a block is rendered.
	 */
	void postRenderBlock(Object renderEnv, ChunkRender chunkRender, RegionRenderCacheBuilder builders, CompiledChunk compiledChunk, boolean[] usedBlockRenderLayers);

	/**
	 * @param obj an object to test
	 * @return if the object is an instanceof ChunkCacheOF
	 */
	boolean isChunkCacheOF(Object obj);

	/**
	 * @param chunkCacheOF a ChunkCacheOF instance
	 * @return chunkCacheOF.chunkCache
	 */
	ChunkRenderCache getChunkRenderCache(Object chunkCacheOF);

	/**
	 * Calls SVertexBuilder.pushEntity if Config.isShaders() returns true
	 */
	void pushShaderEntity(IFluidState state, BlockPos pos, IEnviromentBlockReader reader, BufferBuilder buffer);

	/**
	 * Calls SVertexBuilder.pushEntity if Config.isShaders() returns true
	 */
	void pushShaderEntity(BlockState state, BlockPos pos, IEnviromentBlockReader reader, BufferBuilder buffer);

	/**
	 * Calls SVertexBuilder.popEntity if Config.isShaders() returns true
	 */
	void popShaderEntity(BufferBuilder buffer);

	/**
	 * Calls BufferBuilder#getRenderEnv
	 *
	 * @return the RenderEnv
	 */
	Object getRenderEnvironment(BufferBuilder bufferBuilder, BlockState state, BlockPos pos);

	/**
	 * Calls BlockModelCustomizer.getRenderModel
	 *
	 * @return the IBakedModel
	 */
	IBakedModel getRenderModel(IBakedModel model, BlockState state, Object renderEnv);

	/**
	 * Calls BlockModelCustomizer.getRenderQuads
	 *
	 * @return The list of BakedQuads
	 */
	List<BakedQuad> getRenderQuads(List<BakedQuad> quads, IEnviromentBlockReader reader, BlockState state, BlockPos pos, Direction direction, BlockRenderLayer blockRenderLayer, long posRandLong, Object renderEnv);

}
