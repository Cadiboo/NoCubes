package io.github.cadiboo.nocubes.util.pooled;

import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * Simple pooled 3d vector/point with some util methods
 *
 * @author Cadiboo
 */
public final class Vec3 implements AutoCloseable {

	private static final ArrayList<Vec3> POOL = new ArrayList<>();

	public double x;
	public double y;
	public double z;

	private Vec3(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Nonnull
	public static Vec3 retain(final double x, final double y, final double z) {
		synchronized (POOL) {
			if (!POOL.isEmpty()) {
				Vec3 pooled = POOL.remove(POOL.size() - 1);
				if (pooled != null) {
					pooled.x = x;
					pooled.y = y;
					pooled.z = z;
					return pooled;
				}
			}
		}
		return new Vec3(x, y, z);
	}

	@Nonnull
	public static Vec3 retain(final double[] vertexAsArray) {
		return retain(
				vertexAsArray[0],
				vertexAsArray[1],
				vertexAsArray[2]
		);
	}

	@Nonnull
	public static Vec3 retain(final float[] vertexAsArray) {
		return retain(
				vertexAsArray[0],
				vertexAsArray[1],
				vertexAsArray[2]
		);
	}

	public Vec3 addOffset(final double x, final double y, final double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vec3 copy() {
		return new Vec3(
				this.x,
				this.y,
				this.z
		);
	}

	@Override
	public void close() {
		synchronized (POOL) {
//			if (POOL.size() < (ModConfig.enableCollisions ? 2_000_000 : 60_000)) {
			{
				POOL.add(this);
			}
		}
	}

	public Vec3 multiply(double x, double y, double z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}

	public Vec3 multiply(double d) {
		return multiply(d, d, d);
	}

	public Vec3 normalise() {
		double length = MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
		if (length < 0.0001)
			// Zero vector, everything is already zero
			return this;
		this.x /= length;
		this.y /= length;
		this.z /= length;
		return this;
	}

	/**
	 * @return toUse
	 */
	public static Vec3 normal(Vec3 prevVecInFace, Vec3 vec, Vec3 nextVecInFace, Vec3 toUse) {
//		normal = crossProduct(prev - vec, next - vec).normalise();
		final double x = vec.x;
		final double y = vec.y;
		final double z = vec.z;
		return cross(
				prevVecInFace.x - x, prevVecInFace.y - y, prevVecInFace.z - z,
				nextVecInFace.x - x, nextVecInFace.y - y, nextVecInFace.z - z,
				toUse
		).normalise();
//		).normalise().multiply(-1);
	}

	/**
	 * @return toUse
	 */
	public static Vec3 cross(
			double x0, double y0, double z0,
			double x1, double y1, double z1,
			Vec3 toUse
	) {
		toUse.x = y0 * z1 - z0 * y1;
		toUse.y = z0 * x1 - x0 * z1;
		toUse.z = x0 * y1 - y0 * x1;
		return toUse;
	}

}
