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

	void addSmoothable(BlockStateBase state);

	void removeSmoothable(BlockStateBase state);

	boolean isSmoothable(BlockStateBase state);

	void setSmoothable(boolean newValue, BlockStateBase state);

	class ASM implements SmoothableHandler {

		@Override
		public void addSmoothable(BlockStateBase state) {
			setSmoothable(true, state);
		}

		@Override
		public void removeSmoothable(BlockStateBase state) {
			setSmoothable(false, state);
		}

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
		public void addSmoothable(BlockStateBase state) {
			smoothables.add(state);
		}

		@Override
		public void removeSmoothable(BlockStateBase state) {
			smoothables.remove(state);
		}

		@Override
		public boolean isSmoothable(BlockStateBase state) {
			return smoothables.contains(state);
		}

		@Override
		public void setSmoothable(boolean newValue, BlockStateBase state) {
			if (newValue)
				addSmoothable(state);
			else
				removeSmoothable(state);
		}

	}

}
