package io.github.cadiboo.nocubes.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

/**
 * Simple 3d vector/point with some util methods
 *
 * @author Cadiboo
 */
public class Vec3 {

	public float x;
	public float y;
	public float z;

	public Vec3() {
		this(0, 0, 0);
	}

	public Vec3(final float x, final float y, final float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3(final float[] vertexAsArray) {
		this(
				vertexAsArray[0],
				vertexAsArray[1],
				vertexAsArray[2]
		);
	}

	@Deprecated
	public Vec3 withOffset(final float x, final float y, final float z) {
		return new Vec3(
				this.x + x,
				this.y + y,
				this.z + z
		);
	}

	public Vec3 addOffset(final float x, final float y, final float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public BlockPos toBlockPos() {
		return new BlockPos(this.x, this.y, this.z);
	}

	public AxisAlignedBB toAxisAlignedBB() {
		return new AxisAlignedBB(this.toBlockPos());
	}

	public static class PooledVec3 extends Vec3 implements AutoCloseable {

		private boolean released;
		private static final ArrayList<PooledVec3> POOL = new ArrayList<>();

		private PooledVec3() {
			super();
		}

		private PooledVec3(final float x, final float y, final float z) {
			super(x, y, z);
		}

		private PooledVec3(final float[] vertexAsFloat) {
			super(vertexAsFloat);
		}

		public static PooledVec3 retain() {
			return retain(0, 0, 0);
		}

		private static int debugCounter = 0;

		public static PooledVec3 retain(final float xIn, final float yIn, final float zIn) {
			debugCounter++;
			synchronized (POOL) {
				if (!POOL.isEmpty()) {
					PooledVec3 pooledVec3 = POOL.remove(POOL.size() - 1);

					if (pooledVec3 != null && pooledVec3.released) {
						pooledVec3.released = false;
						pooledVec3.x = xIn;
						pooledVec3.y = yIn;
						pooledVec3.z = zIn;
						return pooledVec3;
					}
				}
			}

			return new PooledVec3(xIn, yIn, zIn);
		}

		public static PooledVec3 retain(final float[] vertexAsArray) {
			return retain(vertexAsArray[0], vertexAsArray[1], vertexAsArray[2]);
		}

		public void release() {
			synchronized (POOL) {
				if (POOL.size() < 100000) {
					debugCounter--;
					POOL.add(this);
				}

				this.released = true;
			}
		}

		@Override
		@Deprecated
		public PooledVec3 withOffset(final float x, final float y, final float z) {
			return new PooledVec3(
					this.x + x,
					this.y + y,
					this.z + z
			);
		}

		@Override
		public PooledVec3 addOffset(final float x, final float y, final float z) {
			return (PooledVec3) super.addOffset(x, y, z);
		}

		@Override
		public void close() {
			this.release();
		}

	}

}
