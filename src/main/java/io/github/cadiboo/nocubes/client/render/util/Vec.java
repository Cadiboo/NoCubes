package io.github.cadiboo.nocubes.client.render.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.io.Closeable;

/**
 * @author Cadiboo
 */
public class Vec implements Closeable {

	static final Pool<Vec> POOL = new Pool<>(100);
	public double x;
	public double y;
	public double z;

	Vec(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static Vec of() {
		return of(0, 0, 0);
	}

	public static Vec of(Vec v) {
		return of(v.x, v.y, v.z);
	}

	public static Vec of(double[] v) {
		return of(v[0], v[1], v[2]);
	}

	public static Vec of(double x, double y, double z) {
		Vec pooled = POOL.get();
		if (pooled != null) {
			pooled.x = x;
			pooled.y = y;
			pooled.z = z;
			return pooled;
		}
		return new Vec(x, y, z);
	}

	public static Direction getDirectionFromNormal(Vec normal) {
		double x = normal.x;
		double y = normal.y;
		double z = normal.z;
		final double ax = Math.abs(x);
		final double ay = Math.abs(y);
		final double az = Math.abs(z);
		double max = Math.max(Math.max(ax, ay), az);
		if (max == ax)
			return x > 0 ? Direction.EAST : Direction.WEST;
		else if (max == ay)
			return y > 0 ? Direction.UP : Direction.DOWN;
		else if (max == az)
			return z > 0 ? Direction.SOUTH : Direction.NORTH;
		else
			throw new IllegalStateException("Could not find a direction from the normal, wtf???");
	}

	public Vec add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vec add(Vec vec) {
		return add(vec.x, vec.y, vec.z);
	}

	public Vec subtract(double x, double y, double z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	public Vec subtract(double d) {
		return subtract(d, d, d);
	}

	public Vec subtract(Vec vec) {
		return subtract(vec.x, vec.y, vec.z);
	}

	public Vec multiply(double x, double y, double z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}

	public Vec multiply(double d) {
		return multiply(d, d, d);
	}

	public Vec multiply(Vec vec) {
		return multiply(vec.x, vec.y, vec.z);
	}

	public Vec normalise() {
		double length = MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
		if (length < 0.0001)
			// Zero vector, everything is already zero
			return this;
		this.x /= length;
		this.y /= length;
		this.z /= length;
		return this;
	}

	public static Vec normal(Vec prevVecInFace, Vec vec, Vec nextVecInFace) {
		return normal(prevVecInFace, vec, nextVecInFace, Vec.of());
	}

	public static Vec normal(Vec prevVecInFace, Vec vec, Vec nextVecInFace, Vec toUse) {
		toUse.x = prevVecInFace.x - vec.x;
		toUse.x = prevVecInFace.y - vec.y;
		toUse.x = prevVecInFace.z - vec.z;
		toUse.cross(
			nextVecInFace.x - vec.x,
			nextVecInFace.y - vec.y,
			nextVecInFace.z - vec.z,
			toUse
		);
		return toUse.normalise();
	}

	/**
	 * Copied from {@link net.minecraft.util.math.vector.Vector4f#transform(Matrix4f)}
	 */
	public void transform(Matrix4f matrixIn) {
		double x = this.x;
		double y = this.y;
		double z = this.z;
//		double w = this.w;
		double w = 1F;
		this.x = matrixIn.m00 * x + matrixIn.m01 * y + matrixIn.m02 * z + matrixIn.m03 * w;
		this.y = matrixIn.m10 * x + matrixIn.m11 * y + matrixIn.m12 * z + matrixIn.m13 * w;
		this.z = matrixIn.m20 * x + matrixIn.m21 * y + matrixIn.m22 * z + matrixIn.m23 * w;
//		this.w = matrixIn.m30 * x + matrixIn.m31 * y + matrixIn.m32 * z + matrixIn.m33 * w;
	}

	public Vec cross(Vec vec) {
		return cross(vec, Vec.of());
	}

	public Vec cross(Vec vec, Vec toUse) {
		return cross(vec.x, vec.y, vec.z, toUse);
	}

	public Vec cross(double ox, double oz, double oy, Vec toUse) {
		final double tx = this.x;
		final double ty = this.y;
		final double tz = this.z;
		toUse.x = ty * oz - tz * oy;
		toUse.y = tz * ox - tx * oz;
		toUse.z = tx * oy - ty * ox;
		return toUse;
	}

	@Override
	public void close() {
		POOL.offer(this);
	}

}
