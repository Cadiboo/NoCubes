package io.github.cadiboo.nocubes.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;

/**
 * @author Cadiboo
 */
public /* inline */ class Vec {

	public float x;
	public float y;
	public float z;

	public Vec() {
	}

	public Vec(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec(Vec v) {
		this(v.x, v.y, v.z);
	}

	public Vec set(Vec v) {
		return set(v.x, v.y, v.z);
	}

	public Vec set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Direction getDirectionFromNormal() {
		float x = this.x;
		float y = this.y;
		float z = this.z;
		float ax = Math.abs(x);
		float ay = Math.abs(y);
		float az = Math.abs(z);
		float max = Math.max(Math.max(ax, ay), az);
		if (max == ax)
			return x > 0 ? Direction.EAST : Direction.WEST;
		else if (max == az)
			return z > 0 ? Direction.SOUTH : Direction.NORTH;
		else if (max == ay)
			return y > 0 ? Direction.UP : Direction.DOWN;
		else
			throw new IllegalStateException("Could not find a direction from the normal, wtf???");
	}

	public Vec add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vec add(Vec vec) {
		return add(vec.x, vec.y, vec.z);
	}

	public Vec subtract(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	public Vec subtract(float d) {
		return subtract(d, d, d);
	}

	public Vec subtract(Vec vec) {
		return subtract(vec.x, vec.y, vec.z);
	}

	public Vec multiply(float x, float y, float z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}

	public Vec multiply(float d) {
		return multiply(d, d, d);
	}

	public Vec multiply(Vec vec) {
		return multiply(vec.x, vec.y, vec.z);
	}

	public Vec normalise() {
		float length = MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
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
	public static Vec normal(Vec prevVecInFace, Vec vec, Vec nextVecInFace, Vec toUse) {
//		normal = crossProduct(prev - vec, next - vec).normalise();
		final float x = vec.x;
		final float y = vec.y;
		final float z = vec.z;
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
	public static Vec cross(
		float x0, float y0, float z0,
		float x1, float y1, float z1,
		Vec toUse
	) {
		toUse.x = y0 * z1 - z0 * y1;
		toUse.y = z0 * x1 - x0 * z1;
		toUse.z = x0 * y1 - y0 * x1;
		return toUse;
	}

	/**
	 * Copied from {@link net.minecraft.util.math.vector.Vector4f#transform(Matrix4f)}
	 */
	public void transform(Matrix4f matrixIn) {
		float x = this.x;
		float y = this.y;
		float z = this.z;
//		float w = this.w;
		float w = 1F;
		this.x = matrixIn.m00 * x + matrixIn.m01 * y + matrixIn.m02 * z + matrixIn.m03 * w;
		this.y = matrixIn.m10 * x + matrixIn.m11 * y + matrixIn.m12 * z + matrixIn.m13 * w;
		this.z = matrixIn.m20 * x + matrixIn.m21 * y + matrixIn.m22 * z + matrixIn.m23 * w;
//		this.w = matrixIn.m30 * x + matrixIn.m31 * y + matrixIn.m32 * z + matrixIn.m33 * w;
	}

	public Vec copy() {
		return new Vec(x, y, z);
	}

	public void copyFrom(float[] floats) {
		this.x = floats[0];
		this.y = floats[1];
		this.z = floats[2];
	}

	public void copyFrom(Vec vec) {
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;
	}

	public Vec interpolate(float t, Vec v0, Vec v1) {
		this.x = v0.x + t * (v1.x - v0.x);
		this.y = v0.y + t * (v1.y - v0.y);
		this.z = v0.z + t * (v1.z - v0.z);
		return this;
	}

}
