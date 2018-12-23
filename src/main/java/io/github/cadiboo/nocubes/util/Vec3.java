package io.github.cadiboo.nocubes.util;

/**
 * @author Cadiboo
 */
public class Vec3 {

	public double xCoord;
	public double yCoord;
	public double zCoord;

	public Vec3() {
		this(0, 0, 0);
	}

	public Vec3(final double x, final double y, final double z) {
		this.xCoord = x;
		this.yCoord = y;
		this.zCoord = z;
	}

	public Vec3 offset(final double x, final double y, final double z) {
		return new Vec3(
				this.xCoord + x,
				this.yCoord + y,
				this.zCoord + z
		);
	}

}
