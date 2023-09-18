package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.collision.SmoothShapes;
import io.github.cadiboo.nocubes.hooks.Hooks;
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

	@Redirect(
		method = "moveTowardsClosestSpace",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;isCollisionShapeFullBlock(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"
		)
	)
	private boolean nocubes_isCollisionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
		if (Hooks.collisionsEnabledFor(state))
			return false;
		return state.isCollisionShapeFullBlock(world, pos);
	}

	@Redirect(
		method = "lambda$isInWall$8",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
		)
	)
	private VoxelShape nocubes_isCollidingWithWall(BlockState state, BlockGetter world, BlockPos pos) {
		if (Hooks.collisionsEnabledFor(state))
			return SmoothShapes.shapeForSmoothBlock(state, world, pos, CollisionContext.empty());
		return state.getCollisionShape(world, pos);
	}

}
