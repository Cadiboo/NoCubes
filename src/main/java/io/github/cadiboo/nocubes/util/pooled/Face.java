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

	public void assignNormalTo(Face toUse) {
		Vec3 v0 = this.vertex0;
		Vec3 v1 = this.vertex1;
		Vec3 v2 = this.vertex2;
		Vec3 v3 = this.vertex3;
		// mul -1
		Vec3.normal(v3, v0, v1, toUse.vertex0);
		Vec3.normal(v0, v1, v2, toUse.vertex1);
		Vec3.normal(v1, v2, v3, toUse.vertex2);
		Vec3.normal(v2, v3, v0, toUse.vertex3);
	}

	public void assignAverageTo(Vec3 toUse) {
		Vec3 v0 = this.vertex0;
		Vec3 v1 = this.vertex1;
		Vec3 v2 = this.vertex2;
		Vec3 v3 = this.vertex3;
		toUse.x = (v0.x + v1.x + v2.x + v3.x) / 4;
		toUse.y = (v0.y + v1.y + v2.y + v3.y) / 4;
		toUse.z = (v0.z + v1.z + v2.z + v3.z) / 4;
	}

}
