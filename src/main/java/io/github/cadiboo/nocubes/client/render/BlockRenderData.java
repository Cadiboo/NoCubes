package io.github.cadiboo.nocubes.client.render;

import net.minecraft.util.BlockRenderLayer;

public class BlockRenderData {

	private final BlockRenderLayer blockRenderLayer;
	private final int red;
	private final int green;
	private final int blue;
	private final int alpha;
	private final float minU;
	private final float maxU;
	private final float minV;
	private final float maxV;
	private final int lightmapSkyLight;
	private final int lightmapBlockLight;

	public BlockRenderData(final BlockRenderLayer blockRenderLayer, final int red, final int green, final int blue, final int alpha, final float minU, final float maxU, final float minV, final float maxV, final int lightmapSkyLight, final int lightmapBlockLight) {
		this.blockRenderLayer = blockRenderLayer;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		this.minU = minU;
		this.maxU = maxU;
		this.minV = minV;
		this.maxV = maxV;
		this.lightmapSkyLight = lightmapSkyLight;
		this.lightmapBlockLight = lightmapBlockLight;
	}

	public BlockRenderLayer getBlockRenderLayer() {
		return this.blockRenderLayer;
	}

	public int getRed() {
		return this.red;
	}

	public int getGreen() {
		return this.green;
	}

	public int getBlue() {
		return this.blue;
	}

	public int getAlpha() {
		return this.alpha;
	}

	public float getMinU() {
		return this.minU;
	}

	public float getMaxU() {
		return this.maxU;
	}

	public float getMinV() {
		return this.minV;
	}

	public float getMaxV() {
		return this.maxV;
	}

	public int getLightmapSkyLight() {
		return this.lightmapSkyLight;
	}

	public int getLightmapBlockLight() {
		return this.lightmapBlockLight;
	}

}
