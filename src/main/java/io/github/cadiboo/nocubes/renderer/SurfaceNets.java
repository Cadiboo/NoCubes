package io.github.cadiboo.nocubes.renderer;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.renderer.SurfaceNets.RenderChunkSurfaceNet.BlockVertices;
import io.github.cadiboo.nocubes.util.LightmapInfo;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SurfaceNets {

	public static final int[] CUBE_EDGES = new int[24];
	public static final int[] EDGE_TABLE = new int[256];
	public static final HashMap<BlockPos, RenderChunkSurfaceNet> surfaceNetHashMap = new HashMap<>();
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

		final BlockPos renderChunkPos = event.getRenderChunkPosition();
		//chunk X
		final int cx = renderChunkPos.getX();
		//chunk Y
		final int cy = renderChunkPos.getY();
		//chunk Z
		final int cz = renderChunkPos.getZ();

		final ChunkCache cache = event.getChunkCache();

		final RenderChunkSurfaceNet renderChunkSurfaceNet = new RenderChunkSurfaceNet();

		// dims: "A 3D vector of integers representing the resolution of the isosurface". Resolution in our context means the size of a render chunk (16x16x16)
		int[] dims = new int[]{16, 16, 16};
		//startPos
		int[] c = new int[]{cx, cy, cz};
		//currentPos
		int[] x = new int[3];
		//edgesIThink
		int[] r = new int[]{1, dims[0] + 3, (dims[0] + 3) * (dims[1] + 3)};
		float[] grid = new float[8];
		float[][] buffer = new float[r[2] * 2][3];
		int bufno = 1;

		// "Resize buffer if necessary" is what mikolalysenko said, but Click_Me seems to have removed this code. This is probably because the buffer should never (and actually
		// can't be in java) be resized

		// March over the voxel grid
		for (x[2] = 0; x[2] < (dims[2] + 1); r[2] = -r[2], ++x[2], bufno ^= 1) {

			// m is the pointer into the buffer we are going to use.
			// "This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(" is what mikolalysenko said, it
			// obviously doesn't apply here
			// The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int m = 1 + ((dims[0] + 3) * (1 + (bufno * (dims[1] + 3))));

			for (x[1] = 0; x[1] < (dims[1] + 1); ++x[1], m += 2) {
				for (x[0] = 0; x[0] < (dims[0] + 1); ++x[0], ++m) {

					// Read in 8 field values around this vertex and store them in an array
					// Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
					int mask = 0;
					int g = 0;

					for (int posZ = 0; posZ < 2; ++posZ) {
						for (int posY = 0; posY < 2; ++posY) {
							for (int posX = 0; posX < 2; ++g) {
								// TODO: mutableblockpos?
								// final float p = potential.apply(new BlockPos(c[0] + x[0] + i, c[1] + x[1] + j, c[2] + x[2] + k), cache);

								final float p = ModUtil.getBlockDensity(new BlockPos(c[0] + x[0] + posX, c[1] + x[1] + posY, c[2] + x[2] + posZ), cache);
								grid[g] = p;
								mask |= p > 0.0F ? 1 << g : 0;
								++posX;

							}
						}
					}

					// Check for early termination if cell does not intersect boundary
					if ((mask == 0) || (mask == 0xff)) {
						continue;
					}

					IBlockState state = Blocks.AIR.getDefaultState();

					final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
					getStateAndPos:
					for (int posY = -1; posY < 2; ++posY) {
						for (int posZ = -1; posZ < 2; ++posZ) {
							for (int posX = -1; posX < 2; ++posX) {
								pos.setPos(c[0] + x[0] + posX, c[1] + x[1] + posY, c[2] + x[2] + posZ);
								final IBlockState tempState = cache.getBlockState(pos);

								// if (shouldSmooth(tempState) && (state.getBlock() != Blocks.GRASS))
								// {
								// state = tempState;
								// if ((tempState.getBlock() == Blocks.GRASS))
								// {
								// break getStateAndPos;
								// }
								// }

								if (ModUtil.shouldSmooth(tempState) && (state.getBlock() != Blocks.SNOW_LAYER) && (state.getBlock() != Blocks.GRASS)) {
									state = tempState;
									if ((tempState.getBlock() == Blocks.SNOW_LAYER) || (tempState.getBlock() == Blocks.GRASS)) {
										break getStateAndPos;
									}
								}
							}
						}
					}

					// Sum up edge intersections
					final int edge_mask = EDGE_TABLE[mask];
					int e_count = 0;
					final float[] v = new float[]{0.0F, 0.0F, 0.0F};

					// For every edge of the cube...
					for (int i = 0; i < 12; ++i) {

						// Use edge mask to check if it is crossed
						if ((edge_mask & (1 << i)) == 0) {
							continue;
						}

						// If it did, increment number of edge crossings
						++e_count;

						// Now find the point of intersection
						final int e0 = CUBE_EDGES[i << 1]; // Unpack vertices
						final int e1 = CUBE_EDGES[(i << 1) + 1];
						final float g0 = grid[e0]; // Unpack grid values
						final float g1 = grid[e1];
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
						v[i] = c[i] + x[i] + (s * v[i]);
					}

					final int tx = x[0] == 16 ? 0 : x[0];
					final int ty = x[1] == 16 ? 0 : x[1];
					final int tz = x[2] == 16 ? 0 : x[2];
					long i1 = (tx * 3129871) ^ (tz * 116129781L) ^ ty;
					i1 = (i1 * i1 * 42317861L) + (i1 * 11L);
					v[0] = (float) (v[0] - (((((i1 >> 16) & 15L) / 15.0F) - 0.5D) * 0.2D));
					v[1] = (float) (v[1] - (((((i1 >> 20) & 15L) / 15.0F) - 1.0D) * 0.2D));
					v[2] = (float) (v[2] - (((((i1 >> 24) & 15L) / 15.0F) - 0.5D) * 0.2D));

					// "Add vertex to buffer, store pointer to vertex index in buffer" is what mikolalysenko said, but Click_Me seems to have changed something

					buffer[m] = v;

					ArrayList<BlockVertices> blockVerticesList = new ArrayList<>();

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
						if ((x[iu] == 0) || (x[iv] == 0)) {
							continue;
						}

						// Otherwise, look up adjacent edges in buffer
						final int du = r[iu];
						final int dv = r[iv];

						final float[] v0 = buffer[m];
						final float[] v1 = buffer[m - du];
						final float[] v2 = buffer[m - du - dv];
						final float[] v3 = buffer[m - dv];

						// Remember to flip orientation depending on the sign of the corner.
						if ((mask & 1) != 0) {
							Vec3 vertex0 = new Vec3(v0[0], v0[1], v0[2]);
							Vec3 vertex1 = new Vec3(v1[0], v1[1], v1[2]);
							Vec3 vertex2 = new Vec3(v2[0], v2[1], v2[2]);
							Vec3 vertex3 = new Vec3(v3[0], v3[1], v3[2]);
							blockVerticesList.add(new BlockVertices(vertex0, vertex1, vertex2, vertex3));
						} else {
							Vec3 vertex0 = new Vec3(v0[0], v0[1], v0[2]);
							Vec3 vertex1 = new Vec3(v3[0], v3[1], v3[2]);
							Vec3 vertex2 = new Vec3(v2[0], v2[1], v2[2]);
							Vec3 vertex3 = new Vec3(v1[0], v1[1], v1[2]);
							blockVerticesList.add(new BlockVertices(vertex0, vertex1, vertex2, vertex3));
						}
					}

					renderChunkSurfaceNet.putVertices(pos, blockVerticesList);

				}

			}
		}

		surfaceNetHashMap.put(renderChunkPos, renderChunkSurfaceNet);

	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {
//		if(event.getBlockState().getBlock() instanceof BlockAir) {
//          if(event.getRenderLayer()==BlockRenderLayer.CUTOUT) {
		event.setResult(Event.Result.ALLOW);
		event.setCanceled(true);
//			}
//		}
	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {
//		if(event.getBlockState().getBlock() instanceof BlockAir) {
		event.setResult(Event.Result.ALLOW);
		event.setCanceled(true);
//		}
	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

		IBlockState state = event.getBlockState();
		if (!ModUtil.shouldRenderInState(state))
			return;
		BlockPos pos = event.getBlockPos();
		final BlockPos renderChunkPos = event.getRenderChunkPosition();
		final ChunkCache cache = event.getChunkCache();
		final BufferBuilder bufferBuilder = event.getBufferBuilder();
		final BlockRendererDispatcher blockRendererDispatcher = event.getBlockRendererDispatcher();

		final RenderChunkSurfaceNet renderChunkSurfaceNet = surfaceNetHashMap.get(renderChunkPos);
		if (renderChunkSurfaceNet == null)
			return;

		final List<BlockVertices> blockVerticesList = renderChunkSurfaceNet.getVertices(pos);
		if (blockVerticesList == null)
			return;

		final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		getStateAndPos:
		for (int posY = -1; posY < 2; ++posY) {
			for (int posZ = -1; posZ < 2; ++posZ) {
				for (int posX = -1; posX < 2; ++posX) {
					mutablePos.setPos(pos.getX() + posX, pos.getY() + posY, pos.getZ() + posZ);
					final IBlockState tempState = cache.getBlockState(pos);

					if (ModUtil.shouldSmooth(tempState) && (state.getBlock() != Blocks.SNOW_LAYER) && (state.getBlock() != Blocks.GRASS)) {
						state = tempState;
						pos = mutablePos.toImmutable();
						if ((tempState.getBlock() == Blocks.SNOW_LAYER) || (tempState.getBlock() == Blocks.GRASS)) {
							break getStateAndPos;
						}
					}
				}
			}
		}

		final BakedQuad quad = ModUtil.getQuad(state, pos, blockRendererDispatcher);
		if (quad == null) {
			return;
		}

		final int red;
		final int green;
		final int blue;

		final int color = ModUtil.getColor(quad, state, cache, pos);
		red = (color >> 16) & 255;
		green = (color >> 8) & 255;
		blue = color & 255;
		final int alpha = color >> 24 & 255;

		final TextureAtlasSprite sprite = ModUtil.getSprite(state, pos, blockRendererDispatcher);

		if (sprite == null) {
			return;
		}

		final double minU = sprite.getMinU();
		final double minV = sprite.getMinV();
		final double maxU = sprite.getMaxU();
		final double maxV = sprite.getMaxV();

		final LightmapInfo lightmapInfo = ModUtil.getLightmapInfo(pos, cache);

		final int lightmapSkyLight = lightmapInfo.getLightmapSkyLight();
		final int lightmapBlockLight = lightmapInfo.getLightmapBlockLight();

		boolean wasAnythingRendered = false;

		for (BlockVertices blockVertices : blockVerticesList) {

			wasAnythingRendered = true;

			final Vec3 v0 = blockVertices.vertex0;
			final Vec3 v1 = blockVertices.vertex1;
			final Vec3 v2 = blockVertices.vertex2;
			final Vec3 v3 = blockVertices.vertex3;

			bufferBuilder.pos(v0.xCoord, v0.yCoord - 1, v0.zCoord).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
			bufferBuilder.pos(v1.xCoord, v1.yCoord - 1, v1.zCoord).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
			bufferBuilder.pos(v2.xCoord, v2.yCoord - 1, v2.zCoord).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
			bufferBuilder.pos(v3.xCoord, v3.yCoord - 1, v3.zCoord).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

		}

		if (pos.equals(event.getBlockPos()))
			event.setCanceled(wasAnythingRendered);

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

		surfaceNetHashMap.remove(event.getRenderChunkPosition());

	}

	public static class RenderChunkSurfaceNet {

		final HashMap<BlockPos, ArrayList<BlockVertices>> blockVerticesHashMap = new HashMap<>();

		public ArrayList<BlockVertices> getVertices(BlockPos pos) {
			return blockVerticesHashMap.get(pos);
		}

		public void putVertices(BlockPos pos, ArrayList<BlockVertices> blockVertices) {
			blockVerticesHashMap.put(pos.toImmutable(), blockVertices);
		}

		public static class BlockVertices {

			public final Vec3 vertex0;
			public final Vec3 vertex1;
			public final Vec3 vertex2;
			public final Vec3 vertex3;

			public BlockVertices(final Vec3 vertex0, final Vec3 vertex1, final Vec3 vertex2, final Vec3 vertex3) {
				this.vertex0 = vertex0;
				this.vertex1 = vertex1;
				this.vertex2 = vertex2;
				this.vertex3 = vertex3;
			}

		}

	}

}
