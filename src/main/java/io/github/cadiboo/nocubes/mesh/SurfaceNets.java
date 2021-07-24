package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

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

	public static final int COMPLETELY_OUTSIDE_ISOSURFACE = 0;
	public static final int COMPLETELY_INSIDE_ISOSURFACE = 0xFF;
	private static final ThreadLocalArrayCache<float[]> DISTANCE_FIELD_CACHE = new ThreadLocalArrayCache<>(float[]::new, array -> array.length);

	@Override
	public Vec3i getPositiveAreaExtension() {
		// Needed otherwise seams appear in the meshes because surface nets generates a mesh 1 smaller than it "should"
		return ModUtil.VEC_ONE;
	}

	@Override
	public Vec3i getNegativeAreaExtension() {
		// I'm not sure why it's needed but it is needed very much
		return NoCubesConfig.Server.extraSmoothMesh ? ModUtil.VEC_TWO : ModUtil.VEC_ONE;
	}

	@Override
	public void generateOrThrow(Area area, Predicate<BlockState> isSmoothable, VoxelAction voxelAction, FaceAction faceAction) {
		boolean smoother = NoCubesConfig.Server.extraSmoothMesh;
		float[] distanceField = generateDistanceField(area, isSmoothable, smoother);
		BlockPos dims = getDimensions(area, smoother);
//		TestData.TestMesh testMesh = TestData.SPHERE;
//		float[] distanceField = testMesh.generateDistanceField(area.start.getX(), area.start.getY(), area.start.getZ());
//		BlockPos dims = testMesh.dimensions;
		generateOrThrow(distanceField, dims, smoother, voxelAction, faceAction);
	}

	public static BlockPos getDimensions(Area area, boolean smoother) {
		return smoother ? area.size.subtract(ModUtil.VEC_ONE) : area.size;
	}

	public static float[] generateDistanceField(Area area, Predicate<BlockState> isSmoothable, boolean smoother) {
		// The area, converted from a BlockState[] to an isSmoothable[]
		// densityField[x, y, z] = isSmoothable(chunk[x, y, z]);
		BlockState[] states = area.getAndCacheBlocks();
		// NB: SurfaceNets expects to be working on the signed distance at the corner of each block
		// To get this we would have to average the densities of each block & its neighbours
		// Doing this results in loss of terrain features (one-block large features effectively disappear)
		// Because we want to preserve these features, we feed SurfaceNets the inverted block densities, pretending that they
		// are the corner distances and then offset the resulting mesh by 0.5
		return smoother ? generateDistanceField(area, isSmoothable, states) : generateNegativeDensityField(area, isSmoothable, states);
	}

	private static float[] generateNegativeDensityField(Area area, Predicate<BlockState> isSmoothable, BlockState[] states) {
		int length = area.numBlocks();
		float[] densityField = DISTANCE_FIELD_CACHE.takeArray(length);
		for (int i = 0; i < length; ++i)
			densityField[i] = -ModUtil.getBlockDensity(isSmoothable, states[i]);
		return densityField;
	}

	private static float[] generateDistanceField(Area area, Predicate<BlockState> isSmoothable, BlockState[] states) {
		int areaX = area.size.getX();
		int areaY = area.size.getY();
		int areaZ = area.size.getZ();

		int distanceFieldSizeX = areaX - 1;
		int distanceFieldSizeY = areaY - 1;
		int distanceFieldSizeZ = areaZ - 1;
		int distanceFieldSize = distanceFieldSizeX * distanceFieldSizeY * distanceFieldSizeZ;
		float[] distanceField = DISTANCE_FIELD_CACHE.takeArray(distanceFieldSize);

		int index = 0;
		for (int z = 0; z < areaZ; ++z) {
			for (int y = 0; y < areaY; ++y) {
				for (int x = 0; x < areaX; ++x, ++index) {
					if (z == distanceFieldSizeZ || y == distanceFieldSizeY || x == distanceFieldSizeX)
						continue;
					float combinedDensity = 0;
					int neighbourIndex = index;
					for (int neighbourZ = 0; neighbourZ < 2; ++neighbourZ, neighbourIndex += areaX * (areaY - 2))
						for (int neighbourY = 0; neighbourY < 2; ++neighbourY, neighbourIndex += areaX - 2)
							for (int neighbourX = 0; neighbourX < 2; ++neighbourX, ++neighbourIndex)
								combinedDensity += ModUtil.getBlockDensity(isSmoothable, states[neighbourIndex]);
					int distanceFieldIndex = ModUtil.get3dIndexInto1dArray(x, y, z, distanceFieldSizeX, distanceFieldSizeY);
					distanceField[distanceFieldIndex] = -combinedDensity / 8F;
				}
			}
		}
		return distanceField;
	}

	private static void generateOrThrow(float[] distanceField, BlockPos dims, boolean smoother, VoxelAction voxelAction, FaceAction faceAction) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		final Face face = new Face();
		int n = 0;
		// Appears to contain the multiplier for an axis.
		// The X axis is stored in columns, the Y axis is stored in rows and the Z axis is stored in slices.
		// (x, y, z) -> [z * fieldSizeX * fieldSizeY + y * fieldSizeX + x]
		// So the multiplier for X is 1, the multiplier for Y is fieldSizeX and the multiplier for z is fieldSizeX * fieldSizeY
		final int[] axisMultipliers = {1, (dims.getX() + 1), (dims.getX() + 1) * (dims.getY() + 1)};
		final float[] cornerDistances = new float[8];
		// Could be a boolean, either 1 or 0, gets flipped each time we go over a z slice
		int buf_no = 1;

		// Contains all the vertices of the previous slice + space for vertices on the current slice
		// When we go to the next slice, we start reading from the side we were previously
		// writing to and start writing to the side we were previously reading from
		// It behaves similarly to how a pixel buffer on a screen works: You write colors to one half
		// of the buffer, while displaying the other half and flip sides each frame (so you're not
		// visibly writing pixels each frame, causing a wipe-down effect as the new data is written
		// the way that happens in old CRT (cathode-ray tube) monitors/TVs)
		final Vec[] verticesBuffer = new Vec[axisMultipliers[2] * 2];
		final float[] vertexUntilIFigureOutTheInterpolationAndIntersection = {0, 0, 0};

		//March over the voxel cornerDistances
		for (int z = 0; z < dims.getZ() - 1; ++z, n += dims.getX(), buf_no ^= 1, axisMultipliers[2] = -axisMultipliers[2]) {

			//bufferPointer is the pointer into the buffer we are going to use.
			//The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int bufferPointer = 1 + (dims.getX() + 1) * (1 + buf_no * (dims.getY() + 1));

			for (int y = 0; y < dims.getY() - 1; ++y, ++n, bufferPointer += 2) {
				for (int x = 0; x < dims.getX() - 1; ++x, ++n, ++bufferPointer) {

					//Read in 8 field values around this vertex and store them in an array
					//Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
					int mask = 0, corner = 0, cornerIndex = n;
					for (int cornerZ = 0; cornerZ < 2; ++cornerZ, cornerIndex += dims.getX() * (dims.getY() - 2))
						for (int cornerY = 0; cornerY < 2; ++cornerY, cornerIndex += dims.getX() - 2)
							for (byte cornerX = 0; cornerX < 2; ++cornerX, ++corner, ++cornerIndex) {
								float signedDistance = distanceField[cornerIndex];
								cornerDistances[corner] = signedDistance;
								// Signed distance field values are negative when they fall inside the shape
								boolean insideIsosurface = signedDistance < 0;
								mask |= insideIsosurface ? (1 << corner) : 0;
							}

					if (!voxelAction.apply(pos.set(x, y, z), getAmountInsideIsosurface(smoother, cornerDistances)))
						return;

					// Check for early termination if cell does not intersect boundary
					if (mask == COMPLETELY_OUTSIDE_ISOSURFACE || mask == COMPLETELY_INSIDE_ISOSURFACE)
						continue;

					// Sum up edge intersections
					int edge_mask = EDGE_TABLE[mask];
					vertexUntilIFigureOutTheInterpolationAndIntersection[0] = vertexUntilIFigureOutTheInterpolationAndIntersection[1] = vertexUntilIFigureOutTheInterpolationAndIntersection[2] = 0;
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
						//Unpack cornerDistances values
						final float edgeStartValue = cornerDistances[edgeStart];
						final float edgeEndValue = cornerDistances[edgeEnd];
						//Compute point of intersection (the point where the isosurface is and the vertex is)
						float t = edgeStartValue - edgeEndValue;
						if (Math.abs(t) <= 0.00000001F)
							continue;
						t = edgeStartValue / t;

						//Interpolate vertices and add up intersections (this can be done without multiplying)
						for (int axis = 0, axisMask = 1; axis < 3; ++axis, axisMask <<= 1) {
							int startAxisValue = edgeStart & axisMask;
							int endAxisValue = edgeEnd & axisMask;
							float axisInterpValue;
							if (startAxisValue != endAxisValue)
								axisInterpValue = startAxisValue != 0 ? 1F - t : t;
							else
								axisInterpValue = startAxisValue != 0 ? 1F : 0;
							vertexUntilIFigureOutTheInterpolationAndIntersection[axis] += axisInterpValue;
						}
					}

					//Now we just average the edge intersections and add them to coordinate
					// 1.0F = isosurfaceLevel
					float s = 1.0F / edgeCrossings;
					Vec vertex = new Vec(vertexUntilIFigureOutTheInterpolationAndIntersection[0], vertexUntilIFigureOutTheInterpolationAndIntersection[1], vertexUntilIFigureOutTheInterpolationAndIntersection[2]);
					vertex.x = x + s * vertex.x;
					vertex.y = y + s * vertex.y;
					vertex.z = z + s * vertex.z;
					if (!smoother) {
						// Because we are passing block densities instead of corner distances (see the NB comment above) we need to offset the mesh
						vertex.add(0.5F, 0.5F, 0.5F);
					} else {
						vertex.add(1, 1, 1);
					}
					//Add vertex to buffer
					verticesBuffer[bufferPointer] = vertex;

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

						//Remember to flip orientation depending on the sign of the corner.
						if ((mask & 1) != 0) {
							face.set(
								verticesBuffer[bufferPointer],
								verticesBuffer[bufferPointer - dv],
								verticesBuffer[bufferPointer - du - dv],
								verticesBuffer[bufferPointer - du]
							);
						} else {
							face.set(
								verticesBuffer[bufferPointer],
								verticesBuffer[bufferPointer - du],
								verticesBuffer[bufferPointer - du - dv],
								verticesBuffer[bufferPointer - dv]
							);
						}
						face.flip(); // TODO: I should probably fix this at some point
						pos.set(x, y, z);
						if (!faceAction.apply(pos, face))
							return;
					}
				}
			}
		}
	}

	private static float getAmountInsideIsosurface(boolean smoother, float[] cornerDistances) {
		if (!smoother) {
			// cornerDistances is not actually the values of the corners, it's the values of the neighbouring cubes
			// The values will be between -1 and 1 (-1 for inside isosurface, +1 for outside isosurface)
			float voxelDensity = 0;
			for (int corner = 0; corner < 8; ++corner) {
				float cornerDensity = -cornerDistances[corner];
				voxelDensity += (cornerDensity + 1) / 2F;
			}
			return voxelDensity / 8;
		}
		float combinedDistance = 0;
		for (int corner = 0; corner < 8; ++corner)
			combinedDistance += cornerDistances[corner];
		float averageDistance = combinedDistance / 8F;
		float voxelDensity = -averageDistance;
		return (voxelDensity + 1) / 2F;
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
