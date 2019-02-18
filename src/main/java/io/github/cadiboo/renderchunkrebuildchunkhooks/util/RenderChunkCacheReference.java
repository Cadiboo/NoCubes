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

	public RenderChunkCache getReference() {
		return reference;
	}

	public void setReference(final RenderChunkCache reference) {
		this.reference = reference;
	}

}
