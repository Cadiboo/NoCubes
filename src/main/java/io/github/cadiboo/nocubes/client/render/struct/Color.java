package io.github.cadiboo.nocubes.client.render.struct;

public final /* inline record */ class Color {
	public float red;
	public float green;
	public float blue;
	public final float alpha = 1.0F;

	public void multiply(float shading) {
		red *= shading;
		green *= shading;
		blue *= shading;
	}

	public void unpackFromARGB(int color) {
		// alpha = (color >> 24 & 0xFF) / 255.0F;
		red = (color >> 16 & 0xFF) / 255.0F;
		green = (color >> 8 & 0xFF) / 255.0F;
		blue = (color >> 0 & 0xFF) / 255.0F;
	}
}
