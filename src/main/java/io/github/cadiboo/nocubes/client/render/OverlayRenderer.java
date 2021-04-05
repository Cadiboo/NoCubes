package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.CubicMeshGenerator;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.mesh.SurfaceNets;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;

import java.util.stream.LongStream;

import static io.github.cadiboo.nocubes.config.ColorParser.Color;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class OverlayRenderer {

	static long[] meshTimings = new long[60 * 10];
	static int timingsIndex = 0;

	@SubscribeEvent
	public static void onHighlightBlock(final DrawHighlightEvent.HighlightBlock event) {
		if (!NoCubesConfig.Client.render)
			return;
		final ClientWorld world = Minecraft.getInstance().level;
		if (world == null)
			return;
		final BlockPos lookingAtPos = event.getTarget().getBlockPos();
		final BlockState state = world.getBlockState(lookingAtPos);
		if (!NoCubes.smoothableHandler.isSmoothable(state))
			return;

		event.setCanceled(true);

		final Vector3d camera = event.getInfo().getPosition();
		final Matrix4f matrix4f = event.getMatrix().last().pose();
		final IVertexBuilder bufferBuilder = event.getBuffers().getBuffer(RenderType.lines());

		try (Area area = new Area(world, lookingAtPos, lookingAtPos.offset(1, 1, 1))) {
			new SurfaceNets().generate(area, NoCubes.smoothableHandler::isSmoothable, (pos, face) -> {
				drawFacePosColor(face, camera, lookingAtPos, NoCubesConfig.Client.selectionBoxColor, bufferBuilder, matrix4f);
				return true;
			});
		}
	}

	@SubscribeEvent
	public static void onRenderWorldLastEvent(final RenderWorldLastEvent event) {
		if (Screen.hasAltDown())
			return;

		final Minecraft minecraft = Minecraft.getInstance();
		Entity viewer = minecraft.gameRenderer.getMainCamera().getEntity();
		if (viewer == null)
			return;

		final World world = viewer.level;
		if (world == null)
			return;

		final ActiveRenderInfo activeRenderInfo = minecraft.gameRenderer.getMainCamera();

		final Vector3d camera = activeRenderInfo.getPosition();
		double d0 = camera.x;
		double d1 = camera.y;
		double d2 = camera.z;
		final MatrixStack matrixStack = event.getMatrixStack();

		final IRenderTypeBuffer.Impl bufferSource = minecraft.renderBuffers().bufferSource();
		final IVertexBuilder bufferBuilder = bufferSource.getBuffer(RenderType.lines());

//		final BlockPos viewerPos = new BlockPos(viewer.getPosition());
//		BlockPos.getAllInBoxMutable(viewerPos.add(-5, -5, -5), viewerPos.add(5, 5, 5)).forEach(blockPos -> {
//			if (NoCubes.smoothableHandler.isSmoothable(viewer.world.getBlockState(blockPos)))
//				drawShape(matrixStack, bufferBuilder, VoxelShapes.fullCube(), -d0 + blockPos.getX(), -d1 + blockPos.getY(), -d2 + blockPos.getZ(), 0.0F, 1.0F, 1.0F, 0.4F);
//		});
//
//		// Draw nearby collisions in green
//		world.getBlockCollisions(viewer, viewer.getBoundingBox().inflate(5.0D)).forEach(voxelShape -> {
//			drawShape(matrixStack, bufferBuilder, voxelShape, -d0, -d1, -d2, 0.0F, 1.0F, 0.0F, 0.4F);
//		});
//		// Draw player intersecting collisions in red
//		world.getBlockCollisions(viewer, viewer.getBoundingBox()).forEach(voxelShape -> {
//			drawShape(matrixStack, bufferBuilder, voxelShape, -d0, -d1, -d2, 1.0F, 0.0F, 0.0F, 0.4F);
//		});

//		BlockPos start = viewer.blockPosition().offset(-5, -5, -5);
//		try (Area area = new Area(world, start, start.offset(10, 10, 10))) {
//			new OOCollisionHandler(new SurfaceNets()).generate(area, (x0, y0, z0, x1, y1, z1) -> {
//				double x = start.getX();
//				double y = start.getY();
//				double z = start.getZ();
//				VoxelShape voxelShape = VoxelShapes.box(
//					x + x0, y + y0, z + z0,
//					x + x1, y + y1, z + z1
//				);
//				drawShape(matrixStack, bufferBuilder, voxelShape, -d0, -d1, -d2, 0.0F, 0.0F, 1.0F, 0.4F);
//			});
//		}

		long startNanos = System.nanoTime();
		drawNearbyMesh(viewer, matrixStack.last().pose(), camera, bufferBuilder);
		long elapsedNanos = System.nanoTime() - startNanos;
		meshTimings[timingsIndex++ % meshTimings.length] = elapsedNanos;
		if (timingsIndex % meshTimings.length == 0)
			LogManager.getLogger("Calc & render chunk mesh").debug("Average " + ((LongStream.of(meshTimings).sum() / meshTimings.length) / 1000_000f) + "ms over the past " + meshTimings.length + " frames");

		// Hack to finish buffer because RenderWorldLastEvent seems to fire after vanilla normally finishes them
		bufferSource.endBatch(RenderType.lines());
	}

	private static void drawNearbyMesh(Entity viewer, Matrix4f matrix4f, Vector3d camera, IVertexBuilder bufferBuilder) {
		MeshGenerator meshGenerator = new SurfaceNets();
		Vector3i meshSize = new BlockPos(16, 16, 16);
		BlockPos start = viewer.blockPosition().offset(-meshSize.getX() / 2, -meshSize.getY() / 2 + 2, -meshSize.getZ() / 2);
		try (Area area = new Area(viewer.level, start, start.offset(meshSize).offset(1, 1, 1))) {
			final Face normal = new Face();
			final Vec averageOfNormal = new Vec();
			final Vec centre = new Vec();
			final Vec mut = new Vec();

			Color faceColor = new Color(0F, 1F, 1F, 1F);
			Color normalDirectionColor = new Color(1F, 0F, 0F, 1F);

			meshGenerator.generate(area, NoCubes.smoothableHandler::isSmoothable, (pos, face) -> {
				if (Screen.hasControlDown())
					return true;
				drawFacePosColor(face, camera, start, faceColor, bufferBuilder, matrix4f);

				face.assignNormalTo(normal);
				normal.multiply(-1);
				normal.assignAverageTo(averageOfNormal);
				Direction direction = averageOfNormal.getDirectionFromNormal();
				face.assignAverageTo(centre);

				// Draw face normal vec + resulting direction
				final float dirMul = 0.2F;
				drawLinePosColor(centre, mut.set(averageOfNormal).multiply(dirMul), camera, start, normalDirectionColor, bufferBuilder, matrix4f);
				drawLinePosColor(centre, mut.set(direction.getStepX(), direction.getStepY(), direction.getStepZ()).multiply(dirMul), camera, start, normalDirectionColor, bufferBuilder, matrix4f);

				// Draw each vertex normal
				drawLinePosColor(face.v0, mut.set(normal.v0).multiply(dirMul), camera, start, normalDirectionColor, bufferBuilder, matrix4f);
				drawLinePosColor(face.v1, mut.set(normal.v1).multiply(dirMul), camera, start, normalDirectionColor, bufferBuilder, matrix4f);
				drawLinePosColor(face.v2, mut.set(normal.v2).multiply(dirMul), camera, start, normalDirectionColor, bufferBuilder, matrix4f);
				drawLinePosColor(face.v3, mut.set(normal.v3).multiply(dirMul), camera, start, normalDirectionColor, bufferBuilder, matrix4f);

				return true;
			});
		}
	}

	private static void drawLinePosColor(Vec start, Vec add, Vector3d camera, BlockPos pos, Color color, IVertexBuilder bufferBuilder, Matrix4f matrix4f) {
		int red = color.red;
		int blue = color.blue;
		int green = color.green;
		int alpha = color.alpha;
		float startX = (float) (pos.getX() - camera.x + start.x);
		float startY = (float) (pos.getY() - camera.y + start.y);
		float startZ = (float) (pos.getZ() - camera.z + start.z);
		vertex(bufferBuilder, matrix4f, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, startX + add.x, startY + add.y, startZ + add.z).color(red, green, blue, alpha).endVertex();
	}

	private static void drawFacePosColor(Face face, Vector3d camera, BlockPos pos, Color color, IVertexBuilder bufferBuilder, Matrix4f matrix4f) {
		int red = color.red;
		int blue = color.blue;
		int green = color.green;
		int alpha = color.alpha;

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
		vertex(bufferBuilder, matrix4f, v0x, v0y, v0z).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, v1x, v1y, v1z).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, v1x, v1y, v1z).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, v2x, v2y, v2z).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, v2x, v2y, v2z).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, v3x, v3y, v3z).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, v3x, v3y, v3z).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, v0x, v0y, v0z).color(red, green, blue, alpha).endVertex();
	}

	private static IVertexBuilder vertex(IVertexBuilder bufferBuilder, Matrix4f matrix4f, float x, float y, float z) {
		// Calling 'bufferBuilder.vertex(matrix4f, x, y, z)' allocates a Vector4f
		// To avoid allocating so many short lived vectors we do the transform ourselves instead
		float w = 1.0F;
		float tx = matrix4f.m00 * x + matrix4f.m01 * y + matrix4f.m02 * z + matrix4f.m03 * w;
		float ty = matrix4f.m10 * x + matrix4f.m11 * y + matrix4f.m12 * z + matrix4f.m13 * w;
		float tz = matrix4f.m20 * x + matrix4f.m21 * y + matrix4f.m22 * z + matrix4f.m23 * w;
//		float tw = matrix4f.m30 * x + matrix4f.m31 * y + matrix4f.m32 * z + matrix4f.m33 * w;
		return bufferBuilder.vertex(tx, ty, tz);
	}

	private static void drawShape(MatrixStack matrixStackIn, IVertexBuilder bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha) {
		Matrix4f matrix4f = matrixStackIn.last().pose();
		shapeIn.forAllEdges((x0, y0, z0, x1, y1, z1) -> {
			bufferIn.vertex(matrix4f, (float) (x0 + xIn), (float) (y0 + yIn), (float) (z0 + zIn)).color(red, green, blue, alpha).endVertex();
			bufferIn.vertex(matrix4f, (float) (x1 + xIn), (float) (y1 + yIn), (float) (z1 + zIn)).color(red, green, blue, alpha).endVertex();
		});
	}

}
