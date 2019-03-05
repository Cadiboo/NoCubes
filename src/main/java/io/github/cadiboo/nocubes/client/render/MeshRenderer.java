package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.LightmapInfo;
import io.github.cadiboo.nocubes.client.OptifineCompatibility;
import io.github.cadiboo.nocubes.client.PackedLightCache;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.IIsSmoothable;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.StateCache;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.nocubes.util.Vec3b;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.Map;

import static io.github.cadiboo.nocubes.client.ClientUtil.getRenderLayer;
import static io.github.cadiboo.nocubes.util.ModUtil.LEAVES_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;

/**
 * @author Cadiboo
 */
public class MeshRenderer {

	private static void renderFaces(
			@Nonnull final RenderChunk renderChunk,
			@Nonnull final ChunkCompileTaskGenerator generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			@Nonnull final IBlockAccess blockAccess,
			@Nonnull final StateCache stateCache,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final PackedLightCache pooledPackedLightCache,
			@Nonnull final Map<Vec3b, FaceList> chunkData,
			@Nonnull final IIsSmoothable isStateSmoothable,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final boolean[] usedBlockRenderLayers,
			final boolean renderOppositeSides
	) {

		try (final ModProfiler ignored = NoCubes.getProfiler().start("renderFaces")) {

//			final Random random = new Random();

			for (Map.Entry<Vec3b, FaceList> entry : chunkData.entrySet()) {
				try (final Vec3b pos = entry.getKey()) {
					try (final FaceList faces = entry.getValue()) {

						if (faces.isEmpty()) {
							if (ModConfig.renderEmptyBlocksOrWhatever) {
								renderEmptyBlocksBadly(renderChunk, generator, compiledChunk, renderChunkPosition, renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ, blockAccess, blockRendererDispatcher, pooledMutableBlockPos, pos);
							}
							continue;
						}

						pooledMutableBlockPos.setPos(
								renderChunkPositionX + pos.x,
								renderChunkPositionY + pos.y,
								renderChunkPositionZ + pos.z
						);

						final IBlockState realState = blockAccess.getBlockState(pooledMutableBlockPos);
//						final IBlockState realState = stateCache.getStateCache()[stateCache.getIndex(pos.x + 1, pos.y + 1, pos.z + 1)];

						final Tuple<BlockPos, IBlockState> texturePosAndState = ClientUtil.getTexturePosAndState(stateCache, blockAccess, pooledMutableBlockPos, realState, isStateSmoothable);
						BlockPos texturePos = texturePosAndState.getFirst();
						final IBlockState textureState = texturePosAndState.getSecond();

						try {

							BakedQuad quad = ClientUtil.getQuad(textureState.getActualState(blockAccess, texturePos), texturePos, blockRendererDispatcher);
							if (quad == null) {
								quad = blockRendererDispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(null, EnumFacing.DOWN, 0L).get(0);
							}

							// Quads are packed xyz|argb|u|v|ts

							final int formatSize = quad.getFormat().getIntegerSize();

							final float v0u = Float.intBitsToFloat(quad.getVertexData()[4]);
							final float v0v = Float.intBitsToFloat(quad.getVertexData()[5]);
							final float v1u = Float.intBitsToFloat(quad.getVertexData()[formatSize + 4]);
							final float v1v = Float.intBitsToFloat(quad.getVertexData()[formatSize + 5]);
							final float v2u = Float.intBitsToFloat(quad.getVertexData()[formatSize * 2 + 4]);
							final float v2v = Float.intBitsToFloat(quad.getVertexData()[formatSize * 2 + 5]);
							final float v3u = Float.intBitsToFloat(quad.getVertexData()[formatSize * 3 + 4]);
							final float v3v = Float.intBitsToFloat(quad.getVertexData()[formatSize * 3 + 5]);

//							final TextureAtlasSprite sprite = quad.getSprite();
							final int color0 = ClientUtil.getColor(quad, textureState, blockAccess, texturePos.south().east());
							final int alpha = 0xFF;

							final int red0 = (color0 >> 16) & 255;
							final int green0 = (color0 >> 8) & 255;
							final int blue0 = color0 & 255;
							final int red1;
							final int green1;
							final int blue1;
							final int red2;
							final int green2;
							final int blue2;
							final int red3;
							final int green3;
							final int blue3;
							if (ModConfig.smoothBiomeColors) {
								final int color1 = ClientUtil.getColor(quad, textureState, blockAccess, texturePos.east());
								red1 = color1 >> 16 & 255;
								green1 = color1 >> 8 & 255;
								blue1 = (color1 & 255);
								final int color2 = ClientUtil.getColor(quad, textureState, blockAccess, texturePos);
								red2 = (color2 >> 16 & 255);
								green2 = (color2 >> 8 & 255);
								blue2 = (color2 & 255);
								final int color3 = ClientUtil.getColor(quad, textureState, blockAccess, texturePos.south());
								red3 = (color3 >> 16 & 255);
								green3 = (color3 >> 8 & 255);
								blue3 = (color3 & 255);
							} else {
								red1 = red0;
								green1 = green0;
								blue1 = blue0;
								red2 = red0;
								green2 = green0;
								blue2 = blue0;
								red3 = red0;
								green3 = green0;
								blue3 = blue0;
							}

//							final float minU = ClientUtil.getMinU(sprite);
//							final float minV = ClientUtil.getMinV(sprite);
//							final float maxU = ClientUtil.getMaxU(sprite);
//							final float maxV = ClientUtil.getMaxV(sprite);

							//TODO: use Event
							final BlockRenderLayer blockRenderLayer = getRenderLayer(textureState);
							final int blockRenderLayerOrdinal = blockRenderLayer.ordinal();
							final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(generator, blockRenderLayerOrdinal, compiledChunk, blockRenderLayer, renderChunk, renderChunkPosition);
							usedBlockRenderLayers[blockRenderLayerOrdinal] = true;

							OptifineCompatibility.pushShaderThing(textureState, texturePos, blockAccess, bufferBuilder);

							for (final Face face : faces) {
								try {
									//0 3
									//1 2
									//south east when looking down onto up face
									final Vec3 v0 = face.getVertex0();
									//north east when looking down onto up face
									final Vec3 v1 = face.getVertex1();
									//north west when looking down onto up face
									final Vec3 v2 = face.getVertex2();
									//south west when looking down onto up face
									final Vec3 v3 = face.getVertex3();

									final int lightmapSkyLight0;
									final int lightmapSkyLight1;
									final int lightmapSkyLight2;
									final int lightmapSkyLight3;

									final int lightmapBlockLight0;
									final int lightmapBlockLight1;
									final int lightmapBlockLight2;
									final int lightmapBlockLight3;

									if (ModConfig.approximateLighting) {

										//TODO: do this better
										try (final LightmapInfo lightmapInfo = LightmapInfo.generateLightmapInfo(pooledPackedLightCache, v0, v1, v2, v3, renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ, pos, blockAccess, pooledMutableBlockPos)) {
											lightmapSkyLight0 = lightmapInfo.skylight0;
											lightmapSkyLight1 = lightmapInfo.skylight1;
											lightmapSkyLight2 = lightmapInfo.skylight2;
											lightmapSkyLight3 = lightmapInfo.skylight3;
											lightmapBlockLight0 = lightmapInfo.blocklight0;
											lightmapBlockLight1 = lightmapInfo.blocklight1;
											lightmapBlockLight2 = lightmapInfo.blocklight2;
											lightmapBlockLight3 = lightmapInfo.blocklight3;
										}

									} else {
										lightmapSkyLight0 = lightmapSkyLight1 = lightmapSkyLight2 = lightmapSkyLight3 = 240;
										lightmapBlockLight0 = lightmapBlockLight1 = lightmapBlockLight2 = lightmapBlockLight3 = 0;
									}

									try {
										bufferBuilder.pos(v0.x, v0.y, v0.z).color(red0, green0, blue0, alpha).tex(v0u, v0v).lightmap(lightmapSkyLight0, lightmapBlockLight0).endVertex();
										bufferBuilder.pos(v1.x, v1.y, v1.z).color(red1, green1, blue1, alpha).tex(v1u, v1v).lightmap(lightmapSkyLight1, lightmapBlockLight1).endVertex();
										bufferBuilder.pos(v2.x, v2.y, v2.z).color(red2, green2, blue2, alpha).tex(v2u, v2v).lightmap(lightmapSkyLight2, lightmapBlockLight2).endVertex();
										bufferBuilder.pos(v3.x, v3.y, v3.z).color(red3, green3, blue3, alpha).tex(v3u, v3v).lightmap(lightmapSkyLight3, lightmapBlockLight3).endVertex();
										if (renderOppositeSides) {
											bufferBuilder.pos(v3.x, v3.y, v3.z).color(red3, green3, blue3, alpha).tex(v0u, v0v).lightmap(lightmapSkyLight3, lightmapBlockLight3).endVertex();
											bufferBuilder.pos(v2.x, v2.y, v2.z).color(red2, green2, blue2, alpha).tex(v1u, v1v).lightmap(lightmapSkyLight2, lightmapBlockLight2).endVertex();
											bufferBuilder.pos(v1.x, v1.y, v1.z).color(red1, green1, blue1, alpha).tex(v2u, v2v).lightmap(lightmapSkyLight1, lightmapBlockLight1).endVertex();
											bufferBuilder.pos(v0.x, v0.y, v0.z).color(red0, green0, blue0, alpha).tex(v3u, v3v).lightmap(lightmapSkyLight0, lightmapBlockLight0).endVertex();
										}
									} finally {
										v0.close();
										v1.close();
										v2.close();
										v3.close();
									}
								} finally {
									face.close();
								}

							}

							OptifineCompatibility.popShaderThing(bufferBuilder);

						} catch (Exception e) {
							final CrashReport crashReport = new CrashReport("Rendering smooth block in world", e);

							CrashReportCategory realBlockCrashReportCategory = crashReport.makeCategory("Block being rendered");
							final BlockPos blockPos = new BlockPos(renderChunkPositionX + pos.x, renderChunkPositionX + pos.y, renderChunkPositionX + pos.z);
							CrashReportCategory.addBlockInfo(realBlockCrashReportCategory, blockPos, realState);

							CrashReportCategory textureBlockCrashReportCategory = crashReport.makeCategory("TextureBlock of Block being rendered");
							CrashReportCategory.addBlockInfo(textureBlockCrashReportCategory, texturePos, textureState);

							throw new ReportedException(crashReport);
						}
					}
				}

			}

		}

	}

	private static void renderEmptyBlocksBadly(@Nonnull final RenderChunk renderChunk, @Nonnull final ChunkCompileTaskGenerator generator, @Nonnull final CompiledChunk compiledChunk, @Nonnull final BlockPos renderChunkPosition, final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ, @Nonnull final IBlockAccess blockAccess, @Nonnull final BlockRendererDispatcher blockRendererDispatcher, @Nonnull final PooledMutableBlockPos pooledMutableBlockPos, final Vec3b pos) {
		pooledMutableBlockPos.setPos(
				renderChunkPositionX + pos.x,
				renderChunkPositionY + pos.y,
				renderChunkPositionZ + pos.z
		);
		final IBlockState blockState = blockAccess.getBlockState(pooledMutableBlockPos);
		final BlockRenderLayer blockRenderLayer = getRenderLayer(blockState);
		final int blockRenderLayerOrdinal = blockRenderLayer.ordinal();
		final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(generator, blockRenderLayerOrdinal, compiledChunk, blockRenderLayer, renderChunk, renderChunkPosition);

		OptifineCompatibility.pushShaderThing(blockState, pooledMutableBlockPos, blockAccess, bufferBuilder);

		blockRendererDispatcher.renderBlock(blockState, pooledMutableBlockPos, blockAccess, bufferBuilder);

		OptifineCompatibility.popShaderThing(bufferBuilder);
	}

	public static void renderChunk(
			@Nonnull final RenderChunk renderChunk,
			@Nonnull final ChunkCompileTaskGenerator generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			@Nonnull final IBlockAccess blockAccess,
			@Nonnull final StateCache stateCache,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final PackedLightCache pooledPackedLightCache
	) {
		try (ModProfiler ignored = NoCubes.getProfiler().start("renderMeshChunk")) {
			//normal terrain
			{
				try (ModProfiler ignored1 = NoCubes.getProfiler().start("renderNormalTerrain")) {
					renderFaces(
							renderChunk,
							generator,
							compiledChunk,
							renderChunkPosition,
							renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
							blockAccess,
							stateCache,
							blockRendererDispatcher,
							pooledPackedLightCache,
							NoCubes.MESH_DISPATCHER.generateOffsetChunk(renderChunkPosition, blockAccess, TERRAIN_SMOOTHABLE),
							TERRAIN_SMOOTHABLE,
							pooledMutableBlockPos, usedBlockRenderLayers, false
					);
				}
			}

			if (ModConfig.smoothLeavesSeparate)
				try (ModProfiler ignored2 = NoCubes.getProfiler().start("renderLeaves")) {
					renderFaces(
							renderChunk,
							generator,
							compiledChunk,
							renderChunkPosition,
							renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
							blockAccess,
							stateCache,
							blockRendererDispatcher,
							pooledPackedLightCache,
							NoCubes.MESH_DISPATCHER.generateOffsetChunk(renderChunkPosition, blockAccess, LEAVES_SMOOTHABLE),
							LEAVES_SMOOTHABLE,
							pooledMutableBlockPos, usedBlockRenderLayers, true
					);
				}
		}
	}

}
