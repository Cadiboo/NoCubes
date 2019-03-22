package io.github.cadiboo.nocubes.util.pooled;

import io.github.cadiboo.nocubes.config.ModConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * Simple pooled 3d byte vector/point with some util methods
 *
 * @author Cadiboo
 */
public class Vec3b implements Cloneable, AutoCloseable {

//	private static int instances = 0;

	public byte x;
	public byte y;
	public byte z;

//	private boolean released;

	private static final ArrayList<Vec3b> POOL = new ArrayList<>();

	private Vec3b(final byte x, final byte y, final byte z) {
		this.x = x;
		this.y = y;
		this.z = z;
//		++instances;
	}

	@Nonnull
	public static Vec3b retain(final byte x, final byte y, final byte z) {
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				Vec3b pooled = POOL.remove(POOL.size() - 1);
				if (pooled != null /*&& pooled.released*/) {
//					pooled.released = false;
					pooled.x = x;
					pooled.y = y;
					pooled.z = z;
					return pooled;
				}
			}
		}
		return new Vec3b(x, y, z);
	}

	public Vec3b copy() {
		return new Vec3b(
				this.x,
				this.y,
				this.z
		);
	}

	@Override
	public void close() {
		if (!ModConfig.enablePools) {
			return;
		}
		synchronized (POOL) {
			if (POOL.size() < 15000) {
				POOL.add(this);
			}
//			this.released = true;
		}
	}

//	public static int getInstances() {
//		return instances;
//	}
//
//	public static int getPoolSize() {
//		return POOL.size();
//	}
//
//	@Override
//	protected void finalize() throws Throwable {
//		super.finalize();
//		--instances;
//	}

}
