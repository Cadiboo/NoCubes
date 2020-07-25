package io.github.cadiboo.nocubes.client.render.util;

import java.io.Closeable;

/**
 * @author Cadiboo
 */
public class Vec implements Closeable {

	static final Pool<Vec> POOL = new Pool<>(30_000);
	public double x;
	public double y;
	public double z;

	Vec(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static Vec of(Vec v) {
		return of(v.x, v.y, v.z);
	}

	public static Vec of(double[] v) {
		return of(v[0], v[1], v[2]);
	}

	public static Vec of(double x, double y, double z) {
		Vec pooled = POOL.get();
		if (pooled != null) {
			pooled.x = x;
			pooled.y = y;
			pooled.z = z;
			return pooled;
		}
		return new Vec(x, y, z);
	}

	@Override
	public void close() {
		POOL.offer(this);
	}

}
