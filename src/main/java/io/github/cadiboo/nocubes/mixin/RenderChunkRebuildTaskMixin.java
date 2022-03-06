package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
		),
		require = 1,
		allow = 1
	)
	public RenderShape getRenderShape(BlockState state) {
		// Invisible blocks are not rendered by vanilla
		return allowVanillaRenderingFor(state) ? state.getRenderShape() : RenderShape.INVISIBLE;
	}

	/**
	 * Disables vanilla rendering for smoothable BlockStates.
	 * Also disables vanilla's rendering for plans (grass, flowers) so that
	 * we can make them render at the proper height in the smooth ground
	 */
	@Unique
	private static boolean allowVanillaRenderingFor(BlockState state) {
		if (!NoCubesConfig.Client.render)
			return true;
		if (!NoCubes.smoothableHandler.isSmoothable(state)) {
			if (!NoCubesConfig.Client.fixPlantHeight)
				return true;
			if (!ModUtil.isShortPlant(state))
				return true;
			return false;
		}
		return false;
	}
}
