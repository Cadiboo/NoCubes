package io.github.cadiboo.nocubes.renderer;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;

public class SurfaceNets {

	public static final int[] CUBE_EDGES = new int[24];
	public static final int[] EDGE_TABLE = new int[256];

	//Precompute edge table, like Paul Bourke does.
	// This saves a bit of time when computing the centroid of each boundary cell
	static {

		//Initialize the cube_edges table
		// This is just the vertex number of each cube
		int k = 0;
		for (int i = 0; i < 8; ++i) {
			for (int em = 1; em <= 4; em <<= 1) {
				int j = i ^ em;
				if (i <= j) {
					CUBE_EDGES[k++] = i;
					CUBE_EDGES[k++] = j;
				}
			}
		}

		//Initialize the intersection table.
		//  This is a 2^(cube configuration) ->  2^(edge configuration) map
		//  There is one entry for each possible cube configuration, and the output is a 12-bit vector enumerating all edges crossing the 0-level.

		for (int i = 0; i < 256; ++i) {
			int em = 0;
			for (int j = 0; j < 24; j += 2) {
				final boolean a = (i & (1 << CUBE_EDGES[j])) != 0;
				final boolean b = (i & (1 << CUBE_EDGES[j + 1])) != 0;
				em |= a != b ? 1 << (j >> 1) : 0;
			}
			EDGE_TABLE[i] = em;
		}

	}

	public static void renderPre(final RebuildChunkPreEvent event) {
	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {
	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {
	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {
	}

	public static void renderPost(final RebuildChunkPostEvent event) {
	}

}
