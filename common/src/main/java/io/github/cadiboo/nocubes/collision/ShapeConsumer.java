package io.github.cadiboo.nocubes.collision;

public interface ShapeConsumer {

	/**
	 * Return if more shapes should be generated.
	 */
	boolean accept(
		double x0, double y0, double z0,
		double x1, double y1, double z1
	);

	static boolean acceptFullCube(double x, double y, double z, ShapeConsumer consumer) {
		return consumer.accept(
			x, y, z,
			x + 1, y + 1, z + 1
		);
	}

}
