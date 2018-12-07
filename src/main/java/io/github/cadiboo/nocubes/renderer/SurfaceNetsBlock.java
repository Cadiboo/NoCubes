package io.github.cadiboo.nocubes.renderer;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class SurfaceNetsBlock {

	public static void renderPre(final RebuildChunkPreEvent event) {

	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {

	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {

	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

		final BlockPos pos = event.getBlockPos();
		final IBlockAccess cache = event.getChunkCache();
		final BlockPos renderChunkPos = event.getRenderChunkPosition();

		final float[] neighbourDensityGrid = new float[8];
		int mask = 0b00000000;
		int neighbourIndex = 0;
		BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.retain();
		try {
			for (int z = 0; z < 2; ++z) {
				for (int y = 0; y < 2; ++y) {
					for (int x = 0; x < 2; ++neighbourIndex) {
						pooledMutableBlockPos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
						final float density = ModUtil.getBlockDensity(pooledMutableBlockPos, cache);
						neighbourDensityGrid[neighbourIndex] = density;
						mask |= (density > 0 ? 1 : 0) << neighbourIndex;
						++x;
					}
				}
			}
		} finally {
			pooledMutableBlockPos.release();
		}

		// Check for early termination if cell does not intersect boundary
		if ((mask == 0b00000000) || (mask == 0b11111111)) {
			return;
		}

		// Sum up edge intersections
		final int edgeMask = SurfaceNets.EDGE_TABLE[mask];
		int edgeCrossingCount = 0;
		final float[] vertex = new float[]{0.0F, 0.0F, 0.0F};

		//For every edge crossing the boundary, create an (n-1) cell.  (Face in 3D)
		//For every face crossing the boundary, create an (n-2) cell. (Edge in 3D)
		//…
		//For every d-dimensional cell, create an (n-d) cell.
		//…
		//For every n-cell, create a vertex.

		// For every edge of the cube...
		for (int cubeEdgeIndex = 0; cubeEdgeIndex < 12; ++cubeEdgeIndex) {

			// Use edge mask to check if it is crossed
			if ((edgeMask & (1 << cubeEdgeIndex)) == 0) {
				continue;
			}

			// If it did, increment number of edge crossings
			++edgeCrossingCount;

			// Now find the point of intersection
			final int cubeEdge0 = SurfaceNets.CUBE_EDGES[cubeEdgeIndex << 1]; // Unpack vertices
			final int cubeEdge1 = SurfaceNets.CUBE_EDGES[(cubeEdgeIndex << 1) + 1];
			final float neighbourDensity0 = neighbourDensityGrid[cubeEdge0]; // Unpack grid values
			final float neighbourDensity1 = neighbourDensityGrid[cubeEdge1];
			float t = neighbourDensity0 - neighbourDensity1; // Compute point of intersection
			if (Math.abs(t) > 0.0F) {
				t = neighbourDensity0 / t;

				int axisIndex = 0;
				// Interpolate vertices and add up intersections (this can be done without multiplying)
				for (int k = 1; axisIndex < 3; k <<= 1) {
					final int a = cubeEdge0 & k;
					final int b = cubeEdge1 & k;
					if (a != b) {
						vertex[axisIndex] += a != 0 ? 1.0F - t : t;
					} else {
						vertex[axisIndex] += a != 0 ? 1.0F : 0.0F;
					}
					++axisIndex;
				}

			}
		}

		// Now we just average the edge intersections and add them to coordinate
		// final float s = 1.0F / edgeCrossingCount;
		final float somethingImportantIDon_tUnderstandThatHasToDoWithMeshExpansion = ModConfig.getIsosurfaceLevel() / edgeCrossingCount;
		vertex[0] = pos.getX() + (somethingImportantIDon_tUnderstandThatHasToDoWithMeshExpansion * vertex[0]);
		vertex[1] = pos.getY() + (somethingImportantIDon_tUnderstandThatHasToDoWithMeshExpansion * vertex[1]);
		vertex[2] = pos.getZ() + (somethingImportantIDon_tUnderstandThatHasToDoWithMeshExpansion * vertex[2]);

		// final int tx = currentPos[0] == 16 ? 0 : currentPos[0];
		// final int ty = currentPos[1] == 16 ? 0 : currentPos[1];
		// final int tz = currentPos[2] == 16 ? 0 : currentPos[2];
		// long i1 = (tx * 3129871) ^ (tz * 116129781L) ^ ty;
		// i1 = (i1 * i1 * 42317861L) + (i1 * 11L);
		// vertex[0] = (float) (vertex[0] - (((((i1 >> 16) & 15L) / 15.0F) - 0.5D) * 0.2D));
		// vertex[1] = (float) (vertex[1] - (((((i1 >> 20) & 15L) / 15.0F) - 1.0D) * 0.2D));
		// vertex[2] = (float) (vertex[2] - (((((i1 >> 24) & 15L) / 15.0F) - 0.5D) * 0.2D));

		int[] currentPos = {pos.getX() - renderChunkPos.getX(), pos.getY() - renderChunkPos.getY(), pos.getZ() - renderChunkPos.getZ()};

		// Now we need to add faces together, to do this we just loop over 3 basis components
		for (int i = 0; i < 3; ++i) {
			// The first three entries of the edge_mask count the crossings along the edge
			if ((edgeMask & (1 << i)) == 0) {
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
//			final int du = edgesIThink[iu];
//			final int dv = edgesIThink[iv];
//
//			final float[] v0 = buffer[m];
//			final float[] v1 = buffer[m - du];
//			final float[] v2 = buffer[m - du - dv];
//			final float[] v3 = buffer[m - dv];
//
//			// Remember to flip orientation depending on the sign of the corner.
//			if ((mask & 1) != 0) {
//				bufferBuilder.pos(v0[0], v0[1], v0[2]).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//				bufferBuilder.pos(v1[0], v1[1], v1[2]).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//				bufferBuilder.pos(v2[0], v2[1], v2[2]).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//				bufferBuilder.pos(v3[0], v3[1], v3[2]).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//			} else {
//				bufferBuilder.pos(v0[0], v0[1], v0[2]).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//				bufferBuilder.pos(v3[0], v3[1], v3[2]).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//				bufferBuilder.pos(v2[0], v2[1], v2[2]).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//				bufferBuilder.pos(v1[0], v1[1], v1[2]).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//			}
		}

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

	}

}
