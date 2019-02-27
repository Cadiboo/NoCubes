package io.github.cadiboo.nocubes.util;

import net.minecraft.profiler.Profiler;

public class ModProfiler extends Profiler {

	public void start(final String name) {
//		startSection(name);
	}

	public void end() {
//		endSection();
	}

//	private LinkedList<String> profiling = new LinkedList<>();
//
//	private LinkedHashMap<String, LongReference> map = new LinkedHashMap<>();
//
//	public ArrayList<String> finished = new ArrayList<>();
//
//	public void start(final String name) {
//		final LongReference ref = new LongReference(0);
//		map.put(name, ref);
//		profiling.add(name);
//		ref.value = System.nanoTime();
//	}
//
//	public void end() {
//		final long endTime = System.nanoTime();
//		final int size = profiling.size();
//		final String name = profiling.removeLast();
//		final LongReference longReference = map.get(name);
//		final long timeTaken = endTime - longReference.value;
//		finished.add(StringUtils.leftPad(name + " took " + timeTaken + " nano seconds", size, " "));
//	}
//
//	private class LongReference {
//
//		private long value;
//
//		public LongReference(final long value) {
//			this.value = value;
//		}
//
//	}

}
