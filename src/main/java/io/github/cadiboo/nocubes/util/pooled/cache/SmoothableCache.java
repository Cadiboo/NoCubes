package io.github.cadiboo.nocubes.util.pooled.cache;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class SmoothableCache extends XYZCache implements AutoCloseable {

	private static final ThreadLocal<SmoothableCache> POOL = ThreadLocal.withInitial(() -> new SmoothableCache(0, 0, 0, 0, 0, 0));

	@Nonnull
	private boolean[] cache;

	private boolean inUse;

	private SmoothableCache(
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			final int sizeX, final int sizeY, final int sizeZ
	) {
		super(startPaddingX, startPaddingY, startPaddingZ, sizeX, sizeY, sizeZ);
		this.cache = new boolean[sizeX * sizeY * sizeZ];
		this.inUse = false;
	}

	@Nonnull
	public static SmoothableCache retain(
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			final int sizeX, final int sizeY, final int sizeZ
	) {

		final SmoothableCache pooled = POOL.get();

		if (pooled.inUse) {
			throw new IllegalStateException("SmoothableCache is already in use!");
		}
		pooled.inUse = true;

		pooled.startPaddingX = startPaddingX;
		pooled.startPaddingY = startPaddingY;
		pooled.startPaddingZ = startPaddingZ;

		if (pooled.sizeX == sizeX && pooled.sizeY == sizeY && pooled.sizeZ == sizeZ) {
			return pooled;
		}

		pooled.sizeX = sizeX;
		pooled.sizeY = sizeY;
		pooled.sizeZ = sizeZ;

		final int size = sizeX * sizeY * sizeZ;

		if (pooled.cache.length < size || pooled.cache.length > size * 1.25F) {
			pooled.cache = new boolean[size];
		}

		return pooled;
	}

	@Nonnull
	public boolean[] getSmoothableCache() {
		return cache;
	}

	@Override
	public void close() {
		this.inUse = false;
	}

}
