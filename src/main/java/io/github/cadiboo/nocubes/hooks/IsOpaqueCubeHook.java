package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

/**
 * @author Cadiboo
 */
@SuppressWarnings({
		"unused", // Hooks get invoked by ASM redirects
		"weakerAccess" // Hooks need to be public to be invoked
})
public final class IsOpaqueCubeHook {

	public static boolean isOpaqueCube(final Block block, final IBlockState state) {
		if (ModConfig.overrideIsOpaqueCube) {
			if (ModUtil.TERRAIN_SMOOTHABLE.isSmoothable(state)) {
				return false;
			}
		}
		return isStateOpaqueCubeDefault(state);
	}

	public static boolean isStateOpaqueCubeDefault(final IBlockState state) {
		runIsOpaqueCubeDefaultOnce((BlockStateContainer.StateImplementation) state);
		return state.isOpaqueCube();
	}

	public static void runIsOpaqueCubeDefaultOnce(final BlockStateContainer.StateImplementation state) {
		// Filled with ASM
//		state.runIsOpaqueCubeDefaultOnce = true;
		throw new UnsupportedOperationException("This method should have been filled by ASM!");
	}

}
