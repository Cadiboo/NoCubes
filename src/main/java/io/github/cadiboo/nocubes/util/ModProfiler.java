package io.github.cadiboo.nocubes.util;

import net.minecraft.profiler.Profiler;

/**
 * @author Cadiboo
 */
public class ModProfiler extends Profiler implements AutoCloseable {

	public void putSection(final String sectionName, final long estimatedTimeTakenNanoseconds) {
//		NoCubes.NO_CUBES_LOG.debug(sectionName + " took approximately " + estimatedTimeTakenNanoseconds + " nanoseconds");
	}

	public ModProfiler start(final String name) {
		startSection(name);
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
