package io.github.cadiboo.nocubes.unusedpools;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;

/**
 * @author Cadiboo
 */
@SideOnly(Side.CLIENT)
public class PooledPackedLightCache implements AutoCloseable {

	private int[] packedLightCache;
	private boolean released;
	private static final ArrayList<PooledPackedLightCache> POOL = new ArrayList<>();

	private PooledPackedLightCache(final int sizeX, final int sizeY, final int sizeZ) {
		packedLightCache = new int[sizeX * sizeY * sizeZ];
	}

	public static PooledPackedLightCache retain(final int sizeX, final int sizeY, final int sizeZ) {
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				PooledPackedLightCache pooledPackedLightCache = POOL.remove(POOL.size() - 1);

				if (pooledPackedLightCache != null && pooledPackedLightCache.released) {
					pooledPackedLightCache.released = false;
					pooledPackedLightCache.setSize(sizeX, sizeY, sizeZ);
					return pooledPackedLightCache;
				}
			}
		}

		return new PooledPackedLightCache(sizeX, sizeY, sizeZ);
	}

	private void setSize(final int sizeX, final int sizeY, final int sizeZ) {
		final int size = sizeX * sizeY * sizeZ;
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
