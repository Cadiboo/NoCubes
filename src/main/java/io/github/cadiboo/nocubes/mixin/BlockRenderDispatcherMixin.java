package io.github.cadiboo.nocubes.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.client.render.RendererDispatcher;
import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRendererDispatcher.class)
public class BlockRenderDispatcherMixin {

	/**
	 * Renders our own smoothed cracking/breaking/damage animation.
	 */
	@Inject(
		method = "renderBlockDamage",
		at = @At("HEAD"),
		remap = false, // Forge-added method
		cancellable = true
	)
	private void nocubes_renderBreakingTexture(BlockState state, BlockPos pos, IBlockDisplayReader world, MatrixStack matrix, IVertexBuilder buffer, IModelData modelData, CallbackInfo ci) {
		if (Hooks.renderingEnabledFor(state)) {
			RendererDispatcher.renderBreakingTexture((BlockRendererDispatcher) (Object) this, state, pos, world, matrix, buffer, modelData);
			ci.cancel();
		}
	}

}
