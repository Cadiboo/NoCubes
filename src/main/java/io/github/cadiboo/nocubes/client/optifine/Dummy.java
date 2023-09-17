package io.github.cadiboo.nocubes.client.optifine;

import io.github.cadiboo.nocubes.client.render.RenderDispatcher.ChunkRenderInfo;
import io.github.cadiboo.nocubes.client.render.struct.PoseStack;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static io.github.cadiboo.nocubes.client.render.RenderDispatcher.ChunkRenderInfo.ColorSupplier;
import static io.github.cadiboo.nocubes.client.render.RenderDispatcher.ChunkRenderInfo.QuadConsumer;

public class Dummy implements OptiFineProxy {

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
	public Object preRenderBlock(RenderChunk chunkRender, RegionRenderCacheBuilder buffers, IBlockAccess chunkCacheOF, BlockRenderLayer renderType, BufferBuilder buffer, IBlockState state, BlockPos pos) {
		return null;
	}

	@Override
	public void preRenderFluid(IBlockState state, BlockPos worldPos, IBlockAccess world, BufferBuilder buffer) {
	}

	@Override
	public IBakedModel getModel(Object renderEnv, IBakedModel originalModel, IBlockState state) {
		return originalModel;
	}

	@Override
	public void postRenderBlock(Object renderEnv, BufferBuilder buffer, RenderChunk chunkRender, CompiledChunk compiledChunk, RegionRenderCacheBuilder buffers, boolean[] usedLayers) {
	}

	@Override
	public void postRenderFluid(BufferBuilder buffer) {
	}

//	@Override
//	public void postRenderFluid(Object renderEnv, BufferBuilder buffer, RenderChunk chunkRender, RegionRenderCacheBuilder buffers, Set<RenderType> usedLayers) {
//	}

	@Override
	public @Nullable BakedQuad getQuadEmissive(BakedQuad quad) {
		return null;
	}

	@Override
	public void preRenderQuad(Object renderEnv, BakedQuad emissiveQuad, IBlockState state, BlockPos pos) {
	}

	@Override
	public List<BakedQuad> getQuadsAndStoreOverlays(List<BakedQuad> quads, IBlockAccess world, IBlockState state, BlockPos worldPos, EnumFacing direction, BlockRenderLayer layer, long rand, Object renderEnv) {
		return quads;
	}

	@Override
	public int forEachOverlayQuad(ChunkRenderInfo renderer, IBlockState state, BlockPos worldPos, ColorSupplier colorSupplier, QuadConsumer action, Object renderEnv) {
		return 0;
	}

}
