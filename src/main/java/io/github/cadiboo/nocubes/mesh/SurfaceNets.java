package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ReusableCache;
import io.github.cadiboo.nocubes.util.Vec;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import java.util.ArrayList;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.mesh.SurfaceNets.Lookup.CUBE_EDGES;
import static io.github.cadiboo.nocubes.mesh.SurfaceNets.Lookup.EDGE_TABLE;

/**
 * Written by Mikola Lysenko (C) 2012
 * Ported from JavaScript to Java and modified for NoCubes by Cadiboo.
 */
public class SurfaceNets {

	// Seams appear in the meshes, surface nets generates a mesh 1 smaller than it "should"
	public static final int MESH_SIZE_POSITIVE_EXTENSION = 1;
	// Never change this. I'm not sure why it's needed but it is needed very much
	public static final int MESH_SIZE_NEGATIVE_EXTENSION = 1;

	public static void generate(
		int startX, int startY, int startZ,
		int meshSizeX, int meshSizeY, int meshSizeZ,
		IBlockReader world, Predicate<BlockState> isSmoothable, ReusableCache<float[]> cache,
		VoxelAction voxelAction, FaceAction faceAction
	) {
		try {
			generateOrThrow(startX, startY, startZ, meshSizeX, meshSizeY, meshSizeZ, world, isSmoothable, cache, voxelAction, faceAction);
		} catch (Throwable t) {
			if (!ModUtil.IS_DEVELOPER_WORKSPACE.get())
				throw t;
			t.getCause();
		}
	}

	private static void generateOrThrow(
		int startX, int startY, int startZ,
		int meshSizeX, int meshSizeY, int meshSizeZ,
		IBlockReader world, Predicate<BlockState> isSmoothable, ReusableCache<float[]> cache,
		VoxelAction voxelAction, FaceAction faceAction
	) {
		meshSizeX += MESH_SIZE_POSITIVE_EXTENSION;
		meshSizeY += MESH_SIZE_POSITIVE_EXTENSION;
		meshSizeZ += MESH_SIZE_POSITIVE_EXTENSION;
		// Subtract the negative extension from the start and add it to the size
		int worldXStart = startX - MESH_SIZE_NEGATIVE_EXTENSION;
		int worldYStart = startY - MESH_SIZE_NEGATIVE_EXTENSION;
		int worldZStart = startZ - MESH_SIZE_NEGATIVE_EXTENSION;
		int fieldSizeX = meshSizeX + MESH_SIZE_NEGATIVE_EXTENSION;
		int fieldSizeY = meshSizeY + MESH_SIZE_NEGATIVE_EXTENSION;
		int fieldSizeZ = meshSizeZ + MESH_SIZE_NEGATIVE_EXTENSION;

		final BlockPos.Mutable pos = new BlockPos.Mutable();

		/*
		 * From Wikipedia:
		 * Apply a threshold to the 2D field to make a binary image containing:
		 * - 1 where the data value is above the isovalue
		 * - 0 where the data value is below the isovalue
		 */
		// The area, converted from a BlockState[] to an isSmoothable[]
		// densityField[x, y, z] = isSmoothable(chunk[x, y, z]);
		final float[] densityField = cache.getOrCreate(() -> new float[fieldSizeZ * fieldSizeY * fieldSizeX]);
		ModUtil.traverseArea(
			worldXStart, worldYStart, worldZStart,
			// Minus one because this is inclusive
			worldXStart + fieldSizeZ - 1, worldYStart + fieldSizeY - 1, worldZStart + fieldSizeX - 1,
			pos, Minecraft.getInstance().level, (blockState, blockPos, index) -> {
				boolean isStateSmoothable = isSmoothable.test(blockState);
				densityField[index] = ModUtil.getBlockDensity(isStateSmoothable, blockState);
			}
		);
		// Old code from before 'traverseArea' was used, kept around because it might be useful for CubicChunks compat
//		{
//			int i = 0;
//			for (int z = 0; z < fieldSizeZ; z++) {
//				for (int y = 0; y < fieldSizeY; y++) {
//					for (int x = 0; x < fieldSizeX; x++, i++) {
//						pos.setPos(worldXStart + x, worldYStart + y, worldZStart + z);
//						if (world instanceof WorldGenRegion && !((WorldGenRegion) world).chunkExists(x << 16, z << 16))
//							binaryField[z][y][x] = false;
//						else
//							binaryField[z][y][x] = isSmoothable.test(world.getBlockState(pos));
//					}
//				}
//			}
//		}

		final Face face = new Face();
		final ArrayList<float[]> vertices = new ArrayList<>(0x180);
		int n = 0;
		// Appears to contain the multiplier for an axis.
		// The X axis is stored in columns, the Y axis is stored in rows and the Z axis is stored in slices.
		// (x, y, z) -> [z * fieldSizeX * fieldSizeY + y * fieldSizeX + x]
		// So the multiplier for X is 1, the multiplier for Y is fieldSizeX and the multiplier for z is fieldSizeX * fieldSizeY
		final int[] axisMultipliers = {1, (fieldSizeX + 1), (fieldSizeX + 1) * (fieldSizeY + 1)};
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
		final int[] verticesBuffer = new int[axisMultipliers[2] * 2];

		//March over the voxel grid
		for (int z = 0; z < meshSizeZ; ++z, n += fieldSizeX, buf_no ^= 1, axisMultipliers[2] = -axisMultipliers[2]) {

			//bufferPointer is the pointer into the buffer we are going to use.
			//The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int bufferPointer = 1 + (fieldSizeX + 1) * (1 + buf_no * (fieldSizeY + 1));

			for (int y = 0; y < meshSizeY; ++y, ++n, bufferPointer += 2) {
				for (int x = 0; x < meshSizeX; ++x, ++n, ++bufferPointer) {

					//Read in 8 field values around this vertex and store them in an array
					//Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
					int mask = 0, corner = 0, idx = n;
					for (int cornerZ = 0; cornerZ < 2; ++cornerZ, idx += fieldSizeX * (fieldSizeY - 2))
						for (int cornerY = 0; cornerY < 2; ++cornerY, idx += fieldSizeX - 2)
							for (byte cornerX = 0; cornerX < 2; ++cornerX, ++corner, ++idx) {
								int index = ModUtil.get3dIndexInto1dArray(x + cornerX, y + cornerY, z + cornerZ, fieldSizeX, fieldSizeY);
								float density = densityField[index];
								grid[corner] = density;
								mask |= (density < 0) ? (1 << corner) : 0;
							}

					pos.set(worldXStart + x, worldYStart + y, worldZStart + z);
					if (!voxelAction.apply(pos, mask))
						return;

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
//						//Compute point of intersection (the point where the isosurface is and the vertex is)
//						float t = edgeStartValue - edgeEndValue;
//						vertex.add(
//							(edgeStart & 0b001) * (1.0 - t) + (edgeEnd & 0b001) * t,
//							(edgeStart & 0b010) * (1.0 - t) + (edgeEnd & 0b010) * t,
//							(edgeStart & 0b100) * (1.0 - t) + (edgeEnd & 0b100) * t
//						);
						//Compute point of intersection
						float t = edgeStartValue - edgeEndValue;
						if (Math.abs(t) > 1e-6) {
							t = edgeStartValue / t;
						} else {
							continue;
						}

						//Interpolate vertices and add up intersections (this can be done without multiplying)
						for (int j = 0, k = 1; j < 3; ++j, k <<= 1) {
							final int a = edgeStart & k;
							final int b = edgeEnd & k;
							if (a != b) {
								vertex[j] += a != 0 ? 1F - t : t;
							} else {
								vertex[j] += a != 0 ? 1F : 0;
							}
						}
					}

					//Now we just average the edge intersections and add them to coordinate
					// 1.0F = isosurfaceLevel
					float s = 1.0F / edgeCrossings;
					vertex[0] = -0.5F + 0 + x + s * vertex[0];
					vertex[1] = -0.5F + 0 + y + s * vertex[1];
					vertex[2] = -0.5F + 0 + z + s * vertex[2];
//					vertex.multiply(s);
//					vertex.add(
//						x + 0.5 - MESH_SIZE_NEGATIVE_EXTENSION,
//						y + 0.5 - MESH_SIZE_NEGATIVE_EXTENSION,
//						z + 0.5 - MESH_SIZE_NEGATIVE_EXTENSION
//					);

//					Vec fromPreviousPreviousSlice = verticesBuffer[bufferPointer];
//					if (fromPreviousPreviousSlice != null)
//						fromPreviousPreviousSlice.close();
//					//Add vertex to buffer
//					verticesBuffer[bufferPointer] = vertex;
					verticesBuffer[bufferPointer] = vertices.size();
					vertices.add(vertex);

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
							face.v0.copyFrom(vertices.get(verticesBuffer[bufferPointer]));
							face.v1.copyFrom(vertices.get(verticesBuffer[bufferPointer - dv]));
							face.v2.copyFrom(vertices.get(verticesBuffer[bufferPointer - du - dv]));
							face.v3.copyFrom(vertices.get(verticesBuffer[bufferPointer - du]));
						} else {
							face.v0.copyFrom(vertices.get(verticesBuffer[bufferPointer]));
							face.v1.copyFrom(vertices.get(verticesBuffer[bufferPointer - du]));
							face.v2.copyFrom(vertices.get(verticesBuffer[bufferPointer - du - dv]));
							face.v3.copyFrom(vertices.get(verticesBuffer[bufferPointer - dv]));
						}
						if (!faceAction.apply(pos, face))
							return;
					}
				}
			}
		}
	}

	public interface VoxelAction {

		boolean apply(BlockPos.Mutable pos, int mask);

	}

	public interface FaceAction {

		boolean apply(BlockPos.Mutable pos, Face face);

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
