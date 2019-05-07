package io.github.cadiboo.nocubes.util;

import net.minecraft.block.state.IBlockState;

/**
 * Removes boxing cost of using generic functions with Boolean
 *
 * @author Cadiboo
 */
public interface IsSmoothable {

	IsSmoothable TERRAIN_SMOOTHABLE = IBlockState::nocubes_isTerrainSmoothable;

	IsSmoothable LEAVES_SMOOTHABLE = IBlockState::nocubes_isLeavesSmoothable;

	/**
	 * @param state the state to be tested
	 * @return If the state should be smoothed
	 */
	boolean apply(final IBlockState state);

}
