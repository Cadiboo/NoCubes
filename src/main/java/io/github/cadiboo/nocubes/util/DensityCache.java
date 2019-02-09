package io.github.cadiboo.nocubes.util;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class DensityCache extends XYZCache {

	@Nonnull
	private final float[] densityCache;

	private DensityCache(final int sizeX, final int sizeY, final int sizeZ) {
		super(sizeX, sizeY, sizeZ);
		densityCache = new float[sizeX * sizeY * sizeZ];
	}

	@Nonnull
	public float[] getDensityCache() {
		return densityCache;
	}

	@Nonnull
	public static DensityCache retain(final int sizeX, final int sizeY, final int sizeZ) {
		return new DensityCache(sizeX, sizeY, sizeZ);
	}

}
