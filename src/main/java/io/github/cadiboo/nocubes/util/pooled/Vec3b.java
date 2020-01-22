package io.github.cadiboo.nocubes.util.pooled;

import java.util.ArrayList;

/**
 * Simple pooled 3d byte vector/point with some util methods
 *
 * @author Cadiboo
 */
public final class Vec3b implements Cloneable, AutoCloseable {

	private static final ArrayList<Vec3b> POOL = new ArrayList<>();

	public byte x;
	public byte y;
	public byte z;

	private Vec3b(final byte x, final byte y, final byte z) {
		this.x = x;
		this.y = y;
		this.z = z;

	}

	public static Vec3b retain(final int x, final int y, final int z) {
		return retain((byte) x, (byte) y, (byte) z);
	}

	public static Vec3b retain(final byte x, final byte y, final byte z) {
		// TODO: better thread safety?
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				Vec3b pooled = POOL.remove(POOL.size() - 1);
				if (pooled != null) {
					pooled.x = x;
					pooled.y = y;
					pooled.z = z;
					return pooled;
				}
			}
		}
		return new Vec3b(x, y, z);
	}

	/**
	 * @return The maximum size of the pool
	 */
	public static int getMaxPoolSize() {
		return 15_000;
	}

	/**
	 * @return A newly retained copy of this vector
	 */
	public Vec3b copy() {
		return retain(this.x, this.y, this.z);
	}

	@Override
	public void close() {
		synchronized (POOL) {
			if (POOL.size() < getMaxPoolSize())
				POOL.add(this);
		}
	}

}
