package io.github.cadiboo.nocubes.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.cadiboo.nocubes.hooks.ClientHooks;
import io.github.cadiboo.nocubes.mixin.Constants;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Changes fluid rendering to support extended fluid rendering
 * Redirects all instances of {@link BlockState#getFluidState()} to use our {@link io.github.cadiboo.nocubes.hooks.ClientHooks#getRenderFluidState}
 */
@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {
	/**
	 * See documentation on {@link Constants#EXTENDED_FLUIDS_BLOCK_POS_REF_NAME}
	 */
	@ModifyReceiver(
		method = {
			"tesselate",
			"getHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;)F",
			"getHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)F"
		},
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/BlockAndTintGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
		)
	)
	private BlockAndTintGetter storeBlockPos(
		BlockAndTintGetter blockAndTintGetter,
		BlockPos pos, @Share(Constants.EXTENDED_FLUIDS_BLOCK_POS_REF_NAME) LocalRef<BlockPos> posRef
	) {
		posRef.set(pos);
		return blockAndTintGetter;
	}

	/**
	 * See documentation on {@link Constants#EXTENDED_FLUIDS_BLOCK_POS_REF_NAME}
	 */
	@Redirect(
		method = {
			"tesselate",
			"getHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;)F",
			"getHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)F"
		},
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
