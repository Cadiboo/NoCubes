package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class SurfaceNetsUtil {

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
		for (int cubeCornerIndex = 0; cubeCornerIndex < 8; ++cubeCornerIndex) {
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
		for (int edgeTableIndex = 0; edgeTableIndex < 256; ++edgeTableIndex) {
			int em = 0;
			for (int cubeEdgesIndex = 0; cubeEdgesIndex < 24; cubeEdgesIndex += 2) {
				final boolean a = (edgeTableIndex & (1 << CUBE_EDGES[cubeEdgesIndex])) != 0;
				final boolean b = (edgeTableIndex & (1 << CUBE_EDGES[cubeEdgesIndex + 1])) != 0;
				em |= a != b ? 1 << (cubeEdgesIndex >> 1) : 0;
			}
			EDGE_TABLE[edgeTableIndex] = em;
		}

	}

	public static final int CHUNK_SIZE_X = 16;

	public static final int CHUNK_SIZE_Y = 16;

	public static final int CHUNK_SIZE_Z = 16;

	public static final int MESH_SIZE_X = CHUNK_SIZE_X + 1;

	public static final int MESH_SIZE_Y = CHUNK_SIZE_Y + 1;

	public static final int MESH_SIZE_Z = CHUNK_SIZE_Z + 1;

	public static final int DENSITY_CACHE_SIZE_X = MESH_SIZE_X + 1;

	public static final int DENSITY_CACHE_SIZE_Y = MESH_SIZE_Y + 1;

	public static final int DENSITY_CACHE_SIZE_Z = MESH_SIZE_Z + 1;

	public static final int DENSITY_CACHE_ARRAY_SIZE = DENSITY_CACHE_SIZE_X * DENSITY_CACHE_SIZE_Y * DENSITY_CACHE_SIZE_Z;

	public static final int CACHE_SIZE_X = DENSITY_CACHE_SIZE_X + 1;

	public static final int CACHE_SIZE_Y = DENSITY_CACHE_SIZE_Y + 1;

	public static final int CACHE_SIZE_Z = DENSITY_CACHE_SIZE_Z + 1;

	public static final int CACHE_ARRAY_SIZE = CACHE_SIZE_X * CACHE_SIZE_Y * CACHE_SIZE_Z;

	public static int calculateNeighbourDensitiesAndMask(final float[] neighbourDensities, final int x, final int y, final int z, final float[] densityCache) {

		final int maxX = DENSITY_CACHE_SIZE_X;
		final int maxY = DENSITY_CACHE_SIZE_Y;

		int bitMask = 0b0000000;
		int neighbourDensitiesIndex = 0;

		for (int xOffset = 0; xOffset < 2; xOffset++) {
			for (int yOffset = 0; yOffset < 2; yOffset++) {
				for (int zOffset = 0; zOffset < 2; zOffset++) {
					// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
					float density = densityCache[(x + xOffset) + maxX * ((y + yOffset) + maxY * (z + zOffset))];
					neighbourDensities[neighbourDensitiesIndex] = density;
					if (density > 0F) {
						bitMask |= (1 << neighbourDensitiesIndex);
					}
					neighbourDensitiesIndex++;
				}
			}
		}

		return bitMask;
	}

	public static void fillStateCache(final IBlockState[] stateCache, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final IBlockAccess cache, BlockPos.PooledMutableBlockPos pos) {
		final int maxX = CACHE_SIZE_X;
		final int maxY = CACHE_SIZE_Y;
		final int maxZ = CACHE_SIZE_Z;
		int index = 0;
		for (int x = 0; x < maxX; x++) {
			for (int y = 0; y < maxY; y++) {
				for (int z = 0; z < maxZ; z++) {
					stateCache[index] = cache.getBlockState(pos.setPos(renderChunkPosX + x, renderChunkPosY + y, renderChunkPosZ + z));
					index++;
				}
			}
		}
	}

	public static void fillSmoothableCache(final boolean[] smoothableCache, final IBlockState[] stateCache) {
		final int maxX = CACHE_SIZE_X;
		final int maxY = CACHE_SIZE_Y;
		final int maxZ = CACHE_SIZE_Z;
		int index = 0;
		for (int x = 0; x < maxX; x++) {
			for (int y = 0; y < maxY; y++) {
				for (int z = 0; z < maxZ; z++) {
					smoothableCache[index] = ModUtil.shouldSmooth(stateCache[index]);
					index++;
				}
			}
		}
	}

	public static void fillDensityCache(final float[] densityCache, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final IBlockAccess cache, BlockPos.PooledMutableBlockPos pos, final IBlockState[] statesCache, final boolean[] smoothableCache) {
		final int maxX = DENSITY_CACHE_SIZE_X;
		final int maxY = DENSITY_CACHE_SIZE_Y;
		final int maxZ = DENSITY_CACHE_SIZE_Z;
		int index = 0;
		for (int x = 0; x < maxX; x++) {
			for (int y = 0; y < maxY; y++) {
				for (int z = 0; z < maxZ; z++) {
					densityCache[index] = getBlockDensity(x, y, z, statesCache, smoothableCache, renderChunkPosX, renderChunkPosY, renderChunkPosZ, pos, cache);
					index++;
				}
			}
		}
	}

	public static float getBlockDensity(final int posX, final int posY, final int posZ, final IBlockState[] stateCache, final boolean[] smoothableCache, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final BlockPos.PooledMutableBlockPos pos, final IBlockAccess cache) {

		final int cacheXSize = CACHE_SIZE_X;
		final int cacheYSize = CACHE_SIZE_Y;
		float density = 0;
		for (int xOffset = 0; xOffset < 2; xOffset++) {
			for (int yOffset = 0; yOffset < 2; yOffset++) {
				for (int zOffset = 0; zOffset < 2; zOffset++) {
					final int x = (posX + xOffset);
					final int y = (posY + yOffset);
					final int z = (posZ + zOffset);
					// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
					final int index = x + cacheXSize * (y + cacheYSize * z);

					final IBlockState state = stateCache[index];
					final boolean shouldSmooth = smoothableCache[index];

					if (shouldSmooth) {
						pos.setPos(
								renderChunkPosX + x,
								renderChunkPosY + y,
								renderChunkPosZ + z
						);
						final AxisAlignedBB box = state.getBoundingBox(cache, pos);
						density += box.maxY - box.minY;
					} else {
						density--;
					}
				}
			}
		}
		return density;
	}

}
