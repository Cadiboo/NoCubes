package io.github.cadiboo.nocubes.client.render.util;

import java.io.Closeable;
import java.util.Iterator;

/**
 * @author Cadiboo
 */
public class Face implements Closeable {

	static final Pool<Face> POOL = new Pool<>(100);
	public Vec v0;
	public Vec v1;
	public Vec v2;
	public Vec v3;

	Face() {
	}

	public Face(final Vec v0, final Vec v1, final Vec v2, final Vec v3) {
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}

	public static Face of() {
		Face pooled = POOL.get();
		if (pooled != null)
			return pooled;
		return new Face();
	}

	public static Face of(Vec v0, Vec v1, Vec v2, Vec v3) {
		Face pooled = POOL.get();
		if (pooled != null) {
			pooled.v0 = v0;
			pooled.v1 = v1;
			pooled.v2 = v2;
			pooled.v3 = v3;
			return pooled;
		}
		return new Face(v0, v1, v2, v3);
	}

	@Override
	public void close() {
		POOL.offer(this);
	}

}
