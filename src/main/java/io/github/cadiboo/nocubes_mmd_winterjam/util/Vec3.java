package io.github.cadiboo.nocubes_mmd_winterjam.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

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

	public BlockPos toBlockPos() {
		return new BlockPos(this.xCoord, this.yCoord, this.zCoord);
	}

	public AxisAlignedBB toAxisAlignedBB() {
		return new AxisAlignedBB(this.toBlockPos());
	}

}
