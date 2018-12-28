package io.github.cadiboo.nocubes.debug.client.render;

import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Implementation of the SurfaceNets algorithm in Minecraft
 *
 * @author Cadiboo
 */
public final class SurfaceNetsChunk {

	private static final int[] CUBE_EDGES = new int[24];
	private static final int[] EDGE_TABLE = new int[256];

	// because the tables are so big we compute them in a static {} instead of hardcoding them
	static {
		generateCubeEdgesTable();
		generateIntersectionTable();
	}

	/**
	 * Utility function to build a table of possible edges for a cube with each
	 * pair of points representing one edge i.e. [0,1,0,2,0,4,...] would be the
	 * edges from points 0 to 1, 0 to 2, and 0 to 4 respectively:
	 *
	 * <pre>
	 *  y         z
	 *  ^        /
	 *  |
	 *    6----7
	 *   /|   /|
	 *  4----5 |
	 *  | 2--|-3
	 *  |/   |/
	 *  0----1   --> x
	 * </pre>
	 */
	private static void generateCubeEdgesTable() {

		//Initialize the cube_edges table
		// This is just the vertex number (number of corners) of each cube
		int cubeEdgesIndex = 0;
		// 8 is the number of corners for a cube
		for (int cubeCornerIndex = 0; cubeCornerIndex < 8; ++cubeCornerIndex) {
			for (int em = 1; em <= 4; em <<= 1) {
				int j = cubeCornerIndex ^ em;
				if (cubeCornerIndex <= j) {
					SurfaceNetsChunk.CUBE_EDGES[cubeEdgesIndex++] = cubeCornerIndex;
					SurfaceNetsChunk.CUBE_EDGES[cubeEdgesIndex++] = j;
				}
			}
		}
	}

	/**
	 * Build an intersection table. This is a 2^(cube config) -> 2^(edge config) map
	 * There is only one entry for each possible cube configuration
	 * and the output is a 12-bit vector enumerating all edges
	 * crossing the 0-level
	 */
	private static void generateIntersectionTable() {

		// nope, I don't understand this either
		// yay, Lookup Tables...
		// Initialize the intersection table.
		// This is a 2^(cube configuration) ->  2^(edge configuration) map
		// There is one entry for each possible cube configuration, and the output is a 12-bit vector enumerating all edges crossing the 0-level.
		for (int edgeTableIndex = 0; edgeTableIndex < 256; ++edgeTableIndex) {
			int em = 0;
			for (int cubeEdgesIndex = 0; cubeEdgesIndex < 24; cubeEdgesIndex += 2) {
				final boolean a = (edgeTableIndex & (1 << CUBE_EDGES[cubeEdgesIndex])) != 0;
				final boolean b = (edgeTableIndex & (1 << CUBE_EDGES[cubeEdgesIndex + 1])) != 0;
				em |= a != b ? 1 << (cubeEdgesIndex >> 1) : 0;
			}
			EDGE_TABLE[edgeTableIndex] = em;
		}

	}

	public static void renderPre(final RebuildChunkPreEvent event) {

		final BlockPos.MutableBlockPos renderChunkPosition = event.getRenderChunkPosition();

		final int[] r = new int[]{1, 19, 361};
		final float[][] buffer = new float[r[2] * 2][3];
		int bufno = 1;
		int bufferIndex = 0;

		for (BlockPos.MutableBlockPos mutableBlockPos : BlockPos.getAllInBoxMutable(renderChunkPosition, renderChunkPosition.add(15, 15, 15))) {

			if (mutableBlockPos.subtract(renderChunkPosition).getZ() == 0) {
				bufno ^= 1;
				bufferIndex = 1 + ((19) * (1 + (bufno * (19))));

			}

		}

	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {

	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {

	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

	}

	@Nullable
	public static Vec3[] getPoints(final BlockPos blockPos, final World world) {
		// D:
		return null;
	}

}
