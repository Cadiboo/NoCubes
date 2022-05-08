package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.hooks.Hooks;
import io.github.cadiboo.nocubes.hooks.INoCubesBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;

@Mixin(BlockStateBase.class)
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
		if (Hooks.shouldCancelOcclusion((BlockStateBase) (Object) this))
			ci.setReturnValue(false);
	}

//	/**
//	 * Very similar logic to {@link Hooks#shouldCancelOcclusion}.
//	 * Fixes some lighting issues with smooth blocks and fixes blocks from the
//	 * <a href="https://www.curseforge.com/minecraft/mc-mods/framedblocks">FramedBlocks mod</a>
//	 * not rendering their sides when up against smooth blocks.
//	 */
//	@Inject(
//		method = "isSolidRender",
//		at = @At("HEAD"),
//		cancellable = true
//	)
//	public void nocubes_isSolidRender(CallbackInfoReturnable<Boolean> ci) {
//		if (Hooks.renderingEnabledFor((BlockStateBase) (Object) this))
//			ci.setReturnValue(false);
//	}

	/**
	 * Makes the 3rd person camera not be super zoomed-in when half inside a smoothed block.
	 */
	@Inject(
		method = "getVisualShape",
		at = @At("HEAD"),
		cancellable = true
	)
	public void nocubes_getVisualShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
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
		method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
		at = @At("HEAD"),
		cancellable = true
	)
	public void nocubes_getCollisionShape(BlockGetter level, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
		nocubes_collisionShapeHelper(asState(), level, pos, CollisionContext.empty(), cir);
	}

	/**
	 * Makes collisions work.
	 */
	@Inject(
		method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
		at = @At("HEAD"),
		cancellable = true
	)
	public void nocubes_getCollisionShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		nocubes_collisionShapeHelper(asState(), level, pos, context, cir);
	}

	@Unique
	private static void nocubes_collisionShapeHelper(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		if (Hooks.collisionsEnabledFor(state))
			cir.setReturnValue(CollisionHandler.getCollisionShape(state, level, pos, context));
	}

	/**
	 * Makes collisions work for normally solid blocks like stone (by overriding the cached value).
	 * Also stops us falling through 1 block wide holes and under the ground.
	 * How? I don't really know.
	 * Returning true from 'hasLargeCollisionShape' makes smooth blocks be included in the blocks checked by
	 * {@link BlockCollisions#computeNext} when they are touching a boundary of the area (normally they wouldn't be).
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
