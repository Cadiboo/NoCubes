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

import java.util.ArrayList;

import static io.github.cadiboo.nocubes.client.render.MarchingCubes.TRI_TABLE;
import static io.github.cadiboo.nocubes.client.render.SurfaceNets.*;

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

		final int maxX = 92;
		final int maxY = 92;
		final int maxZ = 92;

		// Make this mesh centred around the base
		final int worldXStart = base.getX() - maxX / 2;
		final int worldYStart = base.getY() - maxY / 2;
		final int worldZStart = base.getZ() - maxZ / 2;

		final BlockPos.Mutable pos = new BlockPos.Mutable();
		final SmoothableHandler handler = NoCubes.smoothableHandler;


		/*
		 * From Wikipedia:
		 * Apply a threshold to the 2D field to make a binary image containing:
		 * - 1 where the data value is above the isovalue
		 * - 0 where the data value is below the isovalue
		 */
		// The area, converted from a BlockState[] to an isSmoothable[]
		// binaryField[x, y, z] = isSmoothable(chunk[x, y, z]);
		boolean[][][] binaryField = new boolean[maxZ][maxY][maxX];
		{
			int i = 0;
			for (int z = 0; z < maxZ; z++) {
				for (int y = 0; y < maxY; y++) {
					for (int x = 0; x < maxX; x++, i++) {
						pos.setPos(worldXStart + x, worldYStart + y, worldZStart + z);
						binaryField[z][y][x] = handler.isSmoothable(world.getBlockState(pos));
					}
				}
			}
		}

		/*
		 * From Wikipedia:
		 * Every 2x2 block of pixels in the binary image forms a contouring cell, so the whole image is represented by a grid of such cells (shown in green in the picture below).
		 * Note that this contouring grid is one cell smaller in each direction than the original 2D field.
		 */
		final int cellsMaxX = maxX - 1;
		final int cellsMaxY = maxY - 1;
		final int cellsMaxZ = maxZ - 1;

		final ArrayList<double[]> vertices = new ArrayList<>(0x180);
		int n = 0;
		final int[] R = {1, (maxX + 1), (maxX + 1) * (maxY + 1)};
		final float[] grid = new float[8];
		int buf_no = 1;

		final int[] buffer = new int[R[2] * 2];

		//March over the voxel grid
		for (int z = 0; z < cellsMaxZ; ++z, n += maxX, buf_no ^= 1, R[2] = -R[2]) {

			//m is the pointer into the buffer we are going to use.
			//This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(
			//The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int m = 1 + (maxX + 1) * (1 + buf_no * (maxY + 1));

			for (int y = 0; y < cellsMaxY; ++y, ++n, m += 2)
				for (int x = 0; x < cellsMaxX; ++x, ++n, ++m) {

					//Read in 8 field values around this vertex and store them in an array
					//Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
					int mask = 0, g = 0, idx = n;
					for (int z0 = 0; z0 < 2; ++z0, idx += maxX * (maxY - 2))
						for (int y0 = 0; y0 < 2; ++y0, idx += maxX - 2)
							for (byte x0 = 0; x0 < 2; ++x0, ++g, ++idx) {
								float p = binaryField[z + z0][y + y0][x + x0] ? 1 : -1;
								grid[g] = p;
								mask |= (p < 0) ? (1 << g) : 0;
							}

					//Check for early termination if cell does not intersect boundary
					if (mask == 0 || mask == 0xff) {
						continue;
					}

					//Sum up edge intersections
					int edge_mask = EDGE_TABLE[mask];
					final double[] v = {0, 0, 0};
					int e_count = 0;

					//For every edge of the cube...
					for (int i = 0; i < 12; ++i) {

						//Use edge mask to check if it is crossed
						if ((edge_mask & (1 << i)) == 0) {
							continue;
						}

						//If it did, increment number of edge crossings
						++e_count;

						//Now find the point of intersection
						//Unpack vertices
						final int e0 = CUBE_EDGES[i << 1];
						final int e1 = CUBE_EDGES[(i << 1) + 1];
						//Unpack grid values
						final float g0 = grid[e0];
						final float g1 = grid[e1];
						//Compute point of intersection
						float t = g0 - g1;
						if (Math.abs(t) > 1e-6) {
							t = g0 / t;
						} else {
							continue;
						}

						//Interpolate vertices and add up intersections (this can be done without multiplying)
						for (int j = 0, k = 1; j < 3; ++j, k <<= 1) {
							final int a = e0 & k;
							final int b = e1 & k;
							if (a != b) {
								v[j] += a != 0 ? 1F - t : t;
							} else {
								v[j] += a != 0 ? 1F : 0;
							}
						}
					}

					//Now we just average the edge intersections and add them to coordinate
					// 1.0F = isosurfaceLevel
					float s = 1.0F / e_count;
					v[0] = 0.5 + worldXStart + x + s * v[0];
					v[1] = 0.5 + worldYStart + y + s * v[1];
					v[2] = 0.5 + worldZStart + z + s * v[2];

					//Add vertex to buffer, store pointer to vertex index in buffer
					buffer[m] = vertices.size();
					vertices.add(v);

					//Now we need to add faces together, to do this we just loop over 3 basis components
					for (int i = 0; i < 3; ++i) {
						//The first three entries of the edge_mask count the crossings along the edge
						if ((edge_mask & (1 << i)) == 0) {
							continue;
						}

						// i = axes we are point along.  iu, iv = orthogonal axes
						final int iu = (i + 1) % 3;
						final int iv = (i + 2) % 3;

						//If we are on a boundary, skip it
						if (((iu == 0 && x == 0) || (iu == 1 && y == 0)|| (iu == 2 && z == 0)) || ((iv == 0 && x == 0) || (iv == 1 && y == 0)|| (iv == 2 && z == 0))) {
							continue;
						}

						//Otherwise, look up adjacent edges in buffer
						final int du = R[iu];
						final int dv = R[iv];

						//Remember to flip orientation depending on the sign of the corner.
						if ((mask & 1) != 0) {
							mesh.faces.add(
								Face.of(
									Vec.of(vertices.get(buffer[m])),
									Vec.of(vertices.get(buffer[m - du])),
									Vec.of(vertices.get(buffer[m - du - dv])),
									Vec.of(vertices.get(buffer[m - dv]))
								)
							);
						} else {
							mesh.faces.add(
								Face.of(
									Vec.of(vertices.get(buffer[m])),
									Vec.of(vertices.get(buffer[m - dv])),
									Vec.of(vertices.get(buffer[m - du - dv])),
									Vec.of(vertices.get(buffer[m - du]))
								)
							);
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

	private static Face toFace(final Vec v0, final Vec v1) {
		Vec v2 = Vec.of(v1);
		v2.y += 1;
		Vec v3 = Vec.of(v0);
		v3.y += 1;
		return Face.of(v0, v1, v2, v3);
	}

}
