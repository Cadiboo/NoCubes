package io.github.cadiboo.nocubes.util;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public interface ReusableCache<T> {

//	LoadingCache<Object, ReusableCache<?>> cache = CacheBuilder.newBuilder()
//		.maximumSize(16)
//		.expireAfterAccess(1, TimeUnit.SECONDS)
//		.build(new CacheLoader<Object, ReusableCache<?>>()
//		{
//			@Override
//			public ReusableCache<?> load(Object key)
//			{
//				return ChunkRenderCache.generateCache(key.getLeft(), key.getRight().add(-1, -1, -1), key.getRight().add(16, 16, 16), 1);
//			}
//		});

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

	class Uncached<T> implements ReusableCache<T> {

		@Nullable
		@Override
		public T get() {
			return null;
		}

		@Override
		public void set(T value) {
		}

		@Override
		public void clear() {
		}
	}

}
