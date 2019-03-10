package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.CollisionHandler;
import io.github.cadiboo.nocubes.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer.StateImplementation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Cadiboo
 */
@SuppressWarnings({
		"unused", // Hooks get invoked by ASM redirects
		"WeakerAccess" // Hooks need to be public to be invoked
})
public final class AddCollisionBoxToListHook {

	public static void addCollisionBoxToList(final Block block, final IBlockState state, final World worldIn, final BlockPos pos, final AxisAlignedBB entityBox, final List<AxisAlignedBB> collidingBoxes, final @Nullable Entity entityIn, final boolean isActualState) {
		if (ModConfig.enableCollisions) {
			CollisionHandler.addCollisionBoxToList(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
		} else {
			addCollisionBoxToListDefault(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
		}
	}

	public static void addCollisionBoxToListDefault(final IBlockState state, final World worldIn, final BlockPos pos, final AxisAlignedBB entityBox, final List<AxisAlignedBB> collidingBoxes, final Entity entityIn, final boolean isActualState) {
		runAddCollisionBoxToListDefaultOnce((StateImplementation) state);
		state.addCollisionBoxToList(worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
	}

	private static void runAddCollisionBoxToListDefaultOnce(final StateImplementation state) {
		// Filled with ASM
//		state.runAddCollisionBoxToListDefaultOnce = true;
		throw new UnsupportedOperationException("This method should have been filled by ASM!");
	}

}
