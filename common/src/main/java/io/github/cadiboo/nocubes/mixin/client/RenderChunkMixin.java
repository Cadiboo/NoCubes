package io.github.cadiboo.nocubes.mixin.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRender;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class RenderChunkMixin implements INoCubesChunkSectionRender {

	@Shadow
	abstract void shadow$beginLayer(BufferBuilder buffer);

	@Override
	public void noCubes$beginLayer(BufferBuilder buffer) {
		shadow$beginLayer(buffer);
	}
}
