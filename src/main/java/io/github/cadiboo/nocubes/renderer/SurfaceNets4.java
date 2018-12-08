package io.github.cadiboo.nocubes.renderer;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import io.github.cadiboo.nocubes.config.ModConfig;
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

public class SurfaceNets4 {

	public static void renderPre(final RebuildChunkPreEvent event) {

		final ChunkCache cache = event.getChunkCache();
		final BlockPos renderChunkPos = event.getRenderChunkPosition();
		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		final ChunkCompileTaskGenerator generator = event.getGenerator();
		final CompiledChunk compiledchunk = event.getCompiledChunk();
		final RenderChunk renderChunk = event.getRenderChunk();

		final int chunkSize = 16;

		// dims: "A 3D vector of integers representing the resolution of the isosurface". Resolution in our context means size
		final int[] dims = new int[]{chunkSize, chunkSize, chunkSize};
		final int[] startPos = new int[]{renderChunkPos.getX(), renderChunkPos.getY(), renderChunkPos.getZ()};
		final int[] currentPos = new int[3];
		final int[] edgesIThink = new int[]{1, dims[0] + 3, (dims[0] + 3) * (dims[1] + 3)};
		final float[] neighbourDensityGrid = new float[8];
		final float[][] buffer = new float[edgesIThink[2] * 2][3];
		int bufno = 1;

		// "Resize buffer if necessary" is what mikolalysenko said, but Click_Me seems to have removed this code. This is probably because the buffer should never (and actually
		// can't be in java) be resized

		// March over the voxel grid
		for (currentPos[2] = 0; currentPos[2] < (dims[2] + 1); edgesIThink[2] = -edgesIThink[2], ++currentPos[2], bufno ^= 1) {

			// m is the pointer into the buffer we are going to use.
			// "This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(" is what mikolalysenko said, it
			// obviously doesn't apply here
			// The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int m = 1 + ((dims[0] + 3) * (1 + (bufno * (dims[1] + 3))));

			for (currentPos[1] = 0; currentPos[1] < (dims[1] + 1); ++currentPos[1], m += 2) {
				for (currentPos[0] = 0; currentPos[0] < (dims[0] + 1); ++currentPos[0], ++m) {

					// Read in 8 field values around this vertex and store them in an array
					// Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
					int mask = 0b00000000;
					int neighbourIndex = 0;

					for (int z = 0; z < 2; ++z) {
						for (int y = 0; y < 2; ++y) {
							for (int x = 0; x < 2; ++neighbourIndex) {

								final float density = ModUtil.getBlockDensity(new BlockPos(startPos[0] + currentPos[0] + x, startPos[1] + currentPos[1] + y, startPos[2] + currentPos[2] + z), cache);
								neighbourDensityGrid[neighbourIndex] = density;
								int maskPoint = density > 0 ? 1 : 0;
								mask |= maskPoint << neighbourIndex;
								++x;

							}
						}
					}

					// Check for early termination if cell does not intersect boundary
					if ((mask == 0) || (mask == 0xff)) {
						continue;
					}

					// Sum up edge intersections
					final int edge_mask = SurfaceNets.EDGE_TABLE[mask];
					int e_count = 0;
					final float[] vertex = new float[]{0.0F, 0.0F, 0.0F};

					// For every edge of the cube...
					for (int i = 0; i < 12; ++i) {

						// Use edge mask to check if it is crossed
						if ((edge_mask & (1 << i)) == 0) {
							continue;
						}

						// If it did, increment number of edge crossings
						++e_count;

						// Now find the point of intersection
						final int e0 = SurfaceNets.CUBE_EDGES[i << 1]; // Unpack vertices
						final int e1 = SurfaceNets.CUBE_EDGES[(i << 1) + 1];
						final float g0 = neighbourDensityGrid[e0]; // Unpack grid values
						final float g1 = neighbourDensityGrid[e1];
						float t = g0 - g1; // Compute point of intersection
						if (Math.abs(t) > 0.0F) {
							t = g0 / t;
							int j = 0;

							// Interpolate vertices and add up intersections (this can be done without multiplying)
							for (int k = 1; j < 3; k <<= 1) {
								final int a = e0 & k;
								final int b = e1 & k;
								if (a != b) {
									vertex[j] += a != 0 ? 1.0F - t : t;
								} else {
									vertex[j] += a != 0 ? 1.0F : 0.0F;
								}

								++j;
							}

						}
					}

					// Now we just average the edge intersections and add them to coordinate
//					final float s = 1.0F / e_count;
					final float s = ModConfig.getIsosurfaceLevel() / e_count;
					for (int i = 0; i < 3; ++i) {
						vertex[i] = startPos[i] + currentPos[i] + (s * vertex[i]);
					}

//					final int tx = currentPos[0] == 16 ? 0 : currentPos[0];
//					final int ty = currentPos[1] == 16 ? 0 : currentPos[1];
//					final int tz = currentPos[2] == 16 ? 0 : currentPos[2];
//					long i1 = (tx * 3129871) ^ (tz * 116129781L) ^ ty;
//					i1 = (i1 * i1 * 42317861L) + (i1 * 11L);
//					vertex[0] = (float) (vertex[0] - (((((i1 >> 16) & 15L) / 15.0F) - 0.5D) * 0.2D));
//					vertex[1] = (float) (vertex[1] - (((((i1 >> 20) & 15L) / 15.0F) - 1.0D) * 0.2D));
//					vertex[2] = (float) (vertex[2] - (((((i1 >> 24) & 15L) / 15.0F) - 0.5D) * 0.2D));

					// "Add vertex to buffer, store pointer to vertex index in buffer" is what mikolalysenko said, but Click_Me seems to have changed something

					buffer[m] = vertex;

					final BlockPos currentBlockPos = new BlockPos(startPos[0] + currentPos[0], startPos[1] + currentPos[1], startPos[2] + currentPos[2]);
					final IBlockState state = cache.getBlockState(currentBlockPos);

					final BlockRenderLayer blockRenderLayer = state.getBlock().getRenderLayer();
					final BufferBuilder bufferBuilder = generator.getRegionRenderCacheBuilder().getWorldRendererByLayerId(blockRenderLayer.ordinal());

					if (!compiledchunk.isLayerStarted(blockRenderLayer)) {
						compiledchunk.setLayerStarted(blockRenderLayer);
						SurfaceNets3.RenderChunk_preRenderBlocks(renderChunk, bufferBuilder, renderChunkPos);
					}

//					final BufferBuilder bufferBuilder = event.startOrContinueLayer(blockRenderLayer);
//					event.setBlockRenderLayerUsedWithOrOpperation(blockRenderLayer, true);

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

					// Now we need to add faces together, to do this we just loop over 3 basis components
					for (int i = 0; i < 3; ++i) {
						// The first three entries of the edge_mask count the crossings along the edge
						if ((edge_mask & (1 << i)) == 0) {
							continue;
						}

						// i = axes we are point along. iu, iv = orthogonal axes
						final int iu = (i + 1) % 3;
						final int iv = (i + 2) % 3;

						// If we are on a boundary, skip it
						if ((currentPos[iu] == 0) || (currentPos[iv] == 0)) {
							continue;
						}

						// Otherwise, look up adjacent edges in buffer
						final int du = edgesIThink[iu];
						final int dv = edgesIThink[iv];

						final float[] v0 = buffer[m];
						final float[] v1 = buffer[m - du];
						final float[] v2 = buffer[m - du - dv];
						final float[] v3 = buffer[m - dv];

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

	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {
		SurfaceNets.renderLayer(event);
	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {
		SurfaceNets.renderType(event);
	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {
		event.getUsedBlockRenderLayers()[event.getBlockRenderLayer().ordinal()] = true;
		event.setCanceled(ModUtil.shouldSmooth(event.getBlockState()));
	}

	public static void renderPost(final RebuildChunkPostEvent event) {
	}

}
