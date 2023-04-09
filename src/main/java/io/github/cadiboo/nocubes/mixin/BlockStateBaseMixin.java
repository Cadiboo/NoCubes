package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.hooks.Hooks;
import io.github.cadiboo.nocubes.hooks.INoCubesBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;

@Mixin(BlockStateBase.class)
public abstract class BlockStateBaseMixin implements INoCubesBlockState {

	@Shadow
	protected abstract BlockState asState();

	@Shadow
	public abstract Block getBlock();

	public boolean nocubes_isSmoothable;

	@Override
	public void setSmoothable(boolean value) {
		nocubes_isSmoothable = value;
	}

	@Override
	public boolean isSmoothable() {
		return nocubes_isSmoothable;
	}

	/**
	 * Documentation on {@link Hooks#shouldCancelOcclusion}
	 */
	@Inject(
		method = "canOcclude()Z",
		at = @At("HEAD"),
		cancellable = true
	)
	public void nocubes_canOcclude(CallbackInfoReturnable<Boolean> ci) {
		if (Hooks.shouldCancelOcclusion((BlockStateBase) (Object) this))
			ci.setReturnValue(false);
	}

	/**
	 * Makes the 3rd person camera not be super zoomed-in when half inside a smoothed block.
	 */
	@Inject(
		method = "getVisualShape",
		at = @At("HEAD"),
		cancellable = true
	)
	public void nocubes_getVisualShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		var state = asState();
		@SuppressWarnings("deprecation")
		var visualShape = getBlock().getVisualShape(state, level, pos, context);
		if (visualShape.isEmpty() || !Hooks.renderingEnabledFor(state))
			cir.setReturnValue(visualShape);
		else
			cir.setReturnValue(CollisionHandler.getCollisionShape(state, level, pos, context));
	}

}
