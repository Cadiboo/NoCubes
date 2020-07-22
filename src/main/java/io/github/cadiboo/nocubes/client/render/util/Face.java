package io.github.cadiboo.nocubes.client.render.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Cadiboo
 */
public class Face implements AutoCloseable {

	static final Queue<Face> POOL = new LinkedList<>();
	public Vec v0;
	public Vec v1;
	public Vec v2;
	public Vec v3;

	Face(final Vec v0, final Vec v1, final Vec v2, final Vec v3) {
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}

	public static Face of(Vec v0, Vec v1, Vec v2, Vec v3) {
		if (!POOL.isEmpty()) {
			Face pooled = null;
			synchronized (POOL) {
				if (!POOL.isEmpty())
					pooled = POOL.poll();
			}
			if (pooled != null) {
				pooled.v0 = v0;
				pooled.v1 = v1;
				pooled.v2 = v2;
				pooled.v3 = v3;
				return pooled;
			}
		}
		return new Face(v0, v1, v2, v3);
	}

	public Iterable<Vec> getVertices() {
		return () -> new Iterator<Vec>() {
			int idx = 0;

			@Override
			public boolean hasNext() {
				return idx < 3;
			}

			@Override
			public Vec next() {
				switch (idx) {
					case 0:
						return v0;
					case 1:
						return v1;
					case 2:
						return v2;
					case 3:
						return v3;
					default:
						throw new IllegalArgumentException("Must be between 0 and 3, got " + idx);
				}
			}
		};
	}

	@Override
	public void close() {
		synchronized (POOL) {
			POOL.offer(this);
		}
	}

}
