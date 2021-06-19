package io.github.cadiboo.nocubes.client.optifine;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.cadiboo.nocubes.client.render.RendererDispatcher;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import javax.annotation.Nullable;
import java.util.List;

import static io.github.cadiboo.nocubes.client.optifine.HD_U_G8.Reflect.CompiledChunk_hasBlocks;
import static net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;

class Dummy implements OptiFineProxy {

	@Override
	public boolean initialisedAndUsable() {
		return true;
	}

	@Override
	public void preRenderChunk(ChunkRender chunkRender, BlockPos chunkPos, MatrixStack matrix) {
	}

	@Override
	public long getSeed(long originalSeed) {
		return originalSeed;
	}

	@Override
	public Object preRenderBlock(ChunkRender chunkRender, RegionRenderCacheBuilder builder, IBlockDisplayReader chunkCacheOF, RenderType renderType, BufferBuilder buffer, BlockState state, BlockPos pos) {
		return null;
	}

	@Override
	public Object preRenderFluid(ChunkRender chunkRender, RegionRenderCacheBuilder buffers, IBlockDisplayReader chunkCache, RenderType layer, BufferBuilder buffer, BlockState block, FluidState fluid, BlockPos worldPos) {
		return null;
	}

	@Override
	public IBakedModel getModel(Object renderEnv, IBakedModel originalModel, BlockState state) {
		return originalModel;
	}

	@Override
	public void postRenderBlock(Object renderEnv, BufferBuilder buffer, ChunkRender chunkRender, RegionRenderCacheBuilder builder, CompiledChunk compiledChunk) {
	}

	@Override
	public void postRenderFluid(Object renderEnv, BufferBuilder buffer, ChunkRender chunkRender, RegionRenderCacheBuilder builder, CompiledChunk compiledChunk) {

	}

	@Override
	public @Nullable BakedQuad getQuadEmissive(BakedQuad quad) {
		return null;
	}

	@Override
	public void preRenderQuad(Object renderEnv, BakedQuad emissiveQuad, BlockState state, BlockPos pos) {
	}

	@Override
	public void markRenderLayerUsed(CompiledChunk compiledChunk, RenderType renderType) {
		CompiledChunk_hasBlocks(compiledChunk).add(renderType);
	}

    @Override
	public List<BakedQuad> getQuadsAndStoreOverlays(List<BakedQuad> quads, IBlockDisplayReader world, BlockState state, BlockPos worldPos, Direction direction, RenderType layer, long rand, Object renderEnv) {
        return quads;
    }

	@Override
	public int forEachOverlayQuad(RendererDispatcher.ChunkRenderInfo renderer, BlockState state, BlockPos worldPos, RendererDispatcher.ChunkRenderInfo.ColorSupplier colorSupplier, RendererDispatcher.ChunkRenderInfo.QuadConsumer action, Object renderEnv) {
		return 0;
	}

}
