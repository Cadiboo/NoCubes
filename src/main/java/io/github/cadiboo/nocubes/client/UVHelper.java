package io.github.cadiboo.nocubes.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Utility class to help with sprite texture UVs.
 * 1-pixel-wide transparent lines between textures occur when sprites are rendered
 * next to each other. The methods in this class adds/subtracts a very small number
 * from the U/V of the sprite to fix this problem.
 *
 * @author Cadiboo
 */
public final class UVHelper {

	// Add or subtract from the sprites UV location to remove transparent lines in between textures
	public static final float UV_CORRECT = 1 / 10000F;

	/**
	 * Gets the corrected minimum U coordinate to use when rendering the sprite.
	 *
	 * @param sprite the sprite
	 * @return The corrected minimum U coordinate to use when rendering the sprite
	 */
	public static float getMinU(final TextureAtlasSprite sprite) {
		return sprite.getMinU() + UV_CORRECT;
	}

	/**
	 * Gets the corrected maximum U coordinate to use when rendering the sprite.
	 *
	 * @param sprite the sprite
	 * @return The corrected maximum U coordinate to use when rendering the sprite
	 */
	public static float getMaxU(final TextureAtlasSprite sprite) {
		return sprite.getMaxU() - UV_CORRECT;
	}

	/**
	 * Gets the corrected minimum V coordinate to use when rendering the sprite.
	 *
	 * @param sprite the sprite
	 * @return The corrected minimum V coordinate to use when rendering the sprite
	 */
	public static float getMinV(final TextureAtlasSprite sprite) {
		return sprite.getMinV() + UV_CORRECT;
	}

	/**
	 * Gets the corrected maximum V coordinate to use when rendering the sprite.
	 *
	 * @param sprite the sprite
	 * @return The corrected maximum V coordinate to use when rendering the sprite
	 */
	public static float getMaxV(final TextureAtlasSprite sprite) {
		return sprite.getMaxV() - UV_CORRECT;
	}

	/**
	 * Clamps the given u value between the corrected minU and the corrected maxU
	 *
	 * @param u      the u value to clamp
	 * @param sprite the sprite
	 * @return The corrected U coordinate to use when rendering the sprite
	 */
	public static float clampU(final float u, final TextureAtlasSprite sprite) {
		float min = getMinU(sprite);
		float max = getMaxU(sprite);
		if (u < min)
			return min;
		else
			return Math.min(u, max);
	}

	/**
	 * Clamps the given v value between the corrected minV and the corrected maxV
	 *
	 * @param v      the v value to clamp
	 * @param sprite the sprite
	 * @return The corrected V coordinate to use when rendering the sprite
	 */
	public static float clampV(final float v, final TextureAtlasSprite sprite) {
		float min = getMinV(sprite);
		float max = getMaxV(sprite);
		if (v < min)
			return min;
		else
			return Math.min(v, max);
	}

}
