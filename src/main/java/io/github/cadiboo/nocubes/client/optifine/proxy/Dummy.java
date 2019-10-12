package io.github.cadiboo.nocubes.client.optifine.proxy;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
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
public class Dummy implements OptiFine {

	@Override
	public Object preRenderBlock(final BufferBuilder bufferBuilder, final BlockRenderLayer blockRenderLayer, final BlockState blockState, final BlockPos pos, final RegionRenderCacheBuilder builders, final IEnviromentBlockReader blockAccess) {
		return null;
	}

	@Override
	public void postRenderBlock(final Object renderEnv, final ChunkRender chunkRender, final RegionRenderCacheBuilder builders, final CompiledChunk compiledChunk, final boolean[] usedBlockRenderLayers) {
	}

	@Override
	public boolean isChunkCacheOF(final Object obj) {
		return false;
	}

	@Override
	public ChunkRenderCache getChunkRenderCache(final Object chunkCacheOF) {
		return null;
	}

	@Override
	public void pushShaderEntity(final IFluidState state, final BlockPos pos, final IEnviromentBlockReader reader, final BufferBuilder buffer) {
	}

	@Override
	public void pushShaderEntity(final BlockState state, final BlockPos pos, final IEnviromentBlockReader reader, final BufferBuilder buffer) {
	}

	@Override
	public void popShaderEntity(final BufferBuilder buffer) {
	}

	@Override
	public Object getRenderEnvironment(final BufferBuilder bufferBuilder, final BlockState state, final BlockPos pos) {
		return null;
	}

	@Override
	public IBakedModel getRenderModel(final IBakedModel model, final BlockState state, final Object renderEnv) {
		return model;
	}

	@Override
	public List<BakedQuad> getRenderQuads(final List<BakedQuad> quads, final IEnviromentBlockReader reader, final BlockState state, final BlockPos pos, final Direction direction, final BlockRenderLayer blockRenderLayer, final long posRandLong, final Object renderEnv) {
		return quads;
	}

}
