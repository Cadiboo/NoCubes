package io.github.cadiboo.nocubes.mixin.client;

import io.github.cadiboo.nocubes.hooks.ClientHooks;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRenderBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public abstract class RenderChunkRebuildTaskMixin extends RenderChunkCompileTaskMixin implements INoCubesChunkSectionRenderBuilder {

	@Override
	public ModelData noCubes$getModelData(BlockPos worldPos) {
		return shadow$getModelData(worldPos);
	}

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
	public RenderShape noCubes$getRenderShape(BlockState state) {
		return ClientHooks.getRenderShape(state);
	}

}
