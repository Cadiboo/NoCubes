package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.CACHE_ARRAY_SIZE;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.CUBE_EDGES;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.DENSITY_CACHE_ARRAY_SIZE;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.EDGE_TABLE;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.MESH_SIZE_X;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.MESH_SIZE_Y;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.MESH_SIZE_Z;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.calculateNeighbourDensitiesAndMask;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.fillDensityCache;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.fillSmoothableCache;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.fillStateCache;
import static io.github.cadiboo.renderchunkrebuildchunkhooks.hooks.RenderChunkRebuildChunkHooksHooks.renderChunk_preRenderBlocks;

/**
 * Implementation of the SurfaceNets algorithm in Minecraft
 *
 * @author Cadiboo
 * @see "https://mikolalysenko.github.io/Isosurface/js/surfacenets.js"
 */
public final class SurfaceNets {

	private static final ThreadLocal<boolean[]> USED_BLOCK_RENDER_LAYERS_THREAD_LOCAL = ThreadLocal.withInitial(() -> new boolean[BlockRenderLayer.values().length]);

	public static void renderPre(final RebuildChunkPreEvent event) {

		final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
		try {

			//<editor-fold desc="Basic Setup">
			final RenderChunk renderChunk = event.getRenderChunk();
			final IBlockAccess cache = ClientUtil.getCache(event);
			final ChunkCompileTaskGenerator generator = event.getGenerator();
			final CompiledChunk compiledChunk = event.getCompiledChunk();

			final MutableBlockPos renderChunkPos = event.getRenderChunkPosition();

			final int renderChunkPosX = renderChunkPos.getX();
			final int renderChunkPosY = renderChunkPos.getY();
			final int renderChunkPosZ = renderChunkPos.getZ();

			final float isoSurfaceLevel = ModConfig.getIsosurfaceLevel();

			final boolean[] usedBlockRenderLayers = new boolean[BlockRenderLayer.values().length];
			//</editor-fold>

			//<editor-fold desc="Generate Caches">
			// caches need two extra blocks on every positive axis
			final IBlockState[] stateCache = new IBlockState[CACHE_ARRAY_SIZE];
			fillStateCache(stateCache, renderChunkPosX, renderChunkPosY, renderChunkPosZ, cache, pooledMutableBlockPos);
			final boolean[] smoothableCache = new boolean[CACHE_ARRAY_SIZE];
			fillSmoothableCache(smoothableCache, stateCache);

			// densities needs 1 extra block on every positive axis
			final float[] densityCache = new float[DENSITY_CACHE_ARRAY_SIZE];
			fillDensityCache(densityCache, renderChunkPosX, renderChunkPosY, renderChunkPosZ, cache, pooledMutableBlockPos, stateCache, smoothableCache);
			//</editor-fold>

			//<editor-fold desc="Setup SurfaceNets">
			int n = 0;
			final int[] R = {1, (MESH_SIZE_X + 1 + 1), (MESH_SIZE_X + 1 + 1) * (MESH_SIZE_Y + 1 + 1)};
			final float[] neighbourDensities = new float[8];
			int bitMask = 0;
			int mutableIndex = 0;
			int bufNo = 1;
			int bufferIndex = 0;
			final Vec3[] buffer = new Vec3[R[2] * 2];
			final int[] x = new int[3];
			final int[] c = {renderChunkPosX, renderChunkPosY, renderChunkPosZ};
			//</editor-fold>

			//March over the voxel grid
			for (x[2] = 0; x[2] < MESH_SIZE_Z; ++x[2], n += MESH_SIZE_X + 1, bufNo ^= 1, R[2] = -R[2]) {

				//m is the pointer into the buffer we are going to use.  
				//This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(
				//The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
				bufferIndex = 1 + (MESH_SIZE_X + 1 + 1) * (1 + bufNo * (MESH_SIZE_Y + 1 + 1));

				for (x[1] = 0; x[1] < MESH_SIZE_Y; ++x[1], ++n, bufferIndex += 2)
					for (x[0] = 0; x[0] < MESH_SIZE_X; ++x[0], ++n, ++bufferIndex) {

						//Read in 8 field values around this vertex and store them in an array
						//Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
						final int mask = calculateNeighbourDensitiesAndMask(neighbourDensities, x[0], x[1], x[2], densityCache);

						//Check for early termination if cell does not intersect boundary
						if (mask == 0 || mask == 0xff) {
							continue;
						}

						//Sum up edge intersections
						int edge_mask = EDGE_TABLE[mask];
						float[] v = {0, 0, 0};
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
							float
									g0 = neighbourDensities[e0]                 //Unpack grid values
									, g1 = neighbourDensities[e1], t = g0 - g1;                 //Compute point of intersection
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
						for (int i = 0; i < 3; ++i) {
							v[i] = c[i] + x[i] + s * v[i];
						}

						final BlockRenderData renderData = ClientUtil.getBlockRenderData(pooledMutableBlockPos.setPos(c[0] + x[0], c[1] + x[1], c[2] + x[2]), cache);
						final BlockRenderLayer blockRenderLayer = renderData.getBlockRenderLayer();
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
						if (!compiledChunk.isLayerStarted(blockRenderLayer)) {
							compiledChunk.setLayerStarted(blockRenderLayer);
//							compiledChunk_setLayerUsed(compiledChunk, blockRenderLayer);
							usedBlockRenderLayers[blockRenderLayer.ordinal()] = true;
							renderChunk_preRenderBlocks(renderChunk, bufferBuilder, renderChunkPos);
						}

						//Add vertex to buffer, store pointer to vertex index in buffer
						buffer[bufferIndex] = new Vec3(v);

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

							final Vec3 v0 = buffer[bufferIndex];
							final Vec3 v1 = buffer[bufferIndex - du];
							final Vec3 v2 = buffer[bufferIndex - du - dv];
							final Vec3 v3 = buffer[bufferIndex - dv];

							//Remember to flip orientation depending on the sign of the corner.
							if ((mask & 1) != 0) {
								bufferBuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							} else {
								bufferBuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							}
						}
					}
			}

			USED_BLOCK_RENDER_LAYERS_THREAD_LOCAL.set(usedBlockRenderLayers);

		} catch (Exception e) {
			ModUtil.crashIfNotDev(e);
		} finally {
			pooledMutableBlockPos.release();
		}

	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {

	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {

	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

		event.setCanceled(ModUtil.shouldSmooth(event.getBlockState()));

		final int ordinal = event.getBlockRenderLayer().ordinal();
		event.getUsedBlockRenderLayers()[ordinal] |= USED_BLOCK_RENDER_LAYERS_THREAD_LOCAL.get()[ordinal];

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

//		USED_BLOCK_RENDER_LAYERS_THREAD_LOCAL.set(null);

	}

}
