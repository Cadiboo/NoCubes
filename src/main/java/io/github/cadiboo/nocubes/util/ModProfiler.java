package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.profiler.Profiler;

/**
 * @author Cadiboo
 */
public class ModProfiler extends Profiler implements AutoCloseable {

	public ModProfiler() {
		this.profilingEnabled = NoCubes.profilingEnabled;
	}

	public void putSection(final String sectionName, final long estimatedTimeTakenNanoseconds) {
//		NoCubes.NO_CUBES_LOG.debug(sectionName + " took approximately " + estimatedTimeTakenNanoseconds + " nanoseconds");
	}

	public ModProfiler start(final String name) {
		startSection(name);
		// return this to allow use in try-with-resources blocks
		return this;
	}

	public void end() {
		endSection();
	}

	@Override
	public void close() {
		end();
	}

}
