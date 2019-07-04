package io.github.cadiboo.nocubes.util;

import java.util.HashMap;

/**
 * @author Cadiboo
 */
public final class ModProfiler /*extends Profiler*/ implements AutoCloseable {

	public static final HashMap<Thread, ModProfiler> PROFILERS = new HashMap<>();

	private static final ThreadLocal<ModProfiler> PROFILER = ThreadLocal.withInitial(() -> {
		final ModProfiler profiler = new ModProfiler();
		PROFILERS.put(Thread.currentThread(), profiler);
		return profiler;
	});

	public static boolean profilersEnabled = false;

	private int virtualSections = 0;
	private int startedSections = 0;

	public ModProfiler() {
		if (profilersEnabled) {
//			this.startProfiling(0);
		}
	}

	public static void enableProfiling() {
		profilersEnabled = true;
		synchronized (PROFILERS) {
			for (final ModProfiler profiler : PROFILERS.values()) {
//				profiler.startProfiling(0);
			}
		}
	}

	public static void disableProfiling() {
		profilersEnabled = false;
		synchronized (PROFILERS) {
			for (final ModProfiler profiler : PROFILERS.values()) {
//				profiler.stopProfiling();
			}
		}
	}

	public static ModProfiler get() {
		return PROFILER.get();
	}

	public ModProfiler start(final String name) {
		if (startedSections == virtualSections++ && profilersEnabled) {
			++startedSections;
//			startSection(name);
		}
		return this; // return this to allow use in try-with-resources blocks
	}

	public void end() {
		if (startedSections == virtualSections--) {
			--startedSections;
//			endSection();
		}
	}

	@Override
	public void close() {
		end();
	}

}
