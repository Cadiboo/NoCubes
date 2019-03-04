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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
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
			final boolean renderOpposite
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

//						final IBlockState realState = blockAccess.getBlockState(pooledMutableBlockPos);
						final IBlockState realState = stateCache.getStateCache()[stateCache.getIndex(pos.x, pos.y, pos.z)];

						final Object[] texturePosAndState = ClientUtil.getTexturePosAndState(stateCache, blockAccess, pooledMutableBlockPos.toImmutable(), realState, isStateSmoothable, pooledMutableBlockPos);
						final BlockPos texturePos = (BlockPos) texturePosAndState[0];
						final IBlockState textureState = (IBlockState) texturePosAndState[1];

						try {

							//TODO: use Event
							final BlockRenderLayer blockRenderLayer = getRenderLayer(textureState);
							final int blockRenderLayerOrdinal = blockRenderLayer.ordinal();
							final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(generator, blockRenderLayerOrdinal, compiledChunk, blockRenderLayer, renderChunk, renderChunkPosition);
							usedBlockRenderLayers[blockRenderLayerOrdinal] = true;

							OptifineCompatibility.pushShaderThing(textureState, texturePos, blockAccess, bufferBuilder);

							BakedQuad quad = ClientUtil.getQuad(textureState, texturePos, blockRendererDispatcher);
							if (quad == null) {
								quad = blockRendererDispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(null, EnumFacing.DOWN, 0L).get(0);
							}
							final TextureAtlasSprite sprite = quad.getSprite();
							final int color = ClientUtil.getColor(quad, textureState, blockAccess, texturePos);
							final int red = (color >> 16) & 255;
							final int green = (color >> 8) & 255;
							final int blue = color & 255;
							final int alpha = 0xFF;

							final float minU = ClientUtil.getMinU(sprite);
							final float minV = ClientUtil.getMinV(sprite);
							final float maxU = ClientUtil.getMaxU(sprite);
							final float maxV = ClientUtil.getMaxV(sprite);

							for (final Face face : faces) {
								try {
									//0 3
									//1 2
									//south east when looking down onto up face
									final Vec3 v0 = face.getVertex0().addOffset(renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ);
									//north east when looking down onto up face
									final Vec3 v1 = face.getVertex1().addOffset(renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ);
									//north west when looking down onto up face
									final Vec3 v2 = face.getVertex2().addOffset(renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ);
									//south west when looking down onto up face
									final Vec3 v3 = face.getVertex3().addOffset(renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ);

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
							NoCubes.MESH_DISPATCHER.generateChunk(renderChunkPosition, blockAccess, TERRAIN_SMOOTHABLE),
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
							NoCubes.MESH_DISPATCHER.generateChunk(renderChunkPosition, blockAccess, LEAVES_SMOOTHABLE),
							LEAVES_SMOOTHABLE,
							pooledMutableBlockPos, usedBlockRenderLayers, true
					);
				}
		}
	}

}
