package io.github.cadiboo.nocubes.util;

import java.util.ArrayList;

/**
 * @author Cadiboo
 */
public class FaceList extends ArrayList<Face> implements AutoCloseable {

//	private static int instances = 0;

//	private boolean released;

	private static final ArrayList<FaceList> POOL = new ArrayList<>();

	private FaceList() {
//		++instances;
	}

	public static FaceList retain() {
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				FaceList pooled = POOL.remove(POOL.size() - 1);
				if (pooled != null /*&& pooled.released*/) {
					return pooled;
				}
			}
		}
		return new FaceList();
	}

	@Override
	public void close() {
		this.clear();
		synchronized (POOL) {
			POOL.add(this);
//			this.released = true;
		}
	}

//	static
//	public int getInstances() {
//		return instances;
//	}
//
//	static
//	public int getPoolSize() {
//		return POOL.size();
//	}

}
