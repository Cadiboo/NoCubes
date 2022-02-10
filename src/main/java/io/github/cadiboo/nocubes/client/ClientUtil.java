package io.github.cadiboo.nocubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import io.github.cadiboo.nocubes.config.ColorParser;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ClientUtil {

	private static final Logger LOG = LogManager.getLogger();

	public static void reloadAllChunks(String because) {
		LOG.debug("Re-rendering chunks because {}", because);
		var minecraft = Minecraft.getInstance();
		minecraft.execute(minecraft.levelRenderer::allChanged);
	}

	public static void warnPlayer(String translationKey, Object... formatArgs) {
		ModUtil.warnPlayer(Minecraft.getInstance().player, translationKey, formatArgs);
	}

	/**
	 * This is both a convenience method and an optimisation.
	 * Calling 'buffer.vertex(pose, x, y, z)' or 'buffer.normal(normal, x, y, z)' allocate a Vector4f and a Vector3f respectively.
	 * To avoid allocating many short-lived vectors we do the transform ourselves instead.
	 */
	public static void line(
		VertexConsumer buffer, PoseStack matrix, ColorParser.Color color,
		float x0, float y0, float z0,
		float x1, float y1, float z1
	) {
		var currentTransform = matrix.last();
		var pose = currentTransform.pose();
		var normal = currentTransform.normal();
		// Calling 'buffer.vertex(pose, x, y, z)' allocates a Vector4f
		// To avoid allocating so many short-lived vectors we do the transform ourselves instead
		var transformedX0 = getTransformX(pose, x0, y0, z0, 1);
		var transformedY0 = getTransformY(pose, x0, y0, z0, 1);
		var transformedZ0 = getTransformZ(pose, x0, y0, z0, 1);
		var transformedX1 = getTransformX(pose, x1, y1, z1, 1);
		var transformedY1 = getTransformY(pose, x1, y1, z1, 1);
		var transformedZ1 = getTransformZ(pose, x1, y1, z1, 1);

		var normalX = x1 - x0;
		var normalY = y1 - y0;
		var normalZ = z1 - z0;
		var length = Mth.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
		normalX /= length;
		normalY /= length;
		normalZ /= length;

		var transformedNormalX = getTransformX(normal, normalX, normalY, normalZ);
		var transformedNormalY = getTransformY(normal, normalX, normalY, normalZ);
		var transformedNormalZ = getTransformZ(normal, normalX, normalY, normalZ);

		buffer
			.vertex(transformedX0, transformedY0, transformedZ0)
			.color(color.red, color.green, color.blue, color.alpha)
			.normal(transformedNormalX, transformedNormalY, transformedNormalZ)
			.endVertex();
		buffer
			.vertex(transformedX1, transformedY1, transformedZ1)
			.color(color.red, color.green, color.blue, color.alpha)
			.normal(transformedNormalX, transformedNormalY, transformedNormalZ)
			.endVertex();
	}

	/**
	 * This is both a convenience method and an optimisation.
	 * Calling 'buffer.vertex(pose, x, y, z)' or 'buffer.normal(normal, x, y, z)' allocate a Vector4f and a Vector3f respectively.
	 * To avoid allocating many short-lived vectors we do the transform ourselves instead.
	 */
	public static void vertex(
		VertexConsumer buffer, PoseStack matrix,
		float x, float y, float z,
		float red, float green, float blue, float alpha,
		float texU, float texV,
		int overlayUV, int lightmapUV,
		float normalX, float normalY, float normalZ
	) {
		var currentTransform = matrix.last();
		var pose = currentTransform.pose();
		var normal = currentTransform.normal();

		var transformedX = getTransformX(pose, x, y, z, 1);
		var transformedY = getTransformY(pose, x, y, z, 1);
		var transformedZ = getTransformZ(pose, x, y, z, 1);

		var transformedNormalX = getTransformX(normal, normalX, normalY, normalZ);
		var transformedNormalY = getTransformY(normal, normalX, normalY, normalZ);
		var transformedNormalZ = getTransformZ(normal, normalX, normalY, normalZ);

		buffer.vertex(
			transformedX, transformedY, transformedZ,
			red, green, blue, alpha,
			texU, texV,
			overlayUV, lightmapUV,
			transformedNormalX, transformedNormalY, transformedNormalZ
		);
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
