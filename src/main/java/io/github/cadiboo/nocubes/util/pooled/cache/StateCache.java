package io.github.cadiboo.nocubes.util.pooled.cache;

import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.IFluidState;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class StateCache extends XYZCache implements AutoCloseable {

	private static final ThreadLocal<StateCache> POOL = ThreadLocal.withInitial(() -> new StateCache(0, 0, 0));

	private static int instances = 0;

	@Nonnull
	private IBlockState[] blockCache;

	@Nonnull
	private IFluidState[] fluidCache;

	private StateCache(final int sizeX, final int sizeY, final int sizeZ) {
		super(sizeX, sizeY, sizeZ);
		blockCache = new IBlockState[sizeX * sizeY * sizeZ];
		fluidCache = new IFluidState[sizeX * sizeY * sizeZ];
		++instances;
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

		final int blockCacheLength = pooled.blockCache.length;
		if (blockCacheLength < size || blockCacheLength > size * 1.25F) {
			pooled.blockCache = new IBlockState[size];
		}
		final int fluidCacheLength = pooled.fluidCache.length;
		if (fluidCacheLength < size || fluidCacheLength > size * 1.25F) {
			pooled.fluidCache = new IFluidState[size];
		}

		return pooled;
	}

	public static int getInstances() {
		return instances;
	}

	@Nonnull
	public IBlockState[] getBlockStateCache() {
		return blockCache;
	}

	@Nonnull
	public IFluidState[] getFluidStateCache() {
		return fluidCache;
	}

	@Override
	public void close() {
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		--instances;
	}

}
