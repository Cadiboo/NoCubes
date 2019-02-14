package io.github.cadiboo.nocubes.util;

import net.minecraft.block.BlockState;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class StateCache extends XYZCache {

	@Nonnull
	private final BlockState[] stateCache;

	private StateCache(final int sizeX, final int sizeY, final int sizeZ) {
		super(sizeX, sizeY, sizeZ);
		stateCache = new BlockState[sizeX * sizeY * sizeZ];
	}

	@Nonnull
	public BlockState[] getStateCache() {
		return stateCache;
	}

	@Nonnull
	public static StateCache retain(final int sizeX, final int sizeY, final int sizeZ) {
		// STOPSHIP: 2019-02-13 FIXME TODO POOLED CACHES
		return new StateCache(sizeX, sizeY, sizeZ);
	}

}
