package io.github.cadiboo.nocubes.util;

import java.util.HashMap;

/**
 * @author Cadiboo
 */
public final class ModProfiler extends Profiler1122 implements AutoCloseable {

	public static final HashMap<Thread, ModProfiler> PROFILERS = new HashMap<>();

	private static final ThreadLocal<ModProfiler> PROFILER = ThreadLocal.withInitial(() -> {
		final ModProfiler profiler = new ModProfiler();
		PROFILERS.put(Thread.currentThread(), profiler);
		return profiler;
	});

	private int virtualSections = 0;
	private int startedSections = 0;

	public synchronized static void enableProfiling() {
		profilingEnabled = true;
	}

	public synchronized static void disableProfiling() {
		profilingEnabled = false;
	}

	public static ModProfiler get() {
		return PROFILER.get();
	}

	public ModProfiler start(final String name) {
		if (startedSections == virtualSections++ && profilingEnabled) {
			++startedSections;
			startSection(name);
		}
		return this; // return this to allow use in try-with-resources blocks
	}

	public void end() {
		if (startedSections == virtualSections--) {
			--startedSections;
			endSection();
		}
	}

	@Override
	public void close() {
		end();
	}

}
