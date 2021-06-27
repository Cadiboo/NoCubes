package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.*;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES;
import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES_LENGTH;
import static io.github.cadiboo.nocubes.client.ModelHelper.DIRECTION_QUADS_ORDERED;
import static io.github.cadiboo.nocubes.client.ModelHelper.DIRECTION_QUADS_ORDERED_LENGTH;
import static io.github.cadiboo.nocubes.util.IsSmoothable.LEAVES_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN_SMOOTHABLE;
import static net.minecraft.util.EnumFacing.*;

/**
 * @author Cadiboo
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public final class MeshRenderer {

	private static final Logger LOGGER = LogManager.getLogger("NoCubes MeshRenderer");

	private static final Predicate<IBlockState> IS_BLOCK_STATE_GRASS = blockState -> ModUtil.isMaterialGrass(blockState.getMaterial());
	private static final Predicate<IBlockState> IS_BLOCK_STATE_LEAVES = blockState -> ModUtil.isMaterialLeaves(blockState.getMaterial());
	private static final Predicate<IBlockState> ALWAYS_TRUE = blockState -> true;

	public static void renderChunk(
		RenderChunk chunkRender,
		BlockPos chunkRenderPos,
		ChunkCompileTaskGenerator chunkRenderTask,
		CompiledChunk compiledChunk,
		// Use World for eagerly generated caches
		World world,
		// Use RenderChunkCache for lazily generated caches
		IBlockAccess chunkRenderCache,
		boolean[] usedBlockRenderLayers,
		Random random,
		BlockRendererDispatcher blockRendererDispatcher
	) {
		// TODO: Test fluids cache stuff (State, Light & Colors)
		if (!Config.renderSmoothTerrain)
			return;

		PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
		try {
//			MeshGenerator generator = new CullingCubicMeshGenerator();
			MeshGenerator generator = Config.terrainMeshGenerator.getMeshGenerator();
			BlockPos start = chunkRenderPos.subtract(generator.getNegativeAreaExtension());
			BlockPos end = start.add(ModUtil.CHUNK_SIZE).add(generator.getPositiveAreaExtension());
			try (
				Area area = new Area(world, start, end);
//				LazyBlockColorCache colors = new LazyBlockColorCache(area);
				LightCache light = new LightCache(area);
			) {
				TextureInfo tex = new TextureInfo();
				Face normals = new Face();
				Vec normal = new Vec();
				IsSmoothable isSmoothable = TERRAIN_SMOOTHABLE;
				generator.generate(area, isSmoothable, (pos, face) -> {
					face.assignNormalTo(normals);
					normals.assignAverageTo(normal);

					ClientUtil.move(pos, start);
					EnumFacing direction = normal.getDirectionFromNormal();
					IBlockState textureState = ClientUtil.getTexturePosAndState(pos, area, isSmoothable, direction, true, true); //tryForBetterTexturesSnow, tryForBetterTexturesGrass
					final long posRand = MathHelper.getPositionRandom(pos);

					for (int i = 0; i < BLOCK_RENDER_LAYER_VALUES_LENGTH; ++i) {
						BlockRenderLayer initialBlockRenderLayer = BLOCK_RENDER_LAYER_VALUES[i];
						if (!textureState.getBlock().canRenderInLayer(textureState, initialBlockRenderLayer))
							continue;

						BlockRenderLayer correctedBlockRenderLayer = ClientUtil.getCorrectRenderLayer(initialBlockRenderLayer);
						int correctedBlockRenderLayerOrdinal = correctedBlockRenderLayer.ordinal();
						ForgeHooksClient.setRenderLayer(correctedBlockRenderLayer);

						BufferBuilder buffer = ClientUtil.startOrContinueBufferBuilder(chunkRenderTask, correctedBlockRenderLayerOrdinal, compiledChunk, correctedBlockRenderLayer, chunkRender, chunkRenderPos);
						random.setSeed(posRand);
						List<BakedQuad> quads = ModelHelper.getQuads(textureState, pos, buffer, area.world, blockRendererDispatcher, /*modelData, random,*/ posRand, correctedBlockRenderLayer);
						if (quads == null)
							continue;

						for (int i1 = 0; i1 < quads.size(); ++i1) {
							BakedQuad quad = quads.get(i1);

							renderFace(start, light, tex, face, normals, direction, buffer, quad);

							usedBlockRenderLayers[correctedBlockRenderLayerOrdinal] = true;
						}
					}
					ForgeHooksClient.setRenderLayer(null);

//					BakedQuad quad = model.getQuad(world, pos);
//
//					// Can use normal in light calculation
//					buffer.pos(vec).color(colors.get(vec, state)).tex(u, v).light(light.get(vec));
					return true;
				});
			}
		} catch (Exception e) {
			CrashReport crashReport = CrashReport.makeCrashReport(e, "Error rendering NoCubes chunk!");
			crashReport.makeCategory("Rendering chunk");
			throw new ReportedException(crashReport);
		} finally {
			pooledMutableBlockPos.release();
		}
	}

	private static void renderFace(BlockPos start, LightCache light, TextureInfo tex, Face face, Face normals, EnumFacing direction, BufferBuilder buffer, BakedQuad quad) {
		tex.unpackFromQuad(quad, quad.getFormat().getIntegerSize());
		tex.switchForDirection(direction);
		drawVertex(start, light, buffer, face.v0, normals.v0, tex.u0, tex.v0);
		drawVertex(start, light, buffer, face.v1, normals.v1, tex.u1, tex.v1);
		drawVertex(start, light, buffer, face.v2, normals.v2, tex.u2, tex.v2);
		drawVertex(start, light, buffer, face.v3, normals.v3, tex.u3, tex.v3);
	}

	private static void drawVertex(BlockPos start, LightCache light, BufferBuilder buffer, Vec vec, Vec normal, float u, float v) {
		double x = start.getX();
		double y = start.getY();
		double z = start.getZ();

		int packedLight = light.get(vec, normal);
		int skyLight = (packedLight >> 16) & 0xFF;
		int blockLight = packedLight & 0xFF;

		buffer
			.pos(x + vec.x, y + vec.y, z + vec.z)
			.color(1.0F, 1.0F, 1.0F, 1.0F)
			.tex(u, v)
			.lightmap(skyLight, blockLight)
			.endVertex();
	}

//	private static void renderTerrainChunk(
//		final RenderChunk chunkRender, final BlockPos chunkRenderPos, final ChunkCompileTaskGenerator chunkRenderTask, final CompiledChunk compiledChunk, final IBlockAccess chunkRenderCache, final boolean[] usedBlockRenderLayers, final Random random, final BlockRendererDispatcher blockRendererDispatcher,
//		final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
//		final PooledMutableBlockPos pooledMutableBlockPos,
//		final StateCache stateCache, final LightCache lazyPackedLightCache,
//		final MeshGenerator meshGenerator,
//		final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ,
//		final SmoothableCache smoothableCache, final CornerDensityCache cornerDensityCache
//	) {
//		PooledMutableBlockPos texturePooledMutableBlockPos = PooledMutableBlockPos.retain();
//		try {
//			try (LazyBlockColorCache lazyBlockColorCache = ClientCacheUtil.generateLazyBlockColorCache(
//				chunkRenderPosX - 2, chunkRenderPosY - 2, chunkRenderPosZ - 2,
//				chunkRenderPosX + 18, chunkRenderPosY + 18, chunkRenderPosZ + 18,
//				2, 2, 2,
//				chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, chunkRenderCache, BiomeColorHelper.GRASS_COLOR, IS_BLOCK_STATE_GRASS
//			)) {
//				final HashMap<Vec3b, FaceList> mesh;
//				if (Config.terrainMeshGenerator == MeshGeneratorType.OldNoCubes) {
//					// TODO: Remove
//					mesh = OldNoCubes.generateChunk(chunkRenderPos, chunkRenderCache, TERRAIN_SMOOTHABLE, pooledMutableBlockPos);
//				} else {
//					mesh = MeshDispatcher.offsetChunkMesh(
//						chunkRenderPos,
//						meshGenerator.generateChunk(
//							cornerDensityCache.getCornerDensityCache(),
//							new byte[]{meshSizeX, meshSizeY, meshSizeZ}
//						)
//					);
//				}
//				MeshRenderer.renderMesh(
//					chunkRender,
//					chunkRenderTask,
//					compiledChunk,
//					chunkRenderPos,
//					chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
//					chunkRenderCache,
//					stateCache,
//					blockRendererDispatcher,
//					random,
//					lazyPackedLightCache,
//					lazyBlockColorCache,
//					mesh,
//					smoothableCache,
//					pooledMutableBlockPos, texturePooledMutableBlockPos, usedBlockRenderLayers,
//					false,
//					true, true
//				);
//			}
//		} finally {
//			texturePooledMutableBlockPos.release();
//		}
//	}
//
//	private static void renderLeavesChunk(
//		final RenderChunk chunkRender, final BlockPos chunkRenderPos, final ChunkCompileTaskGenerator generator, final CompiledChunk compiledChunk, final IBlockAccess chunkRenderCache, final boolean[] usedBlockRenderLayers, final Random random, final BlockRendererDispatcher blockRendererDispatcher,
//		final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
//		final PooledMutableBlockPos pooledMutableBlockPos,
//		final StateCache stateCache, final LightCache lazyPackedLightCache,
//		final MeshGenerator meshGenerator,
//		final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ
//	) {
//		PooledMutableBlockPos texturePooledMutableBlockPos = PooledMutableBlockPos.retain();
//		try {
//			try (LazyBlockColorCache lazyBlockColorCache = ClientCacheUtil.generateLazyBlockColorCache(
//				chunkRenderPosX - 2, chunkRenderPosY - 2, chunkRenderPosZ - 2,
//				chunkRenderPosX + 18, chunkRenderPosY + 18, chunkRenderPosZ + 18,
//				2, 2, 2,
//				chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, chunkRenderCache, BiomeColorHelper.FOLIAGE_COLOR, IS_BLOCK_STATE_LEAVES
//			)) {
//				switch (Config.smoothLeavesType) {
//					case SEPARATE:
//						for (final Block smoothableBlock : Config.leavesSmoothableBlocks) {
//							final IsSmoothable isSmoothable = (checkState) -> (LEAVES_SMOOTHABLE.test(checkState) && checkState.getBlock() == smoothableBlock);
//							// FIXME: Why is it like this... why
//							try (
////								ModProfiler ignored = ModProfiler.get().start("renderLeaves" + smoothableBlock.getRegistryName());
//								SmoothableCache smoothableCache = CacheUtil.generateSmoothableCache(
//									// Density Cache needs 1 extra on each negative axis
//									chunkRenderPosX - 1, chunkRenderPosY - 1, chunkRenderPosZ - 1,
//									// Density Cache uses 1 extra from the smoothable cache on each positive axis
//									chunkRenderPosX + meshSizeX, chunkRenderPosY + meshSizeY, chunkRenderPosZ + meshSizeZ,
//									1, 1, 1,
//									stateCache, isSmoothable
//								);
//								CornerDensityCache cornerDensityCache = CacheUtil.generateCornerDensityCache(
//									// Density Cache needs 1 extra on each negative axis
//									chunkRenderPosX - 1, chunkRenderPosY - 1, chunkRenderPosZ - 1,
//									// FIXME: Why? Why? Why? It makes no sense
//									chunkRenderPosX + meshSizeX - 1, chunkRenderPosY + meshSizeY - 1, chunkRenderPosZ + meshSizeZ - 1,
//									1, 1, 1,
//									stateCache, smoothableCache
//								)
//							) {
//								final HashMap<Vec3b, FaceList> mesh;
//								if (Config.leavesMeshGenerator == MeshGeneratorType.OldNoCubes) {
//									// TODO: Remove
//									mesh = OldNoCubes.generateChunk(chunkRenderPos, chunkRenderCache, isSmoothable, pooledMutableBlockPos);
//								} else {
//									mesh = MeshDispatcher.offsetChunkMesh(
//										chunkRenderPos,
//										meshGenerator.generateChunk(
//											cornerDensityCache.getCornerDensityCache(),
//											new byte[]{meshSizeX, meshSizeY, meshSizeZ}
//										)
//									);
//								}
//								MeshRenderer.renderMesh(
//									chunkRender,
//									generator,
//									compiledChunk,
//									chunkRenderPos,
//									chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
//									chunkRenderCache,
//									stateCache,
//									blockRendererDispatcher,
//									random,
//									lazyPackedLightCache,
//									lazyBlockColorCache,
//									mesh,
//									smoothableCache,
//									pooledMutableBlockPos, texturePooledMutableBlockPos, usedBlockRenderLayers,
//									true,
//									true, false
//								);
//							}
//						}
//						break;
//					case TOGETHER:
//						final IsSmoothable isSmoothable = LEAVES_SMOOTHABLE;
//						// FIXME: Why is it like this... why
//						try (
////							ModProfiler ignored = ModProfiler.get().start("renderLeavesTogether");
//							SmoothableCache smoothableCache = CacheUtil.generateSmoothableCache(
//								// Density Cache needs 1 extra on each negative axis
//								chunkRenderPosX - 1, chunkRenderPosY - 1, chunkRenderPosZ - 1,
//								// Density Cache uses 1 extra from the smoothable cache on each positive axis
//								chunkRenderPosX + meshSizeX, chunkRenderPosY + meshSizeY, chunkRenderPosZ + meshSizeZ,
//								1, 1, 1,
//								stateCache, isSmoothable
//							);
//							CornerDensityCache cornerDensityCache = CacheUtil.generateCornerDensityCache(
//								// Density Cache needs 1 extra on each negative axis
//								chunkRenderPosX - 1, chunkRenderPosY - 1, chunkRenderPosZ - 1,
//								// FIXME: Why? Why? Why? It makes no sense
//								chunkRenderPosX + meshSizeX - 1, chunkRenderPosY + meshSizeY - 1, chunkRenderPosZ + meshSizeZ - 1,
//								1, 1, 1,
//								stateCache, smoothableCache
//							)
//						) {
//							final HashMap<Vec3b, FaceList> mesh;
//							if (Config.leavesMeshGenerator == MeshGeneratorType.OldNoCubes) {
//								// TODO: Remove
//								mesh = OldNoCubes.generateChunk(chunkRenderPos, chunkRenderCache, isSmoothable, pooledMutableBlockPos);
//							} else {
//								mesh = MeshDispatcher.offsetChunkMesh(
//									chunkRenderPos,
//									meshGenerator.generateChunk(
//										cornerDensityCache.getCornerDensityCache(),
//										new byte[]{meshSizeX, meshSizeY, meshSizeZ}
//									)
//								);
//							}
//							MeshRenderer.renderMesh(
//								chunkRender,
//								generator,
//								compiledChunk,
//								chunkRenderPos,
//								chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
//								chunkRenderCache,
//								stateCache,
//								blockRendererDispatcher,
//								random,
//								lazyPackedLightCache,
//								lazyBlockColorCache,
//								mesh,
//								smoothableCache,
//								pooledMutableBlockPos, texturePooledMutableBlockPos, usedBlockRenderLayers,
//								true,
//								true, false
//							);
//						}
//						break;
//				}
//			}
//		} finally {
//			texturePooledMutableBlockPos.release();
//		}
//	}

	public static void renderSmoothBlockDamage(final Tessellator tessellatorIn, final BufferBuilder bufferBuilderIn, final BlockPos blockpos, final IBlockState iblockstate, final IBlockAccess world, final TextureAtlasSprite textureatlassprite) {
		if (iblockstate.getRenderType() != EnumBlockRenderType.MODEL) {
			return;
		}

		final IsSmoothable isSmoothable;
		final MeshGenerator generator;
		if (Config.renderSmoothTerrain && TERRAIN_SMOOTHABLE.test(iblockstate)) {
			isSmoothable = TERRAIN_SMOOTHABLE;
			generator = Config.terrainMeshGenerator.getMeshGenerator();
		} else if (Config.renderSmoothLeaves && LEAVES_SMOOTHABLE.test(iblockstate)) {
			isSmoothable = LEAVES_SMOOTHABLE;
			generator = Config.leavesMeshGenerator.getMeshGenerator();
		} else
			return;

		// Draw tessellator and start again with color
		tessellatorIn.draw();
		bufferBuilderIn.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		BlockPos start = blockpos.subtract(generator.getNegativeAreaExtension());
		// I'm not going to think about why this needs +2 instead of +1 lest I go insane
		try (Area area = new Area(world, start, start.add(2, 2, 2).add(generator.getPositiveAreaExtension()))) {
			float minU = UVHelper.getMinU(textureatlassprite);
			float maxU = UVHelper.getMaxU(textureatlassprite);
			float minV = UVHelper.getMinV(textureatlassprite);
			float maxV = UVHelper.getMaxV(textureatlassprite);
			final int packed = iblockstate.getPackedLightmapCoords(world, blockpos);
			int lightmapSkyLight = (packed >> 16) & 0xFFFF;
			int lightmapBlockLight = packed & 0xFFFF;

			generator.generate(area, isSmoothable, (pos, face) -> {
				double x = start.getX();
				double y = start.getY();
				double z = start.getZ();
				Vec v0 = face.v0;
				Vec v1 = face.v1;
				Vec v2 = face.v2;
				Vec v3 = face.v3;
				bufferBuilderIn.pos(x + v0.x, y + v0.y, z + v0.z).color(0xFF, 0xFF, 0xFF, 0xFF).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilderIn.pos(x + v1.x, y + v1.y, z + v1.z).color(0xFF, 0xFF, 0xFF, 0xFF).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilderIn.pos(x + v2.x, y + v2.y, z + v2.z).color(0xFF, 0xFF, 0xFF, 0xFF).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilderIn.pos(x + v3.x, y + v3.y, z + v3.z).color(0xFF, 0xFF, 0xFF, 0xFF).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				return true;
			});
		}

		// Draw tessellator and start again without color
		tessellatorIn.draw();
		bufferBuilderIn.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		bufferBuilderIn.noColor();
	}

//	public static void renderMesh(
//		final RenderChunk chunkRender,
//		final ChunkCompileTaskGenerator chunkRenderTask,
//		final CompiledChunk compiledChunk,
//		final BlockPos chunkRenderPos,
//		final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
//		final IBlockAccess reader,
//		final StateCache stateCache,
//		final BlockRendererDispatcher blockRendererDispatcher,
//		final Random random,
//		final LightCache lazyPackedLightCache,
//		final LazyBlockColorCache lazyBlockColorCache,
//		final SmoothableCache smoothableCache,
//		final PooledMutableBlockPos pooledMutableBlockPos,
//		final PooledMutableBlockPos texturePooledMutableBlockPos,
//		final boolean[] usedBlockRenderLayers,
//		final boolean renderOppositeSides,
//		final boolean tryForBetterTexturesSnow, final boolean tryForBetterTexturesGrass
//	) {
//		final Map<IRegistryDelegate<Block>, IBlockColor> blockColorsRegistry = ClientUtil.getBlockColorsMap();
//
//		final IBlockState textureState = ClientUtil.getTexturePosAndState(
//			chunkPos, relativePos, texturePooledMutableBlockPos,
//			stateCache, smoothableCache,
//			tryForBetterTexturesSnow, tryForBetterTexturesGrass
//		);
//
//		try {
//			renderFaces(
//				chunkRender, chunkRenderTask, compiledChunk, chunkRenderPos,
//				chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
//				reader, blockRendererDispatcher, random,
//				usedBlockRenderLayers,
//				lazyPackedLightCache, lazyBlockColorCache,
//				blockColorsRegistry,
//				pos, faces,
//				pooledMutableBlockPos,
//				texturePooledMutableBlockPos, textureState,
//				renderOppositeSides
//			);
//		} catch (Exception e) {
//			final CrashReport crashReport = CrashReport.makeCrashReport(e, "Rendering faces for smooth block in world");
//
//			CrashReportCategory realBlockCrashReportCategory = crashReport.makeCategory("Block being rendered");
//			final BlockPos blockPos = new BlockPos(chunkRenderPosX + pos.x, chunkRenderPosX + pos.y, chunkRenderPosX + pos.z);
//			CrashReportCategory.addBlockInfo(realBlockCrashReportCategory, blockPos, reader.getBlockState(new BlockPos(initialPosX, initialPosY, initialPosZ)));
//
//			CrashReportCategory textureBlockCrashReportCategory = crashReport.makeCategory("TextureBlock of Block being rendered");
//			CrashReportCategory.addBlockInfo(textureBlockCrashReportCategory, texturePooledMutableBlockPos.toImmutable(), textureState);
//
//			throw new ReportedException(crashReport);
//		}
//
//	}
//
//	public static void renderFaces(
//		final RenderChunk chunkRender,
//		final ChunkCompileTaskGenerator chunkRenderTask,
//		final CompiledChunk compiledChunk,
//		final BlockPos chunkRenderPos,
//		final IBlockAccess reader,
//		final BlockRendererDispatcher blockRendererDispatcher,
//		final Random random,
//		final boolean[] usedBlockRenderLayers,
//		final LightCache lazyPackedLightCache,
//		final LazyBlockColorCache lazyBlockColorCache,
//		final Map<IRegistryDelegate<Block>, IBlockColor> blockColorsRegistry,
//		final PooledMutableBlockPos pooledMutableBlockPos,
//		final BlockPos texturePos,
//		final IBlockState textureState,
//		final boolean renderOppositeSides
//	) {
////		final IModelData modelData = chunkRenderTask.getModelData(texturePos);
//		final long posRand = MathHelper.getPositionRandom(texturePos);
//
//		final boolean applyDiffuseLighting = Config.applyDiffuseLighting;
//		final boolean shortGrass = Config.shortGrass;
//
//		final boolean colorsCacheApplicableToTextureState = lazyBlockColorCache.shouldApply.test(textureState);
//
//		final int[] lazyBlockColorCacheCache = lazyBlockColorCache.cache;
//		final int lazyBlockColorCacheSizeX = lazyBlockColorCache.sizeX;
//		final int lazyBlockColorCacheSizeY = lazyBlockColorCache.sizeY;
//		final int biomeBlendRadius = 3;
//		final int d = biomeBlendRadius * 2 + 1;
//		final int lazyBlockColorCacheArea = d * d;
//		final int lazyBlockColorCacheMax = biomeBlendRadius + 1;
//		final ColorResolver colorResolver = lazyBlockColorCache.colorResolver;
//
//		// TODO: Only get if required (on first use)
//		final IBlockColor textureColorGetter = blockColorsRegistry.get(textureState.getBlock().delegate);
//		final boolean textureColorGetterIsNonNull = textureColorGetter != null;
//
//		for (int faceIndex = 0, facesSize = faces.size(); faceIndex < facesSize; ++faceIndex) {
//			try (Face face = faces.get(faceIndex)) {
//				//0 3
//				//1 2
//				try (
//					//south east when looking down onto up face
//					final Vec3 v0 = face.getVertex0();
//					//north east when looking down onto up face
//					final Vec3 v1 = face.getVertex1();
//					//north west when looking down onto up face
//					final Vec3 v2 = face.getVertex2();
//					//south west when looking down onto up face
//					final Vec3 v3 = face.getVertex3()
//				) {
//
//					float diffuse0;
//					float diffuse1;
//					float diffuse2;
//					float diffuse3;
//
//					if (!applyDiffuseLighting) {
//						diffuse0 = diffuse1 = diffuse2 = diffuse3 = 1;
//					} else {
//						profiler.end(); // HACKY
//						profiler.start("calculateDiffuseLighting");
//						diffuse0 = diffuseLight(toSide(
//							v0.x - chunkRenderPosX - pos.x,
//							v0.y - chunkRenderPosY - pos.y,
//							v0.z - chunkRenderPosZ - pos.z
//						));
//						diffuse1 = diffuseLight(toSide(
//							v1.x - chunkRenderPosX - pos.x,
//							v1.y - chunkRenderPosY - pos.y,
//							v1.z - chunkRenderPosZ - pos.z
//						));
//						diffuse2 = diffuseLight(toSide(
//							v2.x - chunkRenderPosX - pos.x,
//							v2.y - chunkRenderPosY - pos.y,
//							v2.z - chunkRenderPosZ - pos.z
//						));
//						diffuse3 = diffuseLight(toSide(
//							v3.x - chunkRenderPosX - pos.x,
//							v3.y - chunkRenderPosY - pos.y,
//							v3.z - chunkRenderPosZ - pos.z
//						));
//						profiler.end();
//						profiler.start("renderMesh"); // HACKY
//					}
//
//					final int lightmapSkyLight0;
//					final int lightmapSkyLight1;
//					final int lightmapSkyLight2;
//					final int lightmapSkyLight3;
//
//					final int lightmapBlockLight0;
//					final int lightmapBlockLight1;
//					final int lightmapBlockLight2;
//					final int lightmapBlockLight3;
//
//					profiler.end(); // HACKY
//					try (final LightmapInfo lightmapInfo = LightmapInfo.generateLightmapInfo(lazyPackedLightCache, v0, v1, v2, v3, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, pooledMutableBlockPos)) {
//
//						lightmapSkyLight0 = lightmapInfo.skylight0;
//						lightmapSkyLight1 = lightmapInfo.skylight1;
//						lightmapSkyLight2 = lightmapInfo.skylight2;
//						lightmapSkyLight3 = lightmapInfo.skylight3;
//
//						lightmapBlockLight0 = lightmapInfo.blocklight0;
//						lightmapBlockLight1 = lightmapInfo.blocklight1;
//						lightmapBlockLight2 = lightmapInfo.blocklight2;
//						lightmapBlockLight3 = lightmapInfo.blocklight3;
//
//					}
//					profiler.start("renderMesh"); // HACKY
//
//					boolean hasSetColors = false;
//					float colorRed0 = -1;
//					float colorGreen0 = -1;
//					float colorBlue0 = -1;
//					float colorRed1 = -1;
//					float colorGreen1 = -1;
//					float colorBlue1 = -1;
//					float colorRed2 = -1;
//					float colorGreen2 = -1;
//					float colorBlue2 = -1;
//					float colorRed3 = -1;
//					float colorGreen3 = -1;
//					float colorBlue3 = -1;
//
//					if (shortGrass) {
//						profiler.end(); // HACKY
//						profiler.start("shortGrass");
//						if (textureState == StateHolder.GRASS_BLOCK_DEFAULT && areVerticesCloseToFlat(v0, v1, v2, v3)) {
//							renderShortGrass(
//								chunkRender, chunkRenderTask, compiledChunk, chunkRenderPos,
//								chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
//								reader,
//								blockRendererDispatcher,
//								random,
//								usedBlockRenderLayers,
//								pooledMutableBlockPos,
//								texturePos,
//								v0, v1, v2, v3,
//								lightmapSkyLight0, lightmapSkyLight1, lightmapSkyLight2, lightmapSkyLight3,
//								lightmapBlockLight0, lightmapBlockLight1, lightmapBlockLight2, lightmapBlockLight3
//							);
//						}
//						profiler.end();
//						profiler.start("renderMesh"); // HACKY
//					}
//
//					for (int i = 0; i < BLOCK_RENDER_LAYER_VALUES_LENGTH; ++i) {
//						final BlockRenderLayer initialBlockRenderLayer = BLOCK_RENDER_LAYER_VALUES[i];
//						if (!textureState.getBlock().canRenderInLayer(textureState, initialBlockRenderLayer)) {
//							continue;
//						}
//						final BlockRenderLayer correctedBlockRenderLayer = ClientUtil.getCorrectRenderLayer(initialBlockRenderLayer);
//						final int correctedBlockRenderLayerOrdinal = correctedBlockRenderLayer.ordinal();
//						ForgeHooksClient.setRenderLayer(correctedBlockRenderLayer);
//
//						final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(chunkRenderTask, correctedBlockRenderLayerOrdinal, compiledChunk, correctedBlockRenderLayer, chunkRender, chunkRenderPos);
//
//						boolean wasAnythingRendered = false;
//
//						OptiFineCompatibility.PROXY.pushShaderThing(textureState, texturePos, reader, bufferBuilder);
//						try {
//
//							List<BakedQuad> quads;
//							try (ModProfiler ignored1 = profiler.start("getQuads")) {
//								random.setSeed(posRand);
//								quads = ModelHelper.getQuads(textureState, texturePos, bufferBuilder, reader, blockRendererDispatcher, /*modelData, random,*/ posRand, correctedBlockRenderLayer);
//								if (quads == null) {
//									LOGGER.warn("Got null quads for " + textureState.getBlock() + " at " + texturePos);
//									quads = new ArrayList<>();
//									quads.add(blockRendererDispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(null, DOWN, posRand /* random, modelData*/).get(0));
//								}
//							}
//
//							for (int quadIndex = 0, quadsSize = quads.size(); quadIndex < quadsSize; ++quadIndex) {
//								final BakedQuad quad = quads.get(quadIndex);
//
//								wasAnythingRendered = true;
//
//								final int formatSize = quad.getFormat().getIntegerSize();
//								final int[] vertexData = quad.getVertexData();
//
//								final float v0u;
//								final float v0v;
//								final float v1u;
//								final float v1v;
//								final float v2u;
//								final float v2v;
//								final float v3u;
//								final float v3v;
//
//								try (ModProfiler ignored1 = profiler.start("getUVs")) {
//									// Quads are packed xyz|argb|u|v|ts
//									v0u = Float.intBitsToFloat(vertexData[4]);
//									v0v = Float.intBitsToFloat(vertexData[5]);
//									v1u = Float.intBitsToFloat(vertexData[formatSize + 4]);
//									v1v = Float.intBitsToFloat(vertexData[formatSize + 5]);
//									v2u = Float.intBitsToFloat(vertexData[formatSize * 2 + 4]);
//									v2v = Float.intBitsToFloat(vertexData[formatSize * 2 + 5]);
//									v3u = Float.intBitsToFloat(vertexData[formatSize * 3 + 4]);
//									v3v = Float.intBitsToFloat(vertexData[formatSize * 3 + 5]);
//								}
//
//								final int quadPackedLight0 = vertexData[6];
//								final int quadPackedLight1 = vertexData[formatSize + 6];
//								final int quadPackedLight2 = vertexData[formatSize * 2 + 6];
//								final int quadPackedLight3 = vertexData[formatSize * 3 + 6];
//
//								final int quadSkyLight0 = (quadPackedLight0 >> 16) & 0xFF;
//								final int quadSkyLight1 = (quadPackedLight1 >> 16) & 0xFF;
//								final int quadSkyLight2 = (quadPackedLight2 >> 16) & 0xFF;
//								final int quadSkyLight3 = (quadPackedLight3 >> 16) & 0xFF;
//								final int quadBlockLight0 = quadPackedLight0 & 0xFF;
//								final int quadBlockLight1 = quadPackedLight1 & 0xFF;
//								final int quadBlockLight2 = quadPackedLight2 & 0xFF;
//								final int quadBlockLight3 = quadPackedLight3 & 0xFF;
//
//								final float red0;
//								final float green0;
//								final float blue0;
//								final float red1;
//								final float green1;
//								final float blue1;
//								final float red2;
//								final float green2;
//								final float blue2;
//								final float red3;
//								final float green3;
//								final float blue3;
//
//								final boolean hasTintIndex = quad.hasTintIndex();
//								if (BlockColorInfo.rainbow || BlockColorInfo.black) {
//									if (!hasSetColors) {
//										profiler.end(); // HACKY
//										try (
//											final ModProfiler ignored = ModProfiler.get().start("generateBlockColorInfo");
//											final BlockColorInfo blockColorInfo = BlockColorInfo.generateBlockColorInfo(lazyBlockColorCache, v0, v1, v2, v3, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, lazyBlockColorCacheCache, lazyBlockColorCacheSizeX, lazyBlockColorCacheSizeY, biomeBlendRadius, lazyBlockColorCacheArea, lazyBlockColorCacheMax, reader, colorResolver, true, pooledMutableBlockPos)
//										) {
//											colorRed0 = blockColorInfo.red0;
//											colorGreen0 = blockColorInfo.green0;
//											colorBlue0 = blockColorInfo.blue0;
//											colorRed1 = blockColorInfo.red1;
//											colorGreen1 = blockColorInfo.green1;
//											colorBlue1 = blockColorInfo.blue1;
//											colorRed2 = blockColorInfo.red2;
//											colorGreen2 = blockColorInfo.green2;
//											colorBlue2 = blockColorInfo.blue2;
//											colorRed3 = blockColorInfo.red3;
//											colorGreen3 = blockColorInfo.green3;
//											colorBlue3 = blockColorInfo.blue3;
//										}
//										hasSetColors = true;
//										profiler.start("renderMesh"); // HACKY
//									}
//									red0 = colorRed0;
//									green0 = colorGreen0;
//									blue0 = colorBlue0;
//									red1 = colorRed1;
//									green1 = colorGreen1;
//									blue1 = colorBlue1;
//									red2 = colorRed2;
//									green2 = colorGreen2;
//									blue2 = colorBlue2;
//									red3 = colorRed3;
//									green3 = colorGreen3;
//									blue3 = colorBlue3;
//								} else if (hasTintIndex) {
//									if (colorsCacheApplicableToTextureState) {
//										if (!hasSetColors) {
//											profiler.end(); // HACKY
//											try (
//												final ModProfiler ignored = ModProfiler.get().start("generateBlockColorInfo");
//												final BlockColorInfo blockColorInfo = BlockColorInfo.generateBlockColorInfo(lazyBlockColorCache, v0, v1, v2, v3, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, lazyBlockColorCacheCache, lazyBlockColorCacheSizeX, lazyBlockColorCacheSizeY, biomeBlendRadius, lazyBlockColorCacheArea, lazyBlockColorCacheMax, reader, colorResolver, true, pooledMutableBlockPos)
//											) {
//												colorRed0 = blockColorInfo.red0;
//												colorGreen0 = blockColorInfo.green0;
//												colorBlue0 = blockColorInfo.blue0;
//												colorRed1 = blockColorInfo.red1;
//												colorGreen1 = blockColorInfo.green1;
//												colorBlue1 = blockColorInfo.blue1;
//												colorRed2 = blockColorInfo.red2;
//												colorGreen2 = blockColorInfo.green2;
//												colorBlue2 = blockColorInfo.blue2;
//												colorRed3 = blockColorInfo.red3;
//												colorGreen3 = blockColorInfo.green3;
//												colorBlue3 = blockColorInfo.blue3;
//											}
//											hasSetColors = true;
//											profiler.start("renderMesh"); // HACKY
//										}
//										red0 = colorRed0;
//										green0 = colorGreen0;
//										blue0 = colorBlue0;
//										red1 = colorRed1;
//										green1 = colorGreen1;
//										blue1 = colorBlue1;
//										red2 = colorRed2;
//										green2 = colorGreen2;
//										blue2 = colorBlue2;
//										red3 = colorRed3;
//										green3 = colorGreen3;
//										blue3 = colorBlue3;
//									} else { // don't use cache
//										if (textureColorGetterIsNonNull) {
//											final int tintIndex = quad.getTintIndex();
//											profiler.end(); // HACKY
//											try (
//												final ModProfiler ignored = ModProfiler.get().start("generateBlockColorInfo");
//												final BlockColorInfo blockColorInfo = BlockColorInfo.generateBlockColorInfo(
//													lazyBlockColorCache, v0, v1, v2, v3,
//													chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
//													lazyBlockColorCacheCache,
//													lazyBlockColorCacheSizeX, lazyBlockColorCacheSizeY,
//													biomeBlendRadius, lazyBlockColorCacheArea, lazyBlockColorCacheMax,
//													reader,
//													(ignored1, colorPos) -> textureColorGetter.colorMultiplier(textureState, reader, colorPos, tintIndex),
//													false,
//													pooledMutableBlockPos
//												)
//											) {
//												red0 = blockColorInfo.red0;
//												green0 = blockColorInfo.green0;
//												blue0 = blockColorInfo.blue0;
//												red1 = blockColorInfo.red1;
//												green1 = blockColorInfo.green1;
//												blue1 = blockColorInfo.blue1;
//												red2 = blockColorInfo.red2;
//												green2 = blockColorInfo.green2;
//												blue2 = blockColorInfo.blue2;
//												red3 = blockColorInfo.red3;
//												green3 = blockColorInfo.green3;
//												blue3 = blockColorInfo.blue3;
//											}
//											profiler.start("renderMesh"); // HACKY
//										} else {
//											red0 = 1F;
//											green0 = 1F;
//											blue0 = 1F;
//											red1 = 1F;
//											green1 = 1F;
//											blue1 = 1F;
//											red2 = 1F;
//											green2 = 1F;
//											blue2 = 1F;
//											red3 = 1F;
//											green3 = 1F;
//											blue3 = 1F;
//										}
//									}
//								} else {
//									red0 = 1F;
//									green0 = 1F;
//									blue0 = 1F;
//									red1 = 1F;
//									green1 = 1F;
//									blue1 = 1F;
//									red2 = 1F;
//									green2 = 1F;
//									blue2 = 1F;
//									red3 = 1F;
//									green3 = 1F;
//									blue3 = 1F;
//								}
//
//								try (final ModProfiler ignored1 = profiler.start("renderSide")) {
//									// TODO use raw puts?
//									bufferBuilder.pos(v0.x, v0.y, v0.z).color(red0 * diffuse0, green0 * diffuse0, blue0 * diffuse0, 1F).tex(v0u, v0v).lightmap((quadSkyLight0 >= lightmapSkyLight0) ? quadSkyLight0 : lightmapSkyLight0, (quadBlockLight0 >= lightmapBlockLight0) ? quadBlockLight0 : lightmapBlockLight0).endVertex();
//									bufferBuilder.pos(v1.x, v1.y, v1.z).color(red1 * diffuse1, green1 * diffuse1, blue1 * diffuse1, 1F).tex(v1u, v1v).lightmap((quadSkyLight1 >= lightmapSkyLight1) ? quadSkyLight1 : lightmapSkyLight1, (quadBlockLight1 >= lightmapBlockLight1) ? quadBlockLight1 : lightmapBlockLight1).endVertex();
//									bufferBuilder.pos(v2.x, v2.y, v2.z).color(red2 * diffuse2, green2 * diffuse2, blue2 * diffuse2, 1F).tex(v2u, v2v).lightmap((quadSkyLight2 >= lightmapSkyLight2) ? quadSkyLight2 : lightmapSkyLight2, (quadBlockLight2 >= lightmapBlockLight2) ? quadBlockLight2 : lightmapBlockLight2).endVertex();
//									bufferBuilder.pos(v3.x, v3.y, v3.z).color(red3 * diffuse3, green3 * diffuse3, blue3 * diffuse3, 1F).tex(v3u, v3v).lightmap((quadSkyLight3 >= lightmapSkyLight3) ? quadSkyLight3 : lightmapSkyLight3, (quadBlockLight3 >= lightmapBlockLight3) ? quadBlockLight3 : lightmapBlockLight3).endVertex();
//								}
//								if (renderOppositeSides) {
//									// TODO use raw puts?
//									try (final ModProfiler ignored1 = profiler.start("renderOppositeSide")) {
//										bufferBuilder.pos(v3.x, v3.y, v3.z).color(red3 * diffuse3, green3 * diffuse3, blue3 * diffuse3, 1F).tex(v0u, v0v).lightmap((quadSkyLight3 >= lightmapSkyLight3) ? quadSkyLight3 : lightmapSkyLight3, (quadBlockLight3 >= lightmapBlockLight3) ? quadBlockLight3 : lightmapBlockLight3).endVertex();
//										bufferBuilder.pos(v2.x, v2.y, v2.z).color(red2 * diffuse2, green2 * diffuse2, blue2 * diffuse2, 1F).tex(v1u, v1v).lightmap((quadSkyLight2 >= lightmapSkyLight2) ? quadSkyLight2 : lightmapSkyLight2, (quadBlockLight2 >= lightmapBlockLight2) ? quadBlockLight2 : lightmapBlockLight2).endVertex();
//										bufferBuilder.pos(v1.x, v1.y, v1.z).color(red1 * diffuse1, green1 * diffuse1, blue1 * diffuse1, 1F).tex(v2u, v2v).lightmap((quadSkyLight1 >= lightmapSkyLight1) ? quadSkyLight1 : lightmapSkyLight1, (quadBlockLight1 >= lightmapBlockLight1) ? quadBlockLight1 : lightmapBlockLight1).endVertex();
//										bufferBuilder.pos(v0.x, v0.y, v0.z).color(red0 * diffuse0, green0 * diffuse0, blue0 * diffuse0, 1F).tex(v3u, v3v).lightmap((quadSkyLight0 >= lightmapSkyLight0) ? quadSkyLight0 : lightmapSkyLight0, (quadBlockLight0 >= lightmapBlockLight0) ? quadBlockLight0 : lightmapBlockLight0).endVertex();
//									}
//								}
//							}
//						} finally {
//							OptiFineCompatibility.PROXY.popShaderThing(bufferBuilder);
//						}
//						usedBlockRenderLayers[correctedBlockRenderLayerOrdinal] |= wasAnythingRendered;
//					}
//				}
//				ForgeHooksClient.setRenderLayer(null);
//			}
//
//		}
//	}


	//TODO: fix bad lighting fix
	private static void renderShortGrass(
		final RenderChunk chunkRender, final ChunkCompileTaskGenerator chunkRenderTask, final CompiledChunk compiledChunk,
		final BlockPos chunkRenderPos,
		final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
		final IBlockAccess reader,
		final BlockRendererDispatcher blockRendererDispatcher,
		final Random random,
		final boolean[] usedBlockRenderLayers,
		final PooledMutableBlockPos pooledMutableBlockPos,
		final BlockPos texturePos,
		final Vec v0, final Vec v1, final Vec v2, final Vec v3,
		final int lightmapSkyLight0, final int lightmapSkyLight1, final int lightmapSkyLight2, final int lightmapSkyLight3,
		final int lightmapBlockLight0, final int lightmapBlockLight1, final int lightmapBlockLight2, final int lightmapBlockLight3
	) {

		final IBlockState grassPlantState = StateHolder.GRASS_PLANT_DEFAULT;

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
		final IBlockState blockStateUp = reader.getBlockState(pooledMutableBlockPos);
		if (blockStateUp == grassPlantState || blockStateUp == StateHolder.TALL_GRASS_PLANT_BOTTOM) {
			return;
		}
		if (blockStateUp.isOpaqueCube(/*reader, pooledMutableBlockPos*/)) {
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
		final int color = BiomeColorHelper.GRASS_COLOR.getColorAtPos(reader.getBiome(texturePos), texturePos);

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

			OptiFineCompatibility.PROXY.pushShaderThing(grassPlantState, texturePos, reader, bufferBuilder);
			try {

				for (int directionIndex = 0; directionIndex < DIRECTION_QUADS_ORDERED_LENGTH; ++directionIndex) {
					final EnumFacing direction = DIRECTION_QUADS_ORDERED[directionIndex];
					random.setSeed(posRand);
					final List<BakedQuad> quads = model.getQuads(grassPlantState, direction, posRand /*random*/);
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
				OptiFineCompatibility.PROXY.popShaderThing(bufferBuilder);
			}
			usedBlockRenderLayers[correctedBlockRenderLayerOrdinal] |= wasAnythingRendered;
		}
		ForgeHooksClient.setRenderLayer(null);
	}

	// Don't need this anymore, can use normal!
	private static boolean areVerticesCloseToFlat(final Vec v0, final Vec v1, final Vec v2, final Vec v3) {
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

	private static EnumFacing toSide(final double x, final double y, final double z) {
		if (Math.abs(x) > Math.abs(y)) {
			if (Math.abs(x) > Math.abs(z))
				return x < 0 ? WEST : EAST;
			else
				return z < 0 ? NORTH : SOUTH;
		} else {
			if (Math.abs(y) > Math.abs(z))
				return y < 0 ? DOWN : UP;
			else
				return z < 0 ? NORTH : SOUTH;
		}
	}

	private static float diffuseLight(final EnumFacing side) {
		if (side == UP) {
			return 1f;
		} else {
			return .97f;
		}
	}

	static final class TextureInfo {
		public float u0;
		public float v0;
		public float u1;
		public float v1;
		public float u2;
		public float v2;
		public float u3;
		public float v3;

		public void unpackFromQuad(BakedQuad quad, int formatSize) {
			final int[] vertexData = quad.getVertexData();
			// Quads are packed xyz|argb|u|v|ts
			u0 = Float.intBitsToFloat(vertexData[4]);
			v0 = Float.intBitsToFloat(vertexData[5]);
			u1 = Float.intBitsToFloat(vertexData[formatSize + 4]);
			v1 = Float.intBitsToFloat(vertexData[formatSize + 5]);
			u2 = Float.intBitsToFloat(vertexData[formatSize * 2 + 4]);
			v2 = Float.intBitsToFloat(vertexData[formatSize * 2 + 5]);
			u3 = Float.intBitsToFloat(vertexData[formatSize * 3 + 4]);
			v3 = Float.intBitsToFloat(vertexData[formatSize * 3 + 5]);
		}

		public void switchForDirection(EnumFacing direction) {
			switch (direction) {
				case NORTH:
				case EAST:
					break;
				case DOWN:
				case SOUTH:
				case WEST: {
					float u0 = this.u0;
					float v0 = this.v0;
					float u1 = this.u1;
					float v1 = this.v1;
					float u2 = this.u2;
					float v2 = this.v2;
					float u3 = this.u3;
					float v3 = this.v3;

					this.u0 = u3;
					this.v0 = v3;
					this.u1 = u0;
					this.v1 = v0;
					this.u2 = u1;
					this.v2 = v1;
					this.u3 = u2;
					this.v3 = v2;
					break;
				}
				case UP: {
					float u0 = this.u0;
					float v0 = this.v0;
					float u1 = this.u1;
					float v1 = this.v1;
					float u2 = this.u2;
					float v2 = this.v2;
					float u3 = this.u3;
					float v3 = this.v3;

					this.u0 = u2;
					this.v0 = v2;
					this.u1 = u3;
					this.v1 = v3;
					this.u2 = u0;
					this.v2 = v0;
					this.u3 = u1;
					this.v3 = v1;
					break;
				}
				default:
					throw new IllegalStateException("Unexpected value: " + direction);
			}
		}

	}

}
