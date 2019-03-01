package io.github.cadiboo.nocubes.util;

import net.minecraft.profiler.Profiler;

public class ModProfiler extends Profiler {

	public void putSection(final String sectionName, final long estimatedTimeTakenNanoseconds) {
//		NoCubes.NO_CUBES_LOG.debug(sectionName + " took approximately " + estimatedTimeTakenNanoseconds + " nanoseconds");
	}

	public void start(final String name) {
		startSection(name);
	}

	public void end() {
		endSection();
	}

}
