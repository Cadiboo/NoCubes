package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.IIsSmoothable;
import io.github.cadiboo.nocubes.util.PooledDensityCache;
import io.github.cadiboo.nocubes.util.PooledFace;
import io.github.cadiboo.nocubes.util.PooledSmoothableCache;
import io.github.cadiboo.nocubes.util.PooledStateCache;
import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;

import static io.github.cadiboo.nocubes.client.ClientUtil.getRenderLayer;
import static io.github.cadiboo.nocubes.client.ClientUtil.getTexturePosAndState;
import static io.github.cadiboo.nocubes.util.ModUtil.LEAVES_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;
import static io.github.cadiboo.renderchunkrebuildchunkhooks.hooks.RenderChunkRebuildChunkHooksHooks.renderChunk_preRenderBlocks;
import static java.lang.Math.floor;

/**
 * @author Cadiboo
 */
public class MeshRenderer {

	private static void renderFaces(
			@Nonnull final RenderChunk renderChunk,
			@Nonnull final ChunkCompileTaskGenerator generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkPosition,
			@Nonnull final IBlockAccess cache,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final Map<int[], ArrayList<PooledFace>> chunkData,
			@Nonnull final IIsSmoothable isStateSmoothable,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final boolean[] usedBlockRenderLayers,
			final boolean renderOpposite
	) {

		final int renderChunkPositionX = renderChunkPosition.getX();
		final int renderChunkPositionY = renderChunkPosition.getY();
		final int renderChunkPositionZ = renderChunkPosition.getZ();

		chunkData.forEach((pos, faces) -> {

			if (faces.isEmpty()) return;

			pooledMutableBlockPos.setPos(
					renderChunkPositionX + pos[0],
					renderChunkPositionY + pos[1],
					renderChunkPositionZ + pos[2]
			);

			final IBlockState realState = cache.getBlockState(pooledMutableBlockPos);

			final Object[] texturePosAndState = getTexturePosAndState(cache, pooledMutableBlockPos.toImmutable(), realState, isStateSmoothable, pooledMutableBlockPos);
			final BlockPos texturePos = (BlockPos) texturePosAndState[0];
			final IBlockState textureState = (IBlockState) texturePosAndState[1];

			try {

				//TODO: use Event
				final BlockRenderLayer blockRenderLayer = getRenderLayer(textureState);

				final int blockRenderLayerOrdinal = blockRenderLayer.ordinal();

				final BufferBuilder bufferBuilder = generator.getRegionRenderCacheBuilder().getWorldRendererByLayerId(blockRenderLayerOrdinal);

				if (!compiledChunk.isLayerStarted(blockRenderLayer)) {
					compiledChunk.setLayerStarted(blockRenderLayer);
					usedBlockRenderLayers[blockRenderLayerOrdinal] = true;
					renderChunk_preRenderBlocks(renderChunk, bufferBuilder, renderChunkPosition);
				}

				BakedQuad quad = ClientUtil.getQuad(textureState, texturePos, blockRendererDispatcher);
				if (quad == null) {
					quad = blockRendererDispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(null, null, 0L).get(0);
				}
				final TextureAtlasSprite sprite = quad.getSprite();
				final int color = ClientUtil.getColor(quad, textureState, cache, texturePos);
				final int red = (color >> 16) & 255;
				final int green = (color >> 8) & 255;
				final int blue = color & 255;
				final int alpha = 0xFF;

				final float minU = ClientUtil.getMinU(sprite);
				final float minV = ClientUtil.getMinV(sprite);
				final float maxU = ClientUtil.getMaxU(sprite);
				final float maxV = ClientUtil.getMaxV(sprite);

				for (final PooledFace face : faces) {
					try {
						//south east when looking down onto up face
						final Vec3.PooledVec3 v0 = face.getVertex0().addOffset(renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ);
						//north east when looking down onto up face
						final Vec3.PooledVec3 v1 = face.getVertex1().addOffset(renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ);
						//north west when looking down onto up face
						final Vec3.PooledVec3 v2 = face.getVertex2().addOffset(renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ);
						//south west when looking down onto up face
						final Vec3.PooledVec3 v3 = face.getVertex3().addOffset(renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ);

						final int lightmapSkyLight0;
						final int lightmapSkyLight1;
						final int lightmapSkyLight2;
						final int lightmapSkyLight3;

						final int lightmapBlockLight0;
						final int lightmapBlockLight1;
						final int lightmapBlockLight2;
						final int lightmapBlockLight3;

						if (ModConfig.approximateLighting) {

							final double pos0X = v0.x + ((v0.x - pos[0] - renderChunkPositionX) * ModConfig.isosurfaceLevel);
							final double pos0Y = v0.y + ((v0.y - pos[1] - renderChunkPositionY) * ModConfig.isosurfaceLevel);
							final double pos0Z = v0.z + (v0.z - pos[2] - renderChunkPositionZ) * ModConfig.isosurfaceLevel;

							final int packedLight0 = cache.getBlockState(pooledMutableBlockPos.setPos(floor(pos0X), floor(pos0Y), floor(pos0Z))).getPackedLightmapCoords(cache, pooledMutableBlockPos);

							lightmapSkyLight0 = packedLight0 >> 16 & 0xFFFF;
							lightmapBlockLight0 = packedLight0 & 0xFFFF;

							final double pos1X = v1.x + ((v1.x - pos[0] - renderChunkPositionX) * ModConfig.isosurfaceLevel);
							final double pos1Y = v1.y + ((v1.y - pos[1] - renderChunkPositionY) * ModConfig.isosurfaceLevel);
							final double pos1Z = v1.z + (v1.z - pos[2] - renderChunkPositionZ) * ModConfig.isosurfaceLevel;

							final int packedLight1 = cache.getBlockState(pooledMutableBlockPos.setPos(floor(pos1X), floor(pos1Y), floor(pos1Z))).getPackedLightmapCoords(cache, pooledMutableBlockPos);

							lightmapSkyLight1 = packedLight1 >> 16 & 0xFFFF;
							lightmapBlockLight1 = packedLight1 & 0xFFFF;

							final double pos2X = v2.x + ((v2.x - pos[0] - renderChunkPositionX) * ModConfig.isosurfaceLevel);
							final double pos2Y = v2.y + ((v2.y - pos[1] - renderChunkPositionY) * ModConfig.isosurfaceLevel);
							final double pos2Z = v2.z + (v2.z - pos[2] - renderChunkPositionZ) * ModConfig.isosurfaceLevel;

							final int packedLight2 = cache.getBlockState(pooledMutableBlockPos.setPos(floor(pos2X), floor(pos2Y), floor(pos2Z))).getPackedLightmapCoords(cache, pooledMutableBlockPos);

							lightmapSkyLight2 = packedLight2 >> 16 & 0xFFFF;
							lightmapBlockLight2 = packedLight2 & 0xFFFF;

							final double pos3X = v3.x + ((v3.x - pos[0] - renderChunkPositionX) * ModConfig.isosurfaceLevel);
							final double pos3Y = v3.y + ((v3.y - pos[1] - renderChunkPositionY) * ModConfig.isosurfaceLevel);
							final double pos3Z = v3.z + (v3.z - pos[2] - renderChunkPositionZ) * ModConfig.isosurfaceLevel;

							final int packedLight3 = cache.getBlockState(pooledMutableBlockPos.setPos(floor(pos3X), floor(pos3Y), floor(pos3Z))).getPackedLightmapCoords(cache, pooledMutableBlockPos);

							lightmapSkyLight3 = packedLight3 >> 16 & 0xFFFF;
							lightmapBlockLight3 = packedLight3 & 0xFFFF;

//							//v0 =
//							//TODO: change to ModUtil#map
//							//<editor-fold desc="Snap to integer coords and light index">
//							final int v0X = clamp((round(v0.x) - pos[0] - renderChunkPositionX), 0, 1);
//							final int v0Y = clamp((round(v0.y) - pos[1] - renderChunkPositionY), 0, 1);
//							final int v0Z = clamp((round(v0.z) - pos[2] - renderChunkPositionZ), 0, 1);
//							final int v1X = clamp((round(v1.x) - pos[0] - renderChunkPositionX), 0, 1);
//							final int v1Y = clamp((round(v1.y) - pos[1] - renderChunkPositionY), 0, 1);
//							final int v1Z = clamp((round(v1.z) - pos[2] - renderChunkPositionZ), 0, 1);
//							final int v2X = clamp((round(v2.x) - pos[0] - renderChunkPositionX), 0, 1);
//							final int v2Y = clamp((round(v2.y) - pos[1] - renderChunkPositionY), 0, 1);
//							final int v2Z = clamp((round(v2.z) - pos[2] - renderChunkPositionZ), 0, 1);
//							final int v3X = clamp((round(v3.x) - pos[0] - renderChunkPositionX), 0, 1);
//							final int v3Y = clamp((round(v3.y) - pos[1] - renderChunkPositionY), 0, 1);
//							final int v3Z = clamp((round(v3.z) - pos[2] - renderChunkPositionZ), 0, 1);
//							//</editor-fold>
//
//							//<editor-fold desc="get and unpack lightmap coords">
//							lightmapSkyLight0 = packedLight[v0X][v0Y][v0Z] >> 16 & 0xFFFF;
//							lightmapBlockLight0 = packedLight[v0X][v0Y][v0Z] & 0xFFFF;
//							lightmapSkyLight1 = packedLight[v1X][v1Y][v1Z] >> 16 & 0xFFFF;
//							lightmapBlockLight1 = packedLight[v1X][v1Y][v1Z] & 0xFFFF;
//							lightmapSkyLight2 = packedLight[v2X][v2Y][v2Z] >> 16 & 0xFFFF;
//							lightmapBlockLight2 = packedLight[v2X][v2Y][v2Z] & 0xFFFF;
//							lightmapSkyLight3 = packedLight[v3X][v3Y][v3Z] >> 16 & 0xFFFF;
//							lightmapBlockLight3 = packedLight[v3X][v3Y][v3Z] & 0xFFFF;
//							//</editor-fold>

						} else {
							lightmapSkyLight0 = lightmapSkyLight1 = lightmapSkyLight2 = lightmapSkyLight3 = 240;
							lightmapBlockLight0 = lightmapBlockLight1 = lightmapBlockLight2 = lightmapBlockLight3 = 0;
						}

						try {
							bufferBuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight0, lightmapBlockLight0).endVertex();
							bufferBuilder.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight1, lightmapBlockLight1).endVertex();
							bufferBuilder.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight2, lightmapBlockLight2).endVertex();
							bufferBuilder.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight3, lightmapBlockLight3).endVertex();
							if (renderOpposite) {
								bufferBuilder.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight3, lightmapBlockLight3).endVertex();
								bufferBuilder.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight2, lightmapBlockLight2).endVertex();
								bufferBuilder.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight1, lightmapBlockLight1).endVertex();
								bufferBuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight0, lightmapBlockLight0).endVertex();
							}
						} finally {
							v0.release();
							v1.release();
							v2.release();
							v3.release();
						}
					} finally {
						face.release();
					}

				}
			} catch (Exception e) {
				final CrashReport crashReport = new CrashReport("Rendering smooth block in world", e);

				CrashReportCategory realBlockCrashReportCategory = crashReport.makeCategory("Block being rendered");
				final BlockPos blockPos = new BlockPos(renderChunkPositionX + pos[0], renderChunkPositionX + pos[0], renderChunkPositionX + pos[0]);
				CrashReportCategory.addBlockInfo(realBlockCrashReportCategory, blockPos, realState.getBlock(), realState.getBlock().getMetaFromState(realState));

				CrashReportCategory textureBlockCrashReportCategory = crashReport.makeCategory("TextureBlock of Block being rendered");
				CrashReportCategory.addBlockInfo(textureBlockCrashReportCategory, texturePos, textureState.getBlock(), textureState.getBlock().getMetaFromState(textureState));

				throw new ReportedException(crashReport);
			}

		});

	}

	public static void renderChunk(
			@Nonnull final RenderChunk renderChunk,
			@Nonnull final ChunkCompileTaskGenerator generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			@Nonnull final IBlockAccess blockAccess,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			final int meshSizeX, final int meshSizeY, final int meshSizeZ,
			final PooledStateCache stateCache,
			final int cachesSizeX, final int cachesSizeY, final int cachesSizeZ
	) {
		//normal terrain
		{
			try (PooledSmoothableCache smoothableCache = CacheUtil.generateSmoothableCache(cachesSizeX, cachesSizeY, cachesSizeZ, stateCache, TERRAIN_SMOOTHABLE)) {
				try (
						final PooledDensityCache data = CacheUtil.generateDensityCache(
								renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
								meshSizeX, meshSizeY, meshSizeZ,
								stateCache, smoothableCache,
								cachesSizeX, cachesSizeY, cachesSizeZ,
								blockAccess,
								pooledMutableBlockPos
						)
				) {
					renderFaces(
							renderChunk,
							generator,
							compiledChunk,
							renderChunkPosition,
							blockAccess,
							blockRendererDispatcher,
							ModConfig.getMeshGenerator().generateChunk(data.getDensityCache(), new int[]{meshSizeX, meshSizeY, meshSizeZ}),
							TERRAIN_SMOOTHABLE,
							pooledMutableBlockPos, usedBlockRenderLayers, false
					);
				}
			}
		}
		if (ModConfig.smoothLeavesSeparate) {
			try (PooledSmoothableCache smoothableCache = CacheUtil.generateSmoothableCache(cachesSizeX, cachesSizeY, cachesSizeZ, stateCache, LEAVES_SMOOTHABLE)) {
				try (
						final PooledDensityCache data = CacheUtil.generateDensityCache(
								renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
								meshSizeX, meshSizeY, meshSizeZ,
								stateCache, smoothableCache,
								cachesSizeX, cachesSizeY, cachesSizeZ,
								blockAccess,
								pooledMutableBlockPos
						)
				) {
					renderFaces(
							renderChunk,
							generator,
							compiledChunk,
							renderChunkPosition,
							blockAccess,
							blockRendererDispatcher,
							ModConfig.getMeshGenerator().generateChunk(data.getDensityCache(), new int[]{meshSizeX, meshSizeY, meshSizeZ}),
							LEAVES_SMOOTHABLE,
							pooledMutableBlockPos, usedBlockRenderLayers, true
					);
				}
			}
		}
	}

//	public static void renderChunk(final RebuildChunkPreEvent event, final PooledStateCache pooledStateCache, final boolean[] usedBlockRenderLayers) {
//		final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
//		try{
//			renderChunk(event.getRenderChunkPosition(),
//					event.getRenderChunk(),
//					event.getCompiledChunk(),
//					event.getGenerator(),
//					event.getChunkCache(),
//					CacheUtil.generateSmoothableCache()
//					event.getIBlockAccess(),
//					pooledMutableBlockPos);
//		} finally {
//			pooledMutableBlockPos.release();
//		}

//		public static void renderChunk(final BlockPos renderChunkPosition,
// final RenderChunk renderChunk,
// final CompiledChunk compiledChunk,
// final ChunkCompileTaskGenerator generator,
// final PooledStateCache stateCache,
// final IBlockAccess blockAccess,
// final PooledMutableBlockPos pooledMutableBlockPos,
// final boolean[] usedBlockRenderLayers
// ) {

//	}

}
