package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapeSpliterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BiPredicate;

@Mixin(VoxelShapeSpliterator.class)
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
		method = "collisionCheck",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/function/BiPredicate;test(Ljava/lang/Object;Ljava/lang/Object;)Z"
		)
	)
	public <T, U> boolean nocubes_isSuffocating(BiPredicate<T, U> instance, T state, U pos) {
		if (Hooks.collisionsEnabledFor((BlockState) state))
			return false;
		return instance.test(state, pos);
	}

}
