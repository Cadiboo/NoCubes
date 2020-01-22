package io.github.cadiboo.nocubes.util.pooled;

import java.util.ArrayList;

/**
 * A Pooled list of {@link Face}s
 *
 * @author Cadiboo
 */
public final class FaceList extends ArrayList<Face> implements AutoCloseable {

	private static final ArrayList<FaceList> POOL = new ArrayList<>();

	private FaceList() {
	}

	public static FaceList retain() {
		// TODO: better thread safety?
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
	 * @return The maximum size of the pool
	 */
	public static int getMaxPoolSize() {
		return 2_000;
	}

	/**
	 * Does not close all faces in this list
	 */
	@Override
	public void close() {
		this.clear();
		synchronized (POOL) {
			if (POOL.size() < getMaxPoolSize())
				POOL.add(this);
		}
	}

}
