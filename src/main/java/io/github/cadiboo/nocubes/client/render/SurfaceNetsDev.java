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
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.ArrayList;
import java.util.HashMap;

import static io.github.cadiboo.renderchunkrebuildchunkhooks.hooks.RenderChunkRebuildChunkHooksHooks.compiledChunk_setLayerUsed;
import static io.github.cadiboo.renderchunkrebuildchunkhooks.hooks.RenderChunkRebuildChunkHooksHooks.renderChunk_preRenderBlocks;

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

		final float isoSurfaceLevel = ModConfig.getIsosurfaceLevel();
		final MutableBlockPos renderChunkPos = event.getRenderChunkPosition();
		final IBlockAccess cache = ClientUtil.getCache(event);
		final RenderChunk renderChunk = event.getRenderChunk();

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

			//

			//

			//

			final int[] EDGE_TABLE = SurfaceNets.EDGE_TABLE;
			final int[] CUBE_EDGES = SurfaceNets.CUBE_EDGES;

			final ArrayList<float[]> vertices = new ArrayList<>();

			final int[] buffer;

//			var vertices = []
//    , faces = []
			int n = 0;
			final int[] x = new int[3],
					R = new int[]{1, (maxX + 1), (maxX + 1) * (maxY + 1)};
			final float[] grid = new float[8];
			int buf_no = 1;

			//Resize buffer if necessary
//		if (R[2] * 2 > buffer.length) {
//			buffer = new Int32Array(R[2] * 2);
//		}
			buffer = new int[R[2] * 2];

			//March over the voxel grid
			for (x[2] = 0; x[2] < maxZ - 1; ++x[2], n += maxX, buf_no ^= 1, R[2] = -R[2]) {

				//m is the pointer into the buffer we are going to use.
				//This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(
				//The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
				int m = 1 + (maxX + 1) * (1 + buf_no * (maxY + 1));

				for (x[1] = 0; x[1] < maxY - 1; ++x[1], ++n, m += 2) {
					for (x[0] = 0; x[0] < maxX - 1; ++x[0], ++n, ++m) {
//						pos.setPos(c[0] + x[0], c[1] + x[1], c[2] + x[2]);

						//Read in 8 field values around this vertex and store them in an array
						//Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
						int mask = calculateNeighbourDensitiesAndMask(grid, x[0], x[1], x[2], densities);

						//Check for early termination if cell does not intersect boundary
						if (mask == 0 || mask == 0xFF) {
							continue;
						}

						//Sum up edge intersections
						final int edge_mask = EDGE_TABLE[mask];
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
							int e0 = CUBE_EDGES[i << 1]       //Unpack vertices
									, e1 = CUBE_EDGES[(i << 1) + 1];
							float g0 = grid[e0]                 //Unpack grid values
									, g1 = grid[e1], t = g0 - g1;                 //Compute point of intersection
							if (Math.abs(t) > 1e-6) {
								t = g0 / t;
							} else {
								continue;
							}

							//Interpolate vertices and add up intersections (this can be done without multiplying)
							for (int j = 0, k = 1; j < 3; ++j, k <<= 1) {
								int a = e0 & k, b = e1 & k;
								if (a != b) {
									v[j] += a != 0 ? 1.0 - t : t;
								} else {
									v[j] += a != 0 ? 1.0 : 0;
								}
							}
						}

						//Now we just average the edge intersections and add them to coordinate
						float s = isoSurfaceLevel / e_count;
						v[0] = renderChunkPosX + x[0] + s * v[0];
						v[1] = renderChunkPosY + x[1] + s * v[1];
						v[2] = renderChunkPosZ + x[2] + s * v[2];

						//Add vertex to buffer, store pointer to vertex index in buffer
						buffer[m] = vertices.size();
						if (ModConfig.offsetVertices)
							ModUtil.offsetVertex(v);
						vertices.add(v);

						final BlockRenderData renderData = ClientUtil.getBlockRenderData(pooledMutableBlockPos.setPos(renderChunkPosX + x[0], renderChunkPosY + x[1], renderChunkPosZ + x[2]), cache);

						final BlockRenderLayer blockRenderLayer = renderData.getBlockRenderLayer();
						ForgeHooksClient.setRenderLayer(blockRenderLayer);
						final int red = renderData.getRed();
						final int green = renderData.getGreen();
						final int blue = renderData.getBlue();
						final int alpha = renderData.getAlpha();
						final float minU = renderData.getMinU();
						final float maxU = renderData.getMaxU();
						final float minV = renderData.getMinV();
						final float maxV = renderData.getMaxV();
						final int lightmapSkyLight = renderData.getLightmapSkyLight();
						final int lightmapBlockLight = renderData.getLightmapBlockLight();

						final BufferBuilder bufferBuilder = event.getGenerator().getRegionRenderCacheBuilder().getWorldRendererByLayer(blockRenderLayer);
						final CompiledChunk compiledChunk = event.getCompiledChunk();

						if (!compiledChunk.isLayerStarted(blockRenderLayer)) {
							compiledChunk.setLayerStarted(blockRenderLayer);
							compiledChunk_setLayerUsed(compiledChunk, blockRenderLayer);
							renderChunk_preRenderBlocks(renderChunk, bufferBuilder, renderChunkPos);
						}

						//Now we need to add faces together, to do this we just loop over 3 basis components
						for (int i = 0; i < 3; ++i) {
							//The first three entries of the edge_mask count the crossings along the edge
							if ((edge_mask & (1 << i)) == 0) {
								continue;
							}

							// i = axes we are point along.  iu, iv = orthogonal axes
							int iu = (i + 1) % 3, iv = (i + 2) % 3;

							//If we are on a boundary, skip it
							if (x[iu] == 0 || x[iv] == 0) {
								continue;
							}

							//Otherwise, look up adjacent edges in buffer
							int du = R[iu], dv = R[iv];

							//TODO: remove float[] -> Vec3 -> float shit
							//Remember to flip orientation depending on the sign of the corner.
							//FIXME:  cunt wtf why do I have to swap vertices (First one is CORRECT but doesnt work)
//						if ((mask & 1) != 0) {
							if ((mask & 1) == 0) {
//							faces.add([buffer[m], buffer[m - du], buffer[m - du - dv], buffer[m - dv]]);

								Vec3 vertex0 = new Vec3(vertices.get(buffer[m]));
								Vec3 vertex1 = new Vec3(vertices.get(buffer[m - du]));
								Vec3 vertex2 = new Vec3(vertices.get(buffer[m - du - dv]));
								Vec3 vertex3 = new Vec3(vertices.get(buffer[m - dv]));

								bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

							} else {
//							faces.add([buffer[m], buffer[m - dv], buffer[m - du - dv], buffer[m - du]]);

								Vec3 vertex0 = new Vec3(vertices.get(buffer[m]));
								Vec3 vertex1 = new Vec3(vertices.get(buffer[m - dv]));
								Vec3 vertex2 = new Vec3(vertices.get(buffer[m - du - dv]));
								Vec3 vertex3 = new Vec3(vertices.get(buffer[m - du]));

								bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

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
					if (density < 0F) {
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

	private static void fillDensityCache(final float[] densityCache, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final IBlockAccess cache, PooledMutableBlockPos pos, final IBlockState[] statesCache, final boolean[] smoothableCache) {
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
