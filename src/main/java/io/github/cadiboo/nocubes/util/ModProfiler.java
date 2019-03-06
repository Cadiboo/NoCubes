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
		--sections;
		if (!profilingEnabled || sections < 0) {
			if (sections < 0) {
				sections = 0;
			}
			return;
		}
		endSection();
	}

	@Override
	public void close() {
		end();
	}

}
