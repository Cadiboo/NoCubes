package io.github.cadiboo.nocubes.util;

/**
 * @author Cadiboo
 */
public class Vec3 {

	public double xCoord;
	public double yCoord;
	public double zCoord;

	public Vec3(final double xCoord, final double yCoord, final double zCoord) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}

	public Vec3 offset(final double offsetX, final double offsetY, final double offsetZ) {
		return new Vec3(
				this.xCoord + offsetX,
				this.yCoord + offsetY,
				this.zCoord + offsetZ
		);
	}

}
