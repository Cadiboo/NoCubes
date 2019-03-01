package io.github.cadiboo.nocubes.hooks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Cadiboo
 */
public class Hooks {

	public static void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185908_6_) {
		LogManager.getLogger().info("fsdfuygihj");
	}

}
