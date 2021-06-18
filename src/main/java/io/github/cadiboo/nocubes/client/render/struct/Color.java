package io.github.cadiboo.nocubes.client.render.struct;

public final /* inline record */ class Color {
	public static final Color WHITE = new Color(1F, 1F, 1F, 1F);
	public static final ThreadLocal<Color> VALHALLA_SOON_PLS = ThreadLocal.withInitial(Color::new);

	public /* final */ float red;
	public /* final */ float green;
	public /* final */ float blue;
	public final float alpha = 1.0F;

	public Color() {
		this(0, 0, 0, 0);
	}

	public Color(float red, float green, float blue, float alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
//		this.alpha = alpha;
	}

	public Color multiply(float shading) {
		red *= shading;
		green *= shading;
		blue *= shading;
		return this;
	}

	public void unpackFromARGB(int color) {
		// alpha = (color >> 24 & 0xFF) / 255.0F;
		red = (color >> 16 & 0xFF) / 255.0F;
		green = (color >> 8 & 0xFF) / 255.0F;
		blue = (color >> 0 & 0xFF) / 255.0F;
	}
}
