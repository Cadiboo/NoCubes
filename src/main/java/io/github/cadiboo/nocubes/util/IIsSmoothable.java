package io.github.cadiboo.nocubes.util;

import net.minecraft.block.state.IBlockState;

/**
 * Removes boxing cost of passing in generic functions with Boolean
 *
 * @author Cadiboo
 */
public interface IIsSmoothable {

	IIsSmoothable TERRAIN_SMOOTHABLE = IBlockState::nocubes_isTerrainSmoothable;

	IIsSmoothable LEAVES_SMOOTHABLE = IBlockState::nocubes_isLeavesSmoothable;

	/**
	 * If the state should be smoothed
	 *
	 * @param state the state
	 * @return If the state should be smoothed
	 */
	boolean isSmoothable(final IBlockState state);

}
