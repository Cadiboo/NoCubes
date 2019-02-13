package io.github.cadiboo.nocubes.util;

import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class StateCache extends XYZCache {

	@Nonnull
	private final IBlockState[] stateCache;

	private StateCache(final int sizeX, final int sizeY, final int sizeZ) {
		super(sizeX, sizeY, sizeZ);
		stateCache = new IBlockState[sizeX * sizeY * sizeZ];
	}

	@Nonnull
	public IBlockState[] getStateCache() {
		return stateCache;
	}

	@Nonnull
	public static StateCache retain(final int sizeX, final int sizeY, final int sizeZ) {
		// STOPSHIP: 2019-02-13 FIXME TODO POOLED CACHES
		return new StateCache(sizeX, sizeY, sizeZ);
	}

}
