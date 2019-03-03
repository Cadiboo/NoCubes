package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.config.ModConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * Simple pooled 3d vector/point with some util methods
 *
 * @author Cadiboo
 */
public class Vec3 implements Cloneable, AutoCloseable {

	private static int instances = 0;

	public float x;
	public float y;
	public float z;

//	private boolean released;

	private static final ArrayList<Vec3> POOL = new ArrayList<>();

	private Vec3(final float x, final float y, final float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		++instances;
	}

	public Vec3 addOffset(final float x, final float y, final float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	@Nonnull
	public static Vec3 retain(final float x, final float y, final float z) {
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				Vec3 pooled = POOL.remove(POOL.size() - 1);
				if (pooled != null /*&& pooled.released*/) {
//					pooled.released = false;
					pooled.x = x;
					pooled.y = y;
					pooled.z = z;
					return pooled;
				}
			}
		}
		return new Vec3(x, y, z);
	}

	@Nonnull
	public static Vec3 retain(final float[] vertexAsArray) {
		return retain(
				vertexAsArray[0],
				vertexAsArray[1],
				vertexAsArray[2]
		);
	}

	@Override
	public Vec3 clone() {
		return new Vec3(
				this.x,
				this.y,
				this.z
		);
	}

	@Override
	public void close() {
		if (!ModConfig.enablePools) {
			return;
		}
		synchronized (POOL) {
			POOL.add(this);
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
