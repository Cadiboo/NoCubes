package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.profiler.Profiler;

/**
 * @author Cadiboo
 */
public class ModProfiler extends Profiler implements AutoCloseable {

	private int sections = 0;

	public ModProfiler() {
		if (NoCubes.profilingEnabled) {
			this.startProfiling(0);
		}
	}

	public ModProfiler start(final String name) {
		if (!this.isProfiling()) {
			return this;
		}
		++sections;
		startSection(name);
		// return this to allow use in try-with-resources blocks
		return this;
	}

	public void end() {
		if (!this.isProfiling()) {
			return;
		}
		--sections;
		if (sections < 0) {
			sections = 0;
			this.stopProfiling();
			return;
		}
		endSection();
	}

	@Override
	public void close() {
		end();
	}

}
