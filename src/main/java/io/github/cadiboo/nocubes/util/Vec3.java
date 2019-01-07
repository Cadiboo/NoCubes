package io.github.cadiboo.nocubes.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * Simple 3d vector/point with some util methods
 * @author Cadiboo
 */
public class Vec3 {

	public double x;

	public double y;

	public double z;

	public Vec3() {
		this(0, 0, 0);
	}

	public Vec3(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3(final float[] vertexAsFloat) {
		this(
				vertexAsFloat[0],
				vertexAsFloat[1],
				vertexAsFloat[2]
		);
	}

	public Vec3 offset(final double x, final double y, final double z) {
		return new Vec3(
				this.x + x,
				this.y + y,
				this.z + z
		);
	}

	public Vec3 move(final double x, final double y, final double z) {
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

}
