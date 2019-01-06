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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.CACHE_ARRAY_SIZE;
import static io.github.cadiboo.nocubes.client.SurfaceNetsUtil.DENSITY_CACHE_ARRAY_SIZE;
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
