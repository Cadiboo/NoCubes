package io.github.cadiboo.nocubes.hooks.trait;

import io.github.cadiboo.nocubes.mixin.client.optifine.RenderChunkMixin;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;

import java.util.Set;

/**
 * Adds extra functionality to {@link net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk.RebuildTask}.
 * Implemented by {@link RenderChunkMixin}.
 */
public interface INoCubesChunkSectionRenderOptiFine {
	int noCubes$regionDX();
	int noCubes$regionDY();
	int noCubes$regionDZ();

	void noCubes$postRenderOverlays(ChunkBufferBuilderPack builder, Set<RenderType> usedLayers);
}
