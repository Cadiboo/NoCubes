package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.util.Face;
import io.github.cadiboo.nocubes.client.render.util.FaceList;
import io.github.cadiboo.nocubes.client.render.util.Vec;
import io.github.cadiboo.nocubes.smoothable.SmoothableHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
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
		if (!Screen.hasAltDown())
			return;

		final Minecraft minecraft = Minecraft.getInstance();
		Entity viewer = minecraft.gameRenderer.getActiveRenderInfo().getRenderViewEntity();
		if (viewer == null)
			return;

		final World world = viewer.world;
		if (world == null)
			return;

		if (cache == null || world.getGameTime() % 5 == 0) {
			if (cache != null) {
				final FaceList faces = cache.faces;
				for (final Face face : faces) {
					for (final Vec vertex : face.getVertices())
						vertex.close();
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

//		final BlockPos viewerPos = new BlockPos(viewer.getPosition());
//		BlockPos.getAllInBoxMutable(viewerPos.add(-5, -5, -5), viewerPos.add(5, 5, 5)).forEach(blockPos -> {
//			if (NoCubes.smoothableHandler.isSmoothable(viewer.world.getBlockState(blockPos)))
//				drawShape(matrixStack, bufferBuilder, VoxelShapes.fullCube(), -d0 + blockPos.getX(), -d1 + blockPos.getY(), -d2 + blockPos.getZ(), 0.0F, 1.0F, 1.0F, 0.4F);
//		});
//
//		// Draw nearby collisions
//		viewer.world.getCollisionShapes(viewer, viewer.getBoundingBox().grow(5.0D)).forEach(voxelShape -> {
//			drawShape(matrixStack, bufferBuilder, voxelShape, -d0, -d1, -d2, 0.0F, 1.0F, 1.0F, 0.4F);
//		});
//		// Draw player intersecting collisions
//		viewer.world.getCollisionShapes(viewer, viewer.getBoundingBox()).forEach(voxelShape -> {
//			drawShape(matrixStack, bufferBuilder, voxelShape, -d0, -d1, -d2, 1.0F, 0.0F, 0.0F, 0.4F);
//		});

		Matrix4f matrix4f = matrixStack.getLast().getMatrix();
		for (final Face face : cache.faces) {
			Vec v0 = face.v0;
			Vec v1 = face.v1;
			Vec v2 = face.v2;
			Vec v3 = face.v3;
			bufferBuilder.pos(matrix4f, (float) (v0.x + -d0), (float) (v0.y + -d1), (float) (v0.z + -d2)).color(0, 1, 0, 1F).endVertex();
			bufferBuilder.pos(matrix4f, (float) (v1.x + -d0), (float) (v1.y + -d1), (float) (v1.z + -d2)).color(0, 1, 0, 1F).endVertex();
			bufferBuilder.pos(matrix4f, (float) (v1.x + -d0), (float) (v1.y + -d1), (float) (v1.z + -d2)).color(0, 1, 0, 1F).endVertex();
			bufferBuilder.pos(matrix4f, (float) (v2.x + -d0), (float) (v2.y + -d1), (float) (v2.z + -d2)).color(0, 1, 0, 1F).endVertex();
			bufferBuilder.pos(matrix4f, (float) (v2.x + -d0), (float) (v2.y + -d1), (float) (v2.z + -d2)).color(0, 1, 0, 1F).endVertex();
			bufferBuilder.pos(matrix4f, (float) (v3.x + -d0), (float) (v3.y + -d1), (float) (v3.z + -d2)).color(0, 1, 0, 1F).endVertex();
			bufferBuilder.pos(matrix4f, (float) (v3.x + -d0), (float) (v3.y + -d1), (float) (v3.z + -d2)).color(0, 1, 0, 1F).endVertex();
			bufferBuilder.pos(matrix4f, (float) (v0.x + -d0), (float) (v0.y + -d1), (float) (v0.z + -d2)).color(0, 1, 0, 1F).endVertex();
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
//		BlockPos base = new BlockPos(viewer.chunkCoordX << 4, viewer.chunkCoordY << 4, viewer.chunkCoordZ << 4);
		BlockPos base = viewer.getPosition();

		final int maxX = 32;
		final int maxY = 32;
		final int maxZ = 32;

		// Make this mesh centred around the base
		final int worldXStart = base.getX() - maxX / 2;
		final int worldYStart = base.getY() - maxY / 2;
		final int worldZStart = base.getZ() - maxZ / 2;

		final BlockPos.Mutable pos = new BlockPos.Mutable();
		final SmoothableHandler handler = NoCubes.smoothableHandler;

		// Declare up here to save memory
		boolean[][] binaryField = new boolean[maxZ][maxX];

		for (int y = 0; y < maxY; y++) {

			/*
			 * From Wikipedia:
			 * Apply a threshold to the 2D field to make a binary image containing:
			 * - 1 where the data value is above the isovalue
			 * - 0 where the data value is below the isovalue
			 */
			// The area, converted from a BlockState[] to an isSmoothable[]
			// binaryField[x, y, z] = isSmoothable(chunk[x, y, z]);
			{
				int i = 0;
				for (int z = 0; z < maxZ; z++) {
					for (int x = 0; x < maxX; x++, i++) {
						pos.setPos(worldXStart + x, worldYStart + y, worldZStart + z);
						binaryField[z][x] = handler.isSmoothable(world.getBlockState(pos));
					}
				}
			}

			/*
			 * From Wikipedia:
			 * Every 2x2 block of pixels in the binary image forms a contouring cell, so the whole image is represented by a grid of such cells (shown in green in the picture below).
			 * Note that this contouring grid is one cell smaller in each direction than the original 2D field.
			 */
			final int cellsMaxX = maxX - 1;
			final int cellsMaxZ = maxZ - 1;
			{
				int i = 0;
				for (int z = 0; z < cellsMaxZ; z++) {
					for (int x = 0; x < cellsMaxX; x++, i++) {
						byte maskNeighborsSmoothable = 0;
						// For each cell in the contouring grid:
						// Compose the 4 bits at the corners of the cell to build a binary index: walk around the cell in a clockwise direction appending the bit to the index, using bitwise OR and left-shift, from most significant bit at the top left, to least significant bit at the bottom left.
						// The resulting 4-bit index can have 16 possible values in the range 0–15.
						// (0, 0)
						if (binaryField[z + 0][x + 0])
							maskNeighborsSmoothable |= 1 << 0;
						// (0, 1)
						if (binaryField[z + 0][x + 1])
							maskNeighborsSmoothable |= 1 << 1;
						// (1, 1)
						if (binaryField[z + 1][x + 1])
							maskNeighborsSmoothable |= 1 << 2;
						// (1, 0)
						if (binaryField[z + 1][x + 0])
							maskNeighborsSmoothable |= 1 << 3;

						double wx = worldXStart + x + 0.5;
						double wy = worldYStart + y + 1;
						double wz = worldZStart + z + 0.5;
						switch (maskNeighborsSmoothable) {
							case 0:
							case 15:
								break;
							case 1:
							case 14: {
								Vec v1 = Vec.of(wx, wy, wz + 0.5);
								Vec v0 = Vec.of(wx + 0.5, wy, wz);
								Vec v2 = Vec.of(v0);
								Vec v3 = Vec.of(v1);
								mesh.faces.add(Face.of(v0, v1, v2, v3));
								break;
							}
							case 2:
							case 13: {
								Vec v0 = Vec.of(wx + 0.5, wy, wz);
								Vec v1 = Vec.of(wx + 1, wy, wz + 0.5);
								Vec v2 = Vec.of(v0);
								Vec v3 = Vec.of(v1);
								mesh.faces.add(Face.of(v0, v1, v2, v3));
								break;
							}
							case 3:
							case 12: {
								Vec v0 = Vec.of(wx, wy, wz + 0.5);
								Vec v1 = Vec.of(wx + 1, wy, wz + 0.5);
								Vec v2 = Vec.of(v0);
								Vec v3 = Vec.of(v1);
								mesh.faces.add(Face.of(v0, v1, v2, v3));
								break;
							}
							case 4:
							case 11: {
								Vec v0 = Vec.of(wx + 0.5, wy, wz + 1);
								Vec v1 = Vec.of(wx + 1, wy, wz + 0.5);
								Vec v2 = Vec.of(v0);
								Vec v3 = Vec.of(v1);
								mesh.faces.add(Face.of(v0, v1, v2, v3));
								break;
							}
							case 5: {
								{
									Vec v0 = Vec.of(wx, wy, wz + 0.5);
									Vec v1 = Vec.of(wx + 0.5, wy, wz + 1);
									Vec v2 = Vec.of(v0);
									Vec v3 = Vec.of(v1);
									mesh.faces.add(Face.of(v0, v1, v2, v3));
								}
								{
									Vec v0 = Vec.of(wx + 0.5, wy, wz);
									Vec v1 = Vec.of(wx + 1, wy, wz + 0.5);
									Vec v2 = Vec.of(v0);
									Vec v3 = Vec.of(v1);
									mesh.faces.add(Face.of(v0, v1, v2, v3));
								}
								break;
							}
							case 6:
							case 9: {
								Vec v0 = Vec.of(wx + 0.5, wy, wz);
								Vec v1 = Vec.of(wx + 0.5, wy, wz + 1);
								Vec v2 = Vec.of(v0);
								Vec v3 = Vec.of(v1);
								mesh.faces.add(Face.of(v0, v1, v2, v3));
								break;
							}
							case 7:
							case 8: {
								Vec v0 = Vec.of(wx, wy, wz + 0.5);
								Vec v1 = Vec.of(wx + 0.5, wy, wz + 1);
								Vec v2 = Vec.of(v0);
								Vec v3 = Vec.of(v1);
								mesh.faces.add(Face.of(v0, v1, v2, v3));
								break;
							}
							case 10: {
								{
									Vec v1 = Vec.of(wx, wy, wz + 0.5);
									Vec v0 = Vec.of(wx + 0.5, wy, wz);
									Vec v2 = Vec.of(v0);
									Vec v3 = Vec.of(v1);
									mesh.faces.add(Face.of(v0, v1, v2, v3));
								}
								{
									Vec v0 = Vec.of(wx + 0.5, wy, wz + 1);
									Vec v1 = Vec.of(wx + 1, wy, wz + 0.5);
									Vec v2 = Vec.of(v0);
									Vec v3 = Vec.of(v1);
									mesh.faces.add(Face.of(v0, v1, v2, v3));
								}
								break;
							}
						}
					}
				}
			}

		}

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
//		mesh.faces.add(Face.of(v0, v1, v2, v3));

		return mesh;
	}

}
