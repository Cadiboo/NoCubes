package io.github.cadiboo.nocubes.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.cadiboo.nocubes.client.render.VanillaRenderer;
import io.github.cadiboo.nocubes.hooks.ClientHooks;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRender;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRenderBuilder;
import io.github.cadiboo.nocubes.mixin.Constants;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.Set;

/**
 * Changes chunk rendering to allow us to do our own custom rendering.
 */
@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public abstract class RenderChunkRebuildTaskMixin extends RenderChunkCompileTaskMixin implements INoCubesChunkSectionRenderBuilder {

	@Shadow(aliases = {
		"this$0",
		"this$1",
		"f_112859_", // Forge
		"field_20839", // Fabric
	})
	@Final
	ChunkRenderDispatcher.RenderChunk parentClass;

	@Override
	public ModelData noCubes$getModelData(BlockPos worldPos) {
		return shadow$getModelData(worldPos);
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
			usedLayers, random, dispatcher
		);
		return iterator;
	}

	/**
	 * Allows us to disable vanilla rendering for blocks we will render
	 * ourselves in {@link io.github.cadiboo.nocubes.client.render.MeshRenderer}
	 */
	@Redirect(
		method = "compile",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;"
		)
	)
	public RenderShape noCubes$getRenderShape(BlockState state) {
		return ClientHooks.getRenderShape(state);
	}

	/**
	 * See documentation on {@link Constants#EXTENDED_FLUIDS_BLOCK_POS_REF_NAME}
	 */
	@ModifyReceiver(
		method = "compile",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
		)
	)
	private RenderChunkRegion storeBlockPos(
		RenderChunkRegion renderChunkRegion,
		BlockPos pos, @Share(Constants.EXTENDED_FLUIDS_BLOCK_POS_REF_NAME) LocalRef<BlockPos> posRef
	) {
		posRef.set(pos);
		return renderChunkRegion;
	}

	/**
	 * See documentation on {@link Constants#EXTENDED_FLUIDS_BLOCK_POS_REF_NAME}
	 */
	@Redirect(
		method = "compile",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;"
		)
	)
	private FluidState getRenderFluidState(
		BlockState instance,
		@Share(Constants.EXTENDED_FLUIDS_BLOCK_POS_REF_NAME) LocalRef<BlockPos> pos
	) {
		return ClientHooks.getRenderFluidState(pos.get(), instance);
	}
}
