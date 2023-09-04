package io.github.cadiboo.nocubes.util;

/**
 * @author Cadiboo
 */
public final /* inline */ class Face {

	public final Vec v0;
	public final Vec v1;
	public final Vec v2;
	public final Vec v3;

	public Face() {
		this(new Vec(), new Vec(), new Vec(), new Vec());
	}

	public Face(Vec v0, Vec v1, Vec v2, Vec v3) {
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}

	public Face assignNormalTo(Face toUse) {
		Vec v0 = this.v0;
		Vec v1 = this.v1;
		Vec v2 = this.v2;
		Vec v3 = this.v3;
		// mul -1
		Vec.normal(v3, v0, v1, toUse.v0);
		Vec.normal(v0, v1, v2, toUse.v1);
		Vec.normal(v1, v2, v3, toUse.v2);
		Vec.normal(v2, v3, v0, toUse.v3);
		return toUse;
	}

	public Vec assignAverageTo(Vec toUse) {
		Vec v0 = this.v0;
		Vec v1 = this.v1;
		Vec v2 = this.v2;
		Vec v3 = this.v3;
		toUse.x = (v0.x + v1.x + v2.x + v3.x) / 4;
		toUse.y = (v0.y + v1.y + v2.y + v3.y) / 4;
		toUse.z = (v0.z + v1.z + v2.z + v3.z) / 4;
		return toUse;
	}

	public void setValuesFrom(Face other) {
		v0.set(other.v0);
		v1.set(other.v1);
		v2.set(other.v2);
		v3.set(other.v3);
	}

	public Face set(
		float v0x, float v0y, float v0z,
		float v1x, float v1y, float v1z,
		float v2x, float v2y, float v2z,
		float v3x, float v3y, float v3z
	) {
		v0.set(v0x, v0y, v0z);
		v1.set(v1x, v1y, v1z);
		v2.set(v2x, v2y, v2z);
		v3.set(v3x, v3y, v3z);
		return this;
	}

	public Face set(Vec v0, Vec v1, Vec v2, Vec v3) {
		this.v0.set(v0);
		this.v1.set(v1);
		this.v2.set(v2);
		this.v3.set(v3);
		return this;
	}

	public Face add(float value) {
		return add(value, value, value);
	}

	public Face add(float x, float y, float z) {
		v0.add(x, y, z);
		v1.add(x, y, z);
		v2.add(x, y, z);
		v3.add(x, y, z);
		return this;
	}

	public Face subtract(float value) {
		return subtract(value, value, value);
	}

	public Face subtract(float x, float y, float z) {
		v0.subtract(x, y, z);
		v1.subtract(x, y, z);
		v2.subtract(x, y, z);
		v3.subtract(x, y, z);
		return this;
	}

	public Face multiply(float value) {
		v0.multiply(value);
		v1.multiply(value);
		v2.multiply(value);
		v3.multiply(value);
		return this;
	}

	public void flip() {
		set(
			v0.x, v0.y, v0.z,
			v3.x, v3.y, v3.z,
			v2.x, v2.y, v2.z,
			v1.x, v1.y, v1.z
		);
	}
}
