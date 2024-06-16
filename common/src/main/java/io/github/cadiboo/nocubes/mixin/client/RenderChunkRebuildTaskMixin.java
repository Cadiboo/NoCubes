package io.github.cadiboo.nocubes.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.cadiboo.nocubes.hooks.ClientHooks;
import io.github.cadiboo.nocubes.mixin.Constants;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Changes chunk rendering to allow us to do our own custom rendering.
 */
@Mixin(targets = "net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderChunk$RebuildTask")
public abstract class RenderChunkRebuildTaskMixin {

	/**
	 * @see ClientHooks#getRenderShape
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
