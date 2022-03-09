package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

public class FluentMatrixStack implements AutoCloseable {

	public final MatrixStack matrix;

	public MatrixStack matrix() {
		return matrix;
	}

	public FluentMatrixStack(MatrixStack matrix) {
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
