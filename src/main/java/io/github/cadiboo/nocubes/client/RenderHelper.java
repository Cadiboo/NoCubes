package io.github.cadiboo.nocubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.cadiboo.nocubes.config.ColorParser;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.*;

/**
 * Convenience functions for rendering things.
 */
public class RenderHelper {

	private static final Logger LOG = LogManager.getLogger();

	public static void reloadAllChunks(String because, Object... becauseArgs) {
		LOG.debug(() -> "Re-rendering chunks because " + because.formatted(becauseArgs));
		var minecraft = Minecraft.getInstance();
		minecraft.execute(minecraft.levelRenderer::allChanged);
	}

	public static void drawLinePosColorFromAdd(BlockPos offset, Vec start, Vec add, ColorParser.Color color, VertexConsumer buffer, PoseStack matrix, Vec3 camera) {
		var startX = (float) (offset.getX() - camera.x + start.x);
		var startY = (float) (offset.getY() - camera.y + start.y);
		var startZ = (float) (offset.getZ() - camera.z + start.z);
		line(
			buffer, matrix, color,
			startX, startY, startZ,
			startX + add.x, startY + add.y, startZ + add.z
		);
	}

	public static void drawLinePosColorFromTo(BlockPos startOffset, Vec start, BlockPos endOffset, Vec end, ColorParser.Color color, VertexConsumer buffer, PoseStack matrix, Vec3 camera) {
		line(
			buffer, matrix, color,
			(float) (startOffset.getX() + start.x - camera.x), (float) (startOffset.getY() + start.y - camera.y), (float) (startOffset.getZ() + start.z - camera.z),
			(float) (endOffset.getX() + end.x - camera.x), (float) (endOffset.getY() + end.y - camera.y), (float) (endOffset.getZ() + end.z - camera.z)
		);
	}

	public static void drawFacePosColor(
		Face face, Vec3 camera,
		BlockPos pos, ColorParser.Color color,
		VertexConsumer buffer, PoseStack matrix
	) {
		drawFacePosColor(
			face, camera.x, camera.y, camera.z,
			pos, color, buffer, matrix
		);
	}

	public static void drawFacePosColor(
		Face face, double cameraX, double cameraY, double cameraZ,
		BlockPos pos, ColorParser.Color color,
		VertexConsumer buffer, PoseStack matrix
	) {
		var v0 = face.v0;
		var v1 = face.v1;
		var v2 = face.v2;
		var v3 = face.v3;
		var x = pos.getX() - cameraX;
		var y = pos.getY() - cameraY;
		var z = pos.getZ() - cameraZ;

		var v0x = (float) (x + v0.x);
		var v1x = (float) (x + v1.x);
		var v2x = (float) (x + v2.x);
		var v3x = (float) (x + v3.x);
		var v0y = (float) (y + v0.y);
		var v1y = (float) (y + v1.y);
		var v2y = (float) (y + v2.y);
		var v3y = (float) (y + v3.y);
		var v0z = (float) (z + v0.z);
		var v1z = (float) (z + v1.z);
		var v2z = (float) (z + v2.z);
		var v3z = (float) (z + v3.z);
		line(buffer, matrix, color, v0x, v0y, v0z, v1x, v1y, v1z);
		line(buffer, matrix, color, v1x, v1y, v1z, v2x, v2y, v2z);
		line(buffer, matrix, color, v2x, v2y, v2z, v3x, v3y, v3z);
		line(buffer, matrix, color, v3x, v3y, v3z, v0x, v0y, v0z);
	}

	public static void drawShape(PoseStack stack, VertexConsumer buffer, VoxelShape shape, BlockPos pos, Vec3 camera, ColorParser.Color color) {
		var x = pos.getX() - camera.x;
		var y = pos.getY() - camera.y;
		var z = pos.getZ() - camera.z;
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

	/** See {@link Vector3f#mul(Matrix3fc)} */
	public static float getTransformX(Matrix3f matrix, float x, float y, float z) {
		return matrix.m00 * x + matrix.m01 * y + matrix.m02 * z;
	}

	/** See {@link Vector3f#mul(Matrix3fc)} */
	public static float getTransformY(Matrix3f matrix, float x, float y, float z) {
		return matrix.m10 * x + matrix.m11 * y + matrix.m12 * z;
	}

	/** See {@link Vector3f#mul(Matrix3fc)} */
	public static float getTransformZ(Matrix3f matrix, float x, float y, float z) {
		return matrix.m20 * x + matrix.m21 * y + matrix.m22 * z;
	}

	/** See {@link Vector4f#mul(Matrix4fc)} */
	public static float getTransformX(Matrix4f matrix, float x, float y, float z, float w) {
		return matrix.m00() * x + matrix.m10() * y + matrix.m20() * z + matrix.m30() * w;
	}

	/** See {@link Vector4f#mul(Matrix4fc)} */
	public static float getTransformY(Matrix4f matrix, float x, float y, float z, float w) {
		return matrix.m01() * x + matrix.m11() * y + matrix.m21() * z + matrix.m31() * w;
	}

	/** See {@link Vector4f#mul(Matrix4fc)} */
	public static float getTransformZ(Matrix4f matrix, float x, float y, float z, float w) {
		return matrix.m02() * x + matrix.m12() * y + matrix.m22() * z + matrix.m32() * w;
	}

	/** See {@link Vector4f#mul(Matrix4fc)} */
	public static float getTransformW(Matrix4f matrix, float x, float y, float z, float w) {
		return matrix.m30() * x + matrix.m31() * y + matrix.m32() * z + matrix.m33() * w;
	}

}
