package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.client.render.util.Face;
import io.github.cadiboo.nocubes.client.render.util.FaceList;
import io.github.cadiboo.nocubes.client.render.util.Vec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class OverlayRenderer {

	static Mesh cache;

	@SubscribeEvent
	public static void onRenderWorldLastEvent(final RenderWorldLastEvent event) {
//		if (!Screen.hasAltDown())
//			return;

		final Minecraft minecraft = Minecraft.getInstance();
		Entity viewer = minecraft.gameRenderer.getActiveRenderInfo().getRenderViewEntity();
		if (viewer == null)
			return;

		final World world = viewer.world;
		if (world == null)
			return;

		if (cache == null || world.getGameTime() % 20 == 0) {
			if (cache != null) {
				final FaceList faces = cache.faces;
				for (final Face face : faces) {
					for (final Vec vertex : face.getVertices()) {
						vertex.close();
					}
					face.close();
				}
				faces.close();
			}
			cache = makeMesh(world, viewer);
		}

		final ActiveRenderInfo activeRenderInfo = minecraft.gameRenderer.getActiveRenderInfo();

		final Vector3d projectedView = activeRenderInfo.getProjectedView();
		double d0 = projectedView.getX();
		double d1 = projectedView.getY();
		double d2 = projectedView.getZ();
		final MatrixStack matrixStack = event.getMatrixStack();

		final IRenderTypeBuffer.Impl bufferSource = minecraft.getRenderTypeBuffers().getBufferSource();
		final IVertexBuilder bufferBuilder = bufferSource.getBuffer(RenderType.getLines());

		// Draw nearby collisions
		viewer.world.getCollisionShapes(viewer, viewer.getBoundingBox().grow(5.0D)).forEach(voxelShape -> {
			drawShape(matrixStack, bufferBuilder, voxelShape, -d0, -d1, -d2, 0.0F, 1.0F, 1.0F, 0.4F);
		});
		// Draw player intersecting collisions
		viewer.world.getCollisionShapes(viewer, viewer.getBoundingBox()).forEach(voxelShape -> {
			drawShape(matrixStack, bufferBuilder, voxelShape, -d0, -d1, -d2, 1.0F, 0.0F, 0.0F, 0.4F);
		});

		Matrix4f matrix4f = matrixStack.getLast().getMatrix();
		for (final Face face : cache.faces) {
			try (Face ignoredF = face) {
				for (final Vec vertex : face.getVertices()) {
					try (Vec ignoredV = vertex) {
						bufferBuilder.pos(matrix4f, (float) vertex.x, (float) vertex.y, (float) vertex.z).color(0, 1, 0, 1).endVertex();
					}
				}
			}
		}

		// Hack to finish buffer because RenderWorldLastEvent seems to fire after vanilla normally finishes them
		bufferSource.finish(RenderType.getLines());
	}

	private static void drawShape(MatrixStack matrixStackIn, IVertexBuilder bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha) {
		Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
		shapeIn.forEachEdge((x0, y0, z0, x1, y1, z1) -> {
			bufferIn.pos(matrix4f, (float) (x0 + xIn), (float) (y0 + yIn), (float) (z0 + zIn)).color(red, green, blue, alpha).endVertex();
			bufferIn.pos(matrix4f, (float) (x1 + xIn), (float) (y1 + yIn), (float) (z1 + zIn)).color(red, green, blue, alpha).endVertex();
		});
	}

	private static Mesh makeMesh(final World world, final Entity viewer) {
		final Mesh mesh = new Mesh();
		BlockPos chunk = new BlockPos(viewer.chunkCoordX << 4, viewer.chunkCoordY << 4, viewer.chunkCoordZ << 4);

		Vec v0 = Vec.of(viewer.getPosX() - 0.5, viewer.getPosY() - 10, viewer.getPosZ() - 0.5);
		Vec v1 = Vec.of(viewer.getPosX(), viewer.getPosY() - 10, viewer.getPosZ() - 0.5);
		Vec v2 = Vec.of(viewer.getPosX(), viewer.getPosY() - 10, viewer.getPosZ());
		Vec v3 = Vec.of(viewer.getPosX() - 0.5, viewer.getPosY() - 10, viewer.getPosZ());

		mesh.faces.add(Face.of(v0, v1, v2, v3));

		return mesh;
	}

}
