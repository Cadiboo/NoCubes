package io.github.cadiboo.nocubes.util;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Copy of 1.12.2's Profiler
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Profiler1122 {

	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Flag profiling enabled
	 */
	protected static volatile boolean profilingEnabled;
	/**
	 * List of parent sections
	 */
	private final List<String> sectionList = Lists.newArrayList();
	/**
	 * List of timestamps (System.nanoTime)
	 */
	private final LongList timestampList = new LongArrayList();
	/**
	 * Profiling map
	 */
	private final Object2LongMap<String> profilingMap = new Object2LongArrayMap<>();
	/**
	 * Current profiling section
	 */
	private String profilingSection = "";

	public synchronized static boolean isProfilingEnabled() {
		return profilingEnabled;
	}
	/**
	 * Clear profiling.
	 */
	public void clearProfiling() {
		this.profilingMap.clear();
		this.profilingSection = "";
		this.sectionList.clear();
	}

	/**
	 * Start section
	 */
	public void startSection(String name) {
		if (profilingEnabled) {
			if (!this.profilingSection.isEmpty()) {
				this.profilingSection = this.profilingSection + ".";
			}

			this.profilingSection = this.profilingSection + name;
			this.sectionList.add(this.profilingSection);
			this.timestampList.add(System.nanoTime());
		}
	}

	public void startSection(Supplier<String> nameSupplier) {
		if (profilingEnabled) {
			this.startSection(nameSupplier.get());
		}
	}

	/**
	 * End section
	 */
	public void endSection() {
		if (profilingEnabled) {
			long i = System.nanoTime();
			long j = this.timestampList.removeLong(this.timestampList.size() - 1);
			this.sectionList.remove(this.sectionList.size() - 1);
			long k = i - j;

			if (this.profilingMap.containsKey(this.profilingSection)) {
				this.profilingMap.put(this.profilingSection, this.profilingMap.getLong(this.profilingSection) + k);
			} else {
				this.profilingMap.put(this.profilingSection, k);
			}

			if (k > 100000000L) {
				LOGGER.warn("Something's taking too long! '{}' took aprox {} ms", this.profilingSection, (double) k / 1000000.0D);
			}

			this.profilingSection = this.sectionList.isEmpty() ? "" : this.sectionList.get(this.sectionList.size() - 1);
		}
	}

	/**
	 * Gets the current profiling data. WARNING: If profiling is enabled, this must not return an empty list, as
	 * otherwise the game will crash when attempting to render the profiler. I.E. don't stub out the profiler code, OK?
	 * It's not necessary.
	 */
	public List<Result> getProfilingData(String profilerName) {
		if (!profilingEnabled) {
			return Collections.emptyList();
		} else {
			final Map<String, Long> profilingMap = this.profilingMap;
			long i = profilingMap.getOrDefault("root", 0L);
			long j = profilingMap.getOrDefault(profilerName, -1L);
			List<Result> list = Lists.newArrayList();

			if (!profilerName.isEmpty()) {
				profilerName = profilerName + ".";
			}

			long k = 0L;

			for (String s : profilingMap.keySet()) {
				if (s.length() > profilerName.length() && s.startsWith(profilerName) && s.indexOf(".", profilerName.length() + 1) < 0) {
					k += profilingMap.get(s);
				}
			}

			float f = (float) k;

			if (k < j) {
				k = j;
			}

			if (i < k) {
				i = k;
			}

			for (String s1 : profilingMap.keySet()) {
				if (s1.length() > profilerName.length() && s1.startsWith(profilerName) && s1.indexOf(".", profilerName.length() + 1) < 0) {
					long l = profilingMap.get(s1);
					double d0 = (double) l * 100.0D / (double) k;
					double d1 = (double) l * 100.0D / (double) i;
					String s2 = s1.substring(profilerName.length());
					list.add(new Result(s2, d0, d1));
				}
			}

			for (String s3 : profilingMap.keySet()) {
				profilingMap.put(s3, profilingMap.get(s3) * 999L / 1000L);
			}

			if ((float) k > f) {
				list.add(new Result("unspecified", (double) ((float) k - f) * 100.0D / (double) k, (double) ((float) k - f) * 100.0D / (double) i));
			}

			Collections.sort(list);
			list.add(0, new Result(profilerName, 100.0D, (double) k * 100.0D / (double) i));
			return list;
		}
	}

	/**
	 * End current section and start a new section
	 */
	public void endStartSection(String name) {
		this.endSection();
		this.startSection(name);
	}

	public String getNameOfLastSection() {
		final List<String> sectionList = this.sectionList;
		return sectionList.isEmpty() ? "[UNKNOWN]" : sectionList.get(sectionList.size() - 1);
	}

	public void endStartSection(Supplier<String> nameSupplier) {
		this.endSection();
		this.startSection(nameSupplier);
	}

	public static final class Result implements Comparable<Result> {

		public double usePercentage;
		public double totalUsePercentage;
		public String profilerName;

		public Result(String profilerName, double usePercentage, double totalUsePercentage) {
			this.profilerName = profilerName;
			this.usePercentage = usePercentage;
			this.totalUsePercentage = totalUsePercentage;
		}

		public int compareTo(Result other) {
			if (other.usePercentage < this.usePercentage) {
				return -1;
			} else {
				return other.usePercentage > this.usePercentage ? 1 : other.profilerName.compareTo(this.profilerName);
			}
		}

		/**
		 * Return a color to display the profiler, generated from the hash code of the profiler's name
		 */
		public int getColor() {
			return (this.profilerName.hashCode() & 0xAAAAAA) + 0x444444;
		}

	}

}
