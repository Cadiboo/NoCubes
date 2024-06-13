package io.github.cadiboo.nocubes.config;

import org.beryx.awt.color.ColorFactory;

public final class ColorParser {

	public static Color parse(String color) {
		try {
			final java.awt.Color parsed = ColorFactory.valueOf(color);
			return new Color(parsed.getRed(), parsed.getGreen(), parsed.getBlue(), parsed.getAlpha());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unable to parse color '" + color + "'", e);
		}
	}

	public static class Color {
		public final int red;
		public final int green;
		public final int blue;
		public final int alpha;

		public Color(int red, int green, int blue, int alpha) {
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.alpha = alpha;
		}

		public Color(float red, float green, float blue, float alpha) {
			this((int) (red * 255F), (int) (green * 255F), (int) (blue * 255F), (int) (alpha * 255F));
		}

		public int toRGBA() {
			return red << (8 * 3) | green << (8 * 2) | blue << (8 * 1) | alpha << (8 * 0);
		}

		public int toARGB() {
			return alpha << (8 * 3) | red << (8 * 2) | green << (8 * 1) | blue << (8 * 0);
		}

		public io.github.cadiboo.nocubes.client.render.struct.Color toRenderableColor() {
			return new io.github.cadiboo.nocubes.client.render.struct.Color(red / 255f, green / 255f, blue / 255f, alpha / 255f);
		}

	}
}
