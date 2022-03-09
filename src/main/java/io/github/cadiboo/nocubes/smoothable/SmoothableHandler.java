package io.github.cadiboo.nocubes.smoothable;

import io.github.cadiboo.nocubes.hooks.INoCubesBlockState;
import net.minecraft.block.AbstractBlock.AbstractBlockState;

/**
 * The in-memory list of smoothables.
 * Shared between client and server in singleplayer.
 * Uses ASM-added fields with an identity set fallback.
 *
 * @author Cadiboo
 */
public interface SmoothableHandler {

	boolean isSmoothable(AbstractBlockState state);

	void setSmoothable(boolean newValue, AbstractBlockState state);

	default void setSmoothable(boolean newValue, AbstractBlockState[] states) {
		for (var state : states)
			setSmoothable(newValue, state);
	}

	static SmoothableHandler create() {
		return new SmoothableHandler() {
			@Override
			public boolean isSmoothable(AbstractBlockState state) {
				return ((INoCubesBlockState) state).isTerrainSmoothable();
			}

			@Override
			public void setSmoothable(boolean newValue, AbstractBlockState state) {
				((INoCubesBlockState) state).setTerrainSmoothable(newValue);
			}
		};
	}


}
