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

	public Face() {
	}

	public Face(final Vec v0, final Vec v1, final Vec v2, final Vec v3) {
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
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

	// TODO: Inline this and other pooled for loops to not create new objects
	public Iterable<Vec> getVertices() {
		return () -> new Iterator<Vec>() {
			int idx = 0;

			@Override
			public boolean hasNext() {
				return idx < 4;
			}

			@Override
			public Vec next() {
				final int idx = this.idx++;
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
		POOL.offer(this);
	}

}
