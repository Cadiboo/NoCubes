package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.hooks.Hooks;
import io.github.cadiboo.nocubes.hooks.INoCubesBlockState;
import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeSpliterator;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlockState.class)
public abstract class BlockStateBaseMixin implements INoCubesBlockState {

	@Shadow
	protected abstract BlockState asState();

	@Shadow
	public abstract Block getBlock();

	public boolean nocubes_isSmoothable;

	@Override
	public void setTerrainSmoothable(boolean value) {
		nocubes_isSmoothable = value;
	}

	@Override
	public boolean isTerrainSmoothable() {
		return nocubes_isSmoothable;
	}

	/**
	 * Documentation on {@link Hooks#shouldCancelOcclusion}
	 */
	@Inject(
		method = "canOcclude()Z",
		at = @At("HEAD"),
		cancellable = true
	)
	public void nocubes_canOcclude(CallbackInfoReturnable<Boolean> ci) {
		if (Hooks.shouldCancelOcclusion((AbstractBlockState) (Object) this))
			ci.setReturnValue(false);
	}

	/**
	 * Makes the 3rd person camera not be super zoomed-in when half inside a smoothed block.
	 */
	@Inject(
		method = "getVisualShape",
		at = @At("HEAD"),
		cancellable = true
	)
	public void nocubes_getVisualShape(IBlockReader level, BlockPos pos, ISelectionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		var state = asState();
		@SuppressWarnings("deprecation")
		var visualShape = getBlock().getVisualShape(state, level, pos, context);
		if (visualShape.isEmpty() || !Hooks.renderingEnabledFor(state))
			cir.setReturnValue(visualShape);
		else
			cir.setReturnValue(state.getCollisionShape(level, pos, context));
	}

	// region Collisions

	/**
	 * Makes collisions work for BlockStates with a cache.
	 * BlockStates with a cache would usually return their cached value, we need to change this (because commonly
	 * smoothed blocks like stone and dirt have a cache).
	 */
	@Inject(
		method = "getCollisionShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/shapes/VoxelShape;",
		at = @At("HEAD"),
		cancellable = true
	)
	public void nocubes_getCollisionShape(IBlockReader level, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
		nocubes_collisionShapeHelper(asState(), level, pos, ISelectionContext.empty(), cir);
	}

	/**
	 * Makes collisions work.
	 */
	@Inject(
		method = "getCollisionShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/shapes/ISelectionContext;)Lnet/minecraft/util/math/shapes/VoxelShape;",
		at = @At("HEAD"),
		cancellable = true
	)
	public void nocubes_getCollisionShape(IBlockReader level, BlockPos pos, ISelectionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		nocubes_collisionShapeHelper(asState(), level, pos, context, cir);
	}

	@Unique
	private static void nocubes_collisionShapeHelper(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		if (Hooks.collisionsEnabledFor(state))
			cir.setReturnValue(CollisionHandler.getCollisionShape(state, level, pos, context));
	}

	/**
	 * Makes collisions work for normally solid blocks like stone (by overriding the cached value).
	 * Also stops us falling through 1 block wide holes and under the ground.
	 * How? I don't really know.
	 * Returning true from 'hasLargeCollisionShape' makes smooth blocks be included in the blocks checked by
	 * {@link VoxelShapeSpliterator#collisionCheck} when they are touching a boundary of the area (normally they wouldn't be).
	 *
	 * @implNote This could be moved to {@link BlockCollisionsMixin} (the only place it's called from as of 1.18.2) to avoid
	 * the {@link CallbackInfoReturnable} allocation at the expense of compatibility with future call sites (e.g. in mods).
	 */
	@Inject(
		method = "hasLargeCollisionShape",
		at = @At("HEAD"),
		cancellable = true
	)
	public void nocubes_hasLargeCollisionShape(CallbackInfoReturnable<Boolean> cir) {
		if (Hooks.collisionsEnabledFor(asState()))
			cir.setReturnValue(true);
	}

	// endregion
}
