package io.github.cadiboo.nocubes.unusedpools;

import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;

/**
 * @author Cadiboo
 */
public class PooledDensityCache implements AutoCloseable {

	private float[] densityCache;
	private boolean released;
	private static final ArrayList<PooledDensityCache> POOL = new ArrayList<>();

	private PooledDensityCache(int size) {
		densityCache = new float[size];
	}

	public static PooledDensityCache retain(final int size) {
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				PooledDensityCache pooledDensityCache = POOL.remove(POOL.size() - 1);

				if (pooledDensityCache != null && pooledDensityCache.released) {
					pooledDensityCache.released = false;
					pooledDensityCache.setSize(size);
					return pooledDensityCache;
				}
			}
		}

		return new PooledDensityCache(size);
	}

	private void setSize(final int size) {
		if (densityCache.length != size) {
			densityCache = new float[size];
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

	public float[] getDensityCache() {
		if (this.released) {
			LogManager.getLogger().error("PooledDensityCache gotten after it was released!", new Throwable());
		}
		return densityCache;
	}

	@Override
	public void close() {
		this.release();
	}

}
