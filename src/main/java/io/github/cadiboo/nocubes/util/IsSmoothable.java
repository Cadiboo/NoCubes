package io.github.cadiboo.nocubes.util;

import net.minecraft.block.BlockState;

/**
 * Removes boxing cost of using generic functions with Boolean
 *
 * @author Cadiboo
 */
public interface IsSmoothable {

	IsSmoothable TERRAIN_SMOOTHABLE = blockState -> blockState.nocubes_isTerrainSmoothable;

//	IsSmoothable LEAVES_SMOOTHABLE = blockState -> blockState.nocubes_isLeavesSmoothable;

	/**
	 * @param state the state to be tested
	 * @return If the state should be smoothed
	 */
	boolean apply(final BlockState state);

}
