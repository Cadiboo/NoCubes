package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientCacheUtil;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.PackedLightCache;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.IIsSmoothable;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

/**
 * @author Cadiboo
 */
public class RenderDispatcher {

	public static void renderChunk(final RenderChunk renderChunk, final BlockPos renderChunkPosition, final ChunkRenderTask generator, final CompiledChunk compiledChunk, final IWorldReader blockAccess, final boolean[] usedBlockRenderLayers) {

		final byte meshSizeX;
		final byte meshSizeY;
		final byte meshSizeZ;
//		if (ModConfig.terrainMeshGenerator == MeshGenerator.SurfaceNets)
		{
			//yay, surface nets is special and needs an extra +1. why? no-one knows
			meshSizeX = 18;
			meshSizeY = 18;
			meshSizeZ = 18;
//		} else {
//			meshSizeX = 17;
//			meshSizeY = 17;
//			meshSizeZ = 17;
		}

		final int renderChunkPositionX = renderChunkPosition.getX();
		final int renderChunkPositionY = renderChunkPosition.getY();
		final int renderChunkPositionZ = renderChunkPosition.getZ();

		try (PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()) {
			renderChunk(
					renderChunk,
					generator,
					compiledChunk,
					renderChunkPosition,
					renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
					blockAccess,
					pooledMutableBlockPos,
					meshSizeX, meshSizeY, meshSizeZ,
					usedBlockRenderLayers);
		}
	}

	private static void renderChunk(
			final RenderChunk renderChunk,
			final ChunkRenderTask generator,
			final CompiledChunk compiledChunk,
			final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			final IWorldReader blockAccess,
			final PooledMutableBlockPos pooledMutableBlockPos,
			final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ,
			final boolean[] usedBlockRenderLayers) {
		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

		// Terrain & leaves rendering
		{
			try (final StateCache lightAndTexturesStateCache = generateLightAndTexturesStateCache(renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ, meshSizeX, meshSizeY, meshSizeZ, blockAccess, pooledMutableBlockPos)) {
//				try (final PackedLightCache packedLightCache = ModConfig.approximateLighting ? ClientCacheUtil.generatePackedLightCache(renderChunkPositionX - 1, renderChunkPositionY - 1, renderChunkPositionZ - 1, lightAndTexturesStateCache, blockAccess, pooledMutableBlockPos) : PackedLightCache.retain(0, 0, 0)) {
				try (final PackedLightCache packedLightCache =  ClientCacheUtil.generatePackedLightCache(renderChunkPositionX - 1, renderChunkPositionY - 1, renderChunkPositionZ - 1, lightAndTexturesStateCache, blockAccess, pooledMutableBlockPos)) {
//					try (final ModProfiler ignored = NoCubes.getProfiler().start("renderMesh")) {
					try {
						MeshRenderer.renderChunkMeshes(
								renderChunk,
								generator,
								compiledChunk,
								renderChunkPosition,
								renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
								blockAccess,
								lightAndTexturesStateCache,
								pooledMutableBlockPos,
								usedBlockRenderLayers,
								blockRendererDispatcher,
								packedLightCache
						);
					} catch (ReportedException e) {
						throw e;
					} catch (Exception e) {
						CrashReport crashReport = new CrashReport("Error rendering mesh!", e);
						crashReport.makeCategory("Rendering mesh");
						throw new ReportedException(crashReport);
					}
//					}
				}
			}
		}

//		if (ModConfig.extendLiquids != ExtendLiquidRange.Off) {
			try (final ModProfiler ignored = NoCubes.getProfiler().start("extendLiquids")) {
				try (final StateCache stateCache = generateExtendedWaterStateCache(renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ, blockAccess, pooledMutableBlockPos, ClientUtil.getExtendLiquidsRange())) {
					try (final SmoothableCache terrainSmoothableCache = CacheUtil.generateSmoothableCache(stateCache, IIsSmoothable.TERRAIN_SMOOTHABLE)) {
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
					}
				}
			}
//		}
	}

	private static StateCache generateLightAndTexturesStateCache(
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			final int meshSizeX, final int meshSizeY, final int meshSizeZ,
			final IBlockReader blockAccess,
			final PooledMutableBlockPos pooledMutableBlockPos
	) {
		// Light uses +1 block on every axis so we need to start at -1 block
		// Textures use +1 block on every axis so we need to start at -1 block
		// All up this is -1 block
		final int cacheStartPosX = renderChunkPositionX - 1;
		final int cacheStartPosY = renderChunkPositionY - 1;
		final int cacheStartPosZ = renderChunkPositionZ - 1;

		// Light uses +1 block on every axis so we need to add 2 to the size of the cache (it takes +1 on EVERY axis)
		// Textures uses+1 block on every axis so we need to add 2 to the size of the cache (they take +1 on EVERY axis)
		// All up this is +2 blocks
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
			final IBlockReader blockAccess,
			final PooledMutableBlockPos pooledMutableBlockPos,
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

}
