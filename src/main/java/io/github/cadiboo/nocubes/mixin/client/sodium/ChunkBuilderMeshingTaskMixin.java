package io.github.cadiboo.nocubes.mixin.client.sodium;

import io.github.cadiboo.nocubes.hooks.ClientHooks;
import io.github.cadiboo.nocubes.mixin.client.RenderChunkRebuildTaskMixin;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * There is more to this mixin, see the documentation in {@link io.github.cadiboo.nocubes.hooks.MixinAsm}
 * and {@link io.github.cadiboo.nocubes.hooks.MixinAsm#transformChunkRenderer}.
 */
@Pseudo // Sodium may not be installed
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask")
public class ChunkBuilderMeshingTaskMixin {

	/**
	 * @see RenderChunkRebuildTaskMixin#noCubes$getRenderShape
	 */
	@Redirect(
		method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;"
		)
	)
	public RenderShape noCubes$getRenderShape(BlockState state) {
		return ClientHooks.getRenderShape(state);
	}

}
