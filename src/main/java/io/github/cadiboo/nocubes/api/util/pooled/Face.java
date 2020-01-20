package io.github.cadiboo.nocubes.api.util.pooled;

import java.util.ArrayList;

/**
 * A Pooled Quad
 *
 * @author Cadiboo
 */
public final class Face implements AutoCloseable {

	private static final ArrayList<Face> POOL = new ArrayList<>();

	private Vec3 vertex0;
	private Vec3 vertex1;
	private Vec3 vertex2;
	private Vec3 vertex3;

	private Face(final Vec3 vertex0, final Vec3 vertex1, final Vec3 vertex2, final Vec3 vertex3) {
		this.vertex0 = vertex0;
		this.vertex1 = vertex1;
		this.vertex2 = vertex2;
		this.vertex3 = vertex3;
	}

	public static Face retain(final Vec3 vertex0, final Vec3 vertex1, final Vec3 vertex2, final Vec3 vertex3) {
		// TODO: better thread safety?
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				Face pooled = POOL.remove(POOL.size() - 1);
				if (pooled != null) {
					pooled.vertex0 = vertex0;
					pooled.vertex1 = vertex1;
					pooled.vertex2 = vertex2;
					pooled.vertex3 = vertex3;
					return pooled;
				}
			}
		}
		return new Face(vertex0, vertex1, vertex2, vertex3);
	}

	public static Face retain(final Vec3 vertex0, final Vec3 vertex1, final Vec3 vertex2) {
		return retain(vertex0.copy(), vertex0, vertex1, vertex2);
	}

	public Vec3 getVertex0() {
		return vertex0;
	}

	public Vec3 getVertex1() {
		return vertex1;
	}

	public Vec3 getVertex2() {
		return vertex2;
	}

	public Vec3 getVertex3() {
		return vertex3;
	}

	@Override
	public void close() {
		synchronized (POOL) {
			if (POOL.size() < getMaxPoolSize())
				POOL.add(this);
		}
	}

	/**
	 * @return The maximum size of the pool
	 */
	public static int getMaxPoolSize() {
		return 2_000;
	}

}
