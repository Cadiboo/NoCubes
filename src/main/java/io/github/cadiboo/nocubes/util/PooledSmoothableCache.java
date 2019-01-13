package io.github.cadiboo.nocubes.util;

import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;

/**
 * @author Cadiboo
 */
public class PooledSmoothableCache {

	private boolean[] smoothableCache;
	private boolean released;
	private static final ArrayList<PooledSmoothableCache> POOL = new ArrayList<>();

	private PooledSmoothableCache(int size) {
		smoothableCache = new boolean[size];
	}

	public static PooledSmoothableCache retain(final int size) {
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				PooledSmoothableCache pooledSmoothableCache = POOL.remove(POOL.size() - 1);

				if (pooledSmoothableCache != null && pooledSmoothableCache.released) {
					pooledSmoothableCache.released = false;
					pooledSmoothableCache.setSize(size);
					return pooledSmoothableCache;
				}
			}
		}

		return new PooledSmoothableCache(size);
	}

	private void setSize(final int size) {
		if (smoothableCache.length != size) {
			smoothableCache = new boolean[size];
		}
	}

	public void release() {
		synchronized (POOL) {
			if (POOL.size() < 100) {
				POOL.add(this);
			}

			this.released = true;
		}
	}

	public boolean[] getSmoothableCache() {
		if (this.released) {
			LogManager.getLogger().error("PooledSmoothableCache gotten after it was released!", new Throwable());
		}
		return smoothableCache;
	}

}
