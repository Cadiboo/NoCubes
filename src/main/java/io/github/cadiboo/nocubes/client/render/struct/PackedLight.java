package io.github.cadiboo.nocubes.client.render.struct;

import io.github.cadiboo.nocubes.client.render.LightCache;

public final /* inline record */ class PackedLight {
	public static final PackedLight MAX_BRIGHTNESS = new PackedLight(LightCache.MAX_BRIGHTNESS, LightCache.MAX_BRIGHTNESS, LightCache.MAX_BRIGHTNESS, LightCache.MAX_BRIGHTNESS);
	public int v0;
	public int v1;
	public int v2;
	public int v3;

	public PackedLight(int v0, int v1, int v2, int v3) {
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}
}
