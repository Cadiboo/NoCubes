package io.github.cadiboo.nocubes.client.optifine;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.cadiboo.nocubes.client.render.VanillaRenderer;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRender;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRenderBuilder;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
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

public interface OptiFineProxy {

	/**
	 * @return Null if this proxy is usable, otherwise a description of what went wrong
	 */
	@Nullable String notUsableBecause();

	void preRenderChunk(INoCubesChunkSectionRender chunkRender, BlockPos chunkPos, PoseStack matrix);

	long getSeed(long originalSeed);

	/** @return null or the RenderEnv */
	Object preRenderBlock(INoCubesChunkSectionRender chunkRender, SectionBufferBuilderPack buffers, BlockAndTintGetter chunkCache, RenderType layer, BufferBuilder buffer, BlockState state, BlockPos worldPos);

	/** @return null or the RenderEnv */
	Object preRenderFluid(INoCubesChunkSectionRender chunkRender, SectionBufferBuilderPack buffers, BlockAndTintGetter chunkCache, RenderType layer, BufferBuilder buffer, BlockState block, FluidState fluid, BlockPos worldPos);

	BakedModel getModel(Object renderEnv, BakedModel originalModel, BlockState state);

	void postRenderBlock(Object renderEnv, BufferBuilder buffer, INoCubesChunkSectionRender chunkRender, SectionBufferBuilderPack buffers, Set<RenderType> usedLayers);

	void postRenderFluid(Object renderEnv, BufferBuilder buffer, INoCubesChunkSectionRender chunkRender, SectionBufferBuilderPack buffers, Set<RenderType> usedLayers);

	@Nullable BakedQuad getQuadEmissive(BakedQuad quad);

	void preRenderQuad(Object renderEnv, BakedQuad emissiveQuad, BlockState state, BlockPos pos);

	List<BakedQuad> getQuadsAndStoreOverlays(List<BakedQuad> quads, BlockAndTintGetter world, BlockState state, BlockPos worldPos, Direction direction, RenderType layer, long rand, Object renderEnv);

	int forEachOverlayQuad(
		INoCubesChunkSectionRenderBuilder rebuildTask, INoCubesChunkSectionRender chunkRender,
		SectionBufferBuilderPack buffers, BlockPos chunkPos,
		BlockAndTintGetter world, PoseStack matrix,
		Set<RenderType> usedLayers, RandomSource random, BlockRenderDispatcher dispatcher,
		BlockState state, BlockPos worldPos,
		VanillaRenderer.ColorSupplier colorSupplier, VanillaRenderer.QuadConsumer action,
		Object renderEnv
	);
}
