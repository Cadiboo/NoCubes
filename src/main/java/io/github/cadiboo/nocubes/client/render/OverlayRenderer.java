package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ColorParser;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.SurfaceNets;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ReusableCache;
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

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class OverlayRenderer {

	private static final ReusableCache<float[]> DEBUGGING = new ReusableCache.Global<>();
	private static final ReusableCache<float[]> HIGHLIGHT = new ReusableCache.Global<>();
	static List<Face> cache;

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

		final Vector3d projectedView = event.getInfo().getPosition();
		final double d0 = projectedView.x;
		final double d1 = projectedView.y;
		final double d2 = projectedView.z;
		final Matrix4f matrix4f = event.getMatrix().last().pose();
		final IVertexBuilder bufferBuilder = event.getBuffers().getBuffer(RenderType.lines());

		final int x = lookingAtPos.getX();
		final int y = lookingAtPos.getY();
		final int z = lookingAtPos.getZ();
		SurfaceNets.generate(
			x, y, z,
			1, 1, 1,
			world, NoCubes.smoothableHandler::isSmoothable, HIGHLIGHT,
			(pos, mask) -> true,
			(pos, face) -> {
				Vec v0 = face.v0.add(x, y, z);
				Vec v1 = face.v1.add(x, y, z);
				Vec v2 = face.v2.add(x, y, z);
				Vec v3 = face.v3.add(x, y, z);
				final ColorParser.Color color = NoCubesConfig.Client.selectionBoxColor;
				final int red = color.red;
				final int blue = color.blue;
				final int green = color.green;
				final int alpha = color.alpha;
				bufferBuilder.vertex(matrix4f, (float) (v0.x + -d0), (float) (v0.y + -d1), (float) (v0.z + -d2)).color(red, green, blue, alpha).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (v1.x + -d0), (float) (v1.y + -d1), (float) (v1.z + -d2)).color(red, green, blue, alpha).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (v1.x + -d0), (float) (v1.y + -d1), (float) (v1.z + -d2)).color(red, green, blue, alpha).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (v2.x + -d0), (float) (v2.y + -d1), (float) (v2.z + -d2)).color(red, green, blue, alpha).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (v2.x + -d0), (float) (v2.y + -d1), (float) (v2.z + -d2)).color(red, green, blue, alpha).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (v3.x + -d0), (float) (v3.y + -d1), (float) (v3.z + -d2)).color(red, green, blue, alpha).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (v3.x + -d0), (float) (v3.y + -d1), (float) (v3.z + -d2)).color(red, green, blue, alpha).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (v0.x + -d0), (float) (v0.y + -d1), (float) (v0.z + -d2)).color(red, green, blue, alpha).endVertex();
				return true;
			}
		);
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

//		if (cache == null || world.getGameTime() % 5 == 0)
//			cache = makeMesh(world, viewer);

		final ActiveRenderInfo activeRenderInfo = minecraft.gameRenderer.getMainCamera();

		final Vector3d projectedView = activeRenderInfo.getPosition();
		double d0 = projectedView.x;
		double d1 = projectedView.y;
		double d2 = projectedView.z;
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
		SurfaceNets.generate(
			start.getX(), start.getY(), start.getZ(),
			10, 10, 10,
			world, NoCubes.smoothableHandler::isSmoothable, DEBUGGING,
			(pos, mask) -> {
				if (mask == 0x00) {
					VoxelShape voxelShape = VoxelShapes.block().move(pos.getX(), pos.getY(), pos.getZ());
					drawShape(matrixStack, bufferBuilder, voxelShape, -d0, -d1, -d2, 0.0F, 0.0F, 1.0F, 0.4F);
				}
				return true;
			},
			(pos, face) -> true
		);

		Matrix4f matrix4f = matrixStack.last().pose();
		{
			final Face normal = new Face(new Vec(), new Vec(), new Vec(), new Vec());
			final Vec averageOfNormal = new Vec();
			final Vec centre = new Vec();
			for (Face face : cache) {
				Vec v0 = face.v0;
				Vec v1 = face.v1;
				Vec v2 = face.v2;
				Vec v3 = face.v3;
				final float red = 0F;
				final float blue = 1F;
				final float green = 1F;
				final float alpha = 1F;
				bufferBuilder.vertex(matrix4f, (float) (v0.x + -d0), (float) (v0.y + -d1), (float) (v0.z + -d2)).color(red, green, blue, alpha).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (v1.x + -d0), (float) (v1.y + -d1), (float) (v1.z + -d2)).color(red, green, blue, alpha).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (v1.x + -d0), (float) (v1.y + -d1), (float) (v1.z + -d2)).color(red, green, blue, alpha).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (v2.x + -d0), (float) (v2.y + -d1), (float) (v2.z + -d2)).color(red, green, blue, alpha).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (v2.x + -d0), (float) (v2.y + -d1), (float) (v2.z + -d2)).color(red, green, blue, alpha).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (v3.x + -d0), (float) (v3.y + -d1), (float) (v3.z + -d2)).color(red, green, blue, alpha).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (v3.x + -d0), (float) (v3.y + -d1), (float) (v3.z + -d2)).color(red, green, blue, alpha).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (v0.x + -d0), (float) (v0.y + -d1), (float) (v0.z + -d2)).color(red, green, blue, alpha).endVertex();

				// Normals
				face.assignNormalTo(normal);
				normal.v0.multiply(-1);
				normal.v1.multiply(-1);
				normal.v2.multiply(-1);
				normal.v3.multiply(-1);

				normal.assignAverageTo(averageOfNormal);
				Direction direction = averageOfNormal.getDirectionFromNormal();
				face.assignAverageTo(centre);

				final float dirMul = 0.2F;
				bufferBuilder.vertex(matrix4f, (float) (centre.x + -d0), (float) (centre.y + -d1), (float) (centre.z + -d2)).color(1F, 0F, 0F, 1F).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (centre.x + averageOfNormal.x * dirMul + -d0), (float) (centre.y + averageOfNormal.y * dirMul + -d1), (float) (centre.z + averageOfNormal.z * dirMul + -d2)).color(0F, 1F, 0F, 1F).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (centre.x + -d0), (float) (centre.y + -d1), (float) (centre.z + -d2)).color(1F, 0F, 0F, 1F).endVertex();
				bufferBuilder.vertex(matrix4f, (float) (centre.x + direction.getStepX() * dirMul + -d0), (float) (centre.y + direction.getStepY() * dirMul + -d1), (float) (centre.z + direction.getStepZ() * dirMul + -d2)).color(1F, 0F, 0F, 1F).endVertex();

				final float normMul = 0.1F;
				{
					Vec n = normal.v0;
					Vec v = v0;
					float nx = (float) (n.x) * normMul;
					float ny = (float) (n.y) * normMul;
					float nz = (float) (n.z) * normMul;
					bufferBuilder.vertex(matrix4f, (float) (v.x + -d0), (float) (v.y + -d1), (float) (v.z + -d2)).color(0F, 0F, 1F, 1F).endVertex();
					bufferBuilder.vertex(matrix4f, (float) (v.x + nx + -d0), (float) (v.y + ny + -d1), (float) (v.z + nz + -d2)).color(0F, 0F, 1F, 1F).endVertex();
				}
				{
					Vec n = normal.v1;
					Vec v = v1;
					float nx = (float) (n.x) * normMul;
					float ny = (float) (n.y) * normMul;
					float nz = (float) (n.z) * normMul;
					bufferBuilder.vertex(matrix4f, (float) (v.x + -d0), (float) (v.y + -d1), (float) (v.z + -d2)).color(0F, 0F, 1F, 1F).endVertex();
					bufferBuilder.vertex(matrix4f, (float) (v.x + nx + -d0), (float) (v.y + ny + -d1), (float) (v.z + nz + -d2)).color(0F, 0F, 1F, 1F).endVertex();
				}
				{
					Vec n = normal.v2;
					Vec v = v2;
					float nx = (float) (n.x) * normMul;
					float ny = (float) (n.y) * normMul;
					float nz = (float) (n.z) * normMul;
					bufferBuilder.vertex(matrix4f, (float) (v.x + -d0), (float) (v.y + -d1), (float) (v.z + -d2)).color(0F, 0F, 1F, 1F).endVertex();
					bufferBuilder.vertex(matrix4f, (float) (v.x + nx + -d0), (float) (v.y + ny + -d1), (float) (v.z + nz + -d2)).color(0F, 0F, 1F, 1F).endVertex();
				}
				{
					Vec n = normal.v3;
					Vec v = v3;
					float nx = (float) (n.x) * normMul;
					float ny = (float) (n.y) * normMul;
					float nz = (float) (n.z) * normMul;
					bufferBuilder.vertex(matrix4f, (float) (v.x + -d0), (float) (v.y + -d1), (float) (v.z + -d2)).color(0F, 0F, 1F, 1F).endVertex();
					bufferBuilder.vertex(matrix4f, (float) (v.x + nx + -d0), (float) (v.y + ny + -d1), (float) (v.z + nz + -d2)).color(0F, 0F, 1F, 1F).endVertex();
				}
			}
		}

//		List<VoxelShape> shapes = new ArrayList<>();
//		if (false) {
//			BlockPos base = viewer.getPosition().add(0, 2, 0);
//			final int collisionSizeX = 16;
//			final int collisionSizeY = 16;
//			final int collisionSizeZ = 16;
//
//			// Make this mesh centred around the base
//			final int startX = base.getX() - collisionSizeX / 2;
//			final int startY = base.getY() - collisionSizeY / 2;
//			final int startZ = base.getZ() - collisionSizeZ / 2;
//
//			for (int z = 0; z < collisionSizeX; z++) {
//				for (int y = 0; y < collisionSizeY; y++) {
//					for (int x = 0; x < collisionSizeZ; x++) {
//						final int currX = startX + x;
//						final int currY = startY + y;
//						final int currZ = startZ + z;
//						SurfaceNets.generate(
//							currX, currY, currZ,
//							1, 1, 1,
//							viewer.world, NoCubes.smoothableHandler::isSmoothable, COLLISIONS,
//							(pos, face) -> {
//								Vec v0 = face.v0;
//								Vec v1 = face.v1;
//								Vec v2 = face.v2;
//								Vec v3 = face.v3;
//								// Normals
//								Vec n0 = Vec.normal(v3, v0, v1).multiply(-1);
//								Vec n1 = Vec.normal(v0, v1, v2).multiply(-1);
//								Vec n2 = Vec.normal(v1, v2, v3).multiply(-1);
//								Vec n3 = Vec.normal(v2, v3, v0).multiply(-1);
//
//								Vec centre = Vec.of(
//									(v0.x + v1.x + v2.x + v3.x) / 4,
//									(v0.y + v1.y + v2.y + v3.y) / 4,
//									(v0.z + v1.z + v2.z + v3.z) / 4
//								);
//
//								final Vec nAverage = Vec.of(
//									(n0.x + n2.x) / 2,
//									(n0.y + n2.y) / 2,
//									(n0.z + n2.z) / 2
//								);
//								nAverage.normalise().multiply(-0.125d);
//
//								shapes.add(makeShape(currX, currY, currZ, centre, nAverage, v0));
//								shapes.add(makeShape(currX, currY, currZ, centre, nAverage, v1));
//								shapes.add(makeShape(currX, currY, currZ, centre, nAverage, v2));
//								shapes.add(makeShape(currX, currY, currZ, centre, nAverage, v3));
//								n0.close();
//								n1.close();
//								n2.close();
//								n3.close();
//								nAverage.close();
//								centre.close();
//
////								for (Vec v : face.getVertices()) {
////									v.add(currX, currY, currZ);
////									shapes.add(VoxelShapes.create(
////										v.x - 0.125, v.y - 0.125, v.z - 0.125,
////										v.x + 0.125, v.y + 0.125, v.z + 0.125
////									));
////								}
//								return true;
//							}
//						);
//					}
//				}
//			}
//		}
//
//		shapes.forEach(voxelShape -> {
//			drawShape(matrixStack, bufferBuilder, voxelShape, -d0, -d1, -d2, 1.0F, 1.0F, 0.0F, 0.8F);
//		});

		// Hack to finish buffer because RenderWorldLastEvent seems to fire after vanilla normally finishes them
		bufferSource.endBatch(RenderType.lines());
	}

	private static void drawShape(MatrixStack matrixStackIn, IVertexBuilder bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha) {
		Matrix4f matrix4f = matrixStackIn.last().pose();
		shapeIn.forAllEdges((x0, y0, z0, x1, y1, z1) -> {
			bufferIn.vertex(matrix4f, (float) (x0 + xIn), (float) (y0 + yIn), (float) (z0 + zIn)).color(red, green, blue, alpha).endVertex();
			bufferIn.vertex(matrix4f, (float) (x1 + xIn), (float) (y1 + yIn), (float) (z1 + zIn)).color(red, green, blue, alpha).endVertex();
		});
	}

	private static List<Face> makeMesh(final World world, final Entity viewer) {
		final List<Face> meshFaces = new LinkedList<>();
//		BlockPos base = new BlockPos(viewer.chunkCoordX << 4, viewer.chunkCoordY << 4, viewer.chunkCoordZ << 4);
		BlockPos base = viewer.blockPosition().offset(0, 2, 0);

		final int meshSizeX = 16;
		final int meshSizeY = 16;
		final int meshSizeZ = 16;

		// Make this mesh centred around the base
		final int startX = base.getX() - meshSizeX / 2;
		final int startY = base.getY() - meshSizeY / 2;
		final int startZ = base.getZ() - meshSizeZ / 2;

		SurfaceNets.generate(
			startX, startY, startZ,
			meshSizeX, meshSizeY, meshSizeZ,
			world, NoCubes.smoothableHandler::isSmoothable, DEBUGGING,
			(pos, mask) -> true,
			(pos, face) -> meshFaces.add(new Face(
				face.v0.add(startX, startY, startZ).copy(),
				face.v1.add(startX, startY, startZ).copy(),
				face.v2.add(startX, startY, startZ).copy(),
				face.v3.add(startX, startY, startZ).copy()
			))
		);

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

//		final double x = viewer.getPosX();
//		final double y = viewer.getPosY();
//		final double z = viewer.getPosZ();
//		Vec v0 = Vec.of(x + 0.5, y - 1, z + 0.5);
//		Vec v1 = Vec.of(x - 0.5, y - 1, z + 0.5);
//		Vec v2 = Vec.of(x - 0.5, y - 1, z - 0.5);
//		Vec v3 = Vec.of(x + 0.5, y - 1, z - 0.5);
//
//		meshFaces.add(Face.of(v0, v1, v2, v3));

		return meshFaces;
	}

//	private static Face toFace(final Vec v0, final Vec v1) {
//		Vec v2 = Vec.of(v1);
//		v2.y += 1;
//		Vec v3 = Vec.of(v0);
//		v3.y += 1;
//		return Face.of(v0, v1, v2, v3);
//	}

}
