package io.github.cadiboo.nocubes.renderer;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.LightmapInfo;
import io.github.cadiboo.nocubes.util.ModEnums;
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
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCache;

import java.util.ArrayList;

import static io.github.cadiboo.nocubes.renderer.SurfaceNets.CUBE_EDGES;
import static io.github.cadiboo.nocubes.renderer.SurfaceNets.EDGE_TABLE;

public class SurfaceNets4 {

	public static void renderPre(final RebuildChunkPreEvent event) {

		final ChunkCache cache = event.getChunkCache();
		final BlockPos renderChunkPos = event.getRenderChunkPosition();

		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

		final ChunkCompileTaskGenerator generator = event.getGenerator();
		final CompiledChunk compiledchunk = event.getCompiledChunk();
		final RenderChunk renderChunk = event.getRenderChunk();

		final int chunkSize = 16;

		int bufno = 1;
		final ArrayList<Vec3d> buffer = new ArrayList<>();
		final int[] edgesIThink = new int[]{1, chunkSize + 3, (chunkSize + 3) * (chunkSize + 3)};

		// March over the voxel grid (chunk blocks)
		for (BlockPos currentPos : BlockPos.getAllInBox(renderChunkPos, renderChunkPos.add(chunkSize - 1, chunkSize - 1, chunkSize - 1))) {

			if (renderChunkPos.subtract(currentPos).getZ() == 0) {
				edgesIThink[2] = -edgesIThink[2];
			}

			// m is the pointer into the buffer we are going to use.
			// "This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(" is what mikolalysenko said, it
			// obviously doesn't apply here
			// The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int m = 1 + ((chunkSize + 3) * (1 + (bufno * (chunkSize + 3))));

			final float[] neighbourDensityGrid = new float[8];

			// Read in 8 field values around this vertex and store them in an array
			// Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
			int mask = 0b00000000;

			int neighbourIndex = 0;

			computeMask:
			for (BlockPos.MutableBlockPos mutablePos : BlockPos.getAllInBoxMutable(currentPos, currentPos.add(1, 1, 1))) {

				if (mutablePos.equals(currentPos)) {
					continue computeMask;
				}

				final float density = ModUtil.getBlockDensity(mutablePos, cache);
				neighbourDensityGrid[neighbourIndex] = density;
				int maskPoint = density > 0 ? 1 : 0;
				mask |= maskPoint << neighbourIndex;
				neighbourIndex++;
			}

			// Check for early termination if cell does not intersect boundary
			if ((mask == 0b00000000) || (mask == 0b11111111)) {
				continue;
			}

			IBlockState textureState = cache.getBlockState(currentPos);
			BlockPos texturePos = currentPos;
			textureStateAndPos:
			for (BlockPos.MutableBlockPos mutablePos : BlockPos.getAllInBoxMutable(currentPos.add(-1, -1, -1), currentPos.add(1, 1, 1))) {
				final IBlockState tempState = cache.getBlockState(mutablePos);
				if (ModUtil.shouldSmooth(tempState)) {
					textureState = tempState;
					texturePos = mutablePos.toImmutable();
					// favour snow & grass textures (over textures like dirt)
					if ((tempState.getBlock() == Blocks.SNOW_LAYER) || (tempState.getBlock() == Blocks.GRASS)) {
						break textureStateAndPos;
					}
				}
			}

			IBlockState brightnessState = cache.getBlockState(currentPos);
			BlockPos brightnessPos = currentPos;
			if (ModConfig.approximateLightingLevel != ModEnums.EffortLevel.OFF) {
				brightnessStateAndPos:
				for (BlockPos.MutableBlockPos mutablePos : BlockPos.getAllInBoxMutable(currentPos.add(-1, -1, -1), currentPos.add(1, 2, 1))) {
					final IBlockState tempState = cache.getBlockState(mutablePos);
					if (!tempState.isOpaqueCube()) {
						brightnessState = tempState;
						brightnessPos = mutablePos.toImmutable();
						break brightnessStateAndPos;
					}
				}
			}

			// Sum up edge intersections
			final int edge_mask = EDGE_TABLE[mask];
			int e_count = 0;
			final float[] v = new float[]{0, 0, 0};

			// For every edge of the cube...
			for (int edgeIndex = 0; edgeIndex < 12; ++edgeIndex) {

				// Use edge mask to check if it is crossed
				if ((edge_mask & (1 << edgeIndex)) == 0) {
					continue;
				}

				// If it did, increment number of edge crossings
				++e_count;

				// Now find the point of intersection
				final int e0 = CUBE_EDGES[edgeIndex << 1]; // Unpack vertices
				final int e1 = CUBE_EDGES[(edgeIndex << 1) + 1];
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
							v[j] += a != 0 ? 1.0F - t : t;
						} else {
							v[j] += a != 0 ? 1.0F : 0.0F;
						}

						++j;
					}

				}
			}

			// Now we just average the edge intersections and add them to coordinate
			final float s = ModConfig.getIsosurfaceLevel() / e_count;
			for (int i = 0; i < 3; ++i) {
				final int val;
				switch (i) {
					default:
					case 0:
						val = currentPos.getX();
						break;
					case 1:
						val = currentPos.getY();
						break;
					case 2:
						val = currentPos.getZ();
						break;
				}
				v[i] = val + (s * v[i]);
			}

			// changed
			final int tx = renderChunkPos.subtract(currentPos).getX();
			final int ty = renderChunkPos.subtract(currentPos).getY();
			final int tz = renderChunkPos.subtract(currentPos).getZ();
			long i1 = (tx * 3129871) ^ (tz * 116129781L) ^ ty;
			i1 = (i1 * i1 * 42317861L) + (i1 * 11L);
			v[0] = (float) (v[0] - (((((i1 >> 16) & 15L) / 15.0F) - 0.5D) * 0.2D));
			v[1] = (float) (v[1] - (((((i1 >> 20) & 15L) / 15.0F) - 1.0D) * 0.2D));
			v[2] = (float) (v[2] - (((((i1 >> 24) & 15L) / 15.0F) - 0.5D) * 0.2D));

			// "Add vertex to buffer, store pointer to vertex index in buffer" is what mikolalysenko said, but Click_Me seems to have changed something

			buffer.set(m, new Vec3d(v[0], v[1], v[2]));

			final BakedQuad quad = ModUtil.getQuad(textureState, texturePos, blockRendererDispatcher);
			if (quad == null) {
				return;
			}

			final int red;
			final int green;
			final int blue;

			final int color = ModUtil.getColor(quad, textureState, cache, texturePos);
			red = (color >> 16) & 255;
			green = (color >> 8) & 255;
			blue = color & 255;
			final int alpha = color >> 24 & 255;

			final TextureAtlasSprite sprite = ModUtil.getSprite(textureState, texturePos, blockRendererDispatcher);

			if (sprite == null) {
				return;
			}

			final BlockRenderLayer blockRenderLayer = textureState.getBlock().getRenderLayer();
			final BufferBuilder bufferBuilder = generator.getRegionRenderCacheBuilder().getWorldRendererByLayerId(blockRenderLayer.ordinal());

			if (!compiledchunk.isLayerStarted(blockRenderLayer)) {
				compiledchunk.setLayerStarted(blockRenderLayer);
				SurfaceNets3.RenderChunk_preRenderBlocks(renderChunk, bufferBuilder, renderChunkPos);
			}

//					final BufferBuilder bufferBuilder = event.startOrContinueLayer(blockRenderLayer);
//					event.setBlockRenderLayerUsedWithOrOpperation(blockRenderLayer, true);

			final double minU = sprite.getMinU();
			final double maxU = sprite.getMaxU();
			final double minV = sprite.getMinV();
			final double maxV = sprite.getMaxV();

			final LightmapInfo lightmapInfo = ModUtil.getLightmapInfo(brightnessPos, cache);
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

				final int[] currentPosA = {currentPos.getX(), currentPos.getY(), currentPos.getZ()};

				// If we are on a boundary, skip it
				if ((currentPosA[iu] == 0) || (currentPosA[iv] == 0)) {
					continue;
				}

				// Otherwise, look up adjacent edges in buffer
				final int du = edgesIThink[iu];
				final int dv = edgesIThink[iv];

				final Vec3d v0 = buffer.get(m);
				final Vec3d v1 = buffer.get(m - du);
				final Vec3d v2 = buffer.get(m - du - dv);
				final Vec3d v3 = buffer.get(m - dv);

				// Remember to flip orientation depending on the sign of the corner.
				if ((mask & 1) != 0) {
					bufferBuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				} else {
					bufferBuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
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
	}

	public static void renderPost(final RebuildChunkPostEvent event) {
	}

}
