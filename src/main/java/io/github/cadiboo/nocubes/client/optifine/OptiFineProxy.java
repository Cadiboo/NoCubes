package io.github.cadiboo.nocubes.client.optifine;

import io.github.cadiboo.nocubes.client.render.RenderDispatcher.ChunkRenderInfo.ColorSupplier;
import io.github.cadiboo.nocubes.client.render.RenderDispatcher.ChunkRenderInfo.QuadConsumer;
import io.github.cadiboo.nocubes.client.render.struct.PoseStack;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;
import java.util.List;

import static io.github.cadiboo.nocubes.client.render.RenderDispatcher.ChunkRenderInfo;

public interface OptiFineProxy {

	/**
	 * @return Null if this proxy is usable, otherwise a description of what went wrong
	 */
	@Nullable String notUsableBecause();

	void preRenderChunk(RenderChunk chunkRender, BlockPos chunkPos, PoseStack matrix);

	long getSeed(long originalSeed);

	/** @return null or the RenderEnv */
	Object preRenderBlock(RenderChunk chunkRender, RegionRenderCacheBuilder buffers, IBlockAccess chunkCache, BlockRenderLayer layer, BufferBuilder buffer, IBlockState state, BlockPos worldPos);

	void preRenderFluid(IBlockState state, BlockPos worldPos, IBlockAccess world, BufferBuilder buffer);

	IBakedModel getModel(Object renderEnv, IBakedModel originalModel, IBlockState state);

	void postRenderBlock(Object renderEnv, BufferBuilder buffer, RenderChunk chunkRender, RegionRenderCacheBuilder buffers, boolean[] usedLayers);

	void postRenderFluid(BufferBuilder buffer);

	@Nullable BakedQuad getQuadEmissive(BakedQuad quad);

	void preRenderQuad(Object renderEnv, BakedQuad emissiveQuad, IBlockState state, BlockPos pos);

	List<BakedQuad> getQuadsAndStoreOverlays(List<BakedQuad> quads, IBlockAccess world, IBlockState state, BlockPos worldPos, EnumFacing direction, BlockRenderLayer layer, long rand, Object renderEnv);

	int forEachOverlayQuad(ChunkRenderInfo renderer, IBlockState state, BlockPos worldPos, ColorSupplier colorSupplier, QuadConsumer action, Object renderEnv);

}
