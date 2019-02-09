package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.ExtendedLiquidChunkRenderer;
import io.github.cadiboo.nocubes.client.render.MeshRenderer;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.SmoothableCache;
import io.github.cadiboo.nocubes.util.StateCache;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import static io.github.cadiboo.nocubes.util.ModUtil.LEAVES_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;

/**
 * @author Cadiboo
 */
public class RenderDispatcher {

	private static final ThreadLocal<boolean[]> USED_RENDER_LAYERS = ThreadLocal.withInitial(() -> new boolean[BlockRenderLayer.values().length]);

	public static void renderChunk(final RebuildChunkPreEvent event) {
		final RenderChunk renderChunk = event.getRenderChunk();
		final ChunkCompileTaskGenerator generator = event.getGenerator();
		final CompiledChunk compiledChunk = event.getCompiledChunk();
		final BlockPos renderChunkPosition = event.getRenderChunkPosition();
		final IBlockAccess blockAccess = event.getIBlockAccess();

		final int meshSizeX;
		final int meshSizeY;
		final int meshSizeZ;
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

		final BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.retain();
		try {
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
		} finally {
			pooledMutableBlockPos.release();
		}
	}

	private static void renderChunk(
			final RenderChunk renderChunk,
			final ChunkCompileTaskGenerator generator,
			final CompiledChunk compiledChunk,
			final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			final IBlockAccess blockAccess,
			final BlockPos.PooledMutableBlockPos pooledMutableBlockPos,
			final int meshSizeX, final int meshSizeY, final int meshSizeZ
	) {
		final ModProfiler profiler = NoCubes.getProfiler();
		final boolean[] usedBlockRenderLayers = USED_RENDER_LAYERS.get();
		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

		{
//			for(MeshLayer layer : meshLayers)
			//TODO get this from world & chunk & layer
			final StateCache stateCache = generateMeshStateCache(
					renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
					meshSizeX, meshSizeY, meshSizeZ,
					blockAccess,
					pooledMutableBlockPos
			);
			//TODO get this from world & chunk & layer
			final SmoothableCache terrainSmoothableCache = CacheUtil.generateSmoothableCache(
					stateCache, TERRAIN_SMOOTHABLE
			);

			final PackedLightCache packedLightCache = ClientCacheUtil.generatePackedLightCache(
					renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
					stateCache, blockAccess, pooledMutableBlockPos
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

		if (ModConfig.extendLiquids) {
			profiler.startSection("extendLiquids");

			//TODO get this from world & chunk & layer
			final StateCache stateCache = generateExtendedWaterStateCache(
					renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
					blockAccess,
					pooledMutableBlockPos
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

	private static StateCache generateMeshStateCache(
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			final int meshSizeX, final int meshSizeY, final int meshSizeZ,
			final IBlockAccess blockAccess,
			final BlockPos.PooledMutableBlockPos pooledMutableBlockPos
	) {
		// Density takes +1 block on every negative axis into account so we need to start at -1 block
		final int cacheStartPosX = renderChunkPositionX - 1;
		final int cacheStartPosY = renderChunkPositionY - 1;
		final int cacheStartPosZ = renderChunkPositionZ - 1;

		// Density takes +1 block on every negative axis into account so we need bigger caches
		// All up this is +2 (1*2 for Density)
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
			final IBlockAccess blockAccess,
			final BlockPos.PooledMutableBlockPos pooledMutableBlockPos
	) {
		// ExtendedWater takes +2 blocks on every negative axis into account so we need to start at -2 blocks
		final int cacheStartPosX = renderChunkPositionX - 2;
		final int cacheStartPosY = renderChunkPositionY - 2;
		final int cacheStartPosZ = renderChunkPositionZ - 2;

		// ExtendedWater takes +2 blocks on every negative axis into account so we need bigger caches
		// All up this is +4 (2*2 for ExtendedWater)
		// 16 is the size of a chunk (blocks 0 -> 15)
		final int cacheSizeX = 16 + 4;
		final int cacheSizeY = 16 + 4;
		final int cacheSizeZ = 16 + 4;

		return CacheUtil.generateStateCache(
				cacheStartPosX, cacheStartPosY, cacheStartPosZ,
				cacheSizeX, cacheSizeY, cacheSizeZ,
				blockAccess,
				pooledMutableBlockPos
		);
	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {
		final ModProfiler profiler = NoCubes.getProfiler();
		profiler.startSection("extendLiquids");
		try {

			final int ordinal = event.getBlockRenderLayer().ordinal();
			event.getUsedBlockRenderLayers()[ordinal] |= USED_RENDER_LAYERS.get()[ordinal];
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
		profiler.endSection();
	}

}
