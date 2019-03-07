package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.profiler.Profiler;

/**
 * @author Cadiboo
 */
public class ModProfiler extends Profiler implements AutoCloseable {

	private int sections = 0;

	public ModProfiler() {
		this.profilingEnabled = NoCubes.profilingEnabled;
	}

	public ModProfiler start(final String name) {
		if (!this.profilingEnabled) {
			return this;
		}
		++sections;
		startSection(name);
		// return this to allow use in try-with-resources blocks
		return this;
	}

	public void end() {
		if (!profilingEnabled) {
			return;
		}
		--sections;
		if (sections < 0) {
			sections = 0;
			this.clearProfiling();
			return;
		}
		endSection();
	}

	@Override
	public void close() {
		end();
	}

}
