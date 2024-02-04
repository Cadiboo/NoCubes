package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {

	/**
	 * Make the suffocation check provide the player to the collision getter (it doesn't otherwise).
	 * This makes collisions work properly even when {@link io.github.cadiboo.nocubes.config.NoCubesConfig.Server#tempMobCollisionsDisabled} is false.
	 */
	@Redirect(
		method = "lambda$isInWall$8",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
		)
	)
	private VoxelShape noCubes$isCollidingWithWall(BlockState state, BlockGetter world, BlockPos pos) {
		if (!Hooks.collisionsEnabledFor(state))
			return state.getCollisionShape(world, pos);
		return Hooks.getSmoothCollisionShapeFor((Entity)(Object)this, state, world, pos);
	}

}
