package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
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
		"weakerAccess" // Hooks need to be public to be invoked
})
public final class AddCollisionBoxToListHook {

	public static void addCollisionBoxToList(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		if (!ModConfig.collisionsEnabled) {
			addCollisionBoxToListDefault(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
		}

		HooksOld.addCollisionBoxToList(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
	}

	public static void addCollisionBoxToListDefault(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		runAddCollisionBoxToListDefaultOnce((BlockStateContainer.StateImplementation) state);
		state.addCollisionBoxToList(worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
	}

	public static void runAddCollisionBoxToListDefaultOnce(final BlockStateContainer.StateImplementation state) {
		// Filled with ASM
//		state.runAddCollisionBoxToListDefaultOnce = true;
		throw new UnsupportedOperationException("This method should have been filled by ASM!");
	}

}
