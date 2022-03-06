package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockCollisions.class)
public class BlockCollisionsMixin {

	/**
	 * Stops grass path collisions being broken.
	 * How? I don't really know:
	 * It's got something to do with {@link Player#moveTowardsClosestSpace} only checking suffocating blocks.
	 * Returning false from 'isSuffocating' stops smooth blocks being included in the blocks checked by
	 * {@link BlockCollisions#computeNext()} when called by the Player method.
	 *
	 * @implNote We don't change the base {@link BlockState#isSuffocating(BlockGetter, BlockPos)} method because it's used
	 * in {@link Player#freeAt} and {@link net.minecraft.world.entity.Entity#isInWall} which we don't want to mess up.
	 */
	@Redirect(
		method = "computeNext()Lnet/minecraft/world/phys/shapes/VoxelShape;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;isSuffocating(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"
		),
		require = 1,
		allow = 1
	)
	public boolean isSuffocating(BlockState state, BlockGetter blockGetter, BlockPos blockPos) {
		if (Hooks.collisionsEnabledFor(state))
			return false;
		return state.isSuffocating(blockGetter, blockPos);
	}

}
