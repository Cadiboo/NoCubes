package io.github.cadiboo.nocubes.tempcompatibility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
//import repose.block.SlopingBlockExtensions;

import java.util.List;

/**
 * @author Cadiboo
 */
public class ReposeCompatibility {

	public static void addCollisionBoxToList(final IBlockState state, final World worldIn, final AxisAlignedBB entityBox, final List<AxisAlignedBB> collidingBoxes, final Entity entityIn, final boolean isActualState) {
//		SlopingBlockExtensions.addCollisionBoxToList(state, worldIn, entityBox, collidingBoxes, entityIn, isActualState);
	}

}
