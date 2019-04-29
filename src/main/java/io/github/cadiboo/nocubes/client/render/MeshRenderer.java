package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.LightmapInfo;
import io.github.cadiboo.nocubes.client.ModelHelper;
import io.github.cadiboo.nocubes.client.PackedLightCache;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.mesh.MeshDispatcher;
import io.github.cadiboo.nocubes.util.IIsSmoothable;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IWorldReaderBase;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;

/**
 * @author Cadiboo
 */
public final class MeshRenderer {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void renderChunkMeshes(
			@Nonnull final RenderChunk renderChunk,
			@Nonnull final ChunkRenderTask generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			@Nonnull final IWorldReaderBase blockAccess,
			@Nonnull final StateCache stateCache,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final PackedLightCache pooledPackedLightCache
	) {
//		try (ModProfiler ignored = NoCubes.getProfiler().start("renderChunkMeshes"))
		{
			if (Config.renderSmoothTerrain) {
//				try (ModProfiler ignored1 = NoCubes.getProfiler().start("renderNormalTerrain"))
				{
					renderMesh(
							renderChunk,
							generator,
							compiledChunk,
							renderChunkPosition,
							renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
							blockAccess,
							stateCache,
							blockRendererDispatcher,
							pooledPackedLightCache,
							MeshDispatcher.generateChunkMeshOffset(renderChunkPosition, blockAccess, TERRAIN_SMOOTHABLE, Config.terrainMeshGenerator),
							TERRAIN_SMOOTHABLE,
							pooledMutableBlockPos, usedBlockRenderLayers, false
					);
				}
			}

//			switch (Config.CLIENT.smoothLeavesLevel) {
//				case SEPARATE:
//					try {
//						for (final IBlockState smoothableState : ModConfig.getLeavesSmoothableBlockStatesCache()) {
////							try (ModProfiler ignored2 = NoCubes.getProfiler().start("renderLeaves" + smoothableState))
//							{
//								final IIsSmoothable isSmoothable = (checkState) -> checkState == smoothableState;
//								renderMesh(
//										renderChunk,
//										generator,
//										compiledChunk,
//										renderChunkPosition,
//										renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
//										blockAccess,
//										stateCache,
//										blockRendererDispatcher,
//										pooledPackedLightCache,
//										MeshDispatcher.generateChunkMeshOffset(renderChunkPosition, blockAccess, isSmoothable, ModConfig.leavesMeshGenerator),
//										isSmoothable,
//										pooledMutableBlockPos, usedBlockRenderLayers, true
//								);
//							}
//						}
//					} catch (ConcurrentModificationException e) {
//						//REEE I don't want to synchronise because performance tho
//						e.printStackTrace();
//					}
//					break;
//				case TOGETHER:
////					try (ModProfiler ignored2 = NoCubes.getProfiler().start("renderLeavesTogether"))
//			{
//				renderMesh(
//						renderChunk,
//						generator,
//						compiledChunk,
//						renderChunkPosition,
//						renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ,
//						blockAccess,
//						stateCache,
//						blockRendererDispatcher,
//						pooledPackedLightCache,
////							MeshDispatcher.generateChunkMeshOffset(renderChunkPosition, blockAccess, LEAVES_SMOOTHABLE, ModConfig.leavesMeshGenerator),
//						MeshDispatcher.generateChunkMeshOffset(renderChunkPosition, blockAccess, LEAVES_SMOOTHABLE, MeshGenerator.SurfaceNets),
//						LEAVES_SMOOTHABLE,
//						pooledMutableBlockPos, usedBlockRenderLayers, true
//				);
//				}
//				break;
//				case OFF:
//					break;
//			}
		}

	}

	private static void renderMesh(
			@Nonnull final RenderChunk renderChunk,
			@Nonnull final ChunkRenderTask generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			@Nonnull final IWorldReaderBase blockAccess,
			@Nonnull final StateCache stateCache,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final PackedLightCache pooledPackedLightCache,
			@Nonnull final Map<Vec3b, FaceList> chunkData,
			@Nonnull final IIsSmoothable isStateSmoothable,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final boolean[] usedBlockRenderLayers,
			final boolean renderOppositeSides
	) {

//		try (final ModProfiler profiler = NoCubes.getProfiler().start("renderMesh"))
		{

//			final Random random = new Random();

			for (Map.Entry<Vec3b, FaceList> entry : chunkData.entrySet()) {
				try (final Vec3b pos = entry.getKey()) {
					try (final FaceList faces = entry.getValue()) {

						if (faces.isEmpty()) {
							continue;
						}

						ModProfiler.get().start("prepareRenderFaces");

						final int initialPosX = renderChunkPositionX + pos.x;
						final int initialPosY = renderChunkPositionY + pos.y;
						final int initialPosZ = renderChunkPositionZ + pos.z;

						pooledMutableBlockPos.setPos(
								initialPosX,
								initialPosY,
								initialPosZ
						);

						//TODO use pos?
						final byte relativePosX = ClientUtil.getRelativePos(renderChunkPositionX, initialPosX);
						final byte relativePosY = ClientUtil.getRelativePos(renderChunkPositionY, initialPosY);
						final byte relativePosZ = ClientUtil.getRelativePos(renderChunkPositionZ, initialPosZ);

//					    final IBlockState realState = blockAccess.getBlockState(pooledMutableBlockPos);
						final IBlockState realState = stateCache.getBlockStates()[stateCache.getIndex(relativePosX + 1, relativePosY + 1, relativePosZ + 1)];
						if (blockAccess.getBlockState(pooledMutableBlockPos) != realState) {
							realState.getBlock();
						}

//					    final Tuple<BlockPos, IBlockState> texturePosAndState = ClientUtil.getTexturePosAndState(stateCache, blockAccess, pooledMutableBlockPos, realState, isStateSmoothable, (byte) (relativePosX+ 1), (byte) (relativePosY+ 1), (byte) (relativePosZ+ 1));
						final Tuple<BlockPos, IBlockState> texturePosAndState = ClientUtil.getTexturePosAndState(stateCache, pooledMutableBlockPos, realState, isStateSmoothable, relativePosX, relativePosY, relativePosZ);
						final BlockPos texturePos = texturePosAndState.getA();
						final IBlockState textureState = texturePosAndState.getB();

						ModProfiler.get().end();

						try {
							renderFaces(renderChunk, generator, compiledChunk, renderChunkPosition, renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ, blockAccess, blockRendererDispatcher, usedBlockRenderLayers, pooledPackedLightCache, renderOppositeSides, pos, faces, texturePos, textureState, new Random());
						} catch (Exception e) {
							final CrashReport crashReport = new CrashReport("Rendering faces for smooth block in world", e);

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

	public static void renderFaces(
			@Nonnull final RenderChunk renderChunk,
			@Nonnull final ChunkRenderTask generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkPosition,
			final int renderChunkPositionX, final int renderChunkPositionY, final int renderChunkPositionZ,
			@Nonnull final IWorldReaderBase blockAccess,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final PackedLightCache pooledPackedLightCache,
			final boolean renderOppositeSides,
			@Nonnull final Vec3b pos,
			@Nonnull final FaceList faces,
			@Nonnull final BlockPos texturePos,
			@Nonnull final IBlockState textureState,
			@Nonnull final Random rand
	) {
		try (ModProfiler ignored15 = ModProfiler.get().start("renderFaces")) {
			for (final Face face : faces) {
				try {
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

						for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
							if (!textureState.canRenderInLayer(blockRenderLayer)) {
								continue;
							}
							blockRenderLayer = ClientUtil.getCorrectRenderLayer(blockRenderLayer);
							ForgeHooksClient.setRenderLayer(blockRenderLayer);

							final int blockRenderLayerOrdinal = blockRenderLayer.ordinal();
							final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(generator, blockRenderLayerOrdinal, compiledChunk, blockRenderLayer, renderChunk, renderChunkPosition);

							List<BakedQuad> quads;
							try (ModProfiler ignored = ModProfiler.get().start("getQuads")) {
								quads = ModelHelper.getQuads(textureState, texturePos, bufferBuilder, blockAccess, blockRendererDispatcher, blockRenderLayer);
								if (quads == null) {
									LOGGER.warn("Got null quads for " + textureState.getBlock() + " at " + texturePos);
									quads = new ArrayList<>();
									quads.add(blockRendererDispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(null, EnumFacing.DOWN, rand).get(0));
								}
							}

							boolean wasAnythingRendered = false;

//	                	    try (final ModProfiler ignored = NoCubes.getProfiler().start("renderFaces"))
							{

//		        	            OptiFineCompatibility.pushShaderThing(textureState, texturePos, blockAccess, bufferBuilder);

								float diffuse0;
								float diffuse1;
								float diffuse2;
								float diffuse3;
//							    if (!ModConfig.applyDiffuseLighting || !quad.shouldApplyDiffuseLighting()) {
//							        diffuse0 = diffuse1 = diffuse2 = diffuse3 = 1;
//						        } else
								{
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

								final int lightmapSkyLight0;
								final int lightmapSkyLight1;
								final int lightmapSkyLight2;
								final int lightmapSkyLight3;

								final int lightmapBlockLight0;
								final int lightmapBlockLight1;
								final int lightmapBlockLight2;
								final int lightmapBlockLight3;

//						    if (ModConfig.approximateLighting)
								{
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
//						    } else {
//						    	lightmapSkyLight0 = lightmapSkyLight1 = lightmapSkyLight2 = lightmapSkyLight3 = 240;
//						    	lightmapBlockLight0 = lightmapBlockLight1 = lightmapBlockLight2 = lightmapBlockLight3 = 0;
								}

								for (final BakedQuad quad : quads) {

									wasAnythingRendered = true;

									// Quads are packed xyz|argb|u|v|ts
									ModProfiler.get().start("getUVs");

									final int formatSize = quad.getFormat().getIntegerSize();

									final int[] vertexData = quad.getVertexData();
									final float v0u = Float.intBitsToFloat(vertexData[4]);
									final float v0v = Float.intBitsToFloat(vertexData[5]);
									final float v1u = Float.intBitsToFloat(vertexData[formatSize + 4]);
									final float v1v = Float.intBitsToFloat(vertexData[formatSize + 5]);
									final float v2u = Float.intBitsToFloat(vertexData[formatSize * 2 + 4]);
									final float v2v = Float.intBitsToFloat(vertexData[formatSize * 2 + 5]);
									final float v3u = Float.intBitsToFloat(vertexData[formatSize * 3 + 4]);
									final float v3v = Float.intBitsToFloat(vertexData[formatSize * 3 + 5]);
									ModProfiler.get().end();

									final int color0 = ClientUtil.getColor(quad, textureState, blockAccess, texturePos.south().east());
									final float alpha = 1F;

									final float red0 = ((color0 >> 16) & 255) / 255F;
									final float green0 = ((color0 >> 8) & 255) / 255F;
									final float blue0 = ((color0) & 255) / 255F;
									final float red1;
									final float green1;
									final float blue1;
									final float red2;
									final float green2;
									final float blue2;
									final float red3;
									final float green3;
									final float blue3;
									//TODO: optimise
//									if (smoothBiomeColorTransitions)
									{
										final int color1 = ClientUtil.getColor(quad, textureState, blockAccess, texturePos.east());
										red1 = ((color1 >> 16) & 255) / 255F;
										green1 = ((color1 >> 8) & 255) / 255F;
										blue1 = ((color1) & 255) / 255F;
										final int color2 = ClientUtil.getColor(quad, textureState, blockAccess, texturePos);
										red2 = ((color2 >> 16) & 255) / 255F;
										green2 = ((color2 >> 8) & 255) / 255F;
										blue2 = ((color2) & 255) / 255F;
										final int color3 = ClientUtil.getColor(quad, textureState, blockAccess, texturePos.south());
										red3 = ((color3 >> 16) & 255) / 255F;
										green3 = ((color3 >> 8) & 255) / 255F;
										blue3 = ((color3) & 255) / 255F;
//									} else {
//										red1 = red0;
//										green1 = green0;
//										blue1 = blue0;
//										red2 = red0;
//										green2 = green0;
//										blue2 = blue0;
//										red3 = red0;
//										green3 = green0;
//										blue3 = blue0;
									}

									try (final ModProfiler ignored1 = ModProfiler.get().start("renderSide")) {
										// TODO use raw puts?
										bufferBuilder.pos(v0.x, v0.y, v0.z).color(red0 * diffuse0, green0 * diffuse0, blue0 * diffuse0, alpha).tex(v0u, v0v).lightmap(lightmapSkyLight0, lightmapBlockLight0).endVertex();
										bufferBuilder.pos(v1.x, v1.y, v1.z).color(red1 * diffuse1, green1 * diffuse1, blue1 * diffuse1, alpha).tex(v1u, v1v).lightmap(lightmapSkyLight1, lightmapBlockLight1).endVertex();
										bufferBuilder.pos(v2.x, v2.y, v2.z).color(red2 * diffuse2, green2 * diffuse2, blue2 * diffuse2, alpha).tex(v2u, v2v).lightmap(lightmapSkyLight2, lightmapBlockLight2).endVertex();
										bufferBuilder.pos(v3.x, v3.y, v3.z).color(red3 * diffuse3, green3 * diffuse3, blue3 * diffuse3, alpha).tex(v3u, v3v).lightmap(lightmapSkyLight3, lightmapBlockLight3).endVertex();
									}
									if (renderOppositeSides) {
										// TODO use raw puts?
										try (final ModProfiler ignored1 = ModProfiler.get().start("renderOppositeSide")) {
											bufferBuilder.pos(v3.x, v3.y, v3.z).color(red3 * diffuse3, green3 * diffuse3, blue3 * diffuse3, alpha).tex(v0u, v0v).lightmap(lightmapSkyLight3, lightmapBlockLight3).endVertex();
											bufferBuilder.pos(v2.x, v2.y, v2.z).color(red2 * diffuse2, green2 * diffuse2, blue2 * diffuse2, alpha).tex(v1u, v1v).lightmap(lightmapSkyLight2, lightmapBlockLight2).endVertex();
											bufferBuilder.pos(v1.x, v1.y, v1.z).color(red1 * diffuse1, green1 * diffuse1, blue1 * diffuse1, alpha).tex(v2u, v2v).lightmap(lightmapSkyLight1, lightmapBlockLight1).endVertex();
											bufferBuilder.pos(v0.x, v0.y, v0.z).color(red0 * diffuse0, green0 * diffuse0, blue0 * diffuse0, alpha).tex(v3u, v3v).lightmap(lightmapSkyLight0, lightmapBlockLight0).endVertex();
										}
									}
								}
							}
							usedBlockRenderLayers[blockRenderLayerOrdinal] |= wasAnythingRendered;
						}
					}
					ForgeHooksClient.setRenderLayer(null);
				} finally {
					face.close();
				}

//			OptiFineCompatibility.popShaderThing(bufferBuilder);
			}
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
