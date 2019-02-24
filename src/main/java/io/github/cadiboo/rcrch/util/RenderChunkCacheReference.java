package io.github.cadiboo.rcrch.util;

import net.minecraft.world.ChunkCache;

/**
 * @author Cadiboo
 */
public final class RenderChunkCacheReference {

	private ChunkCache reference;

	public RenderChunkCacheReference(final ChunkCache reference) {
		this.reference = reference;
	}

	public ChunkCache get() {
		return reference;
	}

	public void set(final ChunkCache reference) {
		this.reference = reference;
	}

}
