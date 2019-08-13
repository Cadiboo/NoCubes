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
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.StateHolder;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.biome.BiomeColors;
import net.minecraft.world.biome.BiomeColors.IColorResolver;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.registries.IRegistryDelegate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES;
import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES_LENGTH;
import static io.github.cadiboo.nocubes.client.ModelHelper.DIRECTION_QUADS_ORDERED;
import static io.github.cadiboo.nocubes.client.ModelHelper.DIRECTION_QUADS_ORDERED_LENGTH;
import static net.minecraft.util.Direction.DOWN;
import static net.minecraft.util.Direction.EAST;
import static net.minecraft.util.Direction.NORTH;
import static net.minecraft.util.Direction.SOUTH;
import static net.minecraft.util.Direction.UP;
import static net.minecraft.util.Direction.WEST;

/**
 * @author Cadiboo
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public final class MeshRenderer {

	private static final Logger LOGGER = LogManager.getLogger("NoCubes MeshRenderer");

	public static void renderMesh(
			@Nonnull final ChunkRender chunkRender,
			@Nonnull final ChunkRenderTask chunkRenderTask,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos chunkRenderPos,
			final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
			@Nonnull final IEnviromentBlockReader reader,
			@Nonnull final StateCache stateCache,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final Random random,
			@Nonnull final LazyPackedLightCache lazyPackedLightCache,
			@Nonnull final LazyBlockColorCache lazyBlockColorCache,
			@Nonnull final Map<Vec3b, FaceList> chunkData,
			@Nonnull final SmoothableCache smoothableCache,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final PooledMutableBlockPos texturePooledMutableBlockPos,
			@Nonnull final boolean[] usedBlockRenderLayers,
			final boolean renderOppositeSides,
			final boolean tryForBetterTexturesSnow, final boolean tryForBetterTexturesGrass
	) {
		try (final ModProfiler profiler = ModProfiler.get().start("renderMesh")) {

			final Map<IRegistryDelegate<Block>, IBlockColor> blockColorsRegistry = Minecraft.getInstance().getBlockColors().colors;

			for (Entry<Vec3b, FaceList> entry : chunkData.entrySet()) {
				try (final Vec3b pos = entry.getKey()) {
					try (final FaceList faces = entry.getValue()) {

						if (faces.isEmpty()) {
							continue;
						}

						profiler.end(); // HACKY
						profiler.start("prepareRenderFaces"); // HACKY

						final int initialPosX = chunkRenderPosX + pos.x;
						final int initialPosY = chunkRenderPosY + pos.y;
						final int initialPosZ = chunkRenderPosZ + pos.z;

						//TODO use pos? (I've forgotten what this todo is even about)
						final byte relativePosX = ModUtil.getRelativePos(chunkRenderPosX, initialPosX);
						final byte relativePosY = ModUtil.getRelativePos(chunkRenderPosY, initialPosY);
						final byte relativePosZ = ModUtil.getRelativePos(chunkRenderPosZ, initialPosZ);

						profiler.end(); // HACKY (end here because getTexturePosAndState profiles itself)

						final BlockState textureState = ClientUtil.getTexturePosAndState(
								initialPosX, initialPosY, initialPosZ,
								texturePooledMutableBlockPos,
								stateCache, smoothableCache,
								relativePosX, relativePosY, relativePosZ,
								tryForBetterTexturesSnow, tryForBetterTexturesGrass
						);

						profiler.start("renderMesh"); // HACKY

						try {
							renderFaces(
									chunkRender, chunkRenderTask, compiledChunk, chunkRenderPos,
									chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
									reader, blockRendererDispatcher, random,
									usedBlockRenderLayers,
									lazyPackedLightCache, lazyBlockColorCache,
									blockColorsRegistry,
									pos, faces,
									pooledMutableBlockPos,
									texturePooledMutableBlockPos, textureState,
									renderOppositeSides
							);
						} catch (Exception e) {
							final CrashReport crashReport = CrashReport.makeCrashReport(e, "Rendering faces for smooth block in world");

							CrashReportCategory realBlockCrashReportCategory = crashReport.makeCategory("Block being rendered");
							final BlockPos blockPos = new BlockPos(chunkRenderPosX + pos.x, chunkRenderPosX + pos.y, chunkRenderPosX + pos.z);
							CrashReportCategory.addBlockInfo(realBlockCrashReportCategory, blockPos, reader.getBlockState(new BlockPos(initialPosX, initialPosY, initialPosZ)));

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
			@Nonnull final ChunkRender chunkRender,
			@Nonnull final ChunkRenderTask chunkRenderTask,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos chunkRenderPos,
			final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
			@Nonnull final IEnviromentBlockReader reader,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final Random random,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final LazyPackedLightCache lazyPackedLightCache,
			@Nonnull final LazyBlockColorCache lazyBlockColorCache,
			@Nonnull final Map<IRegistryDelegate<Block>, IBlockColor> blockColorsRegistry,
			@Nonnull final Vec3b pos,
			@Nonnull final FaceList faces,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final BlockPos texturePos,
			@Nonnull final BlockState textureState,
			final boolean renderOppositeSides
	) {
		final IModelData modelData = chunkRenderTask.getModelData(texturePos);
		final long posRand = textureState.getPositionRandom(texturePos);

		final boolean applyDiffuseLighting = Config.applyDiffuseLighting;
		final boolean shortGrass = Config.shortGrass;

		final boolean colorsCacheApplicableToTextureState = lazyBlockColorCache.shouldApply.test(textureState);

		final int[] lazyBlockColorCacheCache = lazyBlockColorCache.cache;
		final int lazyBlockColorCacheSizeX = lazyBlockColorCache.sizeX;
		final int lazyBlockColorCacheSizeY = lazyBlockColorCache.sizeY;
		final int biomeBlendRadius = Minecraft.getInstance().gameSettings.biomeBlendRadius;
		final int d = biomeBlendRadius * 2 + 1;
		final int lazyBlockColorCacheArea = d * d;
		final int lazyBlockColorCacheMax = biomeBlendRadius + 1;
		final IColorResolver colorResolver = lazyBlockColorCache.colorResolver;

		// TODO: Only get if required (on first use)
		final IBlockColor textureColorGetter = blockColorsRegistry.get(textureState.getBlock().delegate);
		final boolean textureColorGetterIsNonNull = textureColorGetter != null;

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

						if (!applyDiffuseLighting) {
							diffuse0 = diffuse1 = diffuse2 = diffuse3 = 1;
						} else {
							profiler.end(); // HACKY
							profiler.start("calculateDiffuseLighting");
							diffuse0 = diffuseLight(toSide(
									v0.x - chunkRenderPosX - pos.x,
									v0.y - chunkRenderPosY - pos.y,
									v0.z - chunkRenderPosZ - pos.z
							));
							diffuse1 = diffuseLight(toSide(
									v1.x - chunkRenderPosX - pos.x,
									v1.y - chunkRenderPosY - pos.y,
									v1.z - chunkRenderPosZ - pos.z
							));
							diffuse2 = diffuseLight(toSide(
									v2.x - chunkRenderPosX - pos.x,
									v2.y - chunkRenderPosY - pos.y,
									v2.z - chunkRenderPosZ - pos.z
							));
							diffuse3 = diffuseLight(toSide(
									v3.x - chunkRenderPosX - pos.x,
									v3.y - chunkRenderPosY - pos.y,
									v3.z - chunkRenderPosZ - pos.z
							));
							profiler.end();
							profiler.start("renderMesh"); // HACKY
						}

						final int lightmapSkyLight0;
						final int lightmapSkyLight1;
						final int lightmapSkyLight2;
						final int lightmapSkyLight3;

						final int lightmapBlockLight0;
						final int lightmapBlockLight1;
						final int lightmapBlockLight2;
						final int lightmapBlockLight3;

						profiler.end(); // HACKY
						try (final LightmapInfo lightmapInfo = LightmapInfo.generateLightmapInfo(lazyPackedLightCache, v0, v1, v2, v3, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, pooledMutableBlockPos)) {

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

						if (shortGrass) {
							profiler.end(); // HACKY
							profiler.start("shortGrass");
							if (textureState == StateHolder.GRASS_BLOCK_DEFAULT && areVerticesCloseToFlat(v0, v1, v2, v3)) {
								renderShortGrass(
										chunkRender, chunkRenderTask, compiledChunk, chunkRenderPos,
										chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
										reader,
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

						for (int i = 0; i < BLOCK_RENDER_LAYER_VALUES_LENGTH; ++i) {
							final BlockRenderLayer initialBlockRenderLayer = BLOCK_RENDER_LAYER_VALUES[i];
							if (!textureState.canRenderInLayer(initialBlockRenderLayer)) {
								continue;
							}
							final BlockRenderLayer correctedBlockRenderLayer = ClientUtil.getCorrectRenderLayer(initialBlockRenderLayer);
							final int correctedBlockRenderLayerOrdinal = correctedBlockRenderLayer.ordinal();
							ForgeHooksClient.setRenderLayer(correctedBlockRenderLayer);

							final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(chunkRenderTask, correctedBlockRenderLayerOrdinal, compiledChunk, correctedBlockRenderLayer, chunkRender, chunkRenderPos);

							boolean wasAnythingRendered = false;

							OptiFineCompatibility.pushShaderThing(textureState, texturePos, reader, bufferBuilder);
							try {

								List<BakedQuad> quads;
								try (ModProfiler ignored1 = profiler.start("getQuads")) {
									random.setSeed(posRand);
									quads = ModelHelper.getQuads(textureState, texturePos, bufferBuilder, reader, blockRendererDispatcher, modelData, random, posRand, correctedBlockRenderLayer);
									if (quads == null) {
										LOGGER.warn("Got null quads for " + textureState.getBlock() + " at " + texturePos);
										quads = new ArrayList<>();
										quads.add(blockRendererDispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(null, DOWN, random, modelData).get(0));
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

									final boolean hasTintIndex = quad.hasTintIndex();
									if (BlockColorInfo.rainbow || BlockColorInfo.black) {
										if (!hasSetColors) {
											profiler.end(); // HACKY
											try (
													final ModProfiler ignored = ModProfiler.get().start("generateBlockColorInfo");
													final BlockColorInfo blockColorInfo = BlockColorInfo.generateBlockColorInfo(lazyBlockColorCache, v0, v1, v2, v3, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, lazyBlockColorCacheCache, lazyBlockColorCacheSizeX, lazyBlockColorCacheSizeY, biomeBlendRadius, lazyBlockColorCacheArea, lazyBlockColorCacheMax, reader, colorResolver, true, pooledMutableBlockPos)
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
									} else if (hasTintIndex) {
										if (colorsCacheApplicableToTextureState) {
											if (!hasSetColors) {
												profiler.end(); // HACKY
												try (
														final ModProfiler ignored = ModProfiler.get().start("generateBlockColorInfo");
														final BlockColorInfo blockColorInfo = BlockColorInfo.generateBlockColorInfo(lazyBlockColorCache, v0, v1, v2, v3, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, lazyBlockColorCacheCache, lazyBlockColorCacheSizeX, lazyBlockColorCacheSizeY, biomeBlendRadius, lazyBlockColorCacheArea, lazyBlockColorCacheMax, reader, colorResolver, true, pooledMutableBlockPos)
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
										} else { // don't use cache
											if (textureColorGetterIsNonNull) {
												final int tintIndex = quad.getTintIndex();
												profiler.end(); // HACKY
												try (
														final ModProfiler ignored = ModProfiler.get().start("generateBlockColorInfo");
														final BlockColorInfo blockColorInfo = BlockColorInfo.generateBlockColorInfo(
																lazyBlockColorCache, v0, v1, v2, v3,
																chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
																lazyBlockColorCacheCache,
																lazyBlockColorCacheSizeX, lazyBlockColorCacheSizeY,
																biomeBlendRadius, lazyBlockColorCacheArea, lazyBlockColorCacheMax,
																reader,
																(ignored1, colorPos) -> textureColorGetter.getColor(textureState, reader, colorPos, tintIndex),
																false,
																pooledMutableBlockPos
														)
												) {
													red0 = blockColorInfo.red0;
													green0 = blockColorInfo.green0;
													blue0 = blockColorInfo.blue0;
													red1 = blockColorInfo.red1;
													green1 = blockColorInfo.green1;
													blue1 = blockColorInfo.blue1;
													red2 = blockColorInfo.red2;
													green2 = blockColorInfo.green2;
													blue2 = blockColorInfo.blue2;
													red3 = blockColorInfo.red3;
													green3 = blockColorInfo.green3;
													blue3 = blockColorInfo.blue3;
												}
												profiler.start("renderMesh"); // HACKY
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
										}
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

	//TODO: fix bad lighting fix
	private static void renderShortGrass(
			@Nonnull final ChunkRender chunkRender, @Nonnull final ChunkRenderTask chunkRenderTask, @Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos chunkRenderPos,
			final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
			@Nonnull final IEnviromentBlockReader reader,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final Random random,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final BlockPos texturePos,
			final Vec3 v0, final Vec3 v1, final Vec3 v2, final Vec3 v3,
			final int lightmapSkyLight0, final int lightmapSkyLight1, final int lightmapSkyLight2, final int lightmapSkyLight3,
			final int lightmapBlockLight0, final int lightmapBlockLight1, final int lightmapBlockLight2, final int lightmapBlockLight3
	) {

		final BlockState grassPlantState = StateHolder.GRASS_PLANT_DEFAULT;

		// TODO StateCache?
		pooledMutableBlockPos.setPos(texturePos).move(UP);
		// isBlockLoaded only checks x and z
		if (
				pooledMutableBlockPos.getX() > chunkRenderPosX + 16 ||
						pooledMutableBlockPos.getY() > chunkRenderPosY + 16 ||
						pooledMutableBlockPos.getZ() > chunkRenderPosZ + 16
		) {
			return;
		}
		final BlockState blockStateUp = reader.getBlockState(pooledMutableBlockPos);
		if (blockStateUp == grassPlantState || blockStateUp == StateHolder.TALL_GRASS_PLANT_BOTTOM) {
			return;
		}
		if (blockStateUp.isOpaqueCube(reader, pooledMutableBlockPos)) {
			return;
		}
		if (blockStateUp.getMaterial().isLiquid()) {
			return;
		}

		final double shortGrassHeight = 0.25D;

		final Vec3d offset = grassPlantState.getOffset(reader, texturePos);
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

		// TODO: BlockColorsCache?
		final int color = BiomeColors.GRASS_COLOR.getColor(reader.getBiome(texturePos), texturePos);

		final int red = (color & 0xFF0000) >> 16;
		final int green = (color & 0x00FF00) >> 8;
		final int blue = (color & 0x0000FF);

		for (int i = 0; i < BLOCK_RENDER_LAYER_VALUES_LENGTH; ++i) {
			final BlockRenderLayer initialBlockRenderLayer = BLOCK_RENDER_LAYER_VALUES[i];
			if (!grassPlantState.getBlock().canRenderInLayer(grassPlantState, initialBlockRenderLayer)) {
				continue;
			}
			final BlockRenderLayer correctedBlockRenderLayer = ClientUtil.getCorrectRenderLayer(initialBlockRenderLayer);
			final int correctedBlockRenderLayerOrdinal = correctedBlockRenderLayer.ordinal();
			ForgeHooksClient.setRenderLayer(correctedBlockRenderLayer);

			final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(chunkRenderTask, correctedBlockRenderLayerOrdinal, compiledChunk, correctedBlockRenderLayer, chunkRender, chunkRenderPos);

			boolean wasAnythingRendered = false;

			OptiFineCompatibility.pushShaderThing(grassPlantState, texturePos, reader, bufferBuilder);
			try {

				for (int directionIndex = 0; directionIndex < DIRECTION_QUADS_ORDERED_LENGTH; ++directionIndex) {
					final Direction direction = DIRECTION_QUADS_ORDERED[directionIndex];
					random.setSeed(posRand);
					final List<BakedQuad> quads = model.getQuads(grassPlantState, direction, random);
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

						// Weird stuff is going on with emissive lighting > 1.12.2.
						// It could be the new model system, it could be something else.
						// Anyway, not many people are likely to complain about emissive lighting on grass

//						final int quadPackedLight0 = vertexData[6];
//						final int quadPackedLight1 = vertexData[formatSize + 6];
//						final int quadPackedLight2 = vertexData[formatSize * 2 + 6];
//						final int quadPackedLight3 = vertexData[formatSize * 3 + 6];
//
//						final int quadSkyLight0 = (quadPackedLight0 >> 16) & 0xFF;
//						final int quadSkyLight1 = (quadPackedLight1 >> 16) & 0xFF;
//						final int quadSkyLight2 = (quadPackedLight2 >> 16) & 0xFF;
//						final int quadSkyLight3 = (quadPackedLight3 >> 16) & 0xFF;
//						final int quadBlockLight0 = quadPackedLight0 & 0xFF;
//						final int quadBlockLight1 = quadPackedLight1 & 0xFF;
//						final int quadBlockLight2 = quadPackedLight2 & 0xFF;
//						final int quadBlockLight3 = quadPackedLight3 & 0xFF;

						final int quadSkyLight0 = 0;
						final int quadSkyLight1 = 0;
						final int quadSkyLight2 = 0;
						final int quadSkyLight3 = 0;
						final int quadBlockLight0 = 0;
						final int quadBlockLight1 = 0;
						final int quadBlockLight2 = 0;
						final int quadBlockLight3 = 0;

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
		if (d0 < d1) d0 = d1;
		if (d0 < d2) d0 = d2;
		return d0 < d3 ? d3 : d0;
	}

	private static double min(double d0, final double d1, final double d2, final double d3) {
		if (d0 > d1) d0 = d1;
		if (d0 > d2) d0 = d2;
		return d0 > d3 ? d3 : d0;
	}

	private static Direction toSide(final double x, final double y, final double z) {
		if (Math.abs(x) > Math.abs(y)) {
			if (Math.abs(x) > Math.abs(z)) {
				if (x < 0) return WEST;
				return EAST;
			} else {
				if (z < 0) return NORTH;
				return SOUTH;
			}
		} else {
			if (Math.abs(y) > Math.abs(z)) {
				if (y < 0) return DOWN;
				return UP;
			} else {
				if (z < 0) return NORTH;
				return SOUTH;
			}
		}
	}

	private static float diffuseLight(final Direction side) {
		if (side == UP) {
			return 1f;
		} else {
			return .97f;
		}
	}

}
