package io.github.cadiboo.nocubes.client.optifine;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import javax.annotation.Nullable;

import static net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;

public interface OptiFineProxy {

	boolean initialisedAndUsable();

	void preRenderChunk(ChunkRender chunkRender, BlockPos chunkPos, MatrixStack matrix);

	long getSeed(long originalSeed);

	/** @return null or the RenderEnv */
	Object preRenderBlock(ChunkRender chunkRender, RegionRenderCacheBuilder buffers, IBlockDisplayReader chunkCache, RenderType layer, BufferBuilder buffer, BlockState state, BlockPos worldPos);

	/** @return null or the RenderEnv */
	Object preRenderFluid(ChunkRender chunkRender, RegionRenderCacheBuilder buffers, IBlockDisplayReader chunkCache, RenderType layer, BufferBuilder buffer, BlockState block, FluidState fluid, BlockPos worldPos);

	IBakedModel getModel(Object renderEnv, IBakedModel originalModel, BlockState state);

	void postRenderBlock(Object renderEnv, BufferBuilder buffer, ChunkRender chunkRender, RegionRenderCacheBuilder builder, CompiledChunk compiledChunk);

	void postRenderFluid(Object renderEnv, BufferBuilder buffer, ChunkRender chunkRender, RegionRenderCacheBuilder builder, CompiledChunk compiledChunk);

	@Nullable
	BakedQuad getQuadEmissive(BakedQuad quad);

	void preRenderQuad(Object renderEnv, BakedQuad emissiveQuad, BlockState state, BlockPos pos);

	void markRenderLayerUsed(CompiledChunk compiledChunk, RenderType renderType);

}
