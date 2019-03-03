package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.config.ModConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * @author Cadiboo
 */
public class Face implements AutoCloseable {

	private static int instances = 0;

	@Nonnull
	private Vec3 vertex0;
	@Nonnull
	private Vec3 vertex1;
	@Nonnull
	private Vec3 vertex2;
	@Nonnull
	private Vec3 vertex3;

//	private boolean released;

	private static final ArrayList<Face> POOL = new ArrayList<>();

	private Face(@Nonnull final Vec3 vertex0, @Nonnull final Vec3 vertex1, @Nonnull final Vec3 vertex2, @Nonnull final Vec3 vertex3) {
		this.vertex0 = vertex0;
		this.vertex1 = vertex1;
		this.vertex2 = vertex2;
		this.vertex3 = vertex3;
		++instances;
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

	@Nonnull
	public static Face retain(@Nonnull final Vec3 vertex0, @Nonnull final Vec3 vertex1, @Nonnull final Vec3 vertex2, @Nonnull final Vec3 vertex3) {
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				Face pooled = POOL.remove(POOL.size() - 1);
				if (pooled != null /*&& pooled.released*/) {
//					pooled.released = false;
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
		return retain(vertex0.clone(), vertex0, vertex1, vertex2);
	}

	@Override
	public void close() {
		if (!ModConfig.enablePools) {
			return;
		}
		synchronized (POOL) {
			if (POOL.size() < 10000) {
				POOL.add(this);
			}
//			this.released = true;
		}
	}

	public static int getInstances() {
		return instances;
	}

	public static int getPoolSize() {
		return POOL.size();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		--instances;
	}

}
