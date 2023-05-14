package io.github.cadiboo.nocubes.client.render.struct;

import io.github.cadiboo.nocubes.client.render.LightCache;

public final /* inline record */ class FaceLight {
	public static final FaceLight MAX_BRIGHTNESS = new FaceLight(LightCache.MAX_BRIGHTNESS, LightCache.MAX_BRIGHTNESS, LightCache.MAX_BRIGHTNESS, LightCache.MAX_BRIGHTNESS);
	public /* final */ int v0;
	public /* final */ int v1;
	public /* final */ int v2;
	public /* final */ int v3;

	public FaceLight() {
		this(0, 0, 0, 0);
	}

	public FaceLight(int v0, int v1, int v2, int v3) {
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}
}
