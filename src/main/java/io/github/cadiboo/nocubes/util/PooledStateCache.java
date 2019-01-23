package io.github.cadiboo.nocubes.util;

import net.minecraft.block.state.IBlockState;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;

/**
 * @author Cadiboo
 */
public class PooledStateCache implements AutoCloseable {

	private IBlockState[] stateCache;
	private boolean released;
	private static final ArrayList<PooledStateCache> POOL = new ArrayList<>();

	private PooledStateCache(int size) {
		stateCache = new IBlockState[size];
	}

	public static PooledStateCache retain(final int size) {
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				PooledStateCache pooledStateCache = POOL.remove(POOL.size() - 1);

				if (pooledStateCache != null && pooledStateCache.released) {
					pooledStateCache.released = false;
					pooledStateCache.setSize(size);
					return pooledStateCache;
				}
			}
		}

		return new PooledStateCache(size);
	}

	private void setSize(final int size) {
		if (stateCache.length != size) {
			stateCache = new IBlockState[size];
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

	public IBlockState[] getStateCache() {
		if (this.released) {
			LogManager.getLogger().error("PooledStateCache gotten after it was released!", new Throwable());
		}
		return stateCache;
	}

	@Override
	public void close() {
		this.release();
	}

}
