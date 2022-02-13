package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;

@Mixin(BlockState.class)
public abstract class BlockStateMixin {

	/**
	 * Documentation on {@link Hooks#shouldCancelOcclusion}
	 */
	@Inject(
		method = "isCacheOpaqueCube()Z",
		at = @At("HEAD"),
		cancellable = true,
		require = -1, // Don't fail if OptiFine isn't installed
		remap = false // OptiFine added method
	)
	public void isCacheOpaqueCube(CallbackInfoReturnable<Boolean> ci) {
		if (Hooks.shouldCancelOcclusion((BlockStateBase) (Object) this))
			ci.setReturnValue(false);
	}
}
