package io.github.cadiboo.nocubes.smoothable;

import io.github.cadiboo.nocubes.hooks.INoCubesBlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;

/**
 * The in-memory list of smoothables.
 * Shared between client and server in singleplayer.
 * Uses ASM-added fields with an identity set fallback.
 *
 * @author Cadiboo
 */
public interface SmoothableHandler {

	boolean isSmoothable(BlockStateBase state);

	void setSmoothable(boolean newValue, BlockStateBase state);

	default void setSmoothable(boolean newValue, BlockStateBase[] states) {
		for (var state : states)
			setSmoothable(newValue, state);
	}

	static SmoothableHandler create() {
		return new SmoothableHandler() {
			@Override
			public boolean isSmoothable(BlockStateBase state) {
				return ((INoCubesBlockState) state).isTerrainSmoothable();
			}

			@Override
			public void setSmoothable(boolean newValue, BlockStateBase state) {
				((INoCubesBlockState) state).setTerrainSmoothable(newValue);
			}
		};
	}


}
