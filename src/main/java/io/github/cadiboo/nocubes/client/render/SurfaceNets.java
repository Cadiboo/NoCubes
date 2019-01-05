package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;

import static io.github.cadiboo.nocubes.NoCubes.NO_CUBES_LOG;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.CACHE_ARRAY_SIZE;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.DENSITY_CACHE_ARRAY_SIZE;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.MESH_SIZE_X;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.MESH_SIZE_Y;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.MESH_SIZE_Z;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.calculateNeighbourDensitiesAndMask;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.fillDensityCache;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.fillSmoothableCache;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.fillStateCache;

/**
 * Implementation of the SurfaceNets algorithm in Minecraft
 *
 * @author Cadiboo
 * @see "https://mikolalysenko.github.io/Isosurface/js/surfacenets.js"
 */
public final class SurfaceNets {

	public static void renderPre(final RebuildChunkPreEvent event) {

		final BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.retain();
		try {

			final RenderChunk renderChunk = event.getRenderChunk();
			final IBlockAccess cache = ClientUtil.getCache(event);
			final ChunkCompileTaskGenerator generator = event.getGenerator();
			final CompiledChunk compiledChunk = event.getCompiledChunk();

			final MutableBlockPos renderChunkPos = event.getRenderChunkPosition();

			final int renderChunkPosX = renderChunkPos.getX();
			final int renderChunkPosY = renderChunkPos.getY();
			final int renderChunkPosZ = renderChunkPos.getZ();

			final float isoSurfaceLevel = ModConfig.getIsosurfaceLevel();

			// caches need two extra blocks on every positive axis
			final IBlockState[] states = new IBlockState[CACHE_ARRAY_SIZE];
			fillStateCache(states, renderChunkPosX, renderChunkPosY, renderChunkPosZ, cache, pooledMutableBlockPos);
			final boolean[] smoothables = new boolean[CACHE_ARRAY_SIZE];
			fillSmoothableCache(smoothables, states);

			// densities needs 1 extra block on every positive axis
			final float[] densities = new float[DENSITY_CACHE_ARRAY_SIZE];
			fillDensityCache(densities, renderChunkPosX, renderChunkPosY, renderChunkPosZ, cache, pooledMutableBlockPos, states, smoothables);

			ModUtil.dump(NO_CUBES_LOG, densities);

			final int[] dims = {MESH_SIZE_X, MESH_SIZE_Y, MESH_SIZE_Z};
			final int[] pos = new int[3];
			final int[] offset = {renderChunkPosX, renderChunkPosY, renderChunkPosZ};

			int n = 0;
			int buf_no = 0;
			final int[] R = {1, dims[0], dims[0] * dims[1]};
			final float[] neighbourDensities = new float[8];

			//March over the voxel grid
			for (pos[2] = 0; pos[2] < dims[2] - 1; ++pos[2], n += dims[0], buf_no ^= 1, R[2] = -R[2]) {

				//m is the pointer into the buffer we are going to use.
				//This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(
				//The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
				int m = 1 + (dims[0] + 1) * (1 + buf_no * (dims[1] + 1));

				for (pos[1] = 0; pos[1] < dims[1] - 1; ++pos[1], ++n, m += 2) {
					for (pos[0] = 0; pos[0] < dims[0] - 1; ++pos[0], ++n, ++m) {
						pooledMutableBlockPos.setPos(offset[0] + pos[0], offset[1] + pos[1], offset[2] + pos[2]);

						//Read in 8 field values around this vertex and store them in an array
						//Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
						final int mask = calculateNeighbourDensitiesAndMask(neighbourDensities, pos[0], pos[1], pos[2], densities);

						int mask2 = 0b00000000;
						int pointIndex = 0;

						float[] grid = new float[8];

						for (int z = 0; z < 2; ++z) {
							for (int y = 0; y < 2; ++y) {
								for (int x = 0; x < 2; ++pointIndex) {
									// TODO: mutableblockpos?
									// final float p = potential.apply(new BlockPos(c[0] + x[0] + i, c[1] + x[1] + j, c[2] + x[2] + k), cache);

									final float p = ModUtil.getBlockDensity(new BlockPos(offset[0] + pos[0] + x, offset[1] + pos[1] + y, offset[2] + pos[2] + z), cache);
									grid[pointIndex] = p;
									mask2 |= p > 0.0F ? 1 << pointIndex : 0;
									++x;

								}
							}
						}

						grid.clone();

					}
				}
			}

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

		event.getUsedBlockRenderLayers()[event.getBlockRenderLayer().ordinal()] = true;

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

	}

}
