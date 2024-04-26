package io.github.cadiboo.nocubes.hooks.trait;

import com.mojang.blaze3d.vertex.BufferBuilder;

/**
 * Adds extra functionality to {@link net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection}.
 * Implemented by {@link io.github.cadiboo.nocubes.mixin.client.RenderChunkMixin}.
 */
public interface INoCubesChunkSectionRender {
	void noCubes$beginLayer(BufferBuilder buffer);
}
