package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

public record FluentMatrixStack(MatrixStack matrix) implements AutoCloseable {

	public FluentMatrixStack push() {
		matrix.pushPose();
		return this;
	}

	@Override
	public void close() {
		matrix.popPose();
	}
}
