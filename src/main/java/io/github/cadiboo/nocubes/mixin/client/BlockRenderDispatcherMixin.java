package io.github.cadiboo.nocubes.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.cadiboo.nocubes.client.render.LightCache;
import io.github.cadiboo.nocubes.client.render.MeshRenderer;
import io.github.cadiboo.nocubes.client.render.VanillaRenderer;
import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.client.render.struct.FaceLight;
import io.github.cadiboo.nocubes.client.render.struct.Texture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderDispatcher.class)
public class BlockRenderDispatcherMixin {

	@Shadow
	@Final
	private RandomSource random;

	/**
	 * Renders our own smoothed cracking/breaking/damage animation.
	 */
	@Inject(
		method = "renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/neoforged/neoforge/client/model/data/ModelData;)V",
		at = @At("HEAD"),
		remap = false, // Forge-added method
		cancellable = true
	)
	private void noCubes$renderBreakingTexture(BlockState state, BlockPos pos, BlockAndTintGetter world, PoseStack matrix, VertexConsumer buffer, ModelData modelData, CallbackInfo ci) {
		var dispatcher = ((BlockRenderDispatcher) (Object) this);
		var cancel = MeshRenderer.renderSingleBlock(world, pos, state, new MeshRenderer.INoCubesAreaRenderer() {
			@Override
			public void quad(BlockState state, BlockPos worldPos, MeshRenderer.FaceInfo faceInfo, boolean renderBothSides, Color colorOverride, LightCache lightCache, float shade) {
				VanillaRenderer.renderQuad(
					buffer, matrix,
					faceInfo,
					Color.WHITE, Texture.EVERYTHING, FaceLight.MAX_BRIGHTNESS,
					renderBothSides
				);
			}
			@Override
			public void block(BlockState state, BlockPos worldPos, float relativeX, float relativeY, float relativeZ) {
				matrix.pushPose();
				try {
					matrix.translate(relativeX, relativeY, relativeX);
					var blockModelShaper = dispatcher.getBlockModelShaper();
					var modelRenderer = dispatcher.getModelRenderer();

					var model = blockModelShaper.getBlockModel(state);
					var seed = state.getSeed(worldPos);

					modelRenderer.tesselateBlock(
						world, model,
						state, pos,
						matrix, buffer,
						true, random, seed,
						OverlayTexture.NO_OVERLAY, modelData, null
					);
				} finally {
					matrix.popPose();
				}
			}
		});
		if (cancel) {
			ci.cancel();
		}
	}

}
