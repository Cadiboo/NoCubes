package io.github.cadiboo.nocubes.smoothable;

import com.google.common.collect.Sets;
import io.github.cadiboo.nocubes.hooks.INoCubesBlockState;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;

/**
 * The in-memory list of smoothables.
 * Shared between client and server in singleplayer.
 * Uses ASM-added fields with an identity set fallback.
 *
 * @author Cadiboo
 */
public interface SmoothableHandler {

	static SmoothableHandler create(BlockState test) {
		try {
			SmoothableHandler asm = new ASM();
			asm.isSmoothable(test);
			return asm;
		} catch (NoSuchFieldError | ClassCastException e) {
			LogManager.getLogger().warn("Failed to create optimised ASM based handler, falling back to Set implementation, performance may suffer slightly", e);
			return new Set();
		}
	}

	void addSmoothable(BlockState state);

	void removeSmoothable(BlockState state);

	boolean isSmoothable(BlockState state);

	void setSmoothable(boolean newValue, BlockState state);

	class ASM implements SmoothableHandler {

		@Override
		public void addSmoothable(BlockState state) {
			setSmoothable(true, state);
		}

		@Override
		public void removeSmoothable(BlockState state) {
			setSmoothable(false, state);
		}

		@Override
		public boolean isSmoothable(BlockState state) {
			return ((INoCubesBlockState) state).isTerrainSmoothable();
		}

		@Override
		public void setSmoothable(boolean newValue, BlockState state) {
			((INoCubesBlockState) state).setTerrainSmoothable(newValue);
		}
	}

	class Set implements SmoothableHandler {

		private final java.util.Set<BlockState> smoothables = Sets.newIdentityHashSet();

		@Override
		public void addSmoothable(BlockState state) {
			smoothables.add(state);
		}

		@Override
		public void removeSmoothable(BlockState state) {
			smoothables.remove(state);
		}

		@Override
		public boolean isSmoothable(BlockState state) {
			return smoothables.contains(state);
		}

		@Override
		public void setSmoothable(boolean newValue, BlockState state) {
			if (newValue)
				addSmoothable(state);
			else
				removeSmoothable(state);
		}

	}

}
