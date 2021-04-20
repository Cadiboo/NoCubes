package io.github.cadiboo.nocubes.util;

import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public class ThreadLocalArrayCache<T> extends ThreadLocal<T> {

	private final IntFunction<T> constructor;
	private final ToIntFunction<T> length;
	private final BiConsumer<Integer, T> initialiser;

	public ThreadLocalArrayCache(IntFunction<T> constructor, ToIntFunction<T> length) {
		this(constructor, length, (newLength, array) -> {});
	}

	public ThreadLocalArrayCache(IntFunction<T> constructor, ToIntFunction<T> length, BiConsumer<Integer, T> initialiser) {
		this.constructor = constructor;
		this.length = length;
		this.initialiser = initialiser;
	}

	public T takeArray(int minLength) {
		T array = get();
		if (array == null || length.applyAsInt(array) < minLength) {
			array = constructor.apply(minLength);
			set(array);
		}
		initialiser.accept(minLength, array);
		return array;
	}
}
