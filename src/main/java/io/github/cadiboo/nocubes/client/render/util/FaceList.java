package io.github.cadiboo.nocubes.client.render.util;

import java.io.Closeable;
import java.util.ArrayList;

/**
 * @author Cadiboo
 */
public class FaceList extends ArrayList<Face> implements Closeable {

	static final Pool<FaceList> POOL = new Pool<>(100);

	FaceList() {
	}

	public static FaceList of() {
		FaceList pooled = POOL.get();
		if (pooled != null) {
			pooled.clear();
			return pooled;
		}
		return new FaceList();
	}

	@Override
	public void close() {
		POOL.offer(this);
	}

}
