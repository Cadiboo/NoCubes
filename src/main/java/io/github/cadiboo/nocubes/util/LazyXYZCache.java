package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.util.pooled.cache.XYZCache;

/**
 * @author Cadiboo
 */
public class LazyXYZCache extends XYZCache {

	protected static final int[] EMPTY = new int[22 * 22 * 22];

	public LazyXYZCache(final int sizeX, final int sizeY, final int sizeZ) {
		super(sizeX, sizeY, sizeZ);
	}

}
