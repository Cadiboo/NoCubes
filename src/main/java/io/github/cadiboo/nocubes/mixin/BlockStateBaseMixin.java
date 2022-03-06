package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.hooks.Hooks;
import io.github.cadiboo.nocubes.hooks.INoCubesBlockState;
import net.minecraft.core.BlockPos;
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
		cancellable = true,
		require = 1,
		allow = 1
	)
	public void canOcclude(CallbackInfoReturnable<Boolean> ci) {
		if (Hooks.shouldCancelOcclusion((BlockStateBase) (Object) this))
			ci.setReturnValue(false);
	}

	// region Collisions

	/**
	 * Makes collisions work for BlockStates with a cache.
	 */
	@Inject(
		method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
		at = @At("HEAD"),
		cancellable = true,
		require = 1,
		allow = 1
	)
	public void getCollisionShape(BlockGetter level, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
		collisionShapeHelper(asState(), level, pos, CollisionContext.empty(), cir);
	}

	/**
	 * Makes collisions work.
	 */
	@Inject(
		method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
		at = @At("HEAD"),
		cancellable = true,
		require = 1,
		allow = 1
	)
	public void getCollisionShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		collisionShapeHelper(asState(), level, pos, context, cir);
	}

	@Unique
	private static void collisionShapeHelper(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		if (collisionsEnabledFor(state))
			cir.setReturnValue(CollisionHandler.getCollisionShape(state, level, pos, context));
	}

	@Unique
	private static boolean collisionsEnabledFor(BlockState state) {
		return NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state);
	}

	/**
	 * Makes collisions work for normally solid blocks like stone.
	 * <p>
	 * TODO: This is used by {@link Block#getShadeBrightness(BlockState, BlockGetter, BlockPos)} so always returning false breaks AO when collisions are on.
	 * Possible fix: Check if we are on the server or the client thread before running the check?
	 */
	@Inject(
		method = "isCollisionShapeFullBlock",
		at = @At("HEAD"),
		cancellable = true,
		require = 1,
		allow = 1
	)
	public void isCollisionShapeFullBlock(BlockGetter reader, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (collisionsEnabledFor(asState()))
			cir.setReturnValue(false);
	}

	/**
	 * Somehow stops us falling through 1 block wide holes and under the ground.
	 */
	@Inject(
		method = "hasLargeCollisionShape",
		at = @At("HEAD"),
		cancellable = true,
		require = 1,
		allow = 1
	)
	public void hasLargeCollisionShape(CallbackInfoReturnable<Boolean> cir) {
		if (collisionsEnabledFor(asState()))
			cir.setReturnValue(true);
	}

	/**
	 * Stops grass path collisions being broken.
	 */
	@Inject(
		method = "isSuffocating",
		at = @At("HEAD"),
		cancellable = true,
		require = 1,
		allow = 1
	)
	public void isSuffocating(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (collisionsEnabledFor(asState()))
			cir.setReturnValue(false);
	}
	// endregion
}
