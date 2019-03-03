package io.github.cadiboo.nocubes.tempcompatibility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

//import repose.block.SlopingBlockExtensions;

/**
 * @author Cadiboo
 */
public class ReposeCompatibility {

	public static void addCollisionBoxToList(final IBlockState state, final World worldIn, final AxisAlignedBB entityBox, final List<AxisAlignedBB> collidingBoxes, final Entity entityIn, final boolean isActualState) {
//		SlopingBlockExtensions.addCollisionBoxToList(state, worldIn, entityBox, collidingBoxes, entityIn, isActualState);
	}

	@Nullable
	public static AxisAlignedBB getCollisionBoundingBox(final IBlockState state, final IBlockAccess worldIn, final BlockPos pos) {
//		return SlopingBlockExtensions.getCollisionBoundingBox(state, worldIn, pos);
		return null;
	}

}
