package io.github.cadiboo.nocubes.hooks.trait;

import net.minecraft.core.BlockPos;

/**
 * Adds extra functionality to {@link net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk.RebuildTask}.
 * Implemented by {@link io.github.cadiboo.nocubes.mixin.client.RenderChunkRebuildTaskMixin}.
 */
public interface INoCubesChunkSectionRenderBuilder {
	/**
	 * ModelData only exists on Forge, so we use Object as the type here instead.
	 */
	Object noCubes$getModelData(BlockPos worldPos);
}
