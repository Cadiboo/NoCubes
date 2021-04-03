package io.github.cadiboo.nocubes.smoothable;

import net.minecraft.block.BlockState;
import org.apache.logging.log4j.LogManager;

import java.util.Collections;
import java.util.IdentityHashMap;

/**
 * The in-memory list of smoothables.
 * Shared between client & server in singleplayer.
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
		} catch (Exception e) {
			LogManager.getLogger().error("Failed to create optimised ASM based handler, falling back to Set implementation", e);
			return new Set();
		}
	}

	void addSmoothable(BlockState state);

	void removeSmoothable(BlockState state);

	boolean isSmoothable(BlockState state);

	class ASM implements SmoothableHandler {

		@Override
		public void addSmoothable(final BlockState state) {
			state.nocubes_isTerrainSmoothable = true;
		}

		@Override
		public void removeSmoothable(final BlockState state) {
			state.nocubes_isTerrainSmoothable = false;
		}

		@Override
		public boolean isSmoothable(final BlockState state) {
			return state.nocubes_isTerrainSmoothable;
		}

	}

	class Set implements SmoothableHandler {

		private final java.util.Set<BlockState> smoothables = Collections.newSetFromMap(new IdentityHashMap<>());

		@Override
		public void addSmoothable(final BlockState state) {
			smoothables.add(state);
		}

		@Override
		public void removeSmoothable(final BlockState state) {
			smoothables.remove(state);
		}

		@Override
		public boolean isSmoothable(final BlockState state) {
			return smoothables.contains(state);
		}

	}

}
