package io.github.cadiboo.nocubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import io.github.cadiboo.nocubes.config.ColorParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;

public final class ClientUtil {

	public static void reloadAllChunks() {
		var minecraft = Minecraft.getInstance();
		minecraft.execute(() -> {
			var worldRenderer = minecraft.levelRenderer;
			if (worldRenderer != null)
				worldRenderer.allChanged();
		});
	}

	public static void lineVertex(VertexConsumer buffer, PoseStack stack, float x, float y, float z, ColorParser.Color color) {
		var matrix = stack.last().pose();
		// Calling 'buffer.vertex(matrix, x, y, z)' allocates a Vector4f
		// To avoid allocating so many short lived vectors we do the transform ourselves instead
		var transformedX = getTransformX(matrix, x, y, z, 1);
		var transformedY = getTransformY(matrix, x, y, z, 1);
		var transformedZ = getTransformZ(matrix, x, y, z, 1);
		buffer
			.vertex(transformedX, transformedY, transformedZ)
			.color(color.red, color.green, color.blue, color.alpha)
			.normal(0, 0, 0)
			.endVertex();
	}

	public static void vertex(VertexConsumer buffer, PoseStack matrix, float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
		// Calling 'buffer.vertex(matrix, x, y, z)' allocates a Vector4f
		// To avoid allocating so many short lived vectors we do the transform ourselves instead
		var currentTransform = matrix.last();
		var pose = currentTransform.pose();
		var normal = currentTransform.normal();

		var transformedX = getTransformX(pose, x, y, z, 1);
		var transformedY = getTransformY(pose, x, y, z, 1);
		var transformedZ = getTransformZ(pose, x, y, z, 1);

		var transformedNormalX = getTransformX(normal, normalX, normalY, normalZ);
		var transformedNormalY = getTransformY(normal, normalX, normalY, normalZ);
		var transformedNormalZ = getTransformZ(normal, normalX, normalY, normalZ);

		buffer.vertex(transformedX, transformedY, transformedZ, red, green, blue, alpha, texU, texV, overlayUV, lightmapUV, transformedNormalX, transformedNormalY, transformedNormalZ);
	}

	public static float getTransformX(Matrix3f matrix, float x, float y, float z) {
		return matrix.m00 * x + matrix.m01 * y + matrix.m02 * z;
	}

	public static float getTransformY(Matrix3f matrix, float x, float y, float z) {
		return matrix.m10 * x + matrix.m11 * y + matrix.m12 * z;
	}

	public static float getTransformZ(Matrix3f matrix, float x, float y, float z) {
		return matrix.m20 * x + matrix.m21 * y + matrix.m22 * z;
	}

	public static float getTransformX(Matrix4f matrix, float x, float y, float z, float w) {
		return matrix.m00 * x + matrix.m01 * y + matrix.m02 * z + matrix.m03 * w;
	}

	public static float getTransformY(Matrix4f matrix, float x, float y, float z, float w) {
		return matrix.m10 * x + matrix.m11 * y + matrix.m12 * z + matrix.m13 * w;
	}

	public static float getTransformZ(Matrix4f matrix, float x, float y, float z, float w) {
		return matrix.m20 * x + matrix.m21 * y + matrix.m22 * z + matrix.m23 * w;
	}

	public static float getTransformW(Matrix4f matrix, float x, float y, float z, float w) {
		return matrix.m30 * x + matrix.m31 * y + matrix.m32 * z + matrix.m33 * w;
	}
}
