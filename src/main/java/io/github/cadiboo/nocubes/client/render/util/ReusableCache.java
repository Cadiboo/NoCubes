package io.github.cadiboo.nocubes.client.render.util;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public interface ReusableCache<T> {

	@Nullable
	T get();

	void set(T value);

	void clear();

	class Global<T> implements ReusableCache<T> {

		private T value;

		@Nullable
		@Override
		public T get() {
			return value;
		}

		@Override
		public void set(T value) {
			this.value = value;
		}

		@Override
		public void clear() {
			this.value = null;
		}
	}

	static <T> T getOrCreate(@Nullable ReusableCache<T> cache, Supplier<T> creator) {
		if (cache == null)
			return creator.get();
		else if (cache.get() != null)
			return cache.get();
		else {
			T value = creator.get();
			cache.set(value);
			return value;
		}
	}

	class Local<T> implements ReusableCache<T> {

		private final ThreadLocal<T> threadLocal = new ThreadLocal<>();

		@Nullable
		@Override
		public T get() {
			return threadLocal.get();
		}

		@Override
		public void set(T value) {
			threadLocal.set(value);
		}

		@Override
		public void clear() {
			threadLocal.set(null);
		}
	}

}
