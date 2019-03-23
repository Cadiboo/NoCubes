package io.github.cadiboo.nocubes.mesh.generator;

import io.github.cadiboo.nocubes.mesh.IMeshGenerator;
import io.github.cadiboo.nocubes.util.IIsSmoothable;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Cadiboo
 */
public class SurfaceNets implements IMeshGenerator {

	private static final int[] CUBE_EDGES = new int[24];
	private static final int[] EDGE_TABLE = new int[256];

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

	@Override
	@Nonnull
	public HashMap<Vec3b, FaceList> generateChunk(@Nonnull final float[] data, @Nonnull final byte[] dims) {

		final int[] edge_table = EDGE_TABLE;
		final int[] cube_edges = CUBE_EDGES;

		final ArrayList<float[]> vertices = new ArrayList<>();
		int n = 0;
		final byte[] x = {0, 0, 0};
		final int[] R = {1, (dims[0] + 1), (dims[0] + 1) * (dims[1] + 1)};
		final float[] grid = new float[8];
		int buf_no = 1;

		final int[] buffer = new int[R[2] * 2];

		final HashMap<Vec3b, FaceList> posToFaces = new HashMap<>();

		//March over the voxel grid
		for (x[2] = 0; x[2] < dims[2] - 1; ++x[2], n += dims[0], buf_no ^= 1, R[2] = -R[2]) {

			//m is the pointer into the buffer we are going to use.
			//This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(
			//The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int m = 1 + (dims[0] + 1) * (1 + buf_no * (dims[1] + 1));

			for (x[1] = 0; x[1] < dims[1] - 1; ++x[1], ++n, m += 2)
				for (x[0] = 0; x[0] < dims[0] - 1; ++x[0], ++n, ++m) {

					//Read in 8 field values around this vertex and store them in an array
					//Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
					int mask = 0, g = 0, idx = n;
					for (int k = 0; k < 2; ++k, idx += dims[0] * (dims[1] - 2))
						for (int j = 0; j < 2; ++j, idx += dims[0] - 2)
							for (byte i = 0; i < 2; ++i, ++g, ++idx) {
								float p = data[idx];
								grid[g] = p;
								mask |= (p < 0) ? (1 << g) : 0;
							}

					//Check for early termination if cell does not intersect boundary
					if (mask == 0 || mask == 0xff) {
						continue;
					}

					//Sum up edge intersections
					int edge_mask = edge_table[mask];
					final float[] v = {0, 0, 0};
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
						final int e0 = cube_edges[i << 1];
						final int e1 = cube_edges[(i << 1) + 1];
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
					for (int i = 0; i < 3; ++i) {
						v[i] = x[i] + s * v[i];
					}

					//Add vertex to buffer, store pointer to vertex index in buffer
					buffer[m] = vertices.size();
					vertices.add(v);

					final FaceList faces = FaceList.retain();

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
						if (x[iu] == 0 || x[iv] == 0) {
							continue;
						}

						//Otherwise, look up adjacent edges in buffer
						final int du = R[iu];
						final int dv = R[iv];

						//Remember to flip orientation depending on the sign of the corner.
						if ((mask & 1) != 0) {
							faces.add(
									Face.retain(
											Vec3.retain(vertices.get(buffer[m])),
											Vec3.retain(vertices.get(buffer[m - du])),
											Vec3.retain(vertices.get(buffer[m - du - dv])),
											Vec3.retain(vertices.get(buffer[m - dv]))
									)
							);
						} else {
							faces.add(
									Face.retain(
											Vec3.retain(vertices.get(buffer[m])),
											Vec3.retain(vertices.get(buffer[m - dv])),
											Vec3.retain(vertices.get(buffer[m - du - dv])),
											Vec3.retain(vertices.get(buffer[m - du]))
									)
							);
						}
					}
					posToFaces.put(Vec3b.retain(x[0], x[1], x[2]), faces);
				}
		}

		return posToFaces;

	}

	@Override
	@Nonnull
	public FaceList generateBlock(@Nonnull final float[] data, @Nonnull final byte[] dims) {

		final int[] edge_table = EDGE_TABLE;
		final int[] cube_edges = CUBE_EDGES;

		final ArrayList<float[]> vertices = new ArrayList<>();
		int n = 0;
		final byte[] x = {0, 0, 0};
		final int[] R = {1, (dims[0] + 1), (dims[0] + 1) * (dims[1] + 1)};
		final float[] grid = new float[8];
		int buf_no = 1;

		final int[] buffer = new int[R[2] * 2];

		final FaceList faces = FaceList.retain();

		//March over the voxel grid
		for (x[2] = 0; x[2] < dims[2] - 1; ++x[2], n += dims[0], buf_no ^= 1, R[2] = -R[2]) {

			//m is the pointer into the buffer we are going to use.
			//This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(
			//The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int m = 1 + (dims[0] + 1) * (1 + buf_no * (dims[1] + 1));

			for (x[1] = 0; x[1] < dims[1] - 1; ++x[1], ++n, m += 2)
				for (x[0] = 0; x[0] < dims[0] - 1; ++x[0], ++n, ++m) {

					//Read in 8 field values around this vertex and store them in an array
					//Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
					int mask = 0, g = 0, idx = n;
					for (int k = 0; k < 2; ++k, idx += dims[0] * (dims[1] - 2))
						for (int j = 0; j < 2; ++j, idx += dims[0] - 2)
							for (byte i = 0; i < 2; ++i, ++g, ++idx) {
								float p = data[idx];
								grid[g] = p;
								mask |= (p < 0) ? (1 << g) : 0;
							}

					//Check for early termination if cell does not intersect boundary
					if (mask == 0 || mask == 0xff) {
						continue;
					}

					//Sum up edge intersections
					int edge_mask = edge_table[mask];
					final float[] v = {0, 0, 0};
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
						final int e0 = cube_edges[i << 1];
						final int e1 = cube_edges[(i << 1) + 1];
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
					for (int i = 0; i < 3; ++i) {
						v[i] = x[i] + s * v[i];
					}

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
						if (x[iu] == 0 || x[iv] == 0) {
							continue;
						}

						//Otherwise, look up adjacent edges in buffer
						final int du = R[iu];
						final int dv = R[iv];

						//Remember to flip orientation depending on the sign of the corner.
						if ((mask & 1) != 0) {
							faces.add(
									Face.retain(
											Vec3.retain(vertices.get(buffer[m])),
											Vec3.retain(vertices.get(buffer[m - du])),
											Vec3.retain(vertices.get(buffer[m - du - dv])),
											Vec3.retain(vertices.get(buffer[m - dv]))
									)
							);
						} else {
							faces.add(
									Face.retain(
											Vec3.retain(vertices.get(buffer[m])),
											Vec3.retain(vertices.get(buffer[m - dv])),
											Vec3.retain(vertices.get(buffer[m - du - dv])),
											Vec3.retain(vertices.get(buffer[m - du]))
									)
							);
						}
					}
				}
		}

		return faces;
	}

	@Override
	public byte getSizeXExtension() {
		return 1;
	}

	@Override
	public byte getSizeYExtension() {
		return 1;
	}

	@Override
	public byte getSizeZExtension() {
		return 1;
	}

	@Nonnull
	@Override
	public FaceList generateBlock(@Nonnull final BlockPos pos, @Nonnull final IBlockAccess blockAccess, @Nonnull final IIsSmoothable isSmoothable) {
		return FaceList.retain();
	}

}
