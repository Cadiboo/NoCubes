package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.util.Vec3.PooledVec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class PooledFace {

	@Nonnull
	private PooledVec3 vertex0;
	@Nonnull
	private PooledVec3 vertex1;
	@Nonnull
	private PooledVec3 vertex2;
	@Nonnull
	private PooledVec3 vertex3;

	private PooledFace(@Nonnull final PooledVec3 vertex0, @Nonnull final PooledVec3 vertex1, @Nonnull final PooledVec3 vertex2, @Nonnull final PooledVec3 vertex3) {
		this.vertex0 = vertex0;
		this.vertex1 = vertex1;
		this.vertex2 = vertex2;
		this.vertex3 = vertex3;
	}

	public PooledVec3 getVertex0() {
		return vertex0;
	}

	public PooledVec3 getVertex1() {
		return vertex1;
	}

	public PooledVec3 getVertex2() {
		return vertex2;
	}

	public PooledVec3 getVertex3() {
		return vertex3;
	}

	private boolean released;
	private static final ArrayList<PooledFace> POOL = new ArrayList<>();

	public static PooledFace retain(final PooledVec3 vertex0, final PooledVec3 vertex1, final PooledVec3 vertex2) {
		final PooledVec3 copyOfVertex0 = PooledVec3.retain(vertex0.x, vertex0.y, vertex0.z);
		return retain(copyOfVertex0, vertex0, vertex1, vertex2);
	}

	public static PooledFace retain(final PooledVec3 vertex0, final PooledVec3 vertex1, final PooledVec3 vertex2, final PooledVec3 vertex3) {
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				PooledFace pooledFace = POOL.remove(POOL.size() - 1);

				if (pooledFace != null && pooledFace.released) {
					pooledFace.released = false;
					pooledFace.vertex0 = vertex0;
					pooledFace.vertex1 = vertex1;
					pooledFace.vertex2 = vertex3;
					pooledFace.vertex3 = vertex3;
					return pooledFace;
				}
			}
		}

		return new PooledFace(vertex0, vertex1, vertex2, vertex3);
	}

	public void release() {
		synchronized (POOL) {
			if (POOL.size() < 100000) {
				POOL.add(this);
			}
			this.released = true;
		}
	}

}
