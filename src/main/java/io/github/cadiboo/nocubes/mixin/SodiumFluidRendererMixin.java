package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * There is more to this mixin, see the documentation in {@link io.github.cadiboo.nocubes.hooks.MixinAsm}
 * and {@link io.github.cadiboo.nocubes.hooks.MixinAsm#transformSodiumLevelRenderer}.
 */
@Pseudo // Sodium may not be installed
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer")
public class SodiumFluidRendererMixin {

	@Redirect(
		method = "isFluidOccluded",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/BlockAndTintGetter;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"
		),
		require = 2 // Redirect both calls to the function
	)
	private FluidState nocubes_getFluidState(BlockAndTintGetter world, BlockPos adjPos) {
		return Hooks.getRenderFluidState(adjPos, world.getBlockState(adjPos));
	}

}
