package io.github.cadiboo.nocubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

public final class ClientUtil {

	public static void reloadAllChunks() {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.execute(() -> {
			LevelRenderer worldRenderer = minecraft.levelRenderer;
			if (worldRenderer != null)
				worldRenderer.allChanged();
		});
	}

	public static VertexConsumer vertex(VertexConsumer buffer, PoseStack matrix, float x, float y, float z) {
		return vertex(buffer, matrix.last().pose(), x, y, z);
	}

	public static VertexConsumer vertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z) {
		// Calling 'buffer.vertex(matrix, x, y, z)' allocates a Vector4f
		// To avoid allocating so many short lived vectors we do the transform ourselves instead
		float transformedX = getTransformX(matrix, x, y, z, 1);
		float transformedY = getTransformY(matrix, x, y, z, 1);
		float transformedZ = getTransformZ(matrix, x, y, z, 1);
		return buffer.vertex(transformedX, transformedY, transformedZ);
	}

	public static void vertex(VertexConsumer buffer, PoseStack matrix, float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
		// Calling 'buffer.vertex(matrix, x, y, z)' allocates a Vector4f
		// To avoid allocating so many short lived vectors we do the transform ourselves instead
		PoseStack.Pose currentTransform = matrix.last();
		Matrix4f pose = currentTransform.pose();
		Matrix3f normal = currentTransform.normal();

		float transformedX = getTransformX(pose, x, y, z, 1);
		float transformedY = getTransformY(pose, x, y, z, 1);
		float transformedZ = getTransformZ(pose, x, y, z, 1);

		float transformedNormalX = getTransformX(normal, normalX, normalY, normalZ);
		float transformedNormalY = getTransformY(normal, normalX, normalY, normalZ);
		float transformedNormalZ = getTransformZ(normal, normalX, normalY, normalZ);

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
