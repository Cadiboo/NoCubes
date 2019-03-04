package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.config.ModConfig;

import java.util.ArrayList;

/**
 * @author Cadiboo
 */
public class FaceList extends ArrayList<Face> implements AutoCloseable {

	private static int instances = 0;

//	private boolean released;

	private static final ArrayList<FaceList> POOL = new ArrayList<>();

	private FaceList() {
		++instances;
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
		if (!ModConfig.enablePools) {
			return;
		}
		synchronized (POOL) {
			if (POOL.size() < 2000) {
				POOL.add(this);
			}
//			this.released = true;
		}
	}

	public static int getInstances() {
		return instances;
	}

	public static int getPoolSize() {
		return POOL.size();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		--instances;
	}

}
