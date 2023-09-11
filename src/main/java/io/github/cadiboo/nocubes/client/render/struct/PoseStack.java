package io.github.cadiboo.nocubes.client.render.struct;

/**
 * @author Cadiboo
 */
public class PoseStack {

	public double x;
	public double y;
	public double z;

	public void translate(double x, double y, double z) {
		if (this.x != 0 || this.y != 0 || this.z != 0)
			throw new IllegalStateException("PoseStack has aleady been translated");
		this.x = x;
		this.y = y;
		this.z = z;
	}

}
