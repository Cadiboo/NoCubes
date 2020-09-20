package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.client.render.util.Face;
import io.github.cadiboo.nocubes.client.render.util.ReusableCache;
import io.github.cadiboo.nocubes.client.render.util.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.gen.WorldGenRegion;

import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * @author Cadiboo
 */
public class SurfaceNets {

	public static final int[] CUBE_EDGES = new int[24];
	public static final int[] EDGE_TABLE = new int[256];

	// because the tables are so big we compute them in a static {} instead of hardcoding them (I think)
	static {
		generateCubeEdgesTable();
		generateIntersectionTable();
	}

	/**
	 * Utility function to build a table of possible edges for a cube with each
	 * pair of points representing one edge i.e. [0,1,0,2,0,4,...] would be the
	 * edges from points 0 to 1, 0 to 2, and 0 to 4 respectively:
	 *
	 * <pre>
	 *  y         z
	 *  ^        /
	 *  |
	 *    6----7
	 *   /|   /|
	 *  4----5 |
	 *  | 2--|-3
	 *  |/   |/
	 *  0----1   --> x
	 * </pre>
	 */
	private static void generateCubeEdgesTable() {

		//Initialize the cube_edges table
		// This is just the vertex number (number of corners) of each cube
		int cubeEdgesIndex = 0;
		// 8 is the number of corners for a cube
		for (byte cubeCornerIndex = 0; cubeCornerIndex < 8; ++cubeCornerIndex) {
			for (int em = 1; em <= 4; em <<= 1) {
				int j = cubeCornerIndex ^ em;
				if (cubeCornerIndex <= j) {
					CUBE_EDGES[cubeEdgesIndex++] = cubeCornerIndex;
					CUBE_EDGES[cubeEdgesIndex++] = j;
				}
			}
		}
	}

	/**
	 * Build an intersection table. This is a 2^(cube config) -> 2^(edge config) map
	 * There is only one entry for each possible cube configuration
	 * and the output is a 12-bit vector enumerating all edges
	 * crossing the 0-level
	 */
	private static void generateIntersectionTable() {
		// nope, I don't understand this either
		// yay, Lookup Tables...
		// Initialize the intersection table.
		// This is a 2^(cube configuration) ->  2^(edge configuration) map
		// There is one entry for each possible cube configuration, and the output is a 12-bit vector enumerating all edges crossing the 0-level.
		for (short edgeTableIndex = 0; edgeTableIndex < 256; ++edgeTableIndex) {
			short em = 0;
			for (int cubeEdgesIndex = 0; cubeEdgesIndex < 24; cubeEdgesIndex += 2) {
				final boolean a = (edgeTableIndex & (1 << CUBE_EDGES[cubeEdgesIndex])) != 0;
				final boolean b = (edgeTableIndex & (1 << CUBE_EDGES[cubeEdgesIndex + 1])) != 0;
				em |= a != b ? 1 << (cubeEdgesIndex >> 1) : 0;
			}
			EDGE_TABLE[edgeTableIndex] = em;
		}
	}

	public static void generate(
		int startX, int startY, int startZ,
		int meshSizeX, int meshSizeY, int meshSizeZ,
		IBlockReader world, Predicate<BlockState> isSmoothable, ReusableCache<boolean[][][]> cache,
		MeshAction action
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
		final boolean[][][] binaryField = ReusableCache.getOrCreate(cache, () -> new boolean[maxZ][maxY][maxX]);
		{
			int i = 0;
			for (int z = 0; z < maxZ; z++) {
				for (int y = 0; y < maxY; y++) {
					for (int x = 0; x < maxX; x++, i++) {
						pos.setPos(worldXStart + x, worldYStart + y, worldZStart + z);
						if (world instanceof WorldGenRegion && !((WorldGenRegion) world).chunkExists(x << 16, z << 16))
							binaryField[z][y][x] = false;
						else
							binaryField[z][y][x] = isSmoothable.test(world.getBlockState(pos));
					}
				}
			}
		}

		final ArrayList<double[]> vertices = new ArrayList<>(0x180);
		int n = 0;
		// Appears to contain the multiplier for an axis.
		// The X axis is stored in columns, the Y axis is stored in rows and the Z axis is stored in slices.
		// (x, y, z) -> [z * maxX * maxY + y * maxX + x]
		// So the multiplier for X is 1, the multiplier for Y is maxX and the multiplier for z is maxX * maxY
		// Still no clue why the z axis gets inverted each pass, or how m and buff_no work
		final int[] R = {1, (maxX + 1), (maxX + 1) * (maxY + 1)};
		final float[] grid = new float[8];
		// Could be a boolean, either 1 or 0, gets flipped each time we go over a z slice
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
							face = Face.of(
								Vec.of(vertices.get(buffer[m])),
								Vec.of(vertices.get(buffer[m - dv])),
								Vec.of(vertices.get(buffer[m - du - dv])),
								Vec.of(vertices.get(buffer[m - du]))
							);
						else
							face = Face.of(
								Vec.of(vertices.get(buffer[m])),
								Vec.of(vertices.get(buffer[m - du])),
								Vec.of(vertices.get(buffer[m - du - dv])),
								Vec.of(vertices.get(buffer[m - dv]))
							);
						pos.setPos(worldXStart, worldYStart, worldZStart);
						pos.move(x, y, z);
						boolean done = !action.apply(pos, face);
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
