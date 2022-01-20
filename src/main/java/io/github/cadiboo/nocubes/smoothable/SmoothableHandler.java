package io.github.cadiboo.nocubes.smoothable;

import com.google.common.collect.Sets;
import io.github.cadiboo.nocubes.hooks.INoCubesBlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import org.apache.logging.log4j.LogManager;

/**
 * The in-memory list of smoothables.
 * Shared between client and server in singleplayer.
 * Uses ASM-added fields with an identity set fallback.
 *
 * @author Cadiboo
 */
public interface SmoothableHandler {

	static SmoothableHandler create(BlockStateBase test) {
		try {
			var asm = new ASM();
			asm.isSmoothable(test);
			return asm;
		} catch (NoSuchFieldError | ClassCastException e) {
			LogManager.getLogger().warn("Failed to create optimised ASM based handler, falling back to Set implementation, performance may suffer slightly", e);
			return new Set();
		}
	}

	boolean isSmoothable(BlockStateBase state);

	void setSmoothable(boolean newValue, BlockStateBase state);

	default void setSmoothable(boolean newValue, BlockStateBase[] states) {
		for (var state : states)
			setSmoothable(newValue, state);
	}

	class ASM implements SmoothableHandler {

		@Override
		public boolean isSmoothable(BlockStateBase state) {
			return ((INoCubesBlockState) state).isTerrainSmoothable();
		}

		@Override
		public void setSmoothable(boolean newValue, BlockStateBase state) {
			((INoCubesBlockState) state).setTerrainSmoothable(newValue);
		}
	}

	class Set implements SmoothableHandler {

		private final java.util.Set<BlockStateBase> smoothables = Sets.newIdentityHashSet();

		@Override
		public boolean isSmoothable(BlockStateBase state) {
			return smoothables.contains(state);
		}

		@Override
		public void setSmoothable(boolean newValue, BlockStateBase state) {
			if (newValue)
				smoothables.add(state);
			else
				smoothables.remove(state);
		}

	}

}
