package io.github.cadiboo.nocubes.util;

import net.minecraft.block.BlockState;

import java.util.function.Predicate;

/**
 * Interface defining a function to determine if a {@link BlockState} is smoothable or not.
 *
 * @author Cadiboo
 */
@FunctionalInterface
public interface IsSmoothable extends Predicate<BlockState> {

	IsSmoothable TERRAIN_SMOOTHABLE = blockState -> blockState.nocubes_isTerrainSmoothable;

//	IsSmoothable LEAVES_SMOOTHABLE = blockState -> blockState.nocubes_isLeavesSmoothable;

	/**
	 * Tests if the blockState is smoothable.
	 *
	 * @param blockState the blockState to be tested
	 * @return If the blockState should be smoothed
	 */
	@Override
	boolean test(final BlockState blockState);

}
