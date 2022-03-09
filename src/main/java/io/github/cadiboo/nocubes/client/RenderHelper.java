package io.github.cadiboo.nocubes.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.config.ColorParser;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Convenience functions for rendering things.
 */
public class RenderHelper {

	private static final Logger LOG = LogManager.getLogger();

	public static void reloadAllChunks(String because, Object... becauseArgs) {
		LOG.debug(() -> "Re-rendering chunks because " + String.format(because, becauseArgs));
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.execute(minecraft.levelRenderer::allChanged);
	}

	public static void drawLinePosColorFromAdd(BlockPos offset, Vec start, Vec add, ColorParser.Color color, IVertexBuilder buffer, MatrixStack matrix, Vector3d camera) {
		float startX = (float) (offset.getX() - camera.x + start.x);
		float startY = (float) (offset.getY() - camera.y + start.y);
		float startZ = (float) (offset.getZ() - camera.z + start.z);
		line(
			buffer, matrix, color,
			startX, startY, startZ,
			startX + add.x, startY + add.y, startZ + add.z
		);
	}

	public static void drawLinePosColorFromTo(BlockPos startOffset, Vec start, BlockPos endOffset, Vec end, ColorParser.Color color, IVertexBuilder buffer, MatrixStack matrix, Vector3d camera) {
		line(
			buffer, matrix, color,
			(float) (startOffset.getX() + start.x - camera.x), (float) (startOffset.getY() + start.y - camera.y), (float) (startOffset.getZ() + start.z - camera.z),
			(float) (endOffset.getX() + end.x - camera.x), (float) (endOffset.getY() + end.y - camera.y), (float) (endOffset.getZ() + end.z - camera.z)
		);
	}

	public static void drawFacePosColor(Face face, Vector3d camera, BlockPos pos, ColorParser.Color color, IVertexBuilder buffer, MatrixStack matrix) {
		Vec v0 = face.v0;
		Vec v1 = face.v1;
		Vec v2 = face.v2;
		Vec v3 = face.v3;
		double x = pos.getX() - camera.x;
		double y = pos.getY() - camera.y;
		double z = pos.getZ() - camera.z;

		float v0x = (float) (x + v0.x);
		float v1x = (float) (x + v1.x);
		float v2x = (float) (x + v2.x);
		float v3x = (float) (x + v3.x);
		float v0y = (float) (y + v0.y);
		float v1y = (float) (y + v1.y);
		float v2y = (float) (y + v2.y);
		float v3y = (float) (y + v3.y);
		float v0z = (float) (z + v0.z);
		float v1z = (float) (z + v1.z);
		float v2z = (float) (z + v2.z);
		float v3z = (float) (z + v3.z);
		line(buffer, matrix, color, v0x, v0y, v0z, v1x, v1y, v1z);
		line(buffer, matrix, color, v1x, v1y, v1z, v2x, v2y, v2z);
		line(buffer, matrix, color, v2x, v2y, v2z, v3x, v3y, v3z);
		line(buffer, matrix, color, v3x, v3y, v3z, v0x, v0y, v0z);
	}

	public static void drawShape(MatrixStack stack, IVertexBuilder buffer, VoxelShape shape, BlockPos pos, Vector3d camera, ColorParser.Color color) {
		double x = pos.getX() - camera.x;
		double y = pos.getY() - camera.y;
		double z = pos.getZ() - camera.z;
		shape.forAllEdges((x0, y0, z0, x1, y1, z1) -> line(
			buffer, stack, color,
			(float) (x + x0), (float) (y + y0), (float) (z + z0),
			(float) (x + x1), (float) (y + y1), (float) (z + z1)
		));
	}

	/**
	 * This is both a convenience method and an optimisation.
	 * Calling 'buffer.vertex(pose, x, y, z)' or 'buffer.normal(normal, x, y, z)' allocate a Vector4f and a Vector3f respectively.
	 * To avoid allocating many short-lived vectors we do the transform ourselves instead.
	 */
	public static void line(
		IVertexBuilder buffer, MatrixStack matrix, ColorParser.Color color,
		float x0, float y0, float z0,
		float x1, float y1, float z1
	) {
		MatrixStack.Entry currentTransform = matrix.last();
		Matrix4f pose = currentTransform.pose();
		Matrix3f normal = currentTransform.normal();
		// Calling 'buffer.vertex(pose, x, y, z)' allocates a Vector4f
		// To avoid allocating so many short-lived vectors we do the transform ourselves instead
		float transformedX0 = getTransformX(pose, x0, y0, z0, 1);
		float transformedY0 = getTransformY(pose, x0, y0, z0, 1);
		float transformedZ0 = getTransformZ(pose, x0, y0, z0, 1);
		float transformedX1 = getTransformX(pose, x1, y1, z1, 1);
		float transformedY1 = getTransformY(pose, x1, y1, z1, 1);
		float transformedZ1 = getTransformZ(pose, x1, y1, z1, 1);

		float normalX = x1 - x0;
		float normalY = y1 - y0;
		float normalZ = z1 - z0;
		float length = MathHelper.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
		normalX /= length;
		normalY /= length;
		normalZ /= length;

		float transformedNormalX = getTransformX(normal, normalX, normalY, normalZ);
		float transformedNormalY = getTransformY(normal, normalX, normalY, normalZ);
		float transformedNormalZ = getTransformZ(normal, normalX, normalY, normalZ);

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
		IVertexBuilder buffer, MatrixStack matrix,
		float x, float y, float z,
		float red, float green, float blue, float alpha,
		float texU, float texV,
		int overlayUV, int lightmapUV,
		float normalX, float normalY, float normalZ
	) {
		MatrixStack.Entry currentTransform = matrix.last();
		Matrix4f pose = currentTransform.pose();
		Matrix3f normal = currentTransform.normal();

		float transformedX = getTransformX(pose, x, y, z, 1);
		float transformedY = getTransformY(pose, x, y, z, 1);
		float transformedZ = getTransformZ(pose, x, y, z, 1);

		float transformedNormalX = getTransformX(normal, normalX, normalY, normalZ);
		float transformedNormalY = getTransformY(normal, normalX, normalY, normalZ);
		float transformedNormalZ = getTransformZ(normal, normalX, normalY, normalZ);

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
