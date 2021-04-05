package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.mesh.SurfaceNets.Lookup.CUBE_EDGES;
import static io.github.cadiboo.nocubes.mesh.SurfaceNets.Lookup.EDGE_TABLE;

/**
 * Written by Mikola Lysenko (C) 2012
 * Ported from JavaScript to Java and modified for NoCubes by Cadiboo.
 *
 * @see "https://github.com/mikolalysenko/isosurface/blob/master/lib/surfacenets.js"
 * @see "https://github.com/mikolalysenko/mikolalysenko.github.com/blob/master/Isosurface/js/surfacenets.js"
 */
public class SurfaceNets implements MeshGenerator {

	// Seams appear in the meshes, surface nets generates a mesh 1 smaller than it "should"
	public static final int MESH_SIZE_POSITIVE_EXTENSION = 1;
	// Never change this. I'm not sure why it's needed but it is needed very much
	public static final int MESH_SIZE_NEGATIVE_EXTENSION = 1;

	@Override
	public void generate(Area area, Predicate<BlockState> isSmoothable, FaceAction action) {
		try {
			generateOrThrow(area, isSmoothable, action);
		} catch (Throwable t) {
			if (!ModUtil.IS_DEVELOPER_WORKSPACE.get())
				throw t;
			t.getCause();
		}
	}

	private static void generateOrThrow(Area area, Predicate<BlockState> isSmoothable, FaceAction faceAction) {
		BlockPos dims = area.end.subtract(area.start);
		BlockPos.Mutable pos = new BlockPos.Mutable();

		/*
		 * From Wikipedia:
		 * Apply a threshold to the 2D field to make a binary image containing:
		 * - 1 where the data value is above the isovalue
		 * - 0 where the data value is below the isovalue
		 */
		// The area, converted from a BlockState[] to an isSmoothable[]
		// densityField[x, y, z] = isSmoothable(chunk[x, y, z]);
		BlockState[] states = area.getAndCacheBlocks();
		final float[] densityField = new float[states.length];
		for (int i = 0; i < states.length; i++) {
			BlockState state = states[i];
			boolean isStateSmoothable = isSmoothable.test(state);
			densityField[i] = ModUtil.getBlockDensity(isStateSmoothable, state);
		}

		final Face face = new Face();
		int n = 0;
		// Appears to contain the multiplier for an axis.
		// The X axis is stored in columns, the Y axis is stored in rows and the Z axis is stored in slices.
		// (x, y, z) -> [z * fieldSizeX * fieldSizeY + y * fieldSizeX + x]
		// So the multiplier for X is 1, the multiplier for Y is fieldSizeX and the multiplier for z is fieldSizeX * fieldSizeY
		final int[] axisMultipliers = {1, (dims.getX() + 1), (dims.getX() + 1) * (dims.getY() + 1)};
		final float[] grid = new float[8];
		// Could be a boolean, either 1 or 0, gets flipped each time we go over a z slice
		int buf_no = 1;

		// Contains all the vertices of the previous slice + space for vertices on the current slice
		// When we go to the next slice, we start reading from the side we were previously
		// writing to and start writing to the side we were previously reading from
		// It behaves similarly to how a pixel buffer on a screen works: You write colors to one half
		// of the buffer, while displaying the other half and flip sides each frame (so you're not
		// visibly writing pixels each frame, causing a wipe-down effect as the new data is written
		// the way that happens in old CRT (cathode-ray tube) monitors/TVs)
//		// Instead of storing a Vec[] I store the data packed as 3 floats next to each other
//		// TO DO: Undo this once we have inline types
		final Vec[] verticesBuffer = new Vec[axisMultipliers[2] * 2];

		//March over the voxel grid
		for (int z = 0; z < dims.getZ() - 1; ++z, n += dims.getX(), buf_no ^= 1, axisMultipliers[2] = -axisMultipliers[2]) {

			//bufferPointer is the pointer into the buffer we are going to use.
			//The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int bufferPointer = 1 + (dims.getX() + 1) * (1 + buf_no * (dims.getY() + 1));

			for (int y = 0; y < dims.getY() - 1; ++y, ++n, bufferPointer += 2) {
				for (int x = 0; x < dims.getX() - 1; ++x, ++n, ++bufferPointer) {

					//Read in 8 field values around this vertex and store them in an array
					//Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
					int mask = 0, corner = 0, idx = n;
					for (int cornerZ = 0; cornerZ < 2; ++cornerZ, idx += dims.getX() * (dims.getY() - 2))
						for (int cornerY = 0; cornerY < 2; ++cornerY, idx += dims.getX() - 2)
							for (byte cornerX = 0; cornerX < 2; ++cornerX, ++corner, ++idx) {
								int index = ModUtil.get3dIndexInto1dArray(x + cornerX, y + cornerY, z + cornerZ, dims.getX(), dims.getY());
								float density = densityField[index];
								grid[corner] = density;
								mask |= (density < 0) ? (1 << corner) : 0;
							}

//					pos.set(worldXStart + x, worldYStart + y, worldZStart + z);
//					if (!voxelAction.apply(pos, mask))
//						return;

					// Check for early termination if cell does not intersect boundary
					// This cell is either entirely inside or entirely outside the isosurface
					if (mask == 0 || mask == 0xff)
						continue;

					// Sum up edge intersections
					int edge_mask = EDGE_TABLE[mask];
					final float[] vertex = {0, 0, 0};
					int edgeCrossings = 0;

					// For every edge of the cube...
					for (int edge = 0; edge < 12; ++edge) {

						//Use edge mask to check if it is crossed
						if ((edge_mask & (1 << edge)) == 0)
							continue;

						//If it did, increment number of edge crossings
						++edgeCrossings;

						//Now find the point of intersection
						//Unpack vertices
						// These are vertices packed (x, y, z) -> zyx
						// They are also the indices of which corner the vertex is for
						final int edgeStart = CUBE_EDGES[edge << 1];
						final int edgeEnd = CUBE_EDGES[(edge << 1) + 1];
						//Unpack grid values
						final float edgeStartValue = grid[edgeStart];
						final float edgeEndValue = grid[edgeEnd];
						//Compute point of intersection (the point where the isosurface is and the vertex is)
						float t = edgeStartValue - edgeEndValue;
						if (Math.abs(t) > 1e-6)
							t = edgeStartValue / t;
						else
							continue;

						//Interpolate vertices and add up intersections (this can be done without multiplying)
						for (int j = 0, k = 1; j < 3; ++j, k <<= 1) {
							final int a = edgeStart & k;
							final int b = edgeEnd & k;
							if (a != b)
								vertex[j] += a != 0 ? 1F - t : t;
							else
								vertex[j] += a != 0 ? 1F : 0;
						}
					}

					//Now we just average the edge intersections and add them to coordinate
					// 1.0F = isosurfaceLevel
					float s = 1.0F / edgeCrossings;
					Vec vec = new Vec(vertex[0], vertex[1], vertex[2]);
					vec.x = -0.5F + 1 + x + s * vec.x;
					vec.y = -0.5F + 1 + y + s * vec.y;
					vec.z = -0.5F + 1 + z + s * vec.z;
//					//Add vertex to buffer
					verticesBuffer[bufferPointer] = vec;

					//Now we need to add faces together, to do this we just loop over 3 basis components
					for (int axis = 0; axis < 3; ++axis) {
						//The first three entries of the edge_mask count the crossings along the edge
						if ((edge_mask & (1 << axis)) == 0) {
							continue;
						}

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

						//Remember to flip orientation depending on the sign of the corner.
						if ((mask & 1) != 0) {
							face.v0.copyFrom(verticesBuffer[bufferPointer]);
							face.v1.copyFrom(verticesBuffer[bufferPointer - dv]);
							face.v2.copyFrom(verticesBuffer[bufferPointer - du - dv]);
							face.v3.copyFrom(verticesBuffer[bufferPointer - du]);
						} else {
							face.v0.copyFrom(verticesBuffer[bufferPointer]);
							face.v1.copyFrom(verticesBuffer[bufferPointer - du]);
							face.v2.copyFrom(verticesBuffer[bufferPointer - du - dv]);
							face.v3.copyFrom(verticesBuffer[bufferPointer - dv]);
						}
						if (!faceAction.apply(pos, face))
							return;
					}
				}
			}
		}
	}

	static final class Lookup {

		/**
		 * A list of vertices where x/y/z are represented as bits (either 0 or 1)
		 * E.g. (1, 0, 1) -> 101
		 * I think it also has some extra info
		 */
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

	}

}
