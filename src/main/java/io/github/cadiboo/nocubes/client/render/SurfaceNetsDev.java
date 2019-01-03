package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.debug.client.render.IDebugRenderAlgorithm.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Cadiboo
 */
public final class SurfaceNetsDev {

	private static final ThreadLocal<HashMap<BlockPos, HashMap<BlockPos, ArrayList<Face<Vec3>>>>> FACES_BLOCKPOS_MAP = ThreadLocal.withInitial(HashMap::new);

	// because surface nets takes the 8 points of a block into account, we need to get the densities for +1 block on every positive axis of the chunk
	// because of this, we need to cache +2 blocks on every positive axis of the chunk

	private static final int chunkSizeX = 16;

	private static final int chunkSizeY = 16;

	private static final int chunkSizeZ = 16;

	private static final int meshSizeX = chunkSizeX + 1;

	private static final int meshSizeY = chunkSizeY + 1;

	private static final int meshSizeZ = chunkSizeZ + 1;

	private static final int densityCacheSizeX = meshSizeX + 1;

	private static final int densityCacheSizeY = meshSizeY + 1;

	private static final int densityCacheSizeZ = meshSizeZ + 1;

	private static final int densityCacheArraySize = densityCacheSizeX * densityCacheSizeY * densityCacheSizeZ;

	private static final int cacheSizeX = densityCacheSizeX + 1;

	private static final int cacheSizeY = densityCacheSizeY + 1;

	private static final int cacheSizeZ = densityCacheSizeZ + 1;

	private static final int cacheArraySize = cacheSizeX * cacheSizeY * cacheSizeZ;

	private static float getBlockDensity(final int posX, final int posY, final int posZ, final IBlockState[] stateCache, final boolean[] smoothableCache, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final PooledMutableBlockPos pos, final IBlockAccess cache) {
		float density = 0;
		for (int xOffset = 0; xOffset < 2; xOffset++) {
			for (int yOffset = 0; yOffset < 2; yOffset++) {
				for (int zOffset = 0; zOffset < 2; zOffset++) {
					final int x = (posX + xOffset);
					final int y = (posY + yOffset);
					final int z = (posZ + zOffset);
					// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
					final int index = x + cacheSizeX * (y + cacheSizeY * z);

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

	public static void renderPre(final RebuildChunkPreEvent event) {

		final MutableBlockPos renderChunkPos = event.getRenderChunkPosition();
		final IBlockAccess cache = ClientUtil.getCache(event);

		final int renderChunkPosX = renderChunkPos.getX();
		final int renderChunkPosY = renderChunkPos.getY();
		final int renderChunkPosZ = renderChunkPos.getZ();

		final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();

		try {
			// caches need two extra blocks on every positive axis
			final IBlockState[] states = new IBlockState[cacheArraySize];
			fillStateCache(states, renderChunkPosX, renderChunkPosY, renderChunkPosZ, cache, pooledMutableBlockPos);
			final boolean[] smoothables = new boolean[cacheArraySize];
			fillSmoothableCache(smoothables, states);

			// densities needs 1 extra block on every positive axis
			final float[] densities = new float[densityCacheArraySize];
			fillDensityCache(densities, renderChunkPosX, renderChunkPosY, renderChunkPosZ, cache, pooledMutableBlockPos, states, smoothables);

			final int maxX = meshSizeX;
			final int maxY = meshSizeY;
			final int maxZ = meshSizeZ;

			final int[] bufferOffsetAxisIndex = {
					1,
					maxX + 1,
					(maxX + 1) * (maxY + 1)
			};

			final int[] buffer = new int[bufferOffsetAxisIndex[2] * 2];
			int buffNo = 0;
			final int[] EDGE_TABLE = SurfaceNets.EDGE_TABLE;
			final int[] CUBE_EDGES = SurfaceNets.CUBE_EDGES;
			final float isoSurfaceLevel = ModConfig.getIsosurfaceLevel();
			final ArrayList<Vec3> vertices = new ArrayList<>();
			final ArrayList<Face<Vec3>> faces = new ArrayList<>();

			int mutableIndex = 0;
			for (int z = 0; z < maxZ - 1; ++z, buffNo ^= 1, bufferOffsetAxisIndex[2] = -bufferOffsetAxisIndex[2]) {
				int bufferIndex = 1 + (maxX + 1) * (1 + buffNo * (maxY + 1));
				for (int y = 0; y < maxY - 1; ++y, bufferIndex += 2) {
					for (int x = 0; x < maxX - 1; ++x, ++bufferIndex) {

						final float[] neighbourDensities = new float[8];
						int bitMask = calculateNeighbourDensitiesAndMask(neighbourDensities, x, y, z, densities);

						//Check for early termination if cell does not intersect boundary
						if (bitMask == 0 || bitMask == 0xFF) {
							continue;
						}

						final int edgeMask = EDGE_TABLE[bitMask];

						final Vec3 vertex = new Vec3();

						int edgeCrossingCount = 0;

						for (int cubeEdgeIndex = 0; cubeEdgeIndex < 12; ++cubeEdgeIndex) {

							if ((edgeMask & (1 << cubeEdgeIndex)) == 0) {
								continue;
							}

							++edgeCrossingCount;

							// Unpack vertices
							final int cubeEdge0 = CUBE_EDGES[cubeEdgeIndex << 1];
							final int cubeEdge1 = CUBE_EDGES[(cubeEdgeIndex << 1) + 1];

							// Unpack grid values
							final float neighbourDensity0 = neighbourDensities[cubeEdge0];
							final float neighbourDensity1 = neighbourDensities[cubeEdge1];
							float pointOfIntersection = neighbourDensity0 - neighbourDensity1;                 //Compute point of intersection
							if (Math.abs(pointOfIntersection) > 1e-6) {
								pointOfIntersection = neighbourDensity0 / pointOfIntersection;
							} else {
								continue;
							}

							// Lerp
							vertex.xCoord += (cubeEdge0 & 1) * (1.0 - pointOfIntersection) + (cubeEdge1 & 1) * pointOfIntersection;
							vertex.yCoord += (cubeEdge0 & 2) * (1.0 - pointOfIntersection) + (cubeEdge1 & 2) * pointOfIntersection;
							vertex.zCoord += (cubeEdge0 & 3) * (1.0 - pointOfIntersection) + (cubeEdge1 & 3) * pointOfIntersection;
						}

						final float s = isoSurfaceLevel / edgeCrossingCount;
						vertex.xCoord = renderChunkPosX + x + s * vertex.xCoord;
						vertex.yCoord = renderChunkPosY + y + s * vertex.yCoord;
						vertex.zCoord = renderChunkPosZ + z + s * vertex.zCoord;

						if (ModConfig.offsetVertices) {
							ModUtil.offsetVertex(vertex);
						}

						buffer[bufferIndex] = vertices.size();
						vertices.add(vertex);

						for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
							//The first three entries of the edge_mask count the crossings along the edge
							if ((edgeMask & (1 << axisIndex)) == 0) {
								continue;
							}

							// axisIndex = axes we are point along
							// otherAxis0, otherAxis1 = orthogonal axes
							final int otherAxis0 = (axisIndex + 1) % 3;
							final int otherAxis1 = (axisIndex + 2) % 3;

							final int[] pos = {x, y, z};

							//If we are on a boundary, skip it
							if (pos[otherAxis0] == 0 || pos[otherAxis1] == 0) {
								continue;
							}

							final int adjacentEdge0 = bufferOffsetAxisIndex[otherAxis0];
							final int adjacentEdge1 = bufferOffsetAxisIndex[otherAxis1];

							// Remember to flip orientation depending on the sign of the corner.
							if ((bitMask & 1) != 0) {
								faces.add(new Face<Vec3>(
										vertices.get(buffer[bufferIndex]),
										vertices.get(buffer[bufferIndex - adjacentEdge0]),
										vertices.get(buffer[bufferIndex - adjacentEdge0 - adjacentEdge1]),
										vertices.get(buffer[bufferIndex - adjacentEdge1])
								));
							} else {
								faces.add(new Face<Vec3>(
										vertices.get(buffer[bufferIndex]),
										vertices.get(buffer[bufferIndex - adjacentEdge1]),
										vertices.get(buffer[bufferIndex - adjacentEdge0 - adjacentEdge1]),
										vertices.get(buffer[bufferIndex - adjacentEdge0])
								));
							}

						}

					}
				}
			}

		} catch (final Exception e) {
			ModUtil.crashIfNotDev(e);
		} finally {
			pooledMutableBlockPos.release();
		}

	}

	private static int calculateNeighbourDensitiesAndMask(final float[] neighbourDensities, final int x, final int y, final int z, final float[] densityCache) {

		final int maxX = densityCacheSizeX;
		final int maxY = densityCacheSizeY;

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

	private static void fillStateCache(final IBlockState[] stateCache, final int renderChunkPosX,
	                                   final int renderChunkPosY, final int renderChunkPosZ, final IBlockAccess cache, PooledMutableBlockPos pos) {
		final int maxX = cacheSizeX;
		final int maxY = cacheSizeY;
		final int maxZ = cacheSizeZ;
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

	private static void fillSmoothableCache(final boolean[] smoothableCache, final IBlockState[] stateCache) {
		final int maxX = cacheSizeX;
		final int maxY = cacheSizeY;
		final int maxZ = cacheSizeZ;
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

	private static void fillDensityCache(final float[] densityCache, final int renderChunkPosX,
	                                     final int renderChunkPosY, final int renderChunkPosZ, final IBlockAccess cache, PooledMutableBlockPos pos,
	                                     final IBlockState[] statesCache, final boolean[] smoothableCache) {
		final int maxX = densityCacheSizeX;
		final int maxY = densityCacheSizeY;
		final int maxZ = densityCacheSizeZ;
		int index = 0;
		for (int x = 0; x < maxX; x++) {
			for (int y = 0; y < maxY; y++) {
				for (int z = 0; z < maxZ; z++) {
//					densityCache[index] = getBlockDensity(smoothableCache, statesCache, cacheSizeX, cacheSizeY, cacheSizeZ, cache, renderChunkPosX, renderChunkPosY, renderChunkPosZ, x, y, z, pos);
					densityCache[index] = getBlockDensity(x, y, z, statesCache, smoothableCache, renderChunkPosX, renderChunkPosY, renderChunkPosZ, pos, cache);
					index++;
				}
			}
		}
	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {

	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {

	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

		FACES_BLOCKPOS_MAP.get().remove(event.getRenderChunkPosition());

	}

}
