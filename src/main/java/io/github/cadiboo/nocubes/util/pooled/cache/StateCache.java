package io.github.cadiboo.nocubes.util.pooled.cache;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.IFluidState;

import javax.annotation.Nonnull;

/**
 * // Density Cache | -1, n     | n + 1
 * Density Cache    | -1, 18    | 20
 * Vertices         | -1, 16    | 18
 * Texture Cache    | -2, 17    | 20
 * Light Cache      | -2, 17    | 20
 * Color Cache      | -2, 17    | 20
 * Fluids Cache     | 0,15x0,16y| 16, 17
 *
 * @author Cadiboo
 */
public class StateCache extends XYZCache implements AutoCloseable {

	private static final ThreadLocal<StateCache> POOL = ThreadLocal.withInitial(() -> new StateCache(0, 0, 0, 0, 0, 0));

	@Nonnull
	private BlockState[] blockStates;
	@Nonnull
	private IFluidState[] fluidStates;

	private boolean inUse;

	private StateCache(
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			final int sizeX, final int sizeY, final int sizeZ
	) {
		super(startPaddingX, startPaddingY, startPaddingZ, sizeX, sizeY, sizeZ);
		this.blockStates = new BlockState[sizeX * sizeY * sizeZ];
		this.fluidStates = new IFluidState[sizeX * sizeY * sizeZ];
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
			pooled.blockStates = new BlockState[size];
		}
		if (pooled.fluidStates.length < size || pooled.fluidStates.length > size * 1.25F) {
			pooled.fluidStates = new IFluidState[size];
		}

		return pooled;
	}

	@Nonnull
	public BlockState[] getBlockStates() {
		return blockStates;
	}

	@Nonnull
	public IFluidState[] getFluidStates() {
		return fluidStates;
	}

	@Override
	public void close() {
		this.inUse = false;
	}

}
