package io.github.cadiboo.nocubes.util.pooled.cache;


import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class StateCache extends XYZCache implements AutoCloseable {

	private static final ThreadLocal<StateCache> POOL = ThreadLocal.withInitial(() -> new StateCache(0, 0, 0, 0, 0, 0));

	@Nonnull
	private IBlockState[] blockStates;
//	@Nonnull
//	private IFluidState[] fluidStates;

	private boolean inUse;

	private StateCache(
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			final int sizeX, final int sizeY, final int sizeZ
	) {
		super(startPaddingX, startPaddingY, startPaddingZ, sizeX, sizeY, sizeZ);
		this.blockStates = new IBlockState[sizeX * sizeY * sizeZ];
//		this.fluidStates = new IFluidState[sizeX * sizeY * sizeZ];
		this.inUse = false;
	}

	@Nonnull
	public static StateCache retain(
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			final int sizeX, final int sizeY, final int sizeZ
	) {

		final StateCache pooled = POOL.get();

		if (pooled.inUse) {
			throw new IllegalStateException("StateCache is already in use!");
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

		if (pooled.blockStates.length < size || pooled.blockStates.length > size * 1.25F) {
			pooled.blockStates = new IBlockState[size];
		}
//		if (pooled.fluidStates.length < size || pooled.fluidStates.length > size * 1.25F) {
//			pooled.fluidStates = new IFluidState[size];
//		}

		return pooled;
	}

	@Nonnull
	public IBlockState[] getBlockStates() {
		return blockStates;
	}

//	@Nonnull
//	public IFluidState[] getFluidStates() {
//		return fluidStates;
//	}

	@Override
	public void close() {
		this.inUse = false;
	}

}
