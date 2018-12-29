package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.ChunkCache;

import java.util.ArrayList;

/**
 * @author Cadiboo
 */
public final class SurfaceNetsChunk {

	private static float getBlockDensity(final boolean[] isSmoothableCache, final IBlockState[] statesCache, final int scanSize, final ChunkCache cache, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final int x, final int y, final int z, PooledMutableBlockPos pooledMutableBlockPos) {

		float density = 0.0F;

		for (int xOffset = 0; xOffset < 2; ++xOffset) {
			for (int yOffset = 0; yOffset < 2; ++yOffset) {
				for (int zOffset = 0; zOffset < 2; ++zOffset) {

					// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
					final int index = (x + xOffset) + scanSize * ((y + yOffset) + scanSize * (z + zOffset));

					final IBlockState state = statesCache[index];
					final boolean shouldSmooth = isSmoothableCache[index];

					if (shouldSmooth) {
						pooledMutableBlockPos.setPos(
								renderChunkPosX + x + xOffset,
								renderChunkPosY + y + yOffset,
								renderChunkPosZ + z + zOffset
						);
						final AxisAlignedBB aabb = state.getBoundingBox(cache, pooledMutableBlockPos);
						density += aabb.maxY - aabb.minY;

					} else {
						density -= 1;
					}

					if (state.getBlock() == Blocks.BEDROCK) {
						density += 0.000000000000000000000000000000000000000000001F;
					}

				}
			}
		}

		return density;

	}

	public static void renderPre(final RebuildChunkPreEvent event) {

		final MutableBlockPos renderChunkPos = event.getRenderChunkPosition();
		final int renderChunkPosX = renderChunkPos.getX();
		final int renderChunkPosY = renderChunkPos.getY();
		final int renderChunkPosZ = renderChunkPos.getZ();
		final ChunkCache cache = event.getChunkCache();
		final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();

		final float isosurfaceLevel = ModConfig.getIsosurfaceLevel();

		final int[] CUBE_EDGES = SurfaceNets.CUBE_EDGES;
		final int[] EDGE_TABLE = SurfaceNets.EDGE_TABLE;

		//chunk size = 16, this is chunk size + 1 block on every positive axis side
		final int chunkSizeX = 16;
		final int chunkSizeY = 16;
		final int chunkSizeZ = 16;
		final int scanSizeX = chunkSizeX + 1;
		final int scanSizeY = chunkSizeY + 1;
		final int scanSizeZ = chunkSizeZ + 1;

		final boolean[] isSmoothable = new boolean[(scanSizeX + 1) * (scanSizeY + 1) * (scanSizeZ + 1)];
		final IBlockState[] states = new IBlockState[(scanSizeX + 1) * (scanSizeY + 1) * (scanSizeZ + 1)];

		//http://ngildea.blogspot.com/2014/09/dual-contouring-chunked-terrain.html

		try {

			// -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17
			// transverse the chunk + 2 blocks on every positive axis side, plus 1 block on every side
			for (int xOffset = -1; xOffset < scanSizeX + 1; xOffset++) {
				for (int yOffset = -1; yOffset < scanSizeY + 1; yOffset++) {
					for (int zOffset = -1; zOffset < scanSizeZ + 1; zOffset++) {

						final int x = (xOffset + 1);
						final int y = (yOffset + 1);
						final int z = (zOffset + 1);

						// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
						final int index = x + scanSizeX * (y + scanSizeY * z);

						final IBlockState potentiallySmoothableBlockState = cache.getBlockState(pooledMutableBlockPos.setPos(
								renderChunkPosX + xOffset,
								renderChunkPosY + yOffset,
								renderChunkPosZ + zOffset
						));

						states[index] = potentiallySmoothableBlockState;
						isSmoothable[index] = ModUtil.shouldSmooth(potentiallySmoothableBlockState);

					}
				}
			}

			final float[] densities = new float[(scanSizeX + 1) * (scanSizeY + 1) * (scanSizeZ + 1)];

			// transverse the chunk + 2 blocks on every positive axis side
			for (int xOffset = 0; xOffset < scanSizeX + 1; xOffset++) {
				for (int yOffset = 0; yOffset < scanSizeY + 1; yOffset++) {
					for (int zOffset = 0; zOffset < scanSizeZ + 1; zOffset++) {

						final int x = xOffset;
						final int y = yOffset;
						final int z = zOffset;

						// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
						final int index = x + scanSizeX * (y + scanSizeY * z);

						densities[index] = getBlockDensity(isSmoothable, states, scanSizeX, cache, renderChunkPosX, renderChunkPosY, renderChunkPosZ, x, y, z, pooledMutableBlockPos);

					}
				}
			}

			final float[] neighbourDensityGrid = new float[8];

			final int[] r_noClue = new int[]{1, (scanSizeX + 1), (scanSizeX + 1) * (scanSizeY + 1)};
			int bufNo = 0;
			final int[] buffer = new int[r_noClue[2] * 2];

			final ArrayList<Vec3> vertices = new ArrayList<>();

			// transverse the chunk + 1 block on every positive axis side
			for (int posX = 0; posX < scanSizeX; posX++) {
				bufNo ^= 1;
				int bufferPointer = 1 + (scanSizeX + 1) * (1 + bufNo * (scanSizeY + 1));
				for (int posY = 0; posY < scanSizeY; posY++) {
					for (int posZ = 0; posZ < scanSizeZ; posZ++) {

						int mask = 0b00000000;
						int neighbourDensityGridIndex = 0;
						//every corner of the block
						for (int xOffset = 0; xOffset < 2; xOffset++) {
							for (int yOffset = 0; yOffset < 2; yOffset++) {
								for (int zOffset = 0; zOffset < 2; zOffset++) {
									final int x = posX + xOffset;
									final int y = posY + yOffset;
									final int z = posZ + zOffset;

									// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
									final int index = x + scanSizeX * (y + scanSizeY * z);

									float density = densities[index];
									neighbourDensityGrid[neighbourDensityGridIndex] = density;
									mask |= (density < 0) ? (1 << neighbourDensityGridIndex) : 0;

									neighbourDensityGridIndex++;
								}
							}
						}

						//Check for early termination if cell does not intersect boundary
						if (mask == 0 || mask == 0xFF) {
							continue;
						}

						//Sum up edge intersections
						final int edgeMask = EDGE_TABLE[mask];
						final Vec3 vertex = new Vec3();
						int edgeCrossingCount = 0;

						//For every edge of the cube...
						for (int edgeIndex = 0; edgeIndex < 12; ++edgeIndex) {

							//Use edge mask to check if it is crossed
							if ((edgeMask & (1 << edgeIndex)) == 0) {
								continue;
							}

							//If it did, increment number of edge crossings
							++edgeCrossingCount;

							//Now find the point of intersection
							int edge0 = CUBE_EDGES[edgeIndex << 1];       //Unpack vertices
							int edge1 = CUBE_EDGES[(edgeIndex << 1) + 1];
							float density0 = neighbourDensityGrid[edge0];                 //Unpack grid values
							float density1 = neighbourDensityGrid[edge1];
							float pointOfIntersection = density0 - density1;                 //Compute point of intersection
							if (Math.abs(pointOfIntersection) > 1e-6) {
								pointOfIntersection = density0 / pointOfIntersection;
							} else {
								continue;
							}

							final int xA = edge0 & 1;
							final int xB = edge1 & 1;
							if (xA != xB) {
								vertex.xCoord += xA != 0 ? 1.0 - pointOfIntersection : pointOfIntersection;
							} else {
								vertex.xCoord += xA != 0 ? 1.0 : 0;
							}

							final int yA = edge0 & 2;
							final int yB = edge1 & 2;
							if (yA != yB) {
								vertex.yCoord += yA != 0 ? 1.0 - pointOfIntersection : pointOfIntersection;
							} else {
								vertex.yCoord += yA != 0 ? 1.0 : 0;
							}

							final int zA = edge0 & 3;
							final int zB = edge1 & 3;
							if (zA != zB) {
								vertex.zCoord += zA != 0 ? 1.0 - pointOfIntersection : pointOfIntersection;
							} else {
								vertex.zCoord += zA != 0 ? 1.0 : 0;
							}

						}

						//Now we just average the edge intersections and add them to coordinate
						float s = isosurfaceLevel / edgeCrossingCount;
						vertex.xCoord = renderChunkPosX + posX + s * vertex.xCoord;
						vertex.yCoord = renderChunkPosY + posY + s * vertex.yCoord;
						vertex.zCoord = renderChunkPosZ + posZ + s * vertex.zCoord;

						//Add vertex to buffer, store pointer to vertex index in buffer
//						buffer[m] = vertices.size();
						if (ModConfig.offsetVertices)
							ModUtil.offsetVertex(vertex);
						vertices.add(vertex);

					}
				}
			}

		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			pooledMutableBlockPos.release();
		}
	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {

	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {

	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

	}

}


