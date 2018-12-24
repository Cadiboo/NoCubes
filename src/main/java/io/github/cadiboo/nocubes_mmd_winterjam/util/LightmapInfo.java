package io.github.cadiboo.nocubes_mmd_winterjam.util;

public class LightmapInfo {

	private final int lightmapSkyLight;
	private final int lightmapBlockLight;

	public LightmapInfo(final int lightmapSkyLight, final int lightmapBlockLight) {
		this.lightmapSkyLight = lightmapSkyLight;
		this.lightmapBlockLight = lightmapBlockLight;
	}

	public int getLightmapSkyLight() {
		return lightmapSkyLight;
	}

	public int getLightmapBlockLight() {
		return lightmapBlockLight;
	}

}
