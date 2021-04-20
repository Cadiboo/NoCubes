package io.github.cadiboo.nocubes.util.pooled.cache;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class CornerDensityCache extends XYZCache implements AutoCloseable {

	private static final ThreadLocal<CornerDensityCache> POOL = ThreadLocal.withInitial(() -> new CornerDensityCache(0, 0, 0, 0, 0, 0));

	@Nonnull
	private float[] cache;

	private boolean inUse;

	private CornerDensityCache(
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			final int sizeX, final int sizeY, final int sizeZ
	) {
		super(startPaddingX, startPaddingY, startPaddingZ, sizeX, sizeY, sizeZ);
		this.cache = new float[sizeX * sizeY * sizeZ];
		this.inUse = false;
	}

	@Nonnull
	public static CornerDensityCache retain(
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			final int sizeX, final int sizeY, final int sizeZ
	) {

		final CornerDensityCache pooled = POOL.get();

		if (pooled.inUse)
			throw new IllegalStateException("CornerDensityCache is already in use!");
		pooled.inUse = true;

		pooled.startPaddingX = startPaddingX;
		pooled.startPaddingY = startPaddingY;
		pooled.startPaddingZ = startPaddingZ;

		if (pooled.sizeX == sizeX && pooled.sizeY == sizeY && pooled.sizeZ == sizeZ)
			return pooled;

		pooled.sizeX = sizeX;
		pooled.sizeY = sizeY;
		pooled.sizeZ = sizeZ;

		final int size = sizeX * sizeY * sizeZ;

		if (pooled.cache.length < size || pooled.cache.length > size * 1.25F)
			pooled.cache = new float[size];

		return pooled;
	}

	@Nonnull
	public float[] getCornerDensityCache() {
		return cache;
	}

	@Override
	public void close() {
		this.inUse = false;
	}

}
