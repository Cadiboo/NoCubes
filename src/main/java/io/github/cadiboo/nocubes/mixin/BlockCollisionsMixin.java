package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;

@Mixin(BlockCollisions.class)
public class BlockCollisionsMixin {

	@Shadow
	@Final
	private CollisionGetter collisionGetter;
	@Shadow
	@Final
	private AABB box;
	@Shadow
	@Final
	private BlockPos.MutableBlockPos pos;
	@Shadow
	@Final
	private boolean onlySuffocatingBlocks;
	private Iterator<VoxelShape> noCubesCollisions;

	/**
	 * Computes and returns NoCubes custom collisions
	 */
	@Inject(
		method = "computeNext()Lnet/minecraft/world/phys/shapes/VoxelShape;",
		at = @At(
			value = "HEAD",
			target = "Lnet/minecraft/world/level/BlockCollisions;computeNext()Lnet/minecraft/world/phys/shapes/VoxelShape;"
		),
		cancellable = true
	)
	private void nocubes_computeNext(CallbackInfoReturnable<VoxelShape> cir) {
		if (!NoCubesConfig.Server.collisionsEnabled)
			return;
		if (noCubesCollisions == null)
			noCubesCollisions = CollisionHandler.createNoCubesIntersectingCollisionList(collisionGetter, box, pos, onlySuffocatingBlocks).iterator();
		if (noCubesCollisions.hasNext())
			cir.setReturnValue(noCubesCollisions.next());
	}

	/**
	 * Cancels vanilla collisions for smooth blocks (since their collisions have already been handled by {@link #nocubes_computeNext}).
	 */
	@Redirect(
		method = "computeNext()Lnet/minecraft/world/phys/shapes/VoxelShape;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
		)
	)
	private BlockState nocubes_cancelVanillaCollisionsForSmoothBlocks(BlockGetter world, BlockPos pos) {
		var state = world.getBlockState(pos);
		if (Hooks.collisionsEnabledFor(state))
			return Blocks.AIR.defaultBlockState();
		return state;
	}

}
