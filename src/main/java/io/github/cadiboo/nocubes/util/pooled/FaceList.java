package io.github.cadiboo.nocubes.util.pooled;

import java.util.ArrayList;

/**
 * @author Cadiboo
 */
public class FaceList extends ArrayList<Face> implements AutoCloseable {

	private static final ArrayList<FaceList> POOL = new ArrayList<>();

	private FaceList() {
	}

	public static FaceList retain() {
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				FaceList pooled = POOL.remove(POOL.size() - 1);
				if (pooled != null) {
					return pooled;
				}
			}
		}
		return new FaceList();
	}

	/**
	 * Does _NOT_ close all faces in this list
	 */
	@Override
	public void close() {
		this.clear();
		synchronized (POOL) {
			if (POOL.size() < 2000) {
				POOL.add(this);
			}
		}
	}

}
