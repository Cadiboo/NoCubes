package io.github.cadiboo.nocubes.util;

//import net.minecraft.util.math.vector.Matrix4f;
import javax.vecmath.Matrix4f;

/**
 * @author Cadiboo
 */
public class Face {

	public Vec v0;
	public Vec v1;
	public Vec v2;
	public Vec v3;

	public Face(final Vec v0, final Vec v1, final Vec v2, final Vec v3) {
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}

	public void assignNormalTo(Face toUse) {
		final Vec v0 = this.v0;
		final Vec v1 = this.v1;
		final Vec v2 = this.v2;
		final Vec v3 = this.v3;
		// mul -1
		Vec.normal(v3, v0, v1, toUse.v0);
		Vec.normal(v0, v1, v2, toUse.v1);
		Vec.normal(v1, v2, v3, toUse.v2);
		Vec.normal(v2, v3, v0, toUse.v3);
	}

	public void assignAverageTo(Vec toUse) {
		Vec v0 = this.v0;
		Vec v1 = this.v1;
		Vec v2 = this.v2;
		Vec v3 = this.v3;
		toUse.x = (v0.x + v1.x + v2.x + v3.x) / 4;
		toUse.y = (v0.y + v1.y + v2.y + v3.y) / 4;
		toUse.z = (v0.z + v1.z + v2.z + v3.z) / 4;
	}

	public void multiply(double d) {
		v0.multiply(d);
		v1.multiply(d);
		v2.multiply(d);
		v3.multiply(d);
	}

	public void transform(Matrix4f matrix) {
		v0.transform(matrix);
		v1.transform(matrix);
		v2.transform(matrix);
		v3.transform(matrix);
	}

}
