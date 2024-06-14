package io.github.cadiboo.nocubes.hooks.trait;

import net.minecraft.core.BlockPos;
import net.minecraftforge.client.model.data.ModelData;

/**
 * Adds extra functionality to {@link net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk.RebuildTask}.
 * Implemented by {@link io.github.cadiboo.nocubes.mixin.client.RenderChunkRebuildTaskMixin}.
 */
public interface INoCubesChunkSectionRenderBuilder {
	ModelData noCubes$getModelData(BlockPos worldPos);
}
