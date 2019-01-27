package io.github.cadiboo.nocubes.client;

import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;

/**
 * @author Cadiboo
 */
public class PooledPackedLightCache implements AutoCloseable {

	private int[] packedLightCache;
	private boolean released;
	private static final ArrayList<PooledPackedLightCache> POOL = new ArrayList<>();

	private PooledPackedLightCache(int size) {
		packedLightCache = new int[size];
	}

	public static PooledPackedLightCache retain(final int size) {
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				PooledPackedLightCache pooledPackedLightCache = POOL.remove(POOL.size() - 1);

				if (pooledPackedLightCache != null && pooledPackedLightCache.released) {
					pooledPackedLightCache.released = false;
					pooledPackedLightCache.setSize(size);
					return pooledPackedLightCache;
				}
			}
		}

		return new PooledPackedLightCache(size);
	}

	private void setSize(final int size) {
		if (packedLightCache.length != size) {
			packedLightCache = new int[size];
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

	public int[] getPackedLightCache() {
		if (this.released) {
			LogManager.getLogger().error("PooledPackedLightCache gotten after it was released!", new Throwable());
		}
		return packedLightCache;
	}

	@Override
	public void close() {
		this.release();
	}

}
