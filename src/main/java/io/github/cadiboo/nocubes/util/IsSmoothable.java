package io.github.cadiboo.nocubes.util;

import net.minecraft.block.state.IBlockState;

import java.util.function.Predicate;

/**
 * Removes boxing cost of using generic functions with Boolean
 *
 * @author Cadiboo
 */
public interface IsSmoothable extends Predicate<IBlockState> {

	IsSmoothable TERRAIN_SMOOTHABLE = state -> ((INoCubesBlockState) state).nocubes_isTerrainSmoothable();

	IsSmoothable LEAVES_SMOOTHABLE = state -> ((INoCubesBlockState) state).nocubes_isLeavesSmoothable();

	/**
	 * @param state the state to be tested
	 * @return If the state should be smoothed
	 */
	@Override
	boolean test(final IBlockState state);

}
