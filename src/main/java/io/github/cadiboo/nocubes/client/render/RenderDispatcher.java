package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientCacheUtil;
import io.github.cadiboo.nocubes.client.LazyBlockColorCache;
import io.github.cadiboo.nocubes.client.LazyPackedLightCache;
import io.github.cadiboo.nocubes.client.UVHelper;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.mesh.MeshDispatcher;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.IsSmoothable;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;
import io.github.cadiboo.nocubes.util.pooled.cache.DensityCache;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.BiomeColors;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Random;

import static io.github.cadiboo.nocubes.client.ClientUtil.getMeshSizeX;
import static io.github.cadiboo.nocubes.client.ClientUtil.getMeshSizeY;
import static io.github.cadiboo.nocubes.client.ClientUtil.getMeshSizeZ;
import static io.github.cadiboo.nocubes.util.IsSmoothable.LEAVES_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN_SMOOTHABLE;

/**
 * @author Cadiboo
 */
public final class RenderDispatcher {

	public static void renderChunk(
			@Nonnull final ChunkRender chunkRender,
			@Nonnull final BlockPos chunkRenderPos,
			@Nonnull final ChunkRenderTask chunkRenderGenerator,
			@Nonnull final CompiledChunk compiledChunk,
			// Use World for eagerly generated caches
			@Nonnull final IWorld world,
			// Use RenderChunkCache for lazily generated caches
			@Nonnull final IEnviromentBlockReader chunkRenderCache,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final Random random,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher
	) {

		final int chunkRenderPosX = chunkRenderPos.getX();
		final int chunkRenderPosY = chunkRenderPos.getY();
		final int chunkRenderPosZ = chunkRenderPos.getZ();

		// Fluids Cache        | 0, 15   | 16
		// Smoothable Cache    | -1, n+1 | n + 2
		// Density Cache       | -1, n   | n + 1
		// Vertices            | -1, 16  | 18
		// Light Cache         | -2, 17  | 20
		// Color Cache         | -2, 17  | 20

		int stateCachePaddingX = 0;
		int stateCachePaddingY = 0;
		int stateCachePaddingZ = 0;

		int stateCacheEndX = 0;
		int stateCacheEndY = 0;
		int stateCacheEndZ = 0;

		if (Config.renderSmoothTerrain) {
			final MeshGenerator meshGenerator = Config.terrainMeshGenerator.getMeshGenerator();

			// A chunk is 0-15 (so 16). SmoothableCaches need +1 on each positive axis
			stateCacheEndX = Math.max(stateCacheEndX, getMeshSizeX(17, meshGenerator));
			stateCacheEndY = Math.max(stateCacheEndY, getMeshSizeY(17, meshGenerator));
			stateCacheEndZ = Math.max(stateCacheEndZ, getMeshSizeZ(17, meshGenerator));

			// Density caches need 1 block on negative axis
			stateCachePaddingX = Math.max(stateCachePaddingX, 1);
			stateCachePaddingY = Math.max(stateCachePaddingY, 1);
			stateCachePaddingZ = Math.max(stateCachePaddingZ, 1);
		}
		if (Config.renderSmoothLeaves) {
			final MeshGenerator meshGenerator = Config.leavesMeshGenerator.getMeshGenerator();

			// A chunk is 0-15 (so 16). SmoothableCaches need +1 on each positive axis
			stateCacheEndX = Math.max(stateCacheEndX, getMeshSizeX(17, meshGenerator));
			stateCacheEndY = Math.max(stateCacheEndY, getMeshSizeY(17, meshGenerator));
			stateCacheEndZ = Math.max(stateCacheEndZ, getMeshSizeZ(17, meshGenerator));

			// Density caches need 1 block on negative axis
			stateCachePaddingX = Math.max(stateCachePaddingX, 1);
			stateCachePaddingY = Math.max(stateCachePaddingY, 1);
			stateCachePaddingZ = Math.max(stateCachePaddingZ, 1);
		}

		if (Config.renderSmoothTerrain || Config.renderSmoothLeaves) {
			// Texture & light caches needs -2 and +2
			stateCacheEndX = Math.max(stateCacheEndX, 18);
			stateCacheEndY = Math.max(stateCacheEndY, 18);
			stateCacheEndZ = Math.max(stateCacheEndZ, 18);
			stateCachePaddingX = Math.max(stateCachePaddingX, 2);
			stateCachePaddingY = Math.max(stateCachePaddingY, 2);
			stateCachePaddingZ = Math.max(stateCachePaddingZ, 2);
		}

		try (
				PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
				StateCache stateCache = CacheUtil.generateStateCache(
						chunkRenderPosX - stateCachePaddingX, chunkRenderPosY - stateCachePaddingY, chunkRenderPosZ - stateCachePaddingZ,
						chunkRenderPosX + stateCacheEndX, chunkRenderPosY + stateCacheEndY, chunkRenderPosZ + stateCacheEndZ,
						stateCachePaddingX, stateCachePaddingY, stateCachePaddingZ,
						world, pooledMutableBlockPos
				);
				LazyPackedLightCache lazyPackedLightCache = ClientCacheUtil.generateLazyPackedLightCache(
						chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
						stateCache,
						chunkRenderCache
				)
		) {
			if (Config.renderSmoothTerrain) {
				final MeshGenerator meshGenerator = Config.terrainMeshGenerator.getMeshGenerator();
				// TODO FIXME: should be 16
				final byte meshSizeX = getMeshSizeX(17, meshGenerator);
				final byte meshSizeY = getMeshSizeY(17, meshGenerator);
				final byte meshSizeZ = getMeshSizeZ(17, meshGenerator);
				// FIXME: Why is it like this... why
				try (
						SmoothableCache smoothableCache = CacheUtil.generateSmoothableCache(
								// Density Cache needs 1 extra on each negative axis
								chunkRenderPosX - 1, chunkRenderPosY - 1, chunkRenderPosZ - 1,
								// Density Cache uses 1 extra from the smoothable cache on each positive axis
								chunkRenderPosX + meshSizeX, chunkRenderPosY + meshSizeY, chunkRenderPosZ + meshSizeZ,
								1, 1, 1,
								stateCache, TERRAIN_SMOOTHABLE
						);
						DensityCache densityCache = CacheUtil.generateDensityCache(
								// Density Cache needs 1 extra on each negative axis
								chunkRenderPosX - 1, chunkRenderPosY - 1, chunkRenderPosZ - 1,
								// FIXME: Why? Why? Why? It makes no sense
								chunkRenderPosX + meshSizeX - 1, chunkRenderPosY + meshSizeY - 1, chunkRenderPosZ + meshSizeZ - 1,
								1, 1, 1,
								stateCache, smoothableCache
						)
				) {
					// render fluids
					renderTerrainChunk(
							chunkRender, chunkRenderPos, chunkRenderGenerator, compiledChunk, chunkRenderCache, usedBlockRenderLayers, random, blockRendererDispatcher,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							pooledMutableBlockPos, stateCache, lazyPackedLightCache,
							meshGenerator, meshSizeX, meshSizeY, meshSizeZ,
							smoothableCache, densityCache
					);
				}
			} else {
				// render fluids normally
			}
			if (Config.renderSmoothLeaves) {
				final MeshGenerator meshGenerator = Config.leavesMeshGenerator.getMeshGenerator();
				// TODO FIXME: should be 16
				final byte meshSizeX = getMeshSizeX(17, meshGenerator);
				final byte meshSizeY = getMeshSizeY(17, meshGenerator);
				final byte meshSizeZ = getMeshSizeZ(17, meshGenerator);
				renderLeavesChunk(
						chunkRender, chunkRenderPos, chunkRenderGenerator, compiledChunk, chunkRenderCache, usedBlockRenderLayers, random, blockRendererDispatcher,
						chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
						pooledMutableBlockPos, stateCache, lazyPackedLightCache,
						meshGenerator, meshSizeX, meshSizeY, meshSizeZ
				);
			}
		} catch (ReportedException e) {
			throw e;
		} catch (Exception e) {
			CrashReport crashReport = new CrashReport("Error rendering NoCubes chunk!", e);
			crashReport.makeCategory("Rendering chunk");
//			throw new ReportedException(crashReport);
		}
	}

	private static void renderTerrainChunk(
			@Nonnull final ChunkRender renderChunk, @Nonnull final BlockPos chunkRenderPos, @Nonnull final ChunkRenderTask generator, @Nonnull final CompiledChunk compiledChunk, @Nonnull final IEnviromentBlockReader blockAccess, @Nonnull final boolean[] usedBlockRenderLayers, @Nonnull final Random random, @Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
			final PooledMutableBlockPos pooledMutableBlockPos,
			final StateCache stateCache, final LazyPackedLightCache packedLightCache,
			final MeshGenerator meshGenerator,
			final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ,
			final SmoothableCache smoothableCache, final DensityCache densityCache
	) {
		try (
				LazyBlockColorCache blockColorsCache = ClientCacheUtil.generateLazyBlockColorCache(chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, blockAccess, BiomeColors.GRASS_COLOR);
				PooledMutableBlockPos texturePooledMutableBlockPos = PooledMutableBlockPos.retain()
		) {
			final HashMap<Vec3b, FaceList> mesh;
			if (Config.terrainMeshGenerator == MeshGeneratorType.OldNoCubes) {
				mesh = OldNoCubes.generateChunk(chunkRenderPos, blockAccess, TERRAIN_SMOOTHABLE, pooledMutableBlockPos);
			} else {
				mesh = MeshDispatcher.offsetChunkMesh(
						chunkRenderPos,
						meshGenerator.generateChunk(
								densityCache.getDensityCache(),
								new byte[]{meshSizeX, meshSizeY, meshSizeZ}
						)
				);
			}
			MeshRenderer.renderMesh(
					renderChunk,
					generator,
					compiledChunk,
					chunkRenderPos,
					chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
					blockAccess,
					stateCache,
					blockRendererDispatcher,
					random,
					packedLightCache,
					blockColorsCache,
					mesh,
					smoothableCache,
					pooledMutableBlockPos, texturePooledMutableBlockPos, usedBlockRenderLayers,
					false,
					true, true
			);
		}
	}

	private static void renderLeavesChunk(
			@Nonnull final ChunkRender renderChunk, @Nonnull final BlockPos chunkRenderPos, @Nonnull final ChunkRenderTask generator, @Nonnull final CompiledChunk compiledChunk, @Nonnull final IEnviromentBlockReader blockAccess, @Nonnull final boolean[] usedBlockRenderLayers, @Nonnull final Random random, @Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
			final PooledMutableBlockPos pooledMutableBlockPos,
			final StateCache stateCache, final LazyPackedLightCache packedLightCache,
			final MeshGenerator meshGenerator,
			final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ
	) {
		try (
				LazyBlockColorCache blockColorsCache = ClientCacheUtil.generateLazyBlockColorCache(chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, blockAccess, BiomeColors.GRASS_COLOR);
				PooledMutableBlockPos texturePooledMutableBlockPos = PooledMutableBlockPos.retain()
		) {
			switch (Config.smoothLeavesType) {
				case SEPARATE:
					for (final Block smoothableBlock : Config.getLeavesSmoothableBlocks()) {
						final IsSmoothable isSmoothable = (checkState) -> (LEAVES_SMOOTHABLE.apply(checkState) && checkState.getBlock() == smoothableBlock);
						// FIXME: Why is it like this... why
						try (
//								ModProfiler ignored = ModProfiler.get().start("renderLeaves" + smoothableBlock.getRegistryName());
								SmoothableCache smoothableCache = CacheUtil.generateSmoothableCache(
										// Density Cache needs 1 extra on each negative axis
										chunkRenderPosX - 1, chunkRenderPosY - 1, chunkRenderPosZ - 1,
										// Density Cache uses 1 extra from the smoothable cache on each positive axis
										chunkRenderPosX + meshSizeX, chunkRenderPosY + meshSizeY, chunkRenderPosZ + meshSizeZ,
										1, 1, 1,
										stateCache, isSmoothable
								);
								DensityCache densityCache = CacheUtil.generateDensityCache(
										// Density Cache needs 1 extra on each negative axis
										chunkRenderPosX - 1, chunkRenderPosY - 1, chunkRenderPosZ - 1,
										// FIXME: Why? Why? Why? It makes no sense
										chunkRenderPosX + meshSizeX - 1, chunkRenderPosY + meshSizeY - 1, chunkRenderPosZ + meshSizeZ - 1,
										1, 1, 1,
										stateCache, smoothableCache
								)
						) {
							final HashMap<Vec3b, FaceList> mesh;
							if (Config.terrainMeshGenerator == MeshGeneratorType.OldNoCubes) {
								// TODO: Remove
								mesh = OldNoCubes.generateChunk(chunkRenderPos, blockAccess, isSmoothable, pooledMutableBlockPos);
							} else {
								mesh = MeshDispatcher.offsetChunkMesh(
										chunkRenderPos,
										meshGenerator.generateChunk(
												densityCache.getDensityCache(),
												new byte[]{meshSizeX, meshSizeY, meshSizeZ}
										)
								);
							}
							MeshRenderer.renderMesh(
									renderChunk,
									generator,
									compiledChunk,
									chunkRenderPos,
									chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
									blockAccess,
									stateCache,
									blockRendererDispatcher,
									random,
									packedLightCache,
									blockColorsCache,
									mesh,
									smoothableCache,
									pooledMutableBlockPos, texturePooledMutableBlockPos, usedBlockRenderLayers,
									false,
									true, true
							);
						}
					}
					break;
				case TOGETHER:
					final IsSmoothable isSmoothable = LEAVES_SMOOTHABLE;
					// FIXME: Why is it like this... why
					try (
//							ModProfiler ignored = ModProfiler.get().start("renderLeavesTogether");
							SmoothableCache smoothableCache = CacheUtil.generateSmoothableCache(
									// Density Cache needs 1 extra on each negative axis
									chunkRenderPosX - 1, chunkRenderPosY - 1, chunkRenderPosZ - 1,
									// Density Cache uses 1 extra from the smoothable cache on each positive axis
									chunkRenderPosX + meshSizeX, chunkRenderPosY + meshSizeY, chunkRenderPosZ + meshSizeZ,
									1, 1, 1,
									stateCache, isSmoothable
							);
							DensityCache densityCache = CacheUtil.generateDensityCache(
									// Density Cache needs 1 extra on each negative axis
									chunkRenderPosX - 1, chunkRenderPosY - 1, chunkRenderPosZ - 1,
									// FIXME: Why? Why? Why? It makes no sense
									chunkRenderPosX + meshSizeX - 1, chunkRenderPosY + meshSizeY - 1, chunkRenderPosZ + meshSizeZ - 1,
									1, 1, 1,
									stateCache, smoothableCache
							)
					) {
						final HashMap<Vec3b, FaceList> mesh;
						if (Config.terrainMeshGenerator == MeshGeneratorType.OldNoCubes) {
							// TODO: Remove
							mesh = OldNoCubes.generateChunk(chunkRenderPos, blockAccess, isSmoothable, pooledMutableBlockPos);
						} else {
							mesh = MeshDispatcher.offsetChunkMesh(
									chunkRenderPos,
									meshGenerator.generateChunk(
											densityCache.getDensityCache(),
											new byte[]{meshSizeX, meshSizeY, meshSizeZ}
									)
							);
						}
						MeshRenderer.renderMesh(
								renderChunk,
								generator,
								compiledChunk,
								chunkRenderPos,
								chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
								blockAccess,
								stateCache,
								blockRendererDispatcher,
								random,
								packedLightCache,
								blockColorsCache,
								mesh,
								smoothableCache,
								pooledMutableBlockPos, texturePooledMutableBlockPos, usedBlockRenderLayers,
								false,
								true, true
						);
					}
					break;
			}
		}
	}

	public static void renderSmoothBlockDamage(final Tessellator tessellatorIn, final BufferBuilder bufferBuilderIn, final BlockPos blockpos, final BlockState iblockstate, final IEnviromentBlockReader world, final TextureAtlasSprite textureatlassprite) {
		if (iblockstate.getRenderType() == BlockRenderType.MODEL) {
			tessellatorIn.draw();
			bufferBuilderIn.begin(7, DefaultVertexFormats.BLOCK);
			final IsSmoothable isSmoothable;
			final MeshGeneratorType meshGeneratorType;
			if (Config.renderSmoothTerrain && iblockstate.nocubes_isTerrainSmoothable()) {
				isSmoothable = TERRAIN_SMOOTHABLE;
				meshGeneratorType = Config.terrainMeshGenerator;
			} else if (Config.renderSmoothLeaves && iblockstate.nocubes_isLeavesSmoothable()) {
				isSmoothable = LEAVES_SMOOTHABLE;
				meshGeneratorType = Config.leavesMeshGenerator;
			} else {
				return;
			}

			try (FaceList faces = MeshDispatcher.generateBlockMeshOffset(blockpos, world, isSmoothable, meshGeneratorType)) {
				float minU = UVHelper.getMinU(textureatlassprite);
				float maxU = UVHelper.getMaxU(textureatlassprite);
				float minV = UVHelper.getMinV(textureatlassprite);
				float maxV = UVHelper.getMaxV(textureatlassprite);
//				int color = Minecraft.getInstance().getBlockColors().getColor(iblockstate, world, blockpos, -1);
//				float red = (float) (color >> 16 & 255) / 255.0F;
//				float green = (float) (color >> 8 & 255) / 255.0F;
//				float blue = (float) (color & 255) / 255.0F;
				float red = 1.0F;
				float green = 1.0F;
				float blue = 1.0F;
				float alpha = 1.0F;
				final int packed = iblockstate.getPackedLightmapCoords(world, blockpos);
				int lightmapSkyLight = (packed >> 16) & 0xFFFF;
				int lightmapBlockLight = packed & 0xFFFF;
				for (int faceIndex = 0, facesSize = faces.size(); faceIndex < facesSize; ++faceIndex) {
					try (Face face = faces.get(faceIndex)) {
						try (
								Vec3 v0 = face.getVertex0();
								Vec3 v1 = face.getVertex1();
								Vec3 v2 = face.getVertex2();
								Vec3 v3 = face.getVertex3()
						) {
							bufferBuilderIn.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilderIn.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilderIn.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilderIn.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
						}
					}
				}
			}
			tessellatorIn.draw();
			bufferBuilderIn.begin(7, DefaultVertexFormats.BLOCK);
			bufferBuilderIn.noColor();
		}
  }

}
