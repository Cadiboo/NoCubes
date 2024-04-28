package io.github.cadiboo.nocubes.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.cadiboo.nocubes.client.render.OverlayRenderers;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

	@Inject(
		method = "renderHitOutline",
		at = @At("HEAD"),
		cancellable = true
	)
	public void noCubes$renderHitOutline(
		PoseStack matrix, VertexConsumer buffer,
		Entity cameraEntity,
		double cameraX, double cameraY, double cameraZ,
		BlockPos pos, BlockState state,
		CallbackInfo ci
	)
	{
		var world = cameraEntity == null ? null : cameraEntity.level();
		if (world == null)
			return;
		if (OverlayRenderers.renderNoCubesBlockHighlight(
			matrix, buffer,
			cameraX, cameraY, cameraZ,
			world, pos, state
		))
			ci.cancel();
	}

	@Inject(
		method = "renderLevel",
		at = @At("TAIL")
	)
	public void noCubes$renderOverlays(
		PoseStack poseStack,
		float partialTicks, long l, boolean shouldRenderBlockOutline,
		Camera camera, GameRenderer gameRenderer,
		LightTexture lightTexture, Matrix4f matrix4f,
		CallbackInfo ci
	) {
		// Bad, fix me
		OverlayRenderers.register(perFrameAction -> perFrameAction.accept(poseStack));
	}
}
