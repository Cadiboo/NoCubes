package io.github.cadiboo.nocubes.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * Simple 3d vector/point with some util methods
 *
 * @author Cadiboo
 */
public class Vec3 implements Cloneable {

	public float x;
	public float y;
	public float z;

	private Vec3() {
		this(0, 0, 0);
	}

	private Vec3(final float x, final float y, final float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	private Vec3(final float[] vertexAsArray) {
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

	private BlockPos toBlockPos() {
		return new BlockPos(this.x, this.y, this.z);
	}

	public AxisAlignedBB toAxisAlignedBB() {
		return new AxisAlignedBB(this.toBlockPos());
	}

	@Nonnull
	public static Vec3 retain(final float x, final float y, final float z) {
		// STOPSHIP: 2019-02-13 FIXME TODO POOLED VECS
		return new Vec3(x, y, z);
	}

	@Nonnull
	public static Vec3 retain(final float[] vertexAsArray) {
		return retain(
				vertexAsArray[0],
				vertexAsArray[1],
				vertexAsArray[2]
		);
	}

	public void release() {
	}

	@Override
	protected Vec3 clone() {
		return new Vec3(
				this.x,
				this.y,
				this.z
		);
	}

}
