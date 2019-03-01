package io.github.cadiboo.nocubes.hooks;

import net.minecraft.block.Block;
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
public class Hooks {

	public static void addCollisionBoxToList(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		block.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
	}

}
