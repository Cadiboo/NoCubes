package io.github.cadiboo.nocubes.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * @author Cadiboo
 */
public final /* inline */ class Vec {

	public /* final */ float x;
	public /* final */ float y;
	public /* final */ float z;

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

	public EnumFacing getDirectionFromNormal() {
		float x = this.x;
		// Adding a tiny amount solves conflicts caused by floating point errors for faces that are perfectly diagonal
		float y = this.y + 0.0002F;
		float z = this.z + 0.0001F;
		float ax = Math.abs(x);
		float ay = Math.abs(y);
		float az = Math.abs(z);
		float max = Math.max(Math.max(ax, ay), az);
		if (max == ax)
			return x > 0 ? EnumFacing.EAST : EnumFacing.WEST;
		else if (max == az)
			return z > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
		else if (max == ay)
			return y > 0 ? EnumFacing.UP : EnumFacing.DOWN;
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

	public static Vec normal(Vec prevVecInFace, Vec vec, Vec nextVecInFace, Vec toUse) {
//		normal = crossProduct(prev - vec, next - vec).normalise();
		final float x = vec.x;
		final float y = vec.y;
		final float z = vec.z;
		return cross(
			prevVecInFace.x - x, prevVecInFace.y - y, prevVecInFace.z - z,
			nextVecInFace.x - x, nextVecInFace.y - y, nextVecInFace.z - z,
			toUse
		);//.normalise();
//		).normalise().multiply(-1);
	}

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

	public Vec interpolate(float t, Vec v0, Vec v1) {
		this.x = v0.x + t * (v1.x - v0.x);
		this.y = v0.y + t * (v1.y - v0.y);
		this.z = v0.z + t * (v1.z - v0.z);
		return this;
	}

	public BlockPos.MutableBlockPos assignTo(BlockPos.MutableBlockPos pos) {
		return pos.setPos(x, y, z);
	}

}
