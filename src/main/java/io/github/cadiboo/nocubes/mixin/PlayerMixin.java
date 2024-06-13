package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerMixin {

	/**
	 * Make the suffocation check provide the player to the collision getter (it doesn't otherwise).
	 * This makes collisions work properly even when {@link io.github.cadiboo.nocubes.config.NoCubesConfig.Server#tempMobCollisionsDisabled} is false.
	 */
	@Redirect(
		method = "freeAt",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;isSuffocating(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"
		)
	)
	private boolean noCubes$freeAt$isSuffocating(BlockState state, BlockGetter level, BlockPos pos) {
		if (!Hooks.collisionsEnabledFor(state))
			return state.isSuffocating(level, pos); // Original behaviour
		return !state.isSuffocating(level, pos) || !Hooks.shapeOfSmoothBlockIntersectsEntityAABB((Entity) (Object) this, state, level, pos);
	}
}
