package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

public record FluentMatrixStack(PoseStack matrix) implements AutoCloseable {

	public FluentMatrixStack push() {
		matrix.pushPose();
		return this;
	}

	@Override
	public void close() {
		matrix.popPose();
	}
}
