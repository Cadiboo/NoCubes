package io.github.cadiboo.nocubes.util;

import net.minecraft.block.state.IBlockState;

/**
 * Removes boxing cost of passing in generic functions with Boolean
 *
 * @author Cadiboo
 */
public interface IIsSmoothable {

	boolean isSmoothable(final IBlockState state);

}
