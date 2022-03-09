package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapeSpliterator;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public class BlockCollisionsMixin {

	/**
	 * Stops grass path collisions being broken.
	 * How? I don't really know:
	 * It's got something to do with {@link PlayerEntity#moveTowardsClosestSpace} only checking suffocating blocks.
	 * Returning false from 'isSuffocating' stops smooth blocks being included in the blocks checked by
	 * {@link VoxelShapeSpliterator#collisionCheck} when called by the Player method.
	 *
	 * @implNote We don't change the base {@link BlockState#isSuffocating(IBlockReader, BlockPos)} method because it's used
	 * in {@link PlayerEntity#freeAt} and {@link Entity#isInWall} which we don't want to mess up.
	 */
	@Redirect(
		method = "lambda$suffocatesAt$0(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/block/BlockState;isSuffocating(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z"
		)
	)
	public boolean nocubes_isSuffocating(BlockState state, IBlockReader level, BlockPos pos) {
		if (Hooks.collisionsEnabledFor(state))
			return false;
		return state.isSuffocating(level, pos);
	}

}
