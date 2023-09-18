package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.collision.SmoothShapes;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.function.BiFunction;

@Mixin(BlockCollisions.class)
public class BlockCollisionsMixin<T> {

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
	@Shadow
	@Final
	private BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> resultProvider;
	@Unique
	private Iterator<T> noCubes$noCubesCollisions;

	/**
	 * Computes and returns NoCubes custom collisions
	 */
	@Inject(
		method = "computeNext",
		at = @At(
			value = "HEAD",
			target = "Lnet/minecraft/world/level/BlockCollisions;computeNext()Lnet/minecraft/world/phys/shapes/VoxelShape;"
		),
		cancellable = true
	)
	private void nocubes_computeNext(CallbackInfoReturnable<T> cir) {
		if (!NoCubesConfig.Server.collisionsEnabled)
			return;
		if (noCubes$noCubesCollisions == null)
			noCubes$noCubesCollisions = SmoothShapes.createNoCubesIntersectingCollisionList(collisionGetter, box, pos, onlySuffocatingBlocks, resultProvider).iterator();
		if (noCubes$noCubesCollisions.hasNext())
			cir.setReturnValue(noCubes$noCubesCollisions.next());
	}

	/**
	 * Cancels vanilla collisions for smooth blocks (since their collisions have already been handled by {@link #nocubes_computeNext}).
	 */
	@Redirect(
		method = "computeNext",
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
