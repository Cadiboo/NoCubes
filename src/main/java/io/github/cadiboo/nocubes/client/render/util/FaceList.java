package io.github.cadiboo.nocubes.client.render.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Cadiboo
 */
public class FaceList extends LinkedList<Face> implements AutoCloseable {
	static final Queue<FaceList> POOL = new LinkedList<>();

	FaceList() {
	}

	public static FaceList of() {
		if (!POOL.isEmpty()) {
			FaceList pooled = null;
			synchronized (POOL) {
				if (!POOL.isEmpty())
					pooled = POOL.poll();
			}
			if (pooled != null)
				return pooled;
		}
		return new FaceList();
	}

	@Override
	public void close() {
		synchronized (POOL) {
			POOL.offer(this);
		}
	}

}
