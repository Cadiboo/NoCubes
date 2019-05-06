package io.github.cadiboo.nocubes.util;

import net.minecraft.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**
 * @author Cadiboo
 */
public class ModProfiler extends Profiler implements AutoCloseable {

	public static final ArrayList<ModProfiler> PROFILERS = new ArrayList<>();
	private static final Logger LOGGER = LogManager.getLogger();

	private static final ThreadLocal<ModProfiler> PROFILER = ThreadLocal.withInitial(() -> {
		final ModProfiler profiler = new ModProfiler();
		PROFILERS.add(profiler);
		return profiler;
	});

	public static boolean profilersEnabled = false;
	private int sections = 0;

	public ModProfiler() {
		if (profilersEnabled) {
			this.startProfiling(0);
		}
	}

	public static void enableProfiling() {
		profilersEnabled = true;
		synchronized (PROFILERS) {
			for (final ModProfiler profiler : PROFILERS) {
				profiler.startProfiling(0);
			}
		}
	}

	public static void disableProfiling() {
		profilersEnabled = false;
		synchronized (PROFILERS) {
			for (final ModProfiler profiler : PROFILERS) {
				profiler.stopProfiling();
			}
		}
	}

	public static ModProfiler get() {
		return PROFILER.get();
	}

	public ModProfiler start(final String name) {
		if (!profilersEnabled) {
			return this;
		}
		++sections;
		startSection(name);
		// return this to allow use in try-with-resources blocks
		return this;
	}

	public void end() {
		if (!profilersEnabled) {
			return;
		}
		if (shouldEndSection()) {
			endSection();
		}
	}

	/**
	 * Stop crashes when profilers are enabled and disabled quickly (doesn't quite work always)
	 */
	private boolean shouldEndSection() {
		--sections;
		if (sections < 0) {
			sections = 0;
			this.stopProfiling();
			return false;
		}
		return true;
	}

	@Override
	public void close() {
		end();
	}

}
