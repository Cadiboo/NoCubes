package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.hooks.Hooks;
import io.github.cadiboo.nocubes.hooks.INoCubesBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;

@Mixin(BlockStateBase.class)
public abstract class BlockStateBaseMixin implements INoCubesBlockState {

	public boolean nocubes_isSmoothable;

	@Override
	public void setTerrainSmoothable(boolean value) {
		nocubes_isSmoothable = value;
	}

	@Override
	public boolean isTerrainSmoothable() {
		return nocubes_isSmoothable;
	}

	/**
	 * Documentation on {@link Hooks#shouldCancelOcclusion}
	 */
	@Inject(
		method = "canOcclude()Z",
		at = @At("HEAD"),
		cancellable = true,
		require = 1,
		allow = 1
	)
	public void canOcclude(CallbackInfoReturnable<Boolean> ci) {
		if (Hooks.shouldCancelOcclusion((BlockStateBase) (Object) this))
			ci.setReturnValue(false);
	}
}
