package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.atomic.AtomicInteger;

public final class RollingProfiler {

	private final AtomicInteger index = new AtomicInteger();
	private final long[] records;

	public RollingProfiler(int size) {
		this.records = new long[size];
	}

	/**
	 * Records a timing if we are in debug mode.
	 *
	 * @param startNanos When the operation started
	 * @return true if a rollover happened
	 */
	public boolean recordElapsedNanos(long startNanos) {
		if (!NoCubesConfig.Client.debugEnabled)
			return false;
		long elapsed = System.nanoTime() - startNanos;
		int i = index.incrementAndGet() % records.length;
		records[i] = elapsed;
		return i == 0;
	}

	public double average() {
		double sum = 0;
		long[] records = this.records;
		int length = records.length;
		for (int i = 0; i < length; ++i)
			sum += records[i];
		return sum / length;
	}

	public int size() {
		return records.length;
	}

	public void recordAndLogElapsedNanosChunk(long start, String description) {
		if (recordElapsedNanos(start))
			LogManager.getLogger("Render chunk " + description).debug("Average {}ms over the past {} chunks", average() / 1000_000F, size());
	}
}
