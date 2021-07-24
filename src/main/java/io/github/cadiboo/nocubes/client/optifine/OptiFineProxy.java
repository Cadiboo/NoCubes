package io.github.cadiboo.nocubes.client.optifine;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.cadiboo.nocubes.client.render.RendererDispatcher.ChunkRenderInfo;
import io.github.cadiboo.nocubes.client.render.RendererDispatcher.ChunkRenderInfo.ColorSupplier;
import io.github.cadiboo.nocubes.client.render.RendererDispatcher.ChunkRenderInfo.QuadConsumer;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;

public interface OptiFineProxy {

	boolean initialisedAndUsable();

	void preRenderChunk(RenderChunk chunkRender, BlockPos chunkPos, PoseStack matrix);

	long getSeed(long originalSeed);

	/** @return null or the RenderEnv */
	Object preRenderBlock(RenderChunk chunkRender, ChunkBufferBuilderPack buffers, BlockAndTintGetter chunkCache, RenderType layer, BufferBuilder buffer, BlockState state, BlockPos worldPos);

	/** @return null or the RenderEnv */
	Object preRenderFluid(RenderChunk chunkRender, ChunkBufferBuilderPack buffers, BlockAndTintGetter chunkCache, RenderType layer, BufferBuilder buffer, BlockState block, FluidState fluid, BlockPos worldPos);

	BakedModel getModel(Object renderEnv, BakedModel originalModel, BlockState state);

	void postRenderBlock(Object renderEnv, BufferBuilder buffer, RenderChunk chunkRender, ChunkBufferBuilderPack buffers, CompiledChunk compiledChunk);

	void postRenderFluid(Object renderEnv, BufferBuilder buffer, RenderChunk chunkRender, ChunkBufferBuilderPack buffers, CompiledChunk compiledChunk);

	@Nullable BakedQuad getQuadEmissive(BakedQuad quad);

	void preRenderQuad(Object renderEnv, BakedQuad emissiveQuad, BlockState state, BlockPos pos);

	void markRenderLayerUsed(CompiledChunk compiledChunk, RenderType renderType);

	List<BakedQuad> getQuadsAndStoreOverlays(List<BakedQuad> quads, BlockAndTintGetter world, BlockState state, BlockPos worldPos, Direction direction, RenderType layer, long rand, Object renderEnv);

	int forEachOverlayQuad(ChunkRenderInfo renderer, BlockState state, BlockPos worldPos, ColorSupplier colorSupplier, QuadConsumer action, Object renderEnv);
}
