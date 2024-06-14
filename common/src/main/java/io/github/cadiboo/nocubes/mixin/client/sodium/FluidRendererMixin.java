package io.github.cadiboo.nocubes.mixin.client.sodium;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.cadiboo.nocubes.hooks.ClientHooks;
import io.github.cadiboo.nocubes.mixin.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Sodium version of {@link io.github.cadiboo.nocubes.mixin.client.LiquidBlockRendererMixin}
 */
@Pseudo // Sodium may not be installed
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer")
public class FluidRendererMixin {

	@Redirect(
		method = {
			"isFluidOccluded",
			"fluidHeight",
		},
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/BlockAndTintGetter;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"
		),
		require = 2 // Redirect both calls to the function
	)
	private FluidState noCubes$getFluidState(BlockAndTintGetter world, BlockPos adjPos) {
		return ClientHooks.getRenderFluidState(adjPos, world.getBlockState(adjPos));
	}

	/**
	 * See documentation on {@link Constants#EXTENDED_FLUIDS_BLOCK_POS_REF_NAME}
	 */
	@ModifyReceiver(
		method = {
			"render",
			"fluidHeight",
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
			"fluidHeight",
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
