package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;

/**
 * Implementation of the SurfaceNets algorithm in Minecraft
 *
 * @author Cadiboo
 */
public final class SurfaceNets {

	public static final int[] CUBE_EDGES = new int[24];
	public static final int[] EDGE_TABLE = new int[256];

	// because the tables are so big we compute them in a static {} instead of hardcoding them
	//Precompute edge table, like Paul Bourke does.
	// This saves a bit of time when computing the centroid of each boundary cell
	static {

		//Initialize the cube_edges table
		// This is just the vertex number (number of corners) of each cube
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

		// nope, I don't understand this either
		// yay, Lookup Tables...
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

		ClientUtil.handleTransparentBlocksRenderType(event);

	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

		final ChunkCache cache = event.getChunkCache();
		final float isosurfaceLevel = ModConfig.getIsosurfaceLevel();

		final Vec3[] points = new Vec3[]{
				new Vec3(0.0D, 0.0D, 1.0D),
				new Vec3(1.0D, 0.0D, 1.0D),
				new Vec3(1.0D, 0.0D, 0.0D),
				new Vec3(0.0D, 0.0D, 0.0D),
				new Vec3(0.0D, 1.0D, 1.0D),
				new Vec3(1.0D, 1.0D, 1.0D),
				new Vec3(1.0D, 1.0D, 0.0D),
				new Vec3(0.0D, 1.0D, 0.0D)
		};

		// Read in 8 field values around this vertex and store them in an array
		final float[] neighbourDensities = new float[8];
		// Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
		int neighbourMask = 0; // called cubeIndex by lots of implementation
		{
			final BlockPos.PooledMutableBlockPos mutablePos = BlockPos.PooledMutableBlockPos.retain();
			for (int neighbourIndex = 0; neighbourIndex < 8; ++neighbourIndex) {
				//local variable for speed
				final Vec3 point = points[neighbourIndex];
				mutablePos.setPos(point.xCoord, point.yCoord, point.zCoord);
				final float neighbourDensity = ModUtil.getBlockDensity(mutablePos, cache);
				neighbourDensities[neighbourIndex] = neighbourDensity;
				final boolean neighborIsInsideIsosurface = neighbourDensity > isosurfaceLevel;
				neighbourMask |= neighborIsInsideIsosurface ? 1 << neighbourIndex : 0;
				if (ModConfig.offsetVertices) {
					ModUtil.givePointRoughness(point);
				}
			}
			mutablePos.release();
		}

		

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

	}

}
