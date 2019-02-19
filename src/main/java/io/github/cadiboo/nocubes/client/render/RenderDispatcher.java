package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientCacheUtil;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.ExtendLiquidRange;
import io.github.cadiboo.nocubes.client.PackedLightCache;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.SmoothableCache;
import io.github.cadiboo.nocubes.util.StateCache;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreRenderEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import static io.github.cadiboo.nocubes.config.ModConfig.approximateLighting;
import static io.github.cadiboo.nocubes.util.ModUtil.LEAVES_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;

/**
 * @author Cadiboo
 */
public class RenderDispatcher {

	private static final ThreadLocal<boolean[]> USED_RENDER_LAYERS = ThreadLocal.withInitial(() -> new boolean[BlockRenderLayer.values().length]);
	private static final ThreadLocal<Boolean> USED_RENDER_LAYERS_SET = ThreadLocal.withInitial(() -> false);

	public static void renderChunk(final RebuildChunkPreRenderEvent event) {
		final RenderChunk renderChunk = event.getRenderChunk();
		final ChunkRenderTask generator = event.getGenerator();
		final CompiledChunk compiledChunk = event.getCompiledChunk();
		final BlockPos renderChunkPosition = event.getRenderChunkStartPosition();
		final IWorldReader blockAccess = event.getWorld();

		final byte meshSizeX;
		final byte meshSizeY;
		final byte meshSizeZ;
		if (ModConfig.getMeshGenerator() == MeshGenerator.SurfaceNets) {
			//yay, surface nets is special and needs an extra +1. why? no-one knows
			meshSizeX = 18;
			meshSizeY = 18;
			meshSizeZ = 18;
		} else {
			meshSizeX = 17;
			meshSizeY = 17;
			meshSizeZ = 17;
		}

		final int renderChunkPositionX = renderChunkPosition.getX();
		final int renderChunkPositionY = renderChunkPosition.getY();
		final int renderChunkPositionZ = renderChunkPosition.getZ();

		try (final BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.retain()) {
			renderChunk(
					renderChunk,
					generator,
					compiledChunk,
					renderChunkPosition,
					renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
					blockAccess,
					pooledMutableBlockPos,
					meshSizeX, meshSizeY, meshSizeZ
			);
		}
	}

	private static void renderChunk(
			final RenderChunk renderChunk,
			final ChunkRenderTask generator,
			final CompiledChunk compiledChunk,
			final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			final IWorldReader blockAccess,
			final BlockPos.PooledMutableBlockPos pooledMutableBlockPos,
			final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ
	) {
		final ModProfiler profiler = NoCubes.getProfiler();
		final boolean[] usedBlockRenderLayers = USED_RENDER_LAYERS.get();
		USED_RENDER_LAYERS_SET.set(false);
		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

		{
//			for(MeshLayer layer : meshLayers)
			//TODO get this from world & chunk & layer
			final StateCache stateCache = generateMeshAndLightStateCache(
					renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
					meshSizeX, meshSizeY, meshSizeZ,
					blockAccess,
					pooledMutableBlockPos
			);
			//TODO get this from world & chunk & layer
			final SmoothableCache terrainSmoothableCache = CacheUtil.generateSmoothableCache(
					stateCache, TERRAIN_SMOOTHABLE
			);

			//TODO stateCache
			final PackedLightCache packedLightCache =
					ClientCacheUtil.generatePackedLightCache(
							approximateLighting ? renderChunkPositionX - 1 : 0, approximateLighting ? renderChunkPositionY - 1 : 0, approximateLighting ? renderChunkPositionZ - 1 : 0,
							approximateLighting ? stateCache.sizeX : 0, approximateLighting ? stateCache.sizeY : 0, approximateLighting ? stateCache.sizeZ : 0,
							blockAccess, pooledMutableBlockPos
					);

			profiler.startSection("renderMesh");
			try {
				MeshRenderer.renderChunk(
						renderChunk,
						generator,
						compiledChunk,
						renderChunkPosition,
						renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
						blockAccess,
						pooledMutableBlockPos,
						usedBlockRenderLayers,
						blockRendererDispatcher,
						meshSizeX, meshSizeY, meshSizeZ,
						stateCache, terrainSmoothableCache, packedLightCache
				);
			} catch (ReportedException e) {
				throw e;
			} catch (Exception e) {
				CrashReport crashReport = new CrashReport("Error rendering mesh!", e);
				crashReport.makeCategory("Rendering mesh");
				throw new ReportedException(crashReport);
			}
		}

		if (ModConfig.extendLiquids != ExtendLiquidRange.Off) {
			profiler.startSection("extendLiquids");

			//TODO get this from world & chunk & layer
			final StateCache stateCache = generateExtendedWaterStateCache(
					renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
					blockAccess,
					pooledMutableBlockPos,
					ClientUtil.getExtendLiquidsRange()
			);
			//TODO get this from world & chunk & layer
			final SmoothableCache terrainSmoothableCache = CacheUtil.generateSmoothableCache(
					stateCache, TERRAIN_SMOOTHABLE
			);

			try {
				ExtendedLiquidChunkRenderer.renderChunk(
						renderChunk,
						generator,
						compiledChunk,
						renderChunkPosition,
						renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
						blockAccess,
						pooledMutableBlockPos,
						usedBlockRenderLayers,
						blockRendererDispatcher,
						stateCache, terrainSmoothableCache
				);
			} catch (ReportedException e) {
				throw e;
			} catch (Exception e) {
				CrashReport crashReport = new CrashReport("Error extending liquids in Pre event!", e);
				crashReport.makeCategory("Extending liquids");
				throw new ReportedException(crashReport);
			}
			profiler.endSection();
		}
	}

	private static StateCache generateMeshAndLightStateCache(
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ,
			final IWorldReader blockAccess,
			final BlockPos.PooledMutableBlockPos pooledMutableBlockPos
	) {
		// Density takes +1 block on every negative axis into account so we need to start at -1 block
		// Light uses +1 block on every axis so we need to start at -1 block
		final int cacheStartPosX = renderChunkPositionX - 1;
		final int cacheStartPosY = renderChunkPositionY - 1;
		final int cacheStartPosZ = renderChunkPositionZ - 1;

		//TODO: once caches start getting stored in global data I will only need +1 cache size for density
		//TODO: once caches start getting stored in global data I will need +2 cache size for light in a separate method

		// Density takes +1 block on every negative axis into account so we need to add 1 to the size of the cache (it only takes +1 on NEGATIVE axis)
		// Light uses +1 block on every axis so we need to add 2 to the size of the cache (it takes +1 on EVERY axis)
		// All up this is +2 blocks (2 for Light, including the 1 for Density)
		final int cacheSizeX = meshSizeX + 2;
		final int cacheSizeY = meshSizeY + 2;
		final int cacheSizeZ = meshSizeZ + 2;

		return CacheUtil.generateStateCache(
				cacheStartPosX, cacheStartPosY, cacheStartPosZ,
				cacheSizeX, cacheSizeY, cacheSizeZ,
				blockAccess,
				pooledMutableBlockPos
		);
	}

	private static StateCache generateExtendedWaterStateCache(
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			final IWorldReader blockAccess,
			final BlockPos.PooledMutableBlockPos pooledMutableBlockPos,
			final int extendLiquidsRange
	) {
		// ExtendedWater takes +1 or +2 blocks on every horizontal axis into account so we need to start at -1 or -2 blocks
		final int cacheStartPosX = renderChunkPositionX - extendLiquidsRange;
		final int cacheStartPosY = renderChunkPositionY;
		final int cacheStartPosZ = renderChunkPositionZ - extendLiquidsRange;

		// ExtendedWater takes +1 or +2 blocks on each side of the chunk (x and z) into account so we need to add 2 or 4 to the size of the cache (it takes +1 or +2 on EVERY HORIZONTAL axis)
		// ExtendedWater takes +1 block on the Y axis into account so we need to add 1 to the size of the cache (it takes +1 on the POSITIVE Y axis)
		// All up this is +2 or +4 (2 or 4 for ExtendedWater) for every horizontal axis and +1 for the Y axis
		// 16 is the size of a chunk (blocks 0 -> 15)
		final int cacheSizeX = 16 + extendLiquidsRange * 2;
		final int cacheSizeY = 16 + 1;
		final int cacheSizeZ = 16 + extendLiquidsRange * 2;

		return CacheUtil.generateStateCache(
				cacheStartPosX, cacheStartPosY, cacheStartPosZ,
				cacheSizeX, cacheSizeY, cacheSizeZ,
				blockAccess,
				pooledMutableBlockPos
		);
	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {
		try {
			if (!USED_RENDER_LAYERS_SET.get()) {
				for (int ordinal = 0; ordinal < BlockRenderLayer.values().length; ++ordinal) {
					event.getUsedBlockRenderLayers()[ordinal] |= USED_RENDER_LAYERS.get()[ordinal];
				}
				USED_RENDER_LAYERS_SET.set(true);
				//remove so it gets re-initialised so that we don't get ghost blocks from now-unused render layers
				USED_RENDER_LAYERS.remove();
			}

			final IBlockState state = event.getBlockState();
			event.setCanceled(
					TERRAIN_SMOOTHABLE.isSmoothable(state) || LEAVES_SMOOTHABLE.isSmoothable(state)
			);
		} catch (ReportedException e) {
			throw e;
		} catch (Exception e) {
			CrashReport crashReport = new CrashReport("Error rendering smooth chunk in Block event!", e);
			final CrashReportCategory crashReportCategory = crashReport.makeCategory("Rendering smooth chunk");
			CrashReportCategory.addBlockInfo(crashReportCategory, event.getBlockPos(), event.getBlockState());
			throw new ReportedException(crashReport);
		}
	}

}
