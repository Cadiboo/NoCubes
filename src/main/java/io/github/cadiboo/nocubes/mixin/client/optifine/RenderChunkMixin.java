package io.github.cadiboo.nocubes.mixin.client.optifine;

import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRenderOptiFine;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(SectionRenderDispatcher.RenderSection.class)
public class RenderChunkMixin implements INoCubesChunkSectionRenderOptiFine {

	@Dynamic("Added by OptiFine")
	@Shadow
	private int regionDX;
	@Dynamic("Added by OptiFine")
	@Shadow
	private int regionDY;
	@Dynamic("Added by OptiFine")
	@Shadow
	private int regionDZ;


	@Override
	public int noCubes$regionDX() {
		return regionDX;
	}
	@Override
	public int noCubes$regionDY() {
		return regionDY;
	}
	@Override
	public int noCubes$regionDZ() {
		return regionDZ;
	}

	@Dynamic("Added by OptiFine")
	@Shadow
	private void shadow$postRenderOverlays(SectionBufferBuilderPack builder, Set<RenderType> usedLayers) {
	}

	@Override
	public void noCubes$postRenderOverlays(SectionBufferBuilderPack builder, Set<RenderType> usedLayers) {
		shadow$postRenderOverlays(builder, usedLayers);
	}
}
