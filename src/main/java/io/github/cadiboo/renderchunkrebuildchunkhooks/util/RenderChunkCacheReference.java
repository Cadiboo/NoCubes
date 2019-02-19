package io.github.cadiboo.renderchunkrebuildchunkhooks.util;

import net.minecraft.client.renderer.chunk.RenderChunkCache;

/**
 * @author Cadiboo
 */
public class RenderChunkCacheReference {

	private RenderChunkCache reference;

	public RenderChunkCacheReference(final RenderChunkCache reference) {
		this.reference = reference;
	}

	public RenderChunkCache get() {
		return reference;
	}

	public void set(final RenderChunkCache reference) {
		this.reference = reference;
	}

}
