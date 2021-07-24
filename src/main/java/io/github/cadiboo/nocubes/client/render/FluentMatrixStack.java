package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

public class FluentMatrixStack implements AutoCloseable {
	public final PoseStack matrix;

	public FluentMatrixStack(PoseStack matrix) {
		this.matrix = matrix;
	}

	public FluentMatrixStack push() {
		matrix.pushPose();
		return this;
	}

	@Override
	public void close() {
		matrix.popPose();
	}
}
