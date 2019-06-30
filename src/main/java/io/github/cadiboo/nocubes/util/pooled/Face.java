package io.github.cadiboo.nocubes.util.pooled;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * @author Cadiboo
 */
public final class Face implements AutoCloseable {

	private static final ArrayList<Face> POOL = new ArrayList<>();

	@Nonnull
	private Vec3 vertex0;
	@Nonnull
	private Vec3 vertex1;
	@Nonnull
	private Vec3 vertex2;
	@Nonnull
	private Vec3 vertex3;

	private Face(@Nonnull final Vec3 vertex0, @Nonnull final Vec3 vertex1, @Nonnull final Vec3 vertex2, @Nonnull final Vec3 vertex3) {
		this.vertex0 = vertex0;
		this.vertex1 = vertex1;
		this.vertex2 = vertex2;
		this.vertex3 = vertex3;
	}

	@Nonnull
	public static Face retain(@Nonnull final Vec3 vertex0, @Nonnull final Vec3 vertex1, @Nonnull final Vec3 vertex2, @Nonnull final Vec3 vertex3) {
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

	@Nonnull
	public static Face retain(@Nonnull final Vec3 vertex0, @Nonnull final Vec3 vertex1, @Nonnull final Vec3 vertex2) {
		return retain(vertex0.copy(), vertex0, vertex1, vertex2);
	}

	@Nonnull
	public Vec3 getVertex0() {
		return vertex0;
	}

	@Nonnull
	public Vec3 getVertex1() {
		return vertex1;
	}

	@Nonnull
	public Vec3 getVertex2() {
		return vertex2;
	}

	@Nonnull
	public Vec3 getVertex3() {
		return vertex3;
	}

	@Override
	public void close() {
		synchronized (POOL) {
			if (POOL.size() < 2000) {
				POOL.add(this);
			}
		}
	}

}
