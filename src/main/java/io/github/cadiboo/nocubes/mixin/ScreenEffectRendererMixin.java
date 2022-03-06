package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
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
		),
		require = 1,
		allow = 1
	)
	private static boolean isViewBlocking(BlockState state, BlockGetter level, BlockPos pos) {
		var blocking = state.isViewBlocking(level, pos);
		if (blocking && Hooks.renderingEnabledFor(state)) {
			var player = Objects.requireNonNull(Minecraft.getInstance().player, "Rendering overlay for a null player!?");
			return Shapes.joinIsNotEmpty(
				CollisionHandler.getCollisionShape(state, level, pos, CollisionContext.of(player)).move(pos.getX(), pos.getY(), pos.getZ()),
				Shapes.create(player.getBoundingBox()),
				BooleanOp.AND
			);
		}
		return blocking;
	}

}
