package io.github.cadiboo.nocubes.client.optifine;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import javax.annotation.Nullable;

import static net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;

class Dummy implements OptiFineProxy {

	public void preRenderChunk(BlockPos blockpos) {
	}

	public long getSeed(long originalSeed) {
		return originalSeed;
	}

	public void preRenderBlock(ChunkRender chunkRender, RegionRenderCacheBuilder builder, IBlockDisplayReader chunkCacheOF, RenderType renderType, BufferBuilder buffer, BlockState state, BlockPos.Mutable pos) {
	}

	public IBakedModel getModel(IBakedModel originalModel, BlockState state) {
		return originalModel;
	}

	public void postRenderBlock(BufferBuilder buffer, ChunkRender chunkRender, RegionRenderCacheBuilder builder, CompiledChunk compiledChunk) {
	}

	@Nullable
	@Override
	public BakedQuad getQuadEmissive(BakedQuad quad) {
		return null;
	}

	@Override
	public void preRenderQuad(BakedQuad emissiveQuad, BlockState state, BlockPos pos) {
	}
}
