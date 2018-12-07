package io.github.cadiboo.nocubes.renderer;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import io.github.cadiboo.nocubes.util.LightmapInfo;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;

import static io.github.cadiboo.nocubes.renderer.SurfaceNets.CUBE_EDGES;
import static io.github.cadiboo.nocubes.renderer.SurfaceNets.EDGE_TABLE;

public class SurfaceNets5 {

	public static void renderPre(final RebuildChunkPreEvent event) {

		final ChunkCache cache = event.getChunkCache();
		final BlockPos renderChunkPos = event.getRenderChunkPosition();
		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		final ChunkCompileTaskGenerator generator = event.getGenerator();
		final CompiledChunk compiledchunk = event.getCompiledChunk();
		final RenderChunk renderChunk = event.getRenderChunk();

		int[] dims = new int[]{16, 16, 16};
		int[] c = new int[]{renderChunkPos.getX(), renderChunkPos.getY(), renderChunkPos.getZ()};
		int[] x = new int[3];
		int[] r = new int[]{1, dims[0] + 3, (dims[0] + 3) * (dims[1] + 3)};
		float[] grid = new float[8];
		float[][] buffer = new float[r[2] * 2][3];
		int bufno = 1;

		for (x[2] = 0; x[2] < dims[2] + 1; r[2] = -r[2]) {
			int m = 1 + (dims[0] + 3) * (1 + bufno * (dims[1] + 3));

			for (x[1] = 0; x[1] < dims[1] + 1; m += 2) {
				for (x[0] = 0; x[0] < dims[0] + 1; ++m) {
					int mask = 0;
					int g = 0;

					for (int k = 0; k < 2; ++k) {
						for (int j = 0; j < 2; ++j) {
							for (int i = 0; i < 2; ++g) {
								float p = getBlockDensity(c[0] + x[0] + i, c[1] + x[1] + j, c[2] + x[2] + k, cache);
								grid[g] = p;
								mask |= p > 0.0F ? 1 << g : 0;
								++i;
							}
						}
					}

					if (mask != 0 && mask != 255) {

//						Block block = Blocks.air;
//						int meta = 0;
//
//						label368:
//						for (int k = -1; k < 2; ++k) {
//							for (int j = -1; j < 2; ++j) {
//								for (int i = -1; i < 2; ++i) {
//									Block b = cache.getBlock(new BlockPos(c[0] + x[0] + i, c[1] + x[1] + k, c[2] + x[2] + j));
//									if (ModUtil.shouldSmooth(b) && block != Blocks.snow_layer && block != Blocks.grass) {
//										block = b;
//										meta = cache.getBlockMetadata(c[0] + x[0] + i, c[1] + x[1] + k, c[2] + x[2] + j);
//										if (b == Blocks.snow_layer || b == Blocks.grass) {
//											break label368;
//										}
//									}
//								}
//							}
//						}
//
//						int[] br = new int[]{c[0] + x[0], c[1] + x[1] + 1, c[2] + x[2]};
//
//						label594:
//						for (int k = -1; k < 2; ++k) {
//							for (int j = -2; j < 3; ++j) {
//								for (int i = -1; i < 2; ++i) {
//									Block b = cache.getBlock(new BlockPos(c[0] + x[0] + i, c[1] + x[1] + k, c[2] + x[2] + j));
//									if (!b.isOpaqueCube()) {
//										br[0] = c[0] + x[0] + i;
//										br[1] = c[1] + x[1] + k;
//										br[2] = c[2] + x[2] + j;
//										break label594;
//									}
//								}
//							}
//						}

						final BlockPos currentBlockPos = new BlockPos(c[0] + x[0], c[1] + x[1], c[2] + x[2]);
						final IBlockState state = cache.getBlockState(currentBlockPos);
						final BlockRenderLayer blockRenderLayer = state.getBlock().getRenderLayer();
						final BufferBuilder bufferBuilder = generator.getRegionRenderCacheBuilder().getWorldRendererByLayerId(blockRenderLayer.ordinal());
						if (!compiledchunk.isLayerStarted(blockRenderLayer)) {
							compiledchunk.setLayerStarted(blockRenderLayer);
							SurfaceNets3.RenderChunk_preRenderBlocks(renderChunk, bufferBuilder, renderChunkPos);
						}

						final BakedQuad quad = ModUtil.getQuad(state, currentBlockPos, blockRendererDispatcher);
						final TextureAtlasSprite sprite = ModUtil.getSprite(quad);
						if (sprite == null) {
							return;
						}
						final int color = ModUtil.getColor(quad, state, cache, currentBlockPos);
						final int red = (color >> 16) & 255;
						final int green = (color >> 8) & 255;
						final int blue = color & 255;
						final int alpha = 0xFF;

						final double minU = sprite.getMinU();
						final double minV = sprite.getMinV();
						final double maxU = sprite.getMaxU();
						final double maxV = sprite.getMaxV();

						final LightmapInfo lightmapInfo = ModUtil.getLightmapInfo(currentBlockPos, cache);
						final int lightmapSkyLight = lightmapInfo.getLightmapSkyLight();
						final int lightmapBlockLight = lightmapInfo.getLightmapBlockLight();

						int edgemask = EDGE_TABLE[mask];
						int ecount = 0;
						float[] v = new float[]{0.0F, 0.0F, 0.0F};

						// the actual surface net magic
						for (int i = 0; i < 12; ++i) {
							if ((edgemask & 1 << i) != 0) {
								++ecount;
								int e0 = CUBE_EDGES[i << 1];
								int e1 = CUBE_EDGES[(i << 1) + 1];
								float g0 = grid[e0];
								float g1 = grid[e1];
								float t = g0 - g1;
								if (Math.abs(t) > 0.0F) {
									t = g0 / t;
									int j = 0;

									for (int k = 1; j < 3; k <<= 1) {
										int a = e0 & k;
										int b = e1 & k;
										if (a != b) {
											v[j] += a != 0 ? 1.0F - t : t;
										} else {
											v[j] += a != 0 ? 1.0F : 0.0F;
										}

										++j;
									}
								}
							}
						}

						float s = 1.0F / (float) ecount;
						// this looks like its the problem
						for (int i = 0; i < 3; ++i) {
							v[i] = (float) (c[i] + x[i]) + s * v[i];
						}

						// the magic that gives everything a random offset
						int tx = x[0] == 16 ? 0 : x[0];
						int ty = x[1] == 16 ? 0 : x[1];
						int tz = x[2] == 16 ? 0 : x[2];
						long i1 = (long) (tx * 3129871) ^ (long) tz * 116129781L ^ (long) ty;
						i1 = i1 * i1 * 42317861L + i1 * 11L;
						v[0] = (float) ((double) v[0] - ((double) ((float) (i1 >> 16 & 15L) / 15.0F) - 0.5D) * 0.2D);
						v[1] = (float) ((double) v[1] - ((double) ((float) (i1 >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D);
						v[2] = (float) ((double) v[2] - ((double) ((float) (i1 >> 24 & 15L) / 15.0F) - 0.5D) * 0.2D);

						buffer[m] = v;

						for (int i = 0; i < 3; ++i) {
							if ((edgemask & 1 << i) != 0) {
								int iu = (i + 1) % 3;
								int iv = (i + 2) % 3;
								if (x[iu] != 0 && x[iv] != 0) {
									int du = r[iu];
									int dv = r[iv];
//									tess.setBrightness(block.getMixedBrightnessForBlock(Minecraft.getMinecraft().theWorld, br[0], br[1], br[2]));
//									tess.setColorOpaque_I(block.colorMultiplier(cache, c[0] + x[0], c[1] + x[1], c[2] + x[2]));
									float[] v0 = buffer[m];
									float[] v1 = buffer[m - du];
									float[] v2 = buffer[m - du - dv];
									float[] v3 = buffer[m - dv];
//									if ((mask & 1) != 0) {
//										tess.addVertexWithUV((double) v0[0], (double) v0[1], (double) v0[2], tu0, tv1);
//										tess.addVertexWithUV((double) v1[0], (double) v1[1], (double) v1[2], tu1, tv1);
//										tess.addVertexWithUV((double) v2[0], (double) v2[1], (double) v2[2], tu1, tv0);
//										tess.addVertexWithUV((double) v3[0], (double) v3[1], (double) v3[2], tu0, tv0);
//									} else {
//										tess.addVertexWithUV((double) v0[0], (double) v0[1], (double) v0[2], tu0, tv1);
//										tess.addVertexWithUV((double) v3[0], (double) v3[1], (double) v3[2], tu1, tv1);
//										tess.addVertexWithUV((double) v2[0], (double) v2[1], (double) v2[2], tu1, tv0);
//										tess.addVertexWithUV((double) v1[0], (double) v1[1], (double) v1[2], tu0, tv0);
//									}

									// Remember to flip orientation depending on the sign of the corner.
									if ((mask & 1) != 0) {
										bufferBuilder.pos(v0[0], v0[1], v0[2]).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
										bufferBuilder.pos(v1[0], v1[1], v1[2]).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
										bufferBuilder.pos(v2[0], v2[1], v2[2]).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
										bufferBuilder.pos(v3[0], v3[1], v3[2]).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
									} else {
										bufferBuilder.pos(v0[0], v0[1], v0[2]).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
										bufferBuilder.pos(v3[0], v3[1], v3[2]).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
										bufferBuilder.pos(v2[0], v2[1], v2[2]).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
										bufferBuilder.pos(v1[0], v1[1], v1[2]).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
									}

								}
							}
						}
					}

					++x[0];
				}

				++x[1];
			}

			++x[2];
			bufno ^= 1;
		}
	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {
		SurfaceNets.renderLayer(event);
	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {
		SurfaceNets.renderType(event);
	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {
		event.getUsedBlockRenderLayers()[event.getBlockRenderLayer().ordinal()] = true;
	}

	public static void renderPost(final RebuildChunkPostEvent event) {
	}

	private static float getBlockDensity(final int x, final int y, final int z, final ChunkCache cache) {
		return ModUtil.getBlockDensity(new BlockPos(x, y, z), cache);
	}

}