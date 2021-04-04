package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.collision.OOCollisionHandler;
import io.github.cadiboo.nocubes.config.ColorParser;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
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
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedList;
import java.util.List;

import static io.github.cadiboo.nocubes.config.ColorParser.*;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class OverlayRenderer {

	static Tuple<BlockPos, Face[]> cache;

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
		Vector3d offset = new Vector3d(lookingAtPos.getX() - camera.x, lookingAtPos.getY() - camera.y, lookingAtPos.getZ() - camera.z);
		final Matrix4f matrix4f = event.getMatrix().last().pose();
		final IVertexBuilder bufferBuilder = event.getBuffers().getBuffer(RenderType.lines());

		Area area = new Area(world, lookingAtPos, lookingAtPos.offset(1, 1, 1));
		new SurfaceNets().generate(area, NoCubes.smoothableHandler::isSmoothable, (pos, face) -> {
			drawFacePosColor(face, offset, NoCubesConfig.Client.selectionBoxColor, bufferBuilder, matrix4f);
			return true;
		});
	}

	@SubscribeEvent
	public static void onRenderWorldLastEvent(final RenderWorldLastEvent event) {
		if (!Screen.hasAltDown())
			return;

		final Minecraft minecraft = Minecraft.getInstance();
		Entity viewer = minecraft.gameRenderer.getMainCamera().getEntity();
		if (viewer == null)
			return;

		final World world = viewer.level;
		if (world == null)
			return;

		if (cache == null || world.getGameTime() % 5 == 0)
			cache = makeMesh(world, viewer);

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
		// Draw nearby collisions in green
		world.getBlockCollisions(viewer, viewer.getBoundingBox().inflate(5.0D)).forEach(voxelShape -> {
			drawShape(matrixStack, bufferBuilder, voxelShape, -d0, -d1, -d2, 0.0F, 1.0F, 0.0F, 0.4F);
		});
		// Draw player intersecting collisions in red
		world.getBlockCollisions(viewer, viewer.getBoundingBox()).forEach(voxelShape -> {
			drawShape(matrixStack, bufferBuilder, voxelShape, -d0, -d1, -d2, 1.0F, 0.0F, 0.0F, 0.4F);
		});

		BlockPos start = viewer.blockPosition().offset(-5, -5, -5);
		Area area = new Area(world, start, start.offset(10, 10, 10));
		new OOCollisionHandler(new SurfaceNets()).generate(area, (x0, y0, z0, x1, y1, z1) -> {
			VoxelShape voxelShape = VoxelShapes.box(
				start.getX() + x0, start.getY() + y0, start.getZ() + z0,
				start.getX() + x1, start.getY() + y1, start.getZ() + z1
			);
			drawShape(matrixStack, bufferBuilder, voxelShape, -d0, -d1, -d2, 0.0F, 0.0F, 1.0F, 0.4F);
		});

		Matrix4f matrix4f = matrixStack.last().pose();
		{
			final Face normal = new Face();
			final Vec averageOfNormal = new Vec();
			final Vec centre = new Vec();
			final Vec mutable = new Vec();

			Face[] cachedFaces = cache.getB();
			BlockPos pos = cache.getA();
			Vector3d offset = new Vector3d(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
			Color faceColor = new Color(0F, 1F, 1F, 1F);
			Color normalDirectionColor = new Color(1F, 0F, 0F, 1F);
			for (int i = 0, length = cachedFaces.length; i < length; i++) {
				Face face = cachedFaces[i];
				drawFacePosColor(face, offset, faceColor, bufferBuilder, matrix4f);

				// Normals
				face.assignNormalTo(normal);
				normal.multiply(-1);

				normal.assignAverageTo(averageOfNormal);
				Direction direction = averageOfNormal.getDirectionFromNormal();
				face.assignAverageTo(centre);

				// Draw face normal vec + resulting direction
				final float dirMul = 0.2F;
				mutable.set(averageOfNormal).multiply(dirMul).add(centre);
				drawLinePosColor(centre, mutable, offset, normalDirectionColor, bufferBuilder, matrix4f);
				mutable.set(direction.getStepX(), direction.getStepY(), direction.getStepZ()).multiply(dirMul).add(centre);
				drawLinePosColor(centre, mutable, offset, normalDirectionColor, bufferBuilder, matrix4f);

				// Draw each vertex normal
				mutable.set(normal.v0).multiply(dirMul);
				drawLinePosColor(normal.v0, mutable, offset, normalDirectionColor, bufferBuilder, matrix4f);
				mutable.set(normal.v1).multiply(dirMul);
				drawLinePosColor(normal.v1, mutable, offset, normalDirectionColor, bufferBuilder, matrix4f);
				mutable.set(normal.v2).multiply(dirMul);
				drawLinePosColor(normal.v3, mutable, offset, normalDirectionColor, bufferBuilder, matrix4f);
				mutable.set(normal.v3).multiply(dirMul);
				drawLinePosColor(normal.v3, mutable, offset, normalDirectionColor, bufferBuilder, matrix4f);
			}
		}

		// Hack to finish buffer because RenderWorldLastEvent seems to fire after vanilla normally finishes them
		bufferSource.endBatch(RenderType.lines());
	}

	private static void drawLinePosColor(Vec start, Vec end, Vector3d offset, Color color, IVertexBuilder bufferBuilder, Matrix4f matrix4f) {
		int red = color.red;
		int blue = color.blue;
		int green = color.green;
		int alpha = color.alpha;
		double x = offset.x;
		double y = offset.y;
		double z = offset.z;
		bufferBuilder.vertex(matrix4f, (float) (x + start.x), (float) (y + start.y), (float) (z + start.z)).color(red, green, blue, alpha).endVertex();
		bufferBuilder.vertex(matrix4f, (float) (x + end.x), (float) (y + end.y), (float) (z + end.z)).color(red, green, blue, alpha).endVertex();
	}

	private static void drawFacePosColor(Face face, Vector3d offset, Color color, IVertexBuilder bufferBuilder, Matrix4f matrix4f) {
		int red = color.red;
		int blue = color.blue;
		int green = color.green;
		int alpha = color.alpha;

		Vec v0 = face.v0;
		Vec v1 = face.v1;
		Vec v2 = face.v2;
		Vec v3 = face.v3;
		double x = offset.x;
		double y = offset.y;
		double z = offset.z;

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
		bufferBuilder.vertex(matrix4f, v0x, v0y, v0z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.vertex(matrix4f, v1x, v1y, v1z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.vertex(matrix4f, v1x, v1y, v1z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.vertex(matrix4f, v2x, v2y, v2z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.vertex(matrix4f, v2x, v2y, v2z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.vertex(matrix4f, v3x, v3y, v3z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.vertex(matrix4f, v3x, v3y, v3z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.vertex(matrix4f, v0x, v0y, v0z).color(red, green, blue, alpha).endVertex();
	}

	private static void drawShape(MatrixStack matrixStackIn, IVertexBuilder bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha) {
		Matrix4f matrix4f = matrixStackIn.last().pose();
		shapeIn.forAllEdges((x0, y0, z0, x1, y1, z1) -> {
			bufferIn.vertex(matrix4f, (float) (x0 + xIn), (float) (y0 + yIn), (float) (z0 + zIn)).color(red, green, blue, alpha).endVertex();
			bufferIn.vertex(matrix4f, (float) (x1 + xIn), (float) (y1 + yIn), (float) (z1 + zIn)).color(red, green, blue, alpha).endVertex();
		});
	}

	private static Tuple<BlockPos, Face[]> makeMesh(final World world, final Entity viewer) {
//		BlockPos base = new BlockPos(viewer.chunkCoordX << 4, viewer.chunkCoordY << 4, viewer.chunkCoordZ << 4);
		BlockPos base = viewer.blockPosition().offset(0, 2, 0);

		final int meshSizeX = 16;
		final int meshSizeY = 16;
		final int meshSizeZ = 16;

		// Make this mesh centred around the base
		final int offsetX = meshSizeX / 2;
		final int offsetY = meshSizeY / 2;
		final int offsetZ = meshSizeZ / 2;
		BlockPos start = base.offset(-offsetX, -offsetY, -offsetZ);
		Area area = new Area(world, start, base.offset(offsetX, offsetY, offsetZ));
		final List<Face> meshFaces = new LinkedList<>();
		new SurfaceNets().generate(area, NoCubes.smoothableHandler::isSmoothable, (pos, face) -> {
			meshFaces.add(new Face(face.v0.copy(), face.v1.copy(), face.v2.copy(), face.v3.copy()));
			return true;
		});

		return new Tuple<>(start, meshFaces.toArray(new Face[0]));

//		BlockPos.getAllInBoxMutable(base.add(-8, -8, -8), base.add(7, 7, 7)).forEach(blockPos -> {
//			final BlockState state = viewer.world.getBlockState(blockPos);
//			if (NoCubes.smoothableHandler.isSmoothable(state)) {
//				final VoxelShape shape = state.getCollisionShape(world, blockPos);
//				shape.forEachBox((x0, y0, z0, x1, y1, z1) -> {
//					x0 += blockPos.getX();
//					y0 += blockPos.getY();
//					z0 += blockPos.getZ();
//					x1 += blockPos.getX();
//					y1 += blockPos.getY();
//					z1 += blockPos.getZ();
//					// Bottom
//					{
//						Vec v0 = Vec.of(x1, y0, z1);
//						Vec v1 = Vec.of(x0, y0, z1);
//						Vec v2 = Vec.of(x0, y0, z0);
//						Vec v3 = Vec.of(x1, y0, z0);
//						mesh.faces.add(Face.of(v0, v1, v2, v3));
//					}
//					// Top
//					{
//						Vec v0 = Vec.of(x1, y1, z1);
//						Vec v1 = Vec.of(x0, y1, z1);
//						Vec v2 = Vec.of(x0, y1, z0);
//						Vec v3 = Vec.of(x1, y1, z0);
//						mesh.faces.add(Face.of(v0, v1, v2, v3));
//					}
//					// south (pos z)
//					{
//						Vec v0 = Vec.of(x1, y1, z1);
//						Vec v1 = Vec.of(x0, y1, z1);
//						Vec v2 = Vec.of(x0, y0, z1);
//						Vec v3 = Vec.of(x1, y0, z1);
//						mesh.faces.add(Face.of(v0, v1, v2, v3));
//					}
//					// north (neg z)
//					{
//						Vec v0 = Vec.of(x1, y1, z0);
//						Vec v1 = Vec.of(x0, y1, z0);
//						Vec v2 = Vec.of(x0, y0, z0);
//						Vec v3 = Vec.of(x1, y0, z0);
//						mesh.faces.add(Face.of(v0, v1, v2, v3));
//					}
//					// east (pos x)
//					{
//						Vec v0 = Vec.of(x1, y1, z1);
//						Vec v1 = Vec.of(x1, y1, z0);
//						Vec v2 = Vec.of(x1, y0, z0);
//						Vec v3 = Vec.of(x1, y0, z1);
//						mesh.faces.add(Face.of(v0, v1, v2, v3));
//					}
//					// west (neg x)
//					{
//						Vec v0 = Vec.of(x0, y1, z1);
//						Vec v1 = Vec.of(x0, y1, z0);
//						Vec v2 = Vec.of(x0, y0, z0);
//						Vec v3 = Vec.of(x0, y0, z1);
//						mesh.faces.add(Face.of(v0, v1, v2, v3));
//					}
//				});
//			}
//		});

	}

}
