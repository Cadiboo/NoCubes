package io.github.cadiboo.nocubes.client.render.util;

import javax.annotation.Nullable;

/**
 * @author Cadiboo
 */
public class Pool<T> {

	private final Object[] items;
	private volatile int size;

	public Pool(int maxSize) {
		this.items = new Object[maxSize];
	}

	public int getSize() {
		return size;
	}

	@Nullable
	public T get() {
		if (size > 0)
			synchronized (this) {
				if (size > 0) {
					final int i = size - 1;
					Object o = items[i];
					items[i] = null;
					--size;
					return (T) o;
				}
			}
		return null;
	}

	public void offer(T o) {
		if (size < items.length)
			synchronized (this) {
				if (size < items.length)
					items[size++] = o;
			}
	}

	public void clean() {
		synchronized (this) {
			for (int i = 0; i < size; i++)
				items[i] = null;
			size = 0;
		}
	}

}
