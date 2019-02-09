package io.github.cadiboo.nocubes.util;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class SmoothableCache extends XYZCache {

	@Nonnull
	private final boolean[] smoothableCache;

	private SmoothableCache(final int sizeX, final int sizeY, final int sizeZ) {
		super(sizeX, sizeY, sizeZ);
		smoothableCache = new boolean[sizeX * sizeY * sizeZ];
	}

	@Nonnull
	public boolean[] getSmoothableCache() {
		return smoothableCache;
	}

	@Nonnull
	public static SmoothableCache retain(final int sizeX, final int sizeY, final int sizeZ) {
		return new SmoothableCache(sizeX, sizeY, sizeZ);
	}

}
