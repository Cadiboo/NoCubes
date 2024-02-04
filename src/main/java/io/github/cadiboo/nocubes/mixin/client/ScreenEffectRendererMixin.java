package io.github.cadiboo.nocubes.mixin.client;

import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

	/**
	 * Makes the suffocation overlay for smoothed blocks properly conform to their new shape.
	 */
	@Redirect(
		method = "getOverlayBlock",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;isViewBlocking(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"
		)
	)
	private static boolean noCubes$isViewBlocking(BlockState state, BlockGetter level, BlockPos pos) {
		var blocking = state.isViewBlocking(level, pos);
		if (blocking && Hooks.renderingEnabledFor(state)) {
			var player = Objects.requireNonNull(Minecraft.getInstance().player, "Rendering overlay for a null player!?");
			return Hooks.collisionShapeOfSmoothBlockIntersectsEntityAABB(player, state, level, pos);
		}
		return blocking;
	}

}
