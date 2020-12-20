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

	default T getOrCreate(Supplier<T> creator) {
		T cached = get();
		if (cached != null)
			return cached;
		T value = creator.get();
		set(value);
		return value;
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
