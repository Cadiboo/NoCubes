package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.BlockColorInfo;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.LazyBlockColorCache;
import io.github.cadiboo.nocubes.client.LazyPackedLightCache;
import io.github.cadiboo.nocubes.client.LightmapInfo;
import io.github.cadiboo.nocubes.client.ModelHelper;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.StateHolder;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import static io.github.cadiboo.nocubes.client.ModelHelper.ENUMFACING_QUADS_ORDERED;

/**
 * @author Cadiboo
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public final class MeshRenderer {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void renderMesh(
			@Nonnull final RenderChunk renderChunk,
			@Nonnull final ChunkCompileTaskGenerator generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			@Nonnull final IBlockAccess blockAccess,
			@Nonnull final StateCache stateCache,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final Random random,
			@Nonnull final LazyPackedLightCache pooledPackedLightCache,
			@Nonnull final LazyBlockColorCache blockColorsCache,
			@Nonnull final Map<Vec3b, FaceList> chunkData,
			@Nonnull final SmoothableCache smoothableCache,
			final int cacheAddX, final int cacheAddY, final int cacheAddZ,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final PooledMutableBlockPos texturePooledMutableBlockPos,
			@Nonnull final boolean[] usedBlockRenderLayers,
			final boolean renderOppositeSides,
			final boolean tryForBetterTexturesSnow, final boolean tryForBetterTexturesGrass
	) {

		try (final ModProfiler ignored = ModProfiler.get().start("renderMesh")) {

			for (Entry<Vec3b, FaceList> entry : chunkData.entrySet()) {
				try (final Vec3b pos = entry.getKey()) {
					try (final FaceList faces = entry.getValue()) {

						if (faces.isEmpty()) {
							continue;
						}

						ModProfiler.get().end(); // HACKY
						ModProfiler.get().start("prepareRenderFaces"); // HACKY

						final int initialPosX = renderChunkPositionX + pos.x;
						final int initialPosY = renderChunkPositionY + pos.y;
						final int initialPosZ = renderChunkPositionZ + pos.z;

						//TODO use pos? (I've forgotten what this todo is even about)
						final byte relativePosX = ClientUtil.getRelativePos(renderChunkPositionX, initialPosX);
						final byte relativePosY = ClientUtil.getRelativePos(renderChunkPositionY, initialPosY);
						final byte relativePosZ = ClientUtil.getRelativePos(renderChunkPositionZ, initialPosZ);

						ModProfiler.get().end(); // HACKY (end here because getTexturePosAndState profiles itself)

						final IBlockState textureState = ClientUtil.getTexturePosAndState(
								initialPosX, initialPosY, initialPosZ,
								texturePooledMutableBlockPos,
								stateCache, smoothableCache,
								cacheAddX, cacheAddY, cacheAddZ,
								relativePosX, relativePosY, relativePosZ,
								tryForBetterTexturesSnow, tryForBetterTexturesGrass
						);

						ModProfiler.get().start("renderMesh"); // HACKY

						try {
							renderFaces(
									renderChunk, generator, compiledChunk, renderChunkPosition,
									renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
									blockAccess, blockRendererDispatcher, random,
									usedBlockRenderLayers,
									pooledPackedLightCache, blockColorsCache,
									pos, faces,
									pooledMutableBlockPos,
									texturePooledMutableBlockPos, textureState,
									renderOppositeSides
							);
						} catch (Exception e) {
							final CrashReport crashReport = new CrashReport("Rendering faces for smooth block in world", e);

							CrashReportCategory realBlockCrashReportCategory = crashReport.makeCategory("Block being rendered");
							final BlockPos blockPos = new BlockPos(renderChunkPositionX + pos.x, renderChunkPositionX + pos.y, renderChunkPositionX + pos.z);
							CrashReportCategory.addBlockInfo(realBlockCrashReportCategory, blockPos, blockAccess.getBlockState(new BlockPos(initialPosX, initialPosY, initialPosZ)));

							CrashReportCategory textureBlockCrashReportCategory = crashReport.makeCategory("TextureBlock of Block being rendered");
							CrashReportCategory.addBlockInfo(textureBlockCrashReportCategory, texturePooledMutableBlockPos.toImmutable(), textureState);

							throw new ReportedException(crashReport);
						}
					}
				}

			}

		}

	}

	public static void renderFaces(
			@Nonnull final RenderChunk renderChunk,
			@Nonnull final ChunkCompileTaskGenerator generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			@Nonnull final IBlockAccess blockAccess,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final Random random,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final LazyPackedLightCache pooledPackedLightCache,
			@Nonnull final LazyBlockColorCache blockColorsCache,
			@Nonnull final Vec3b pos,
			@Nonnull final FaceList faces,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final BlockPos texturePos,
			@Nonnull final IBlockState textureState,
			final boolean renderOppositeSides
	) {
//		final IModelData modelData = generator.getModelData(texturePos);
		final long posRand = MathHelper.getPositionRandom(texturePos);

		final ModProfiler profiler = ModProfiler.get();
//		try (ModProfiler ignored = ModProfiler.get().start("renderFaces"))
		{
			for (int faceIndex = 0, facesSize = faces.size(); faceIndex < facesSize; ++faceIndex) {
				try (Face face = faces.get(faceIndex)) {
					//0 3
					//1 2
					try (
							//south east when looking down onto up face
							final Vec3 v0 = face.getVertex0();
							//north east when looking down onto up face
							final Vec3 v1 = face.getVertex1();
							//north west when looking down onto up face
							final Vec3 v2 = face.getVertex2();
							//south west when looking down onto up face
							final Vec3 v3 = face.getVertex3()
					) {

						float diffuse0;
						float diffuse1;
						float diffuse2;
						float diffuse3;
						profiler.end(); // HACKY
						profiler.start("calculateDiffuseLighting");
						{
							if (!Config.applyDiffuseLighting) {
								diffuse0 = diffuse1 = diffuse2 = diffuse3 = 1;
							} else {
								diffuse0 = diffuseLight(toSide(
										v0.x - renderChunkPositionX - pos.x,
										v0.y - renderChunkPositionY - pos.y,
										v0.z - renderChunkPositionZ - pos.z
								));
								diffuse1 = diffuseLight(toSide(
										v1.x - renderChunkPositionX - pos.x,
										v1.y - renderChunkPositionY - pos.y,
										v1.z - renderChunkPositionZ - pos.z
								));
								diffuse2 = diffuseLight(toSide(
										v2.x - renderChunkPositionX - pos.x,
										v2.y - renderChunkPositionY - pos.y,
										v2.z - renderChunkPositionZ - pos.z
								));
								diffuse3 = diffuseLight(toSide(
										v3.x - renderChunkPositionX - pos.x,
										v3.y - renderChunkPositionY - pos.y,
										v3.z - renderChunkPositionZ - pos.z
								));
							}
						}
						profiler.end();
						profiler.start("renderMesh"); // HACKY

						final int lightmapSkyLight0;
						final int lightmapSkyLight1;
						final int lightmapSkyLight2;
						final int lightmapSkyLight3;

						final int lightmapBlockLight0;
						final int lightmapBlockLight1;
						final int lightmapBlockLight2;
						final int lightmapBlockLight3;

						profiler.end(); // HACKY
						try (final LightmapInfo lightmapInfo = LightmapInfo.generateLightmapInfo(pooledPackedLightCache, v0, v1, v2, v3, renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ)) {

							lightmapSkyLight0 = lightmapInfo.skylight0;
							lightmapSkyLight1 = lightmapInfo.skylight1;
							lightmapSkyLight2 = lightmapInfo.skylight2;
							lightmapSkyLight3 = lightmapInfo.skylight3;

							lightmapBlockLight0 = lightmapInfo.blocklight0;
							lightmapBlockLight1 = lightmapInfo.blocklight1;
							lightmapBlockLight2 = lightmapInfo.blocklight2;
							lightmapBlockLight3 = lightmapInfo.blocklight3;

						}
						profiler.start("renderMesh"); // HACKY

						boolean hasSetColors = false;
						float colorRed0 = -1;
						float colorGreen0 = -1;
						float colorBlue0 = -1;
						float colorRed1 = -1;
						float colorGreen1 = -1;
						float colorBlue1 = -1;
						float colorRed2 = -1;
						float colorGreen2 = -1;
						float colorBlue2 = -1;
						float colorRed3 = -1;
						float colorGreen3 = -1;
						float colorBlue3 = -1;

						if (Config.shortGrass) {
							profiler.end(); // HACKY
							profiler.start("shortGrass");
							if (textureState == StateHolder.GRASS_BLOCK_DEFAULT && areVerticesCloseToFlat(v0, v1, v2, v3)) {
								renderShortGrass(
										renderChunk, generator, compiledChunk, renderChunkPosition,
										blockAccess,
										blockRendererDispatcher,
										random,
										usedBlockRenderLayers,
										pooledMutableBlockPos,
										texturePos,
										v0, v1, v2, v3,
										lightmapSkyLight0, lightmapSkyLight1, lightmapSkyLight2, lightmapSkyLight3,
										lightmapBlockLight0, lightmapBlockLight1, lightmapBlockLight2, lightmapBlockLight3
								);
							}
							profiler.end();
							profiler.start("renderMesh"); // HACKY
						}

						final BlockRenderLayer[] values = BlockRenderLayer.values();
						for (int i = 0, valuesLength = values.length; i < valuesLength; ++i) {
							final BlockRenderLayer initialBlockRenderLayer = values[i];
							if (!textureState.getBlock().canRenderInLayer(textureState, initialBlockRenderLayer)) {
								continue;
							}
							final BlockRenderLayer correctedBlockRenderLayer = ClientUtil.getCorrectRenderLayer(initialBlockRenderLayer);
							final int correctedBlockRenderLayerOrdinal = correctedBlockRenderLayer.ordinal();
							ForgeHooksClient.setRenderLayer(correctedBlockRenderLayer);

							final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(generator, correctedBlockRenderLayerOrdinal, compiledChunk, correctedBlockRenderLayer, renderChunk, renderChunkPosition);

							boolean wasAnythingRendered = false;

							OptiFineCompatibility.pushShaderThing(textureState, texturePos, blockAccess, bufferBuilder);
							try {

								List<BakedQuad> quads;
								try (ModProfiler ignored1 = profiler.start("getQuads")) {
//									random.setSeed(posRand);
									quads = ModelHelper.getQuads(textureState, texturePos, bufferBuilder, blockAccess, blockRendererDispatcher, /*modelData,*/ posRand, correctedBlockRenderLayer);
									if (quads == null) {
										LOGGER.warn("Got null quads for " + textureState.getBlock() + " at " + texturePos);
										quads = new ArrayList<>();
										quads.add(blockRendererDispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(null, EnumFacing.DOWN, posRand).get(0));
									}
								}

								for (int quadIndex = 0, quadsSize = quads.size(); quadIndex < quadsSize; ++quadIndex) {
									final BakedQuad quad = quads.get(quadIndex);

									wasAnythingRendered = true;

									final int formatSize = quad.getFormat().getIntegerSize();
									final int[] vertexData = quad.getVertexData();

									final float v0u;
									final float v0v;
									final float v1u;
									final float v1v;
									final float v2u;
									final float v2v;
									final float v3u;
									final float v3v;

									try (ModProfiler ignored1 = profiler.start("getUVs")) {
										// Quads are packed xyz|argb|u|v|ts
										v0u = Float.intBitsToFloat(vertexData[4]);
										v0v = Float.intBitsToFloat(vertexData[5]);
										v1u = Float.intBitsToFloat(vertexData[formatSize + 4]);
										v1v = Float.intBitsToFloat(vertexData[formatSize + 5]);
										v2u = Float.intBitsToFloat(vertexData[formatSize * 2 + 4]);
										v2v = Float.intBitsToFloat(vertexData[formatSize * 2 + 5]);
										v3u = Float.intBitsToFloat(vertexData[formatSize * 3 + 4]);
										v3v = Float.intBitsToFloat(vertexData[formatSize * 3 + 5]);
									}

									final int quadPackedLight0 = vertexData[6];
									final int quadPackedLight1 = vertexData[formatSize + 6];
									final int quadPackedLight2 = vertexData[formatSize * 2 + 6];
									final int quadPackedLight3 = vertexData[formatSize * 3 + 6];

									final int quadSkyLight0 = (quadPackedLight0 >> 16) & 0xFF;
									final int quadSkyLight1 = (quadPackedLight1 >> 16) & 0xFF;
									final int quadSkyLight2 = (quadPackedLight2 >> 16) & 0xFF;
									final int quadSkyLight3 = (quadPackedLight3 >> 16) & 0xFF;
									final int quadBlockLight0 = quadPackedLight0 & 0xFF;
									final int quadBlockLight1 = quadPackedLight1 & 0xFF;
									final int quadBlockLight2 = quadPackedLight2 & 0xFF;
									final int quadBlockLight3 = quadPackedLight3 & 0xFF;

									final float red0;
									final float green0;
									final float blue0;
									final float red1;
									final float green1;
									final float blue1;
									final float red2;
									final float green2;
									final float blue2;
									final float red3;
									final float green3;
									final float blue3;

									if (quad.hasTintIndex() || BlockColorInfo.RAINBOW || BlockColorInfo.BLACK) {
										if (!hasSetColors) {
											profiler.end(); // HACKY
											try (
													final ModProfiler ignored = ModProfiler.get().start("generateBlockColorInfo");
													final BlockColorInfo blockColorInfo = BlockColorInfo.generateBlockColorInfo(blockColorsCache, v0, v1, v2, v3, renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ)
											) {
												colorRed0 = blockColorInfo.red0;
												colorGreen0 = blockColorInfo.green0;
												colorBlue0 = blockColorInfo.blue0;
												colorRed1 = blockColorInfo.red1;
												colorGreen1 = blockColorInfo.green1;
												colorBlue1 = blockColorInfo.blue1;
												colorRed2 = blockColorInfo.red2;
												colorGreen2 = blockColorInfo.green2;
												colorBlue2 = blockColorInfo.blue2;
												colorRed3 = blockColorInfo.red3;
												colorGreen3 = blockColorInfo.green3;
												colorBlue3 = blockColorInfo.blue3;
											}
											hasSetColors = true;
											profiler.start("renderMesh"); // HACKY
										}
										red0 = colorRed0;
										green0 = colorGreen0;
										blue0 = colorBlue0;
										red1 = colorRed1;
										green1 = colorGreen1;
										blue1 = colorBlue1;
										red2 = colorRed2;
										green2 = colorGreen2;
										blue2 = colorBlue2;
										red3 = colorRed3;
										green3 = colorGreen3;
										blue3 = colorBlue3;
									} else {
										red0 = 1F;
										green0 = 1F;
										blue0 = 1F;
										red1 = 1F;
										green1 = 1F;
										blue1 = 1F;
										red2 = 1F;
										green2 = 1F;
										blue2 = 1F;
										red3 = 1F;
										green3 = 1F;
										blue3 = 1F;
									}

									try (final ModProfiler ignored1 = profiler.start("renderSide")) {
										// TODO use raw puts?
										bufferBuilder.pos(v0.x, v0.y, v0.z).color(red0 * diffuse0, green0 * diffuse0, blue0 * diffuse0, 1F).tex(v0u, v0v).lightmap((quadSkyLight0 >= lightmapSkyLight0) ? quadSkyLight0 : lightmapSkyLight0, (quadBlockLight0 >= lightmapBlockLight0) ? quadBlockLight0 : lightmapBlockLight0).endVertex();
										bufferBuilder.pos(v1.x, v1.y, v1.z).color(red1 * diffuse1, green1 * diffuse1, blue1 * diffuse1, 1F).tex(v1u, v1v).lightmap((quadSkyLight1 >= lightmapSkyLight1) ? quadSkyLight1 : lightmapSkyLight1, (quadBlockLight1 >= lightmapBlockLight1) ? quadBlockLight1 : lightmapBlockLight1).endVertex();
										bufferBuilder.pos(v2.x, v2.y, v2.z).color(red2 * diffuse2, green2 * diffuse2, blue2 * diffuse2, 1F).tex(v2u, v2v).lightmap((quadSkyLight2 >= lightmapSkyLight2) ? quadSkyLight2 : lightmapSkyLight2, (quadBlockLight2 >= lightmapBlockLight2) ? quadBlockLight2 : lightmapBlockLight2).endVertex();
										bufferBuilder.pos(v3.x, v3.y, v3.z).color(red3 * diffuse3, green3 * diffuse3, blue3 * diffuse3, 1F).tex(v3u, v3v).lightmap((quadSkyLight3 >= lightmapSkyLight3) ? quadSkyLight3 : lightmapSkyLight3, (quadBlockLight3 >= lightmapBlockLight3) ? quadBlockLight3 : lightmapBlockLight3).endVertex();
									}
									if (renderOppositeSides) {
										// TODO use raw puts?
										try (final ModProfiler ignored1 = profiler.start("renderOppositeSide")) {
											bufferBuilder.pos(v3.x, v3.y, v3.z).color(red3 * diffuse3, green3 * diffuse3, blue3 * diffuse3, 1F).tex(v0u, v0v).lightmap((quadSkyLight3 >= lightmapSkyLight3) ? quadSkyLight3 : lightmapSkyLight3, (quadBlockLight3 >= lightmapBlockLight3) ? quadBlockLight3 : lightmapBlockLight3).endVertex();
											bufferBuilder.pos(v2.x, v2.y, v2.z).color(red2 * diffuse2, green2 * diffuse2, blue2 * diffuse2, 1F).tex(v1u, v1v).lightmap((quadSkyLight2 >= lightmapSkyLight2) ? quadSkyLight2 : lightmapSkyLight2, (quadBlockLight2 >= lightmapBlockLight2) ? quadBlockLight2 : lightmapBlockLight2).endVertex();
											bufferBuilder.pos(v1.x, v1.y, v1.z).color(red1 * diffuse1, green1 * diffuse1, blue1 * diffuse1, 1F).tex(v2u, v2v).lightmap((quadSkyLight1 >= lightmapSkyLight1) ? quadSkyLight1 : lightmapSkyLight1, (quadBlockLight1 >= lightmapBlockLight1) ? quadBlockLight1 : lightmapBlockLight1).endVertex();
											bufferBuilder.pos(v0.x, v0.y, v0.z).color(red0 * diffuse0, green0 * diffuse0, blue0 * diffuse0, 1F).tex(v3u, v3v).lightmap((quadSkyLight0 >= lightmapSkyLight0) ? quadSkyLight0 : lightmapSkyLight0, (quadBlockLight0 >= lightmapBlockLight0) ? quadBlockLight0 : lightmapBlockLight0).endVertex();
										}
									}
								}
							} finally {
								OptiFineCompatibility.popShaderThing(bufferBuilder);
							}
							usedBlockRenderLayers[correctedBlockRenderLayerOrdinal] |= wasAnythingRendered;
						}
					}
					ForgeHooksClient.setRenderLayer(null);
				}

			}
		}
	}

	private static void renderShortGrass(
			@Nonnull final RenderChunk renderChunk, @Nonnull final ChunkCompileTaskGenerator generator, @Nonnull final CompiledChunk compiledChunk, @Nonnull final BlockPos renderChunkPosition,
			@Nonnull final IBlockAccess blockAccess,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final Random random,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final BlockPos texturePos,
			final Vec3 v0, final Vec3 v1, final Vec3 v2, final Vec3 v3,
			final int lightmapSkyLight0, final int lightmapSkyLight1, final int lightmapSkyLight2, final int lightmapSkyLight3,
			final int lightmapBlockLight0, final int lightmapBlockLight1, final int lightmapBlockLight2, final int lightmapBlockLight3
	) {

		final IBlockState grassPlantState = StateHolder.GRASS_PLANT_DEFAULT;

		pooledMutableBlockPos.setPos(texturePos).move(EnumFacing.UP);
//		// isBlockLoaded only checks x and z
//		if (pooledMutableBlockPos.getY() > renderChunkPosition.getY() + 16 || !blockAccess.isBlockLoaded(pooledMutableBlockPos)) {
//			return;
//		}
		final IBlockState blockStateUp = blockAccess.getBlockState(pooledMutableBlockPos);
		if (blockStateUp == grassPlantState) {
			return;
		}
		if (blockStateUp.isOpaqueCube()) {
			return;
		}
		if (blockStateUp.getMaterial().isLiquid()) {
			return;
		}

		final double shortGrassHeight = 0.25D;

		final Vec3d offset = grassPlantState.getOffset(blockAccess, texturePos);
		final double offX = offset.x;
		final double offY = 0;
		final double offZ = offset.z;

		final double v0x = v0.x;
		final double v0y = v0.y;
		final double v0z = v0.z;
		final double v1x = v1.x;
		final double v1y = v1.y;
		final double v1z = v1.z;
		final double v2x = v2.x;
		final double v2y = v2.y;
		final double v2z = v2.z;
		final double v3x = v3.x;
		final double v3y = v3.y;
		final double v3z = v3.z;

		final IBakedModel model = blockRendererDispatcher.getModelForState(grassPlantState);
		final long posRand = MathHelper.getPositionRandom(texturePos);

		final int color = BiomeColorHelper.GRASS_COLOR.getColorAtPos(blockAccess.getBiome(texturePos), texturePos);

		final int red = (color & 0xFF0000) >> 16;
		final int green = (color & 0x00FF00) >> 8;
		final int blue = (color & 0x0000FF);

		final BlockRenderLayer[] values = BlockRenderLayer.values();
		for (int i = 0, valuesLength = values.length; i < valuesLength; ++i) {
			final BlockRenderLayer initialBlockRenderLayer = values[i];
			if (!grassPlantState.getBlock().canRenderInLayer(grassPlantState, initialBlockRenderLayer)) {
				continue;
			}
			final BlockRenderLayer correctedBlockRenderLayer = ClientUtil.getCorrectRenderLayer(initialBlockRenderLayer);
			final int correctedBlockRenderLayerOrdinal = correctedBlockRenderLayer.ordinal();
			ForgeHooksClient.setRenderLayer(correctedBlockRenderLayer);

			final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(generator, correctedBlockRenderLayerOrdinal, compiledChunk, correctedBlockRenderLayer, renderChunk, renderChunkPosition);

			boolean wasAnythingRendered = false;

			OptiFineCompatibility.pushShaderThing(grassPlantState, texturePos, blockAccess, bufferBuilder);
			try {

				for (int facingIndex = 0, enumfacing_quads_orderedLength = ENUMFACING_QUADS_ORDERED.length; facingIndex < enumfacing_quads_orderedLength; ++facingIndex) {
					final EnumFacing facing = ENUMFACING_QUADS_ORDERED[facingIndex];
//					random.setSeed(posRand);
					final List<BakedQuad> quads = model.getQuads(grassPlantState, facing, posRand);
					for (int quadIndex = 0, quadsSize = quads.size(); quadIndex < quadsSize; ++quadIndex) {
						final BakedQuad quad = quads.get(quadIndex);
						wasAnythingRendered = true;

						final int formatSize = quad.getFormat().getIntegerSize();
						final int[] vertexData = quad.getVertexData();

						final float qv0x = Float.intBitsToFloat(vertexData[0]);
						final float qv0y = Float.intBitsToFloat(vertexData[1]);
						final float qv0z = Float.intBitsToFloat(vertexData[2]);
						final float qv1x = Float.intBitsToFloat(vertexData[formatSize]);
						final float qv1y = Float.intBitsToFloat(vertexData[formatSize + 1]);
						final float qv1z = Float.intBitsToFloat(vertexData[formatSize + 2]);
						final float qv2x = Float.intBitsToFloat(vertexData[formatSize * 2]);
						final float qv2y = Float.intBitsToFloat(vertexData[formatSize * 2 + 1]);
						final float qv2z = Float.intBitsToFloat(vertexData[formatSize * 2 + 2]);
						final float qv3x = Float.intBitsToFloat(vertexData[formatSize * 3]);
						final float qv3y = Float.intBitsToFloat(vertexData[formatSize * 3 + 1]);
						final float qv3z = Float.intBitsToFloat(vertexData[formatSize * 3 + 2]);

						final boolean qr0x = qv0x > 0.5F;
						final boolean qr0y = qv0y > 0.5F;
						final boolean qr0z = qv0z > 0.5F;
						final boolean qr1x = qv1x > 0.5F;
						final boolean qr1y = qv1y > 0.5F;
						final boolean qr1z = qv1z > 0.5F;
						final boolean qr2x = qv2x > 0.5F;
						final boolean qr2y = qv2y > 0.5F;
						final boolean qr2z = qv2z > 0.5F;
						final boolean qr3x = qv3x > 0.5F;
						final boolean qr3y = qv3y > 0.5F;
						final boolean qr3z = qv3z > 0.5F;

						// 0 3
						// 1 2
						final double r0x;
						final double r1x;
						final double r2x;
						final double r3x;

						// 0 3
						// 1 2
						final double r0y;
						final double r1y;
						final double r2y;
						final double r3y;

						// 0 3
						// 1 2
						final double r0z;
						final double r1z;
						final double r2z;
						final double r3z;

						// Find the Vec3 vertex for each quad vertex

						// 0 3
						// 1 2
						// 10 00
						// 11 01
						if (qr0x && qr0y && qr0z) {
							// (1, 1, 1)
							r0x = v1x;
							r0y = v1y + shortGrassHeight;
							r0z = v1z;
						} else if (qr0x && qr0y && !qr0z) {
							// (1, 1, 0)
							r0x = v0x;
							r0y = v0y + shortGrassHeight;
							r0z = v0z;
						} else if (qr0x && !qr0y && qr0z) {
							// (1, 0, 1)
							r0x = v1x;
							r0y = v1y;
							r0z = v1z;
						} else if (qr0x && !qr0y && !qr0z) {
							// (1, 0, 0)
							r0x = v0x;
							r0y = v0y;
							r0z = v0z;
						} else if (!qr0x && qr0y && qr0z) {
							// (0, 1, 1)
							r0x = v2x;
							r0y = v2y + shortGrassHeight;
							r0z = v2z;
						} else if (!qr0x && qr0y && !qr0z) {
							// (0, 1, 0)
							r0x = v3x;
							r0y = v3y + shortGrassHeight;
							r0z = v3z;
						} else if (!qr0x && !qr0y && qr0z) {
							// (0, 0, 1)
							r0x = v2x;
							r0y = v2y;
							r0z = v2z;
						} else /*if (!qr0x && !qr0y && !qr0z)*/ {
							// (0, 0, 0)
							r0x = v3x;
							r0y = v3y;
							r0z = v3z;
						}
						if (qr1x && qr1y && qr1z) {
							// (1, 1, 1)
							r1x = v1x;
							r1y = v1y + shortGrassHeight;
							r1z = v1z;
						} else if (qr1x && qr1y && !qr1z) {
							// (1, 1, 0)
							r1x = v0x;
							r1y = v0y + shortGrassHeight;
							r1z = v0z;
						} else if (qr1x && !qr1y && qr1z) {
							// (1, 0, 1)
							r1x = v1x;
							r1y = v1y;
							r1z = v1z;
						} else if (qr1x && !qr1y && !qr1z) {
							// (1, 0, 0)
							r1x = v0x;
							r1y = v0y;
							r1z = v0z;
						} else if (!qr1x && qr1y && qr1z) {
							// (0, 1, 1)
							r1x = v2x;
							r1y = v2y + shortGrassHeight;
							r1z = v2z;
						} else if (!qr1x && qr1y && !qr1z) {
							// (0, 1, 0)
							r1x = v3x;
							r1y = v3y + shortGrassHeight;
							r1z = v3z;
						} else if (!qr1x && !qr1y && qr1z) {
							// (0, 0, 1)
							r1x = v2x;
							r1y = v2y;
							r1z = v2z;
						} else /*if (!qr1x && !qr1y && !qr1z)*/ {
							// (0, 0, 0)
							r1x = v3x;
							r1y = v3y;
							r1z = v3z;
						}
						if (qr2x && qr2y && qr2z) {
							// (1, 1, 1)
							r2x = v1x;
							r2y = v1y + shortGrassHeight;
							r2z = v1z;
						} else if (qr2x && qr2y && !qr2z) {
							// (1, 1, 0)
							r2x = v0x;
							r2y = v0y + shortGrassHeight;
							r2z = v0z;
						} else if (qr2x && !qr2y && qr2z) {
							// (1, 0, 1)
							r2x = v1x;
							r2y = v1y;
							r2z = v1z;
						} else if (qr2x && !qr2y && !qr2z) {
							// (1, 0, 0)
							r2x = v0x;
							r2y = v0y;
							r2z = v0z;
						} else if (!qr2x && qr2y && qr2z) {
							// (0, 1, 1)
							r2x = v2x;
							r2y = v2y + shortGrassHeight;
							r2z = v2z;
						} else if (!qr2x && qr2y && !qr2z) {
							// (0, 1, 0)
							r2x = v3x;
							r2y = v3y + shortGrassHeight;
							r2z = v3z;
						} else if (!qr2x && !qr2y && qr2z) {
							// (0, 0, 1)
							r2x = v2x;
							r2y = v2y;
							r2z = v2z;
						} else /*if (!qr2x && !qr2y && !qr2z)*/ {
							// (0, 0, 0)
							r2x = v3x;
							r2y = v3y;
							r2z = v3z;
						}
						if (qr3x && qr3y && qr3z) {
							// (1, 1, 1)
							r3x = v1x;
							r3y = v1y + shortGrassHeight;
							r3z = v1z;
						} else if (qr3x && qr3y && !qr3z) {
							// (1, 1, 0)
							r3x = v0x;
							r3y = v0y + shortGrassHeight;
							r3z = v0z;
						} else if (qr3x && !qr3y && qr3z) {
							// (1, 0, 1)
							r3x = v1x;
							r3y = v1y;
							r3z = v1z;
						} else if (qr3x && !qr3y && !qr3z) {
							// (1, 0, 0)
							r3x = v0x;
							r3y = v0y;
							r3z = v0z;
						} else if (!qr3x && qr3y && qr3z) {
							// (0, 1, 1)
							r3x = v2x;
							r3y = v2y + shortGrassHeight;
							r3z = v2z;
						} else if (!qr3x && qr3y && !qr3z) {
							// (0, 1, 0)
							r3x = v3x;
							r3y = v3y + shortGrassHeight;
							r3z = v3z;
						} else if (!qr3x && !qr3y && qr3z) {
							// (0, 0, 1)
							r3x = v2x;
							r3y = v2y;
							r3z = v2z;
						} else /*if (!qr3x && !qr3y && !qr3z)*/ {
							// (0, 0, 0)
							r3x = v3x;
							r3y = v3y;
							r3z = v3z;
						}

						// Quads are packed xyz|argb|u|v|ts
						final float v0u = Float.intBitsToFloat(vertexData[4]);
						final float v0v = Float.intBitsToFloat(vertexData[5]);
						final float v1u = Float.intBitsToFloat(vertexData[formatSize + 4]);
						final float v1v = Float.intBitsToFloat(vertexData[formatSize + 5]);
						final float v2u = Float.intBitsToFloat(vertexData[formatSize * 2 + 4]);
						final float v2v = Float.intBitsToFloat(vertexData[formatSize * 2 + 5]);
						final float v3u = Float.intBitsToFloat(vertexData[formatSize * 3 + 4]);
						final float v3v = Float.intBitsToFloat(vertexData[formatSize * 3 + 5]);

						final int quadPackedLight0 = vertexData[6];
						final int quadPackedLight1 = vertexData[formatSize + 6];
						final int quadPackedLight2 = vertexData[formatSize * 2 + 6];
						final int quadPackedLight3 = vertexData[formatSize * 3 + 6];

						final int quadSkyLight0 = (quadPackedLight0 >> 16) & 0xFF;
						final int quadSkyLight1 = (quadPackedLight1 >> 16) & 0xFF;
						final int quadSkyLight2 = (quadPackedLight2 >> 16) & 0xFF;
						final int quadSkyLight3 = (quadPackedLight3 >> 16) & 0xFF;
						final int quadBlockLight0 = quadPackedLight0 & 0xFF;
						final int quadBlockLight1 = quadPackedLight1 & 0xFF;
						final int quadBlockLight2 = quadPackedLight2 & 0xFF;
						final int quadBlockLight3 = quadPackedLight3 & 0xFF;

						bufferBuilder.pos(offX + r0x, offY + r0y, offZ + r0z).color(red, green, blue, 255).tex(v0u, v0v).lightmap((quadSkyLight0 >= lightmapSkyLight0) ? quadSkyLight0 : lightmapSkyLight0, (quadBlockLight0 >= lightmapBlockLight0) ? quadBlockLight0 : lightmapBlockLight0).endVertex();
						bufferBuilder.pos(offX + r1x, offY + r1y, offZ + r1z).color(red, green, blue, 255).tex(v1u, v1v).lightmap((quadSkyLight1 >= lightmapSkyLight1) ? quadSkyLight1 : lightmapSkyLight1, (quadBlockLight1 >= lightmapBlockLight1) ? quadBlockLight1 : lightmapBlockLight1).endVertex();
						bufferBuilder.pos(offX + r2x, offY + r2y, offZ + r2z).color(red, green, blue, 255).tex(v2u, v2v).lightmap((quadSkyLight2 >= lightmapSkyLight2) ? quadSkyLight2 : lightmapSkyLight2, (quadBlockLight2 >= lightmapBlockLight2) ? quadBlockLight2 : lightmapBlockLight2).endVertex();
						bufferBuilder.pos(offX + r3x, offY + r3y, offZ + r3z).color(red, green, blue, 255).tex(v3u, v3v).lightmap((quadSkyLight3 >= lightmapSkyLight3) ? quadSkyLight3 : lightmapSkyLight3, (quadBlockLight3 >= lightmapBlockLight3) ? quadBlockLight3 : lightmapBlockLight3).endVertex();

					}
				}

			} finally {
				OptiFineCompatibility.popShaderThing(bufferBuilder);
			}
			usedBlockRenderLayers[correctedBlockRenderLayerOrdinal] |= wasAnythingRendered;
		}
		ForgeHooksClient.setRenderLayer(null);
	}

	private static boolean areVerticesCloseToFlat(final Vec3 v0, final Vec3 v1, final Vec3 v2, final Vec3 v3) {
		final double v0y = v0.y;
		final double v1y = v1.y;
		final double v2y = v2.y;
		final double v3y = v3.y;

		return max(v0y, v1y, v2y, v3y) - min(v0y, v1y, v2y, v3y) <= 0.25D;

	}

	private static double max(double d0, final double d1, final double d2, final double d3) {
		if (d0 < d1) {
			d0 = d1;
		}
		if (d0 < d2) {
			d0 = d2;
		}
		if (d0 < d3) {
			return d3;
		} else {
			return d0;
		}
	}

	private static double min(double d0, final double d1, final double d2, final double d3) {
		if (d0 > d1) {
			d0 = d1;
		}
		if (d0 > d2) {
			d0 = d2;
		}
		if (d0 > d3) {
			return d3;
		} else {
			return d0;
		}
	}

	private static EnumFacing toSide(final double x, final double y, final double z) {
		if (Math.abs(x) > Math.abs(y)) {
			if (Math.abs(x) > Math.abs(z)) {
				if (x < 0) return EnumFacing.WEST;
				return EnumFacing.EAST;
			} else {
				if (z < 0) return EnumFacing.NORTH;
				return EnumFacing.SOUTH;
			}
		} else {
			if (Math.abs(y) > Math.abs(z)) {
				if (y < 0) return EnumFacing.DOWN;
				return EnumFacing.UP;
			} else {
				if (z < 0) return EnumFacing.NORTH;
				return EnumFacing.SOUTH;
			}
		}
	}

	private static float diffuseLight(final EnumFacing side) {
		if (side == EnumFacing.UP) {
			return 1f;
		} else {
			return .97f;
		}
	}

}
