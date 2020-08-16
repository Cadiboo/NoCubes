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

	public static void normal(Face face, Face normal) {
		final Vec v0 = face.v0;
		final Vec v1 = face.v1;
		final Vec v2 = face.v2;
		final Vec v3 = face.v3;
		// mul -1
		Vec.normal(v3, v0, v1, normal.v0);
		Vec.normal(v0, v1, v2, normal.v1);
		Vec.normal(v1, v2, v3, normal.v2);
		Vec.normal(v2, v3, v0, normal.v3);
	}

	public static void average(Face face, Vec toUse) {
		average(face.v0, face.v1, face.v2, face.v3, toUse);
	}

	public static void average(Vec v0, Vec v1, Vec v2, Vec v3, Vec toUse) {
		toUse.x = (v0.x + v1.x + v2.x + v3.x) / 4;
		toUse.y = (v0.y + v1.y + v2.y + v3.y) / 4;
		toUse.z = (v0.z + v1.z + v2.z + v3.z) / 4;
	}

	@Override
	public void close() {
		v0.close();
		v1.close();
		v2.close();
		v3.close();
		POOL.offer(this);
	}

}
