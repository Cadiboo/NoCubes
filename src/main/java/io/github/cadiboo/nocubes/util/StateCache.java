package io.github.cadiboo.nocubes.util;

import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class StateCache extends XYZCache implements AutoCloseable {

	private static int instances = 0;

	@Nonnull
	private IBlockState[] cache;

	private static final ThreadLocal<StateCache> POOL = ThreadLocal.withInitial(() -> new StateCache(0, 0, 0));

	private StateCache(final int sizeX, final int sizeY, final int sizeZ) {
		super(sizeX, sizeY, sizeZ);
		cache = new IBlockState[sizeX * sizeY * sizeZ];
		++instances;
	}

	@Nonnull
	public IBlockState[] getStateCache() {
		return cache;
	}

	@Nonnull
	public static StateCache retain(final int sizeX, final int sizeY, final int sizeZ) {

		final StateCache pooled = POOL.get();

		if (pooled.sizeX == sizeX && pooled.sizeY == sizeY && pooled.sizeZ == sizeZ) {
			return pooled;
		}

		pooled.sizeX = sizeX;
		pooled.sizeY = sizeY;
		pooled.sizeZ = sizeZ;

		final int size = sizeX * sizeY * sizeZ;

		if (pooled.cache.length < size || pooled.cache.length > size * 1.25F) {
			pooled.cache = new IBlockState[size];
		}

		return pooled;
	}

	@Override
	public void close() {
	}

	public static int getInstances() {
		return instances;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		--instances;
	}

}
