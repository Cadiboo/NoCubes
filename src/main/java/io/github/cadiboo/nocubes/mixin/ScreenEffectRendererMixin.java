package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OverlayRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(OverlayRenderer.class)
public class ScreenEffectRendererMixin {

	/**
	 * Makes the suffocation overlay for smoothed blocks properly conform to their new shape.
	 */
	@Redirect(
		method = "getOverlayBlock",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/block/BlockState;isViewBlocking(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z"
		)
	)
	private static boolean nocubes_isViewBlocking(BlockState state, IBlockReader level, BlockPos pos) {
		boolean blocking = state.isViewBlocking(level, pos);
		if (blocking && Hooks.renderingEnabledFor(state)) {
			PlayerEntity player = Objects.requireNonNull(Minecraft.getInstance().player, "Rendering overlay for a null player!?");
			return VoxelShapes.joinIsNotEmpty(
				CollisionHandler.getCollisionShape(state, level, pos, ISelectionContext.of(player)).move(pos.getX(), pos.getY(), pos.getZ()),
				VoxelShapes.create(player.getBoundingBox()),
				IBooleanFunction.AND
			);
		}
		return blocking;
	}

}
