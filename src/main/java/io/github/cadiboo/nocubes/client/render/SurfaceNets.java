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
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Implementation of the SurfaceNets algorithm in Minecraft
 *
 * @author Cadiboo
 * @see "https://mikolalysenko.github.io/Isosurface/js/surfacenets.js"
 */
public final class SurfaceNets {

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
					SurfaceNets.CUBE_EDGES[cubeEdgesIndex++] = cubeCornerIndex;
					SurfaceNets.CUBE_EDGES[cubeEdgesIndex++] = j;
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

		final float isoSurfaceLevel = ModConfig.getIsosurfaceLevel();
		final BlockPos renderChunkPos = event.getRenderChunkPosition();
		final RenderChunk renderChunk = event.getRenderChunk();
		final BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
		final BlockPos.PooledMutableBlockPos pooledMutablePos = BlockPos.PooledMutableBlockPos.retain();
		final ChunkCache cache = event.getChunkCache();

//		final int[] dims = {16, 16, 16};
		// make the algorithm look on the sides of chunks aswell
		// I tweaked the loop in Marching cubes, this time I just edited dims
		final int[] dims = {18, 18, 18};
		final int[] c = {renderChunkPos.getX(), renderChunkPos.getY(), renderChunkPos.getZ()};
		final ArrayList<float[]> vertices = new ArrayList<>();

		//Internal buffer, this may get resized at run time
		final int[] buffer;

//			var vertices = []
//    , faces = []
		int n = 0;
		final int[] x = new int[3],
				R = new int[]{1, (dims[0] + 1), (dims[0] + 1) * (dims[1] + 1)};
		final float[] grid = new float[8];
		int buf_no = 1;

		//Resize buffer if necessary
//		if (R[2] * 2 > buffer.length) {
//			buffer = new Int32Array(R[2] * 2);
//		}
		buffer = new int[R[2] * 2];

		//March over the voxel grid
		for (x[2] = 0; x[2] < dims[2] - 1; ++x[2], n += dims[0], buf_no ^= 1, R[2] = -R[2]) {

			//m is the pointer into the buffer we are going to use.
			//This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(
			//The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int m = 1 + (dims[0] + 1) * (1 + buf_no * (dims[1] + 1));

			for (x[1] = 0; x[1] < dims[1] - 1; ++x[1], ++n, m += 2)
				for (x[0] = 0; x[0] < dims[0] - 1; ++x[0], ++n, ++m) {
					pos.setPos(c[0] + x[0], c[1] + x[1], c[2] + x[2]);

					//Read in 8 field values around this vertex and store them in an array
					//Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
					int mask = 0, g = 0, idx = n;
					for (int k = 0; k < 2; ++k, idx += dims[0] * (dims[1] - 2))
						for (int j = 0; j < 2; ++j, idx += dims[0] - 2)
							for (int i = 0; i < 2; ++i, ++g, ++idx) {
								// assuming i = x, j = y, k = z
								pooledMutablePos.setPos(c[0] + x[0] + i, c[1] + x[1] + j, c[2] + x[2] + k);
//								float p = data[idx];
								float p = ModUtil.getBlockDensity(pooledMutablePos, cache);
								grid[g] = p;
								mask |= (p < 0) ? (1 << g) : 0;
							}

					//Check for early termination if cell does not intersect boundary
					if (mask == 0 || mask == 0xFF) {
						continue;
					}

					//Sum up edge intersections
					final int edge_mask = EDGE_TABLE[mask];
					final float[] v = {0, 0, 0};
					int e_count = 0;

					//For every edge of the cube...
					for (int i = 0; i < 12; ++i) {

						//Use edge mask to check if it is crossed
						if ((edge_mask & (1 << i)) == 0) {
							continue;
						}

						//If it did, increment number of edge crossings
						++e_count;

						//Now find the point of intersection
						int e0 = CUBE_EDGES[i << 1]       //Unpack vertices
								, e1 = CUBE_EDGES[(i << 1) + 1];
						float g0 = grid[e0]                 //Unpack grid values
								, g1 = grid[e1], t = g0 - g1;                 //Compute point of intersection
						if (Math.abs(t) > 1e-6) {
							t = g0 / t;
						} else {
							continue;
						}

						//Interpolate vertices and add up intersections (this can be done without multiplying)
						for (int j = 0, k = 1; j < 3; ++j, k <<= 1) {
							int a = e0 & k, b = e1 & k;
							if (a != b) {
								v[j] += a != 0 ? 1.0 - t : t;
							} else {
								v[j] += a != 0 ? 1.0 : 0;
							}
						}
					}

					//Now we just average the edge intersections and add them to coordinate
					float s = isoSurfaceLevel / e_count;
					for (int i = 0; i < 3; ++i) {
						v[i] = c[i] + x[i] + s * v[i];
					}

					//Add vertex to buffer, store pointer to vertex index in buffer
					buffer[m] = vertices.size();
					if(ModConfig.offsetVertices)
						ModUtil.offsetVertex(v);
					vertices.add(v);

					final BlockRenderData renderData = ClientUtil.getBlockRenderData(pos, cache);

					final BlockRenderLayer blockRenderLayer = renderData.getBlockRenderLayer();
					final int red = renderData.getRed();
					final int green = renderData.getGreen();
					final int blue = renderData.getBlue();
					final int alpha = renderData.getAlpha();
					final float minU = renderData.getMinU();
					final float maxU = renderData.getMaxU();
					final float minV = renderData.getMinV();
					final float maxV = renderData.getMaxV();
					final int lightmapSkyLight = renderData.getLightmapSkyLight();
					final int lightmapBlockLight = renderData.getLightmapBlockLight();

					final BufferBuilder bufferBuilder = event.getGenerator().getRegionRenderCacheBuilder().getWorldRendererByLayer(blockRenderLayer);
					final CompiledChunk compiledChunk = event.getCompiledChunk();

					if (!compiledChunk.isLayerStarted(blockRenderLayer)) {
						compiledChunk.setLayerStarted(blockRenderLayer);
						ClientUtil.compiledChunk_setLayerUsed(compiledChunk, blockRenderLayer);
						ClientUtil.renderChunk_preRenderBlocks(renderChunk, bufferBuilder, pos);
					}

					//Now we need to add faces together, to do this we just loop over 3 basis components
					for (int i = 0; i < 3; ++i) {
						//The first three entries of the edge_mask count the crossings along the edge
						if ((edge_mask & (1 << i)) == 0) {
							continue;
						}

						// i = axes we are point along.  iu, iv = orthogonal axes
						int iu = (i + 1) % 3, iv = (i + 2) % 3;

						//If we are on a boundary, skip it
						if (x[iu] == 0 || x[iv] == 0) {
							continue;
						}

						//Otherwise, look up adjacent edges in buffer
						int du = R[iu], dv = R[iv];

						//TODO: remove float[] -> Vec3 -> float shit
						//Remember to flip orientation depending on the sign of the corner.
						//FIXME:  cunt wtf why do I have to swap vertices (First one is CORRECT but doesnt work)
//						if ((mask & 1) != 0) {
						if ((mask & 1) == 0) {
//							faces.add([buffer[m], buffer[m - du], buffer[m - du - dv], buffer[m - dv]]);

							Vec3 vertex0 = new Vec3(vertices.get(buffer[m]));
							Vec3 vertex1 = new Vec3(vertices.get(buffer[m - du]));
							Vec3 vertex2 = new Vec3(vertices.get(buffer[m - du - dv]));
							Vec3 vertex3 = new Vec3(vertices.get(buffer[m - dv]));

							bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

						} else {
//							faces.add([buffer[m], buffer[m - dv], buffer[m - du - dv], buffer[m - du]]);

							Vec3 vertex0 = new Vec3(vertices.get(buffer[m]));
							Vec3 vertex1 = new Vec3(vertices.get(buffer[m - dv]));
							Vec3 vertex2 = new Vec3(vertices.get(buffer[m - du - dv]));
							Vec3 vertex3 = new Vec3(vertices.get(buffer[m - du]));

							bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

						}
					}
				}
		}

		//All done!  Return the result
//		return {vertices:vertices, faces:faces };

	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {

		if (ModUtil.shouldSmooth(event.getBlockState())) {
			event.setResult(Event.Result.DENY);
			event.setCanceled(true);
		}

	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {

		if (ModUtil.shouldSmooth(event.getBlockState())) {
			event.setResult(Event.Result.DENY);
			event.setCanceled(true);
		}

	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

		event.setCanceled(ModUtil.shouldSmooth(event.getBlockState()));

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

	}

	@Nullable
	public static Vec3[] getPoints(final BlockPos blockPos, final World world) {
		// D:
		return null;
	}

}
