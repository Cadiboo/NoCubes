package io.github.cadiboo.nocubes.smoothable;

import io.github.cadiboo.nocubes.util.INoCubesBlockState;
import net.minecraft.block.state.IBlockState;

/**
 * The in-memory list of smoothables.
 * Shared between client and server in singleplayer.
 *
 * @author Cadiboo
 */
public interface SmoothableHandler {

	boolean isSmoothable(IBlockState state);

	void setSmoothable(boolean newValue, IBlockState state);

	default void setSmoothable(boolean newValue, IBlockState[] states) {
		for (IBlockState state : states)
			setSmoothable(newValue, state);
	}

	static SmoothableHandler create() {
		return new SmoothableHandler() {
			@Override
			public boolean isSmoothable(IBlockState state) {
				return ((INoCubesBlockState) state).nocubes_isSmoothable();
			}

			@Override
			public void setSmoothable(boolean newValue, IBlockState state) {
				((INoCubesBlockState) state).nocubes_setSmoothable(newValue);
			}
		};
	}


}
