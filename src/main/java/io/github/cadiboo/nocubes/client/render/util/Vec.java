package io.github.cadiboo.nocubes.client.render.util;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Cadiboo
 */
public class Vec implements AutoCloseable {

	static final Queue<Vec> POOL = new LinkedList<>();
	public double x;
	public double y;
	public double z;

	Vec(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static Vec of(double x, double y, double z) {
		if (!POOL.isEmpty()) {
			Vec pooled = null;
			synchronized (POOL) {
				if (!POOL.isEmpty())
					pooled = POOL.poll();
			}
			if (pooled != null) {
				pooled.x = x;
				pooled.y = y;
				pooled.z = z;
				return pooled;
			}
		}
		return new Vec(x, y, z);
	}

	@Override
	public void close() {
		synchronized (POOL) {
			POOL.offer(this);
		}
	}

}
