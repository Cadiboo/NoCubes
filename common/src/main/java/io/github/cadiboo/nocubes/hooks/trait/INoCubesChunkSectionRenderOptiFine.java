package io.github.cadiboo.nocubes.hooks.trait;

import io.github.cadiboo.nocubes.mixin.client.optifine.RenderChunkMixin;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;

import java.util.Set;

/**
 * Adds extra functionality to {@link net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection.RebuildTask}.
 * Implemented by {@link RenderChunkMixin}.
 */
public interface INoCubesChunkSectionRenderOptiFine {
	int noCubes$regionDX();
	int noCubes$regionDY();
	int noCubes$regionDZ();

	void noCubes$postRenderOverlays(SectionBufferBuilderPack builder, Set<RenderType> usedLayers);
}
