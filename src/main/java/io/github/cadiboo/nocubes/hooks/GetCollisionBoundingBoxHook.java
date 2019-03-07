package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * @author Cadiboo
 */
@SuppressWarnings({
		"unused", // Hooks get invoked by ASM redirects
		"weakerAccess" // Hooks need to be public to be invoked
})
//TODO: do I need this?
public final class GetCollisionBoundingBoxHook {

	public static AxisAlignedBB getCollisionBoundingBox(final Block block, final IBlockState state, final IBlockAccess worldIn, final BlockPos pos) {
		if (!ModConfig.collisionsEnabled) {
			return getCollisionBoundingBoxDefault(state, worldIn, pos);
		}

		final AxisAlignedBB box = getCollisionBoundingBoxDefault(state, worldIn, pos);
		return box == null || box.maxY == 0 ? null : box;
	}

	public static AxisAlignedBB getCollisionBoundingBoxDefault(final IBlockState state, final IBlockAccess worldIn, final BlockPos pos) {
		runGetCollisionBoundingBoxDefaultOnce((BlockStateContainer.StateImplementation) state);
		return state.getCollisionBoundingBox(worldIn, pos);
	}

	public static void runGetCollisionBoundingBoxDefaultOnce(final BlockStateContainer.StateImplementation state) {
		// Filled with ASM
//		state.runGetCollisionBoundingBoxHookDefaultOnce = true;
		throw new UnsupportedOperationException("This method should have been filled by ASM!");
	}

}
