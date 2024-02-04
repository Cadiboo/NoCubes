package io.github.cadiboo.nocubes.mixin.client.optifine;

import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRenderOptiFine;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk.class)
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
	private void shadow$postRenderOverlays(ChunkBufferBuilderPack builder, Set<RenderType> usedLayers) {
	}

	@Override
	public void noCubes$postRenderOverlays(ChunkBufferBuilderPack builder, Set<RenderType> usedLayers) {
		shadow$postRenderOverlays(builder, usedLayers);
	}
}
