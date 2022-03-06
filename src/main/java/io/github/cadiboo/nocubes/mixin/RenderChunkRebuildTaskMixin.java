package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkRenderDispatcher.RenderChunk.RebuildTask.class)
public class RenderChunkRebuildTaskMixin {

	/**
	 * Allows us to disable vanilla rendering for blocks we will render
	 * ourselves in {@link io.github.cadiboo.nocubes.client.render.MeshRenderer}
	 */
	@Redirect(
		method = "compile",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;"
		)
	)
	public RenderShape nocubes_getRenderShape(BlockState state) {
		// Invisible blocks are not rendered by vanilla
		return Hooks.allowVanillaRenderingFor(state) ? state.getRenderShape() : RenderShape.INVISIBLE;
	}
}
