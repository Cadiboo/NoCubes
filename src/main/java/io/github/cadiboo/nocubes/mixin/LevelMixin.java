package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class LevelMixin {

	/**
	 * Allows extended fluids to work
	 */
	@Inject(
		method = "getFluidState",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/World;getChunkAt(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/chunk/Chunk;"
		),
		cancellable = true
	)
	public void nocubes_getExtendedFluidState(BlockPos pos, CallbackInfoReturnable<FluidState> ci) {
		if (NoCubesConfig.Server.extendFluidsRange > 0)
			ci.setReturnValue(ModUtil.getExtendedFluidState((World) (Object) this, pos));
	}
}
