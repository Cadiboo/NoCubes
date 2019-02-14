package io.github.cadiboo.nocubes.util;

import net.minecraft.block.BlockState;

/**
 * Removes boxing cost of passing in generic functions with Boolean
 *
 * @author Cadiboo
 */
public interface IIsSmoothable {

	boolean isSmoothable(final BlockState state);

}
