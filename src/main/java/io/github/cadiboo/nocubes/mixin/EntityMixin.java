package io.github.cadiboo.nocubes.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {

	/**
	 * Make the suffocation check provide the player to the collision getter (it doesn't otherwise).
	 * This makes collisions work properly even when {@link io.github.cadiboo.nocubes.config.NoCubesConfig.Server#tempMobCollisionsDisabled} is false.
	 * This is disabled when Apoli is installed, because Apoli includes the same fix https://github.com/apace100/apoli/blob/a417c4a2d5b1cfd3a972319c641e0b83443ab708/src/main/java/io/github/apace100/apoli/mixin/EntityMixin.java#L153-L156.
	 * Lithium also seems to mess with this function, probably I need to add an override like https://github.com/apace100/apoli/pull/80/files.
	 */
	@Redirect(
		method = {
			"lambda$isInWall$8",  // Forge < 47.2
			"lambda$isInWall$11", // Forge > 47.2
		},
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
		)
	)
	private VoxelShape noCubes$isInWall$getCollisionShape$entityAware(BlockState state, BlockGetter world, BlockPos pos) {
		return state.getCollisionShape(world, pos, CollisionContext.of((Entity)(Object)this));
	}

}
