package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.client.render.util.Face;
import io.github.cadiboo.nocubes.client.render.util.ReusableCache;
import io.github.cadiboo.nocubes.client.render.util.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Written by Mikola Lysenko (C) 2012
 * Ported from JavaScript to Java and modified for NoCubes by Cadiboo.
 */
public class SurfaceNets {

	/**
	 * A list of vertices where x/y/z are represented as bits (either 0 or 1)
	 * E.g. (1, 0, 1) -> 101
	 * I think it also has some extra info
	 */
	public static final int[] CUBE_EDGES = new int[24];
	public static final int[] EDGE_TABLE = new int[256];
	public static final int MESH_SIZE_POSITIVE_EXTENSION = 1;
	public static final int MESH_SIZE_NEGATIVE_EXTENSION = 1;

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
		// Initialize the cube_edges table
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
		IBlockReader world, Predicate<BlockState> isSmoothable, ReusableCache<float[]> cache,
		MeshAction action
	) {
		// Seams appear in the meshes, surface nets generates a mesh 1 smaller than it "should"
		meshSizeX += MESH_SIZE_POSITIVE_EXTENSION;
		meshSizeY += MESH_SIZE_POSITIVE_EXTENSION;
		meshSizeZ += MESH_SIZE_POSITIVE_EXTENSION;
		// Surface needs data for n+1 to generate n
		// Need an extra block on each axis because surface nets
		final int worldXStart = startX - MESH_SIZE_NEGATIVE_EXTENSION;
		final int worldYStart = startY - MESH_SIZE_NEGATIVE_EXTENSION;
		final int worldZStart = startZ - MESH_SIZE_NEGATIVE_EXTENSION;
		final int maxX = meshSizeX + MESH_SIZE_NEGATIVE_EXTENSION;
		final int maxY = meshSizeY + MESH_SIZE_NEGATIVE_EXTENSION;
		final int maxZ = meshSizeZ + MESH_SIZE_NEGATIVE_EXTENSION;

		final BlockPos.Mutable pos = new BlockPos.Mutable();

		/*
		 * From Wikipedia:
		 * Apply a threshold to the 2D field to make a binary image containing:
		 * - 1 where the data value is above the isovalue
		 * - 0 where the data value is below the isovalue
		 */
		// The area, converted from a BlockState[] to an isSmoothable[]
		// densities[x, y, z] = isSmoothable(chunk[x, y, z]);
		final float[] densities = ReusableCache.getOrCreate(cache, () -> new float[maxZ * maxY * maxX]);
		{
			int i = 0;
			for (int z = 0; z < maxZ; ++z) {
				for (int y = 0; y < maxY; ++y) {
					for (int x = 0; x < maxX; ++x, ++i) {
						pos.setPos(worldXStart + x, worldYStart + y, worldZStart + z);
						densities[i] = isSmoothable.test(world.getBlockState(pos)) ? 1 : -1;
					}
				}
			}
		}

		final Face face = Face.of();
		final Face normal = Face.of(Vec.of(), Vec.of(), Vec.of(), Vec.of());
		final Vec averageNormal = Vec.of();

		int n = 0;
		// Appears to contain the multiplier for an axis.
		// The X axis is stored in columns, the Y axis is stored in rows and the Z axis is stored in slices.
		// (x, y, z) -> [z * maxX * maxY + y * maxX + x]
		// So the multiplier for X is 1, the multiplier for Y is maxX and the multiplier for z is maxX * maxY
		// Still no clue why the z axis gets inverted each pass, or how m and buff_no work
		final int[] axisMultipliers = {1, (maxX + 1), (maxX + 1) * (maxY + 1)};
		final float[] cornerValues = new float[8];
		// Could be a boolean, either 1 or 0, gets flipped each time we go over a z slice
		int buf_no = 1;

		// Contains all the vertices of the previous slice + space for vertices on the current slice
		// When we go to the next slice, we start reading from the side we were previously
		// writing to and start writing to the side we were previously reading from
		final Vec[] verticesBuffer = new Vec[axisMultipliers[2] * 2];

		//March over the voxel grid
		for (int z = 0; z < meshSizeZ; ++z, n += maxX, buf_no ^= 1, axisMultipliers[2] = -axisMultipliers[2]) {

			//m is the pointer into the buffer we are going to use.
			//The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int m = 1 + (maxX + 1) * (1 + buf_no * (maxY + 1));

			for (int y = 0; y < meshSizeY; ++y, ++n, m += 2) {
				for (int x = 0; x < meshSizeX; ++x, ++n, ++m) {

					//Read in 8 field values around this vertex and store them in an array
					//Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
					int mask = 0;
					int corner = 0;
					int idx = n;
					for (int cornerZ = 0; cornerZ < 2; ++cornerZ, idx += maxX * (maxY - 2))
						for (int cornerY = 0; cornerY < 2; ++cornerY, idx += maxX - 2)
							for (byte cornerX = 0; cornerX < 2; ++cornerX, ++corner, ++idx) {
								float p = densities[idx];
								cornerValues[corner] = p;
								mask |= (p < 0) ? (1 << corner) : 0;
							}

					// Check for early termination if cell does not intersect boundary
					// This cell is either entirely inside or entirely outside the isosurface
					if (mask == 0 || mask == 0xff)
						continue;

					// Sum up edge intersections
					int edge_mask = EDGE_TABLE[mask];
					final Vec vertex = Vec.of(0, 0, 0);
					int edgeCrossings = 0;

					// For every edge of the cube...
					for (int edge = 0; edge < 12; ++edge) {
						//Use edge mask to check if it is crossed
						if ((edge_mask & (1 << edge)) == 0)
							continue;

						// If it did, increment number of edge crossings
						++edgeCrossings;

						//Now find the point of intersection
						//Unpack vertices
						// These are vertices packed (x, y, z) -> zyx
						final int edgeStart = CUBE_EDGES[edge << 1];
						final int edgeEnd = CUBE_EDGES[(edge << 1) + 1];
						// Unpack values
						final float edgeStartValue = cornerValues[edgeStart];
						final float edgeEndValue = cornerValues[edgeEnd];
						//Compute point of intersection (the point where the isosurface is and the vertex is)
						float t = edgeStartValue - edgeEndValue;
						vertex.add(
							(edgeStart & 0b001) * (1.0 - t) + (edgeEnd & 0b001) * t,
							(edgeStart & 0b010) * (1.0 - t) + (edgeEnd & 0b010) * t,
							(edgeStart & 0b100) * (1.0 - t) + (edgeEnd & 0b100) * t
						);
					}

					//Now we just average the edge intersections and add them to coordinate
					// 1.0F = isosurfaceLevel
					float s = 1.0F / edgeCrossings;
					vertex.multiply(s);
					vertex.add(x - 0.5, y - 0.5, z - 0.5);

					// Add vertex to buffer
					verticesBuffer[m] = vertex;

					//Now we need to add faces together, to do this we just loop over 3 basis components
					for (int axis = 0; axis < 3; ++axis) {
						//The first three entries of the edge_mask count the crossings along the edge
						if ((edge_mask & (1 << axis)) == 0)
							continue;

						// axis = axes we are point along.  nextAxis, nextNextAxis = orthogonal axes
						final int nextAxis = (axis + 1) % 3;
						final int nextNextAxis = (axis + 2) % 3;

						// If we are on a boundary, skip it
						if ((nextAxis == 0 && x == 0) || (nextAxis == 1 && y == 0) || (nextAxis == 2 && z == 0))
							continue;
						else if ((nextNextAxis == 0 && x == 0) || (nextNextAxis == 1 && y == 0) || (nextNextAxis == 2 && z == 0))
							continue;

						//Otherwise, look up adjacent edges in buffer
						final int du = axisMultipliers[nextAxis];
						final int dv = axisMultipliers[nextNextAxis];

						face.v0 = verticesBuffer[m];
						// Remember to flip orientation depending on the sign of the corner.
						if ((mask & 1) != 0) {
							face.v1 = verticesBuffer[m - dv];
							face.v2 = verticesBuffer[m - du - dv];
							face.v3 = verticesBuffer[m - du];
						} else {
							face.v1 = verticesBuffer[m - du];
							face.v2 = verticesBuffer[m - du - dv];
							face.v3 = verticesBuffer[m - dv];
						}
						pos.setPos(worldXStart + x, worldYStart + y, worldZStart + z);
						Vec.normal(face.v3, face.v0, face.v1, normal.v0).multiply(-1);
						Vec.normal(face.v0, face.v1, face.v2, normal.v1).multiply(-1);
						Vec.normal(face.v1, face.v2, face.v3, normal.v2).multiply(-1);
						Vec.normal(face.v2, face.v3, face.v0, normal.v3).multiply(-1);
						averageNormal.x = (normal.v0.x + normal.v1.x + normal.v2.x + normal.v3.x) / 4;
						averageNormal.y = (normal.v0.y + normal.v1.y + normal.v2.y + normal.v3.y) / 4;
						averageNormal.z = (normal.v0.z + normal.v1.z + normal.v2.z + normal.v3.z) / 4;
						final Direction normalDirection = Vec.getDirectionFromNormal(averageNormal);
						boolean done = !action.apply(pos, face, normal, normalDirection);
						if (done) {
							close(verticesBuffer, face, normal, averageNormal);
							return;
						}
					}
				}
			}
		}
		close(verticesBuffer, face, normal, averageNormal);
	}

	private static void close(Vec[] verticesBuffer, Face face, Face normal, Vec averageNormal) {
		for (int i = verticesBuffer.length - 1; i >= 0; i--) {
			final Vec vec = verticesBuffer[i];
			if (vec != null)
				vec.close();
		}
		if (face != null) {
			if (face.v0 != null)
				face.v0.close();
			if (face.v1 != null)
				face.v1.close();
			if (face.v2 != null)
				face.v2.close();
			if (face.v3 != null)
				face.v3.close();
			face.close();
		}
		if (normal != null) {
			if (normal.v0 != null)
				normal.v0.close();
			if (normal.v1 != null)
				normal.v1.close();
			if (normal.v2 != null)
				normal.v2.close();
			if (normal.v3 != null)
				normal.v3.close();
			normal.close();
		}
		if (averageNormal != null)
			averageNormal.close();
	}

	public static void generateCollisions(
		int startX, int startY, int startZ,
		int meshSizeX, int meshSizeY, int meshSizeZ,
		IBlockReader world, Predicate<BlockState> isSmoothable,
		ReusableCache<BlockState[]> blockStatesCache, ReusableCache<boolean[]> binaryFieldCache,
		CollisionMeshAction action
	) {
		// Seams appear in the meshes, surface nets generates a mesh 1 smaller than it "should"
		meshSizeX += MESH_SIZE_POSITIVE_EXTENSION;
		meshSizeY += MESH_SIZE_POSITIVE_EXTENSION;
		meshSizeZ += MESH_SIZE_POSITIVE_EXTENSION;
		// Surface needs data for n+1 to generate n
		// Need an extra block on each axis because surface nets
		final int worldXStart = startX - MESH_SIZE_NEGATIVE_EXTENSION;
		final int worldYStart = startY - MESH_SIZE_NEGATIVE_EXTENSION;
		final int worldZStart = startZ - MESH_SIZE_NEGATIVE_EXTENSION;
		final int maxX = meshSizeX + MESH_SIZE_NEGATIVE_EXTENSION;
		final int maxY = meshSizeY + MESH_SIZE_NEGATIVE_EXTENSION;
		final int maxZ = meshSizeZ + MESH_SIZE_NEGATIVE_EXTENSION;

		final BlockPos.Mutable pos = new BlockPos.Mutable();

		final int arraySize = maxZ * maxY * maxX;
		final BlockState[] blockStates = ReusableCache.getOrCreate(blockStatesCache, a -> a.length == arraySize, () -> new BlockState[arraySize]);
		/*
		 * From Wikipedia:
		 * Apply a threshold to the 2D field to make a binary image containing:
		 * - 1 where the data value is above the isovalue
		 * - 0 where the data value is below the isovalue
		 */
		// The area, converted from a BlockState[] to an isSmoothable[]
		// binaryField[x, y, z] = isSmoothable(chunk[x, y, z]);
		final boolean[] binaryField = ReusableCache.getOrCreate(binaryFieldCache, a -> a.length == arraySize, () -> new boolean[arraySize]);
		{
			int i = 0;
			for (int z = 0; z < maxZ; z++) {
				for (int y = 0; y < maxY; y++) {
					for (int x = 0; x < maxX; x++, i++) {
						pos.setPos(worldXStart + x, worldYStart + y, worldZStart + z);
						final BlockState state = world.getBlockState(pos);
						blockStates[i] = state;
						binaryField[i] = isSmoothable.test(state);
					}
				}
			}
		}

		int n = 0;
		final int[] R = {1, (maxX + 1), (maxX + 1) * (maxY + 1)};
		final float[] grid = new float[8];
		int buf_no = 1;

		final Vec[] buffer = new Vec[R[2] * 2];

		final Face face = Face.of();
		final double[] v = {0, 0, 0};

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
								float p = binaryField[idx] ? 1 : -1;
								grid[g] = p;
								mask |= (p < 0) ? (1 << g) : 0;
							}

					// 0 entirely inside surface, just generate a full cube
					if (mask == 0 && !action.apply(blockStates[n], pos, face)) {
						close(buffer, face);
						return;
					}

					//Check for early termination if cell does not intersect boundary
					if (mask == 0 || mask == 0xff) {
						continue;
					}

					//Sum up edge intersections
					int edge_mask = EDGE_TABLE[mask];
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
					buffer[m] = Vec.of(v);

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

						//Remember to flip orientation depending on the sign of the corner.
						face.v0 = buffer[m];
						if ((mask & 1) != 0) {
							face.v1 = buffer[m - dv];
							face.v2 = buffer[m - du - dv];
							face.v3 = buffer[m - du];
						} else {
							face.v3 = buffer[m - du];
							face.v2 = buffer[m - du - dv];
							face.v1 = buffer[m - dv];
						}

						pos.setPos(worldXStart + x, worldYStart + y, worldZStart + z);
						if (!action.apply(blockStates[n], pos, face)) {
							close(buffer, face);
							return;
						}
					}
				}
			}
		}
		close(buffer, face);
	}

	private static void close(final Vec[] vertices, final Face face) {
		for (int i = vertices.length - 1; i >= 0; --i)
			vertices[i].close();
		face.close();
	}

	public interface MeshAction {

		boolean apply(BlockPos.Mutable pos, Face face, Face normals, Direction normal);

	}

	public interface CollisionMeshAction {

		boolean apply(BlockState state, BlockPos.Mutable pos, @Nullable Face face);

	}

	private static class MeshData {

	}

	public static class MeshSpliterator extends Spliterators.AbstractSpliterator<MeshData> {

		protected MeshSpliterator() {
			super(Long.MAX_VALUE, Spliterator.NONNULL | Spliterator.IMMUTABLE);
		}

		@Override
		public boolean tryAdvance(final Consumer<? super MeshData> action) {
			return false;
		}

	}

}
