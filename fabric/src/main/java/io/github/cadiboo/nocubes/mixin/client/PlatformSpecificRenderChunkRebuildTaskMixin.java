package io.github.cadiboo.nocubes.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.cadiboo.nocubes.client.render.VanillaRenderer;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRender;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRenderBuilder;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Iterator;
import java.util.Set;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public abstract class PlatformSpecificRenderChunkRebuildTaskMixin implements INoCubesChunkSectionRenderBuilder {

	@Shadow(aliases = {
		"this$0",
		"this$1",
		"f_112859_", // Forge
		"field_20839", // Fabric
	})
	@Final
	ChunkRenderDispatcher.RenderChunk parentClass;

	@Override
	public Object noCubes$getModelData(BlockPos worldPos) {
		return null;
	}

	@ModifyExpressionValue(
		method = "compile",
		at = @At(
			value = "INVOKE",
			target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;"
		)
	)
	public Iterator<BlockPos> noCubes$renderChunk(
		Iterator<BlockPos> iterator,
		float x, float y, float z,
		ChunkBufferBuilderPack buffers,
		@Local(ordinal = 0) BlockPos chunkPos,
		@Local(ordinal = 0) RenderChunkRegion region,
		@Local(ordinal = 0) PoseStack matrix,
		@Local(ordinal = 0) Set<RenderType> usedLayers,
		@Local(ordinal = 0) RandomSource random,
		@Local(ordinal = 0) BlockRenderDispatcher dispatcher
	) {
		VanillaRenderer.renderChunk(
			this, (INoCubesChunkSectionRender) parentClass, buffers,
			chunkPos, region, matrix,
			usedLayers, random, dispatcher,
			(state, worldPos, modelData, layer, buffer, optiFineRenderEnv) -> dispatcher.renderBatched(
				state, worldPos, region, matrix, buffer,
				false, random
			)
		);
		return iterator;
	}
}
