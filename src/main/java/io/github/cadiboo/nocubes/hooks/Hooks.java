package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;

/**
 * Contains logic that gets used by traits/mixins in the {@link io.github.cadiboo.nocubes.mixin} package.
 */
@SuppressWarnings("unused") // Called via ASM
public final class Hooks {

	public static boolean renderingEnabledFor(BlockStateBase state) {
		return NoCubesConfig.Client.render && NoCubes.smoothableHandler.isSmoothable(state);
	}

	public static boolean collisionsEnabledFor(BlockStateBase state) {
		return NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state);
	}

	/**
	 * Hooking this makes {@link Block#shouldRenderFace} return true and causes cubic terrain (including fluids) to be
	 * rendered when they are up against smooth terrain, stopping us from being able to see through the ground near
	 * smooth terrain.
	 */
	public static boolean shouldCancelOcclusion(BlockStateBase state) {
		return renderingEnabledFor(state);
	}

	/**
	 * Helper function for use by other hooks/mixins.
	 */
	public static VoxelShape getSmoothCollisionShapeFor(Entity entity, BlockState state, BlockGetter world, BlockPos pos) {
		assert collisionsEnabledFor(state);
		return CollisionHandler.getCollisionShape(state, world, pos, CollisionContext.of(entity));
	}

	/**
	 * Helper function for use by other hooks/mixins.
	 */
	public static boolean collisionShapeOfSmoothBlockIntersectsEntityAABB(Entity entity, BlockState state, BlockGetter level, BlockPos pos) {
		assert collisionsEnabledFor(state);
		return Shapes.joinIsNotEmpty(
			getSmoothCollisionShapeFor(entity, state, level, pos).move(pos.getX(), pos.getY(), pos.getZ()),
			Shapes.create(entity.getBoundingBox()),
			BooleanOp.AND
		);
	}


}
