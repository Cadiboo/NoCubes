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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.ArrayList;
import java.util.HashMap;

import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.CACHE_ARRAY_SIZE;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.DENSITY_CACHE_ARRAY_SIZE;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.MESH_SIZE_X;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.MESH_SIZE_Y;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.MESH_SIZE_Z;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.fillDensityCache;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.fillSmoothableCache;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.fillStateCache;
import static io.github.cadiboo.nocubes.client.render.MarchingCubes.EDGE_TABLE;
import static io.github.cadiboo.nocubes.client.render.MarchingCubes.TRIANGLE_TABLE;
import static io.github.cadiboo.renderchunkrebuildchunkhooks.hooks.RenderChunkRebuildChunkHooksHooks.compiledChunk_setLayerUsed;
import static io.github.cadiboo.renderchunkrebuildchunkhooks.hooks.RenderChunkRebuildChunkHooksHooks.renderChunk_preRenderBlocks;

/**
 * @author Cadiboo
 */
public final class SurfaceNetsDev {

	private static final ThreadLocal<HashMap<BlockPos, HashMap<BlockPos, ArrayList<Face<Vec3>>>>> FACES_BLOCKPOS_MAP = ThreadLocal.withInitial(HashMap::new);

	// because surface nets takes the 8 points of a block into account, we need to get the densities for +1 block on every positive axis of the chunk
	// because of this, we need to cache +2 blocks on every positive axis of the chunk

	public static void renderPre(final RebuildChunkPreEvent event) {

		final MutableBlockPos renderChunkPos = event.getRenderChunkPosition();
		final IBlockAccess cache = ClientUtil.getCache(event);
		final RenderChunk renderChunk = event.getRenderChunk();

		final int renderChunkPosX = renderChunkPos.getX();
		final int renderChunkPosY = renderChunkPos.getY();
		final int renderChunkPosZ = renderChunkPos.getZ();

		final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();

		final float isoSurfaceLevel = ModConfig.getIsosurfaceLevel();

		try {
			// caches need two extra blocks on every positive axis
			final IBlockState[] states = new IBlockState[CACHE_ARRAY_SIZE];
			fillStateCache(states, renderChunkPosX, renderChunkPosY, renderChunkPosZ, cache, pooledMutableBlockPos);
			final boolean[] smoothables = new boolean[CACHE_ARRAY_SIZE];
			fillSmoothableCache(smoothables, states);

			// densities needs 1 extra block on every positive axis
			final float[] densities = new float[DENSITY_CACHE_ARRAY_SIZE];
			fillDensityCache(densities, renderChunkPosX, renderChunkPosY, renderChunkPosZ, cache, pooledMutableBlockPos, states, smoothables);

			final int maxX = MESH_SIZE_X;
			final int maxY = MESH_SIZE_Y;
			final int maxZ = MESH_SIZE_Z;

			final float[] neighbourDensities = new float[8];
			int bitMask = 0;
			final ArrayList<Face<Vec3>> faces = new ArrayList<>();

			int mutableIndex = 0;
			for (int x = 0; x < maxX; x++) {
				for (int y = 0; y < maxY; y++) {
					for (int z = 0; z < maxZ; z++) {

//						bitMask = calculateMask(neighbourDensities, x, y, z, densities, isoSurfaceLevel);
						bitMask = 0;

						//Check for early termination if cell does not intersect boundary
						if (bitMask == 0 || bitMask == 0xFF) {
							continue;
						}

						int edgeMask = EDGE_TABLE[bitMask];


						/* Cube is entirely in/out of the surface */
						if (edgeMask == 0)
							continue;

						final Vec3[] edgeVertices = new Vec3[12];
						final Vec3[] pointList = new Vec3[]{
								new Vec3(0, 0, 0),
								new Vec3(1, 0, 0),
								new Vec3(1, 0, 1),
								new Vec3(0, 0, 1),
								new Vec3(0, 1, 0),
								new Vec3(1, 1, 0),
								new Vec3(1, 1, 1),
								new Vec3(0, 1, 1)
						};

						/* Find the vertices where the surface intersects the cube */
						if ((edgeMask & 1) != 0)
							edgeVertices[0] = vertexInterp(isoSurfaceLevel, pointList[0], pointList[1], neighbourDensities[0], neighbourDensities[1]);
						if ((edgeMask & 2) != 0)
							edgeVertices[1] = vertexInterp(isoSurfaceLevel, pointList[1], pointList[2], neighbourDensities[1], neighbourDensities[2]);
						if ((edgeMask & 4) != 0)
							edgeVertices[2] = vertexInterp(isoSurfaceLevel, pointList[2], pointList[3], neighbourDensities[2], neighbourDensities[3]);
						if ((edgeMask & 8) != 0)
							edgeVertices[3] = vertexInterp(isoSurfaceLevel, pointList[3], pointList[0], neighbourDensities[3], neighbourDensities[0]);
						if ((edgeMask & 16) != 0)
							edgeVertices[4] = vertexInterp(isoSurfaceLevel, pointList[4], pointList[5], neighbourDensities[4], neighbourDensities[5]);
						if ((edgeMask & 32) != 0)
							edgeVertices[5] = vertexInterp(isoSurfaceLevel, pointList[5], pointList[6], neighbourDensities[5], neighbourDensities[6]);
						if ((edgeMask & 64) != 0)
							edgeVertices[6] = vertexInterp(isoSurfaceLevel, pointList[6], pointList[7], neighbourDensities[6], neighbourDensities[7]);
						if ((edgeMask & 128) != 0)
							edgeVertices[7] = vertexInterp(isoSurfaceLevel, pointList[7], pointList[4], neighbourDensities[7], neighbourDensities[4]);
						if ((edgeMask & 256) != 0)
							edgeVertices[8] = vertexInterp(isoSurfaceLevel, pointList[0], pointList[4], neighbourDensities[0], neighbourDensities[4]);
						if ((edgeMask & 512) != 0)
							edgeVertices[9] = vertexInterp(isoSurfaceLevel, pointList[1], pointList[5], neighbourDensities[1], neighbourDensities[5]);
						if ((edgeMask & 1024) != 0)
							edgeVertices[10] = vertexInterp(isoSurfaceLevel, pointList[2], pointList[6], neighbourDensities[2], neighbourDensities[6]);
						if ((edgeMask & 2048) != 0)
							edgeVertices[11] = vertexInterp(isoSurfaceLevel, pointList[3], pointList[7], neighbourDensities[3], neighbourDensities[7]);

						int[] triangleTableIndex = TRIANGLE_TABLE[bitMask];
						/* Create the triangle */
						for (int i = 0; i < triangleTableIndex.length; i += 3) {
							faces.add(new Face<>(
									edgeVertices[triangleTableIndex[i] + 0].move(renderChunkPosX + x, renderChunkPosY + y, renderChunkPosZ + z),
									edgeVertices[triangleTableIndex[i + 1]].move(renderChunkPosX + x, renderChunkPosY + y, renderChunkPosZ + z),
									edgeVertices[triangleTableIndex[i + 2]].move(renderChunkPosX + x, renderChunkPosY + y, renderChunkPosZ + z)
							));
						}

					}
				}
			}

			for (Face<Vec3> face : faces) {

				final Vec3 v0 = face.getVertex0();
				final Vec3 v1 = face.getVertex1();
				final Vec3 v2 = face.getVertex2();
				final Vec3 v3 = face.getVertex3();

				final BlockRenderData renderData = ClientUtil.getBlockRenderData(pooledMutableBlockPos.setPos(
						ModUtil.average(v0.x, v1.x, v2.x, v3.x),
						ModUtil.average(v0.y, v1.y, v2.y, v3.y),
						ModUtil.average(v0.z, v1.z, v2.z, v3.z)
				), cache);

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

				bufferBuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

			}

		} catch (final Exception e) {
			ModUtil.crashIfNotDev(e);
		} finally {
			pooledMutableBlockPos.release();
		}

	}

	/**
	 * Linearly interpolate the position where an isosurface cuts
	 * an edge between two vertices, each with their own scalar value
	 */
	private static Vec3 vertexInterp(float isolevel, Vec3 p1, Vec3 p2, float valp1, float valp2) {
		if (Math.abs(isolevel - valp1) < 0.00001)
			return (p1);
		if (Math.abs(isolevel - valp2) < 0.00001)
			return (p2);
		if (Math.abs(valp1 - valp2) < 0.00001)
			return (p1);
		final float mu = (isolevel - valp1) / (valp2 - valp1);
		return new Vec3(
				p1.x + mu * (p2.x - p1.x),
				p1.y + mu * (p2.y - p1.y),
				p1.z + mu * (p2.z - p1.z)
		);
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
