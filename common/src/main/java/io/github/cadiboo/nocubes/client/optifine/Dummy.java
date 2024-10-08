package io.github.cadiboo.nocubes.client.optifine;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.cadiboo.nocubes.client.render.VanillaRenderer;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRender;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRenderBuilder;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

class Dummy implements OptiFineProxy {

	@Override
	public @Nullable String notUsableBecause() {
		return null;
	}

	@Override
	public void preRenderChunk(INoCubesChunkSectionRender chunkRender, BlockPos chunkPos, PoseStack matrix) {
	}

	@Override
	public long getSeed(long originalSeed) {
		return originalSeed;
	}

	@Override
	public Object preRenderBlock(INoCubesChunkSectionRender chunkRender, ChunkBufferBuilderPack builder, BlockAndTintGetter chunkCacheOF, RenderType renderType, BufferBuilder buffer, BlockState state, BlockPos pos) {
		return null;
	}

	@Override
	public Object preRenderFluid(INoCubesChunkSectionRender chunkRender, ChunkBufferBuilderPack buffers, BlockAndTintGetter chunkCache, RenderType layer, BufferBuilder buffer, BlockState block, FluidState fluid, BlockPos worldPos) {
		return null;
	}

	@Override
	public BakedModel getModel(Object renderEnv, BakedModel originalModel, BlockState state) {
		return originalModel;
	}

	@Override
	public void postRenderBlock(Object renderEnv, BufferBuilder buffer, INoCubesChunkSectionRender chunkRender, ChunkBufferBuilderPack builder, Set<RenderType> usedLayers) {
	}

	@Override
	public void postRenderFluid(Object renderEnv, BufferBuilder buffer, INoCubesChunkSectionRender chunkRender, ChunkBufferBuilderPack builder, Set<RenderType> usedLayers) {
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
	public int forEachOverlayQuad(
		INoCubesChunkSectionRenderBuilder rebuildTask, INoCubesChunkSectionRender chunkRender,
		ChunkBufferBuilderPack buffers, BlockPos chunkPos,
		BlockAndTintGetter world, PoseStack matrix,
		Set<RenderType> usedLayers, RandomSource random, BlockRenderDispatcher dispatcher,
		BlockState state, BlockPos worldPos,
		VanillaRenderer.ColorSupplier colorSupplier, VanillaRenderer.QuadConsumer action,
		Object renderEnv
	) {
		return 0;
	}

}
