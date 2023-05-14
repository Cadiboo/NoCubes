package io.github.cadiboo.nocubes.smoothable;

import io.github.cadiboo.nocubes.hooks.INoCubesBlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;

/**
 * The in-memory list of smoothables.
 * Shared between client and server in singleplayer.
 * Uses the {@link io.github.cadiboo.nocubes.mixin.BlockStateBaseMixin#nocubes_isSmoothable} field which is added via ASM at runtime.
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
				return ((INoCubesBlockState) state).isSmoothable();
			}

			@Override
			public void setSmoothable(boolean newValue, BlockStateBase state) {
				((INoCubesBlockState) state).setSmoothable(newValue);
			}
		};
	}


}
