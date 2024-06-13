package io.github.cadiboo.nocubes.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$ChunkCompileTask")
public abstract class RenderChunkCompileTaskMixin {

	@Shadow
	public abstract net.minecraftforge.client.model.data.ModelData shadow$getModelData(net.minecraft.core.BlockPos pos);

}
