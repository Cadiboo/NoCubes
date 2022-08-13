package io.github.cadiboo.nocubes.client.optifine;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.cadiboo.nocubes.client.render.RendererDispatcher;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nullable;
import java.util.List;

class Dummy implements OptiFineProxy {

	@Override
	public @Nullable String notUsableBecause() {
		return null;
	}

	@Override
	public void preRenderChunk(RenderChunk chunkRender, BlockPos chunkPos, PoseStack matrix) {
	}

	@Override
	public long getSeed(long originalSeed) {
		return originalSeed;
	}

	@Override
	public Object preRenderBlock(RenderChunk chunkRender, ChunkBufferBuilderPack builder, BlockAndTintGetter chunkCacheOF, RenderType renderType, BufferBuilder buffer, BlockState state, BlockPos pos) {
		return null;
	}

	@Override
	public Object preRenderFluid(RenderChunk chunkRender, ChunkBufferBuilderPack buffers, BlockAndTintGetter chunkCache, RenderType layer, BufferBuilder buffer, BlockState block, FluidState fluid, BlockPos worldPos) {
		return null;
	}

	@Override
	public BakedModel getModel(Object renderEnv, BakedModel originalModel, BlockState state) {
		return originalModel;
	}

	@Override
	public void postRenderBlock(Object renderEnv, BufferBuilder buffer, RenderChunk chunkRender, ChunkBufferBuilderPack builder, CompileResults compileResults) {
	}

	@Override
	public void postRenderFluid(Object renderEnv, BufferBuilder buffer, RenderChunk chunkRender, ChunkBufferBuilderPack builder, CompileResults compileResults) {

	}

	@Override
	public @Nullable BakedQuad getQuadEmissive(BakedQuad quad) {
		return null;
	}

	@Override
	public void preRenderQuad(Object renderEnv, BakedQuad emissiveQuad, BlockState state, BlockPos pos) {
	}

    @Override
	public List<BakedQuad> getQuadsAndStoreOverlays(List<BakedQuad> quads, BlockAndTintGetter world, BlockState state, BlockPos worldPos, Direction direction, RenderType layer, long rand, Object renderEnv) {
        return quads;
    }

	@Override
	public int forEachOverlayQuad(RendererDispatcher.ChunkRenderInfo renderer, BlockState state, BlockPos worldPos, RendererDispatcher.ChunkRenderInfo.ColorSupplier colorSupplier, RendererDispatcher.ChunkRenderInfo.QuadConsumer action, Object renderEnv) {
		return 0;
	}

}
