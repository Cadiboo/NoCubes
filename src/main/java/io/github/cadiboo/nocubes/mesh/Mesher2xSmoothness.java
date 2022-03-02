package io.github.cadiboo.nocubes.mesh;

// TODO: package-private
public abstract class Mesher2xSmoothness implements Mesher {

	// TODO: protected
	public final boolean smoothness2x;

	protected Mesher2xSmoothness(boolean smoothness2x) {
		this.smoothness2x = smoothness2x;
	}
}
