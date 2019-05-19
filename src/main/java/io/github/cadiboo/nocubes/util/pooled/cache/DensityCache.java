package io.github.cadiboo.nocubes.util.pooled.cache;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class DensityCache extends XYZCache implements AutoCloseable {

	private static final ThreadLocal<DensityCache> POOL = ThreadLocal.withInitial(() -> new DensityCache(0, 0, 0));
	@Nonnull
	private float[] cache;

	private DensityCache(final int sizeX, final int sizeY, final int sizeZ) {
		super(sizeX, sizeY, sizeZ);
		cache = new float[sizeX * sizeY * sizeZ];
	}

	@Nonnull
	public static DensityCache retain(final int sizeX, final int sizeY, final int sizeZ) {

		final DensityCache pooled = POOL.get();

		if (pooled.sizeX == sizeX && pooled.sizeY == sizeY && pooled.sizeZ == sizeZ) {
			return pooled;
		}

		pooled.sizeX = sizeX;
		pooled.sizeY = sizeY;
		pooled.sizeZ = sizeZ;

		final int size = sizeX * sizeY * sizeZ;

		if (pooled.cache.length < size || pooled.cache.length > size * 1.25F) {
			pooled.cache = new float[size];
		}

		return pooled;
	}

	@Nonnull
	public float[] getDensityCache() {
		return cache;
	}

	@Override
	public void close() {
	}

}
