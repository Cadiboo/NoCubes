package io.github.cadiboo.nocubes.mixin.client;

import net.minecraft.core.BlockPos;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderChunk$ChunkCompileTask")
public abstract class RenderChunkCompileTaskMixin {

	@Shadow
	public abstract ModelData shadow$getModelData(BlockPos pos);

}
