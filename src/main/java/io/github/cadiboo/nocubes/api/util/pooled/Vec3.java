package io.github.cadiboo.nocubes.api.util.pooled;

import java.util.ArrayList;

/**
 * Simple Pooled 3d vector/point with some util methods
 *
 * @author Cadiboo
 */
public final class Vec3 implements AutoCloseable {

	private static final ArrayList<Vec3> POOL = new ArrayList<>();

	public double x;
	public double y;
	public double z;

	private Vec3(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static Vec3 retain(final double x, final double y, final double z) {
		// TODO: better thread safety?
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				Vec3 pooled = POOL.remove(POOL.size() - 1);
				if (pooled != null) {
					pooled.x = x;
					pooled.y = y;
					pooled.z = z;
					return pooled;
				}
			}
		}
		return new Vec3(x, y, z);
	}

	/**
	 * @return The maximum size of the pool
	 */
	public static int getMaxPoolSize() {
		// TODO
		return Integer.MAX_VALUE;
	}

	/**
	 * Adds the offset to this vector.
	 * Mutates this vector.
	 *
	 * @return this
	 */
	public Vec3 addOffset(final double x, final double y, final double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	/**
	 * @return A newly retained copy of this vector
	 */
	public Vec3 copy() {
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
