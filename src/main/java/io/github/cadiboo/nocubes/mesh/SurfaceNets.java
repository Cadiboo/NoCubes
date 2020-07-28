package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.client.render.util.Face;
import io.github.cadiboo.nocubes.client.render.util.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IWorld;

import java.util.ArrayList;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.client.render.SurfaceNets.CUBE_EDGES;
import static io.github.cadiboo.nocubes.client.render.SurfaceNets.EDGE_TABLE;

/**
 * @author Cadiboo
 */
public class SurfaceNets {

	public static void generate(
		int startX, int startY, int startZ,
		int meshSizeX, int meshSizeY, int meshSizeZ,
		IBlockDisplayReader world, Predicate<BlockState> isSmoothable, MeshAction action
	) {
		// Seams appear in the meshes, surface nets generates a mesh 1 smaller than it "should"
		meshSizeX += 1;
		meshSizeY += 1;
		meshSizeZ += 1;
		// Surface needs data for n+1 to generate n
		// Need an extra block on each axis because surface nets
		final int worldXStart = startX - 1;
		final int worldYStart = startY - 1;
		final int worldZStart = startZ - 1;
		final int maxX = meshSizeX + 1;
		final int maxY = meshSizeY + 1;
		final int maxZ = meshSizeZ + 1;

		final BlockPos.Mutable pos = new BlockPos.Mutable();

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
						binaryField[z][y][x] = isSmoothable.test(world.getBlockState(pos));
					}
				}
			}
		}

		final ArrayList<double[]> vertices = new ArrayList<>(0x180);
		int n = 0;
		final int[] R = {1, (maxX + 1), (maxX + 1) * (maxY + 1)};
		final float[] grid = new float[8];
		int buf_no = 1;

		final int[] buffer = new int[R[2] * 2];

		//March over the voxel grid
		for (int z = 0; z < meshSizeZ; ++z, n += maxX, buf_no ^= 1, R[2] = -R[2]) {

			//m is the pointer into the buffer we are going to use.
			//This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(
			//The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int m = 1 + (maxX + 1) * (1 + buf_no * (maxY + 1));

			for (int y = 0; y < meshSizeY; ++y, ++n, m += 2) {
				for (int x = 0; x < meshSizeX; ++x, ++n, ++m) {

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
					v[0] = -0.5 + 0 + x + s * v[0];
					v[1] = -0.5 + 0 + y + s * v[1];
					v[2] = -0.5 + 0 + z + s * v[2];

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
						if (((iu == 0 && x == 0) || (iu == 1 && y == 0) || (iu == 2 && z == 0)) || ((iv == 0 && x == 0) || (iv == 1 && y == 0) || (iv == 2 && z == 0))) {
							continue;
						}

						//Otherwise, look up adjacent edges in buffer
						final int du = R[iu];
						final int dv = R[iv];

						final Face face;
						//Remember to flip orientation depending on the sign of the corner.
						if ((mask & 1) != 0)
							// Traditionally ([m], [m - dv], [m - du - dv], [m - du])
							// but minecraft to expects a different orientation
							face = Face.of(
								Vec.of(vertices.get(buffer[m - du - dv])),
								Vec.of(vertices.get(buffer[m - du])),
								Vec.of(vertices.get(buffer[m])),
								Vec.of(vertices.get(buffer[m - dv]))
							);
						else
							// Traditionally ([m], [m - du], [m - du - dv], [m - dv])
							// but minecraft to expects a different orientation
							face = Face.of(
								Vec.of(vertices.get(buffer[m - du - dv])),
								Vec.of(vertices.get(buffer[m - dv])),
								Vec.of(vertices.get(buffer[m])),
								Vec.of(vertices.get(buffer[m - du]))
							);
						pos.setPos(worldXStart, worldYStart, worldZStart);
						pos.move(x, y, z);
						boolean done = !action.apply(pos, face);
						for (final Vec vertex : face.getVertices())
							vertex.close();
						face.close();
						if (done)
							return;
					}
				}
			}
		}
	}

	public interface MeshAction {

		boolean apply(BlockPos.Mutable pos, Face face);

	}

}
