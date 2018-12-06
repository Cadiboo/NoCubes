package io.github.cadiboo.nocubes.renderer;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.LightmapInfo;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import static io.github.cadiboo.nocubes.renderer.SurfaceNets.CUBE_EDGES;
import static io.github.cadiboo.nocubes.renderer.SurfaceNets.EDGE_TABLE;

public class SurfaceNetsOOP {

	public static volatile HashMap<BlockPos, SurfaceNet> posSurfaceNetHashMap = new HashMap<>();

	public static void renderPre(final RebuildChunkPreEvent event) {

		//final BlockPos startingPositionIn, final IBlockAccess cache, final BiFunction<BlockPos, IBlockAccess, Float> potential) {

		final BlockPos renderChunkPos = event.getRenderChunkPosition();
		NoCubes.LOGGER.info("Start - " + renderChunkPos);

		posSurfaceNetHashMap.put(renderChunkPos, generateSurfaceNet(renderChunkPos, event.getChunkCache(), ModUtil::getBlockDensity));

		NoCubes.LOGGER.info("End - " + renderChunkPos);

	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {
		SurfaceNets.renderLayer(event);
	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {
		SurfaceNets.renderType(event);
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

//		if (pos.getX() == -449 && pos.getY() == 72 && pos.getZ() == 431)
//			pos.getX();

		BlockPosSurfaceNetInfo posInfo = null;
		try {
			posInfo = posSurfaceNetHashMap.get(renderChunkPos).get(pos);
		} catch (NullPointerException e) {
			NoCubes.LOGGER.error(renderChunkPos);
			posSurfaceNetHashMap.isEmpty();
		}
		if (posInfo == null)
			return;

		final List<QuadVertexList> blockVerticesList = posInfo.vertexList;
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
		final int alpha = 0xFF;

		if (quad.hasTintIndex()) {
			final int colorMultiplier = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, cache, pos, 0);
			red = (colorMultiplier >> 16) & 255;
			green = (colorMultiplier >> 8) & 255;
			blue = colorMultiplier & 255;
		} else {
			red = 0xFF;
			green = 0xFF;
			blue = 0xFF;
		}

		final TextureAtlasSprite sprite = ModUtil.getSprite(state, pos, event.getBlockRendererDispatcher());

		if (sprite == null)
			return;

		final double minU = sprite.getMinU();
		final double minV = sprite.getMinV();
		final double maxU = sprite.getMaxU();
		final double maxV = sprite.getMaxV();

		final LightmapInfo lightmapInfo = ModUtil.getLightmapInfo(pos, cache);

		final int lightmapSkyLight = lightmapInfo.getLightmapSkyLight();
		final int lightmapBlockLight = lightmapInfo.getLightmapBlockLight();

		boolean wasAnythingRendered = false;

		for (QuadVertexList blockVertices : blockVerticesList) {

			wasAnythingRendered = true;

			final Vec3d v0 = blockVertices.vertex1;
			final Vec3d v1 = blockVertices.vertex2;
			final Vec3d v2 = blockVertices.vertex3;
			final Vec3d v3 = blockVertices.vertex4;

			bufferBuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
			bufferBuilder.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
			bufferBuilder.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
			bufferBuilder.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

		}

		if (pos.equals(event.getBlockPos()))
			event.setCanceled(wasAnythingRendered);

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

//		posSurfaceNetHashMap.remove(event.getRenderChunkPosition());

	}

	public static SurfaceNet generateSurfaceNet(final BlockPos startingPositionIn, final IBlockAccess cache, final BiFunction<BlockPos, IBlockAccess, Float> potential) {
		return generateSurfaceNetUNUSED(startingPositionIn, cache, potential);
	}

	public static SurfaceNet generateSurfaceNetUNUSED(final BlockPos startingPositionIn, final IBlockAccess cache, final BiFunction<BlockPos, IBlockAccess, Float> potential) {
		// dims: "A 3D vector of integers representing the resolution of the isosurface". Resolution in our context means size
		final int[] dims = new int[]{16, 16, 16};
		final int[] startPos = new int[]{startingPositionIn.getX(), startingPositionIn.getY(), startingPositionIn.getZ()};
		final int[] currentPos = new int[3];
		final int[] edgesIThink = new int[]{1, dims[0] + 3, (dims[0] + 3) * (dims[1] + 3)};
		final float[] grid = new float[8];
		final float[][] buffer = new float[edgesIThink[2] * 2][3];
		int bufno = 1;

		// "Resize buffer if necessary" is what mikolalysenko said, but Click_Me seems to have removed this code. This is probably because the buffer should never (and actually
		// can't be in java) be resized
		final HashMap<BlockPos, BlockPosSurfaceNetInfo> posInfos = new HashMap<>();
		// March over the voxel grid
		for (currentPos[2] = 0; currentPos[2] < (dims[2]); edgesIThink[2] = -edgesIThink[2], currentPos[2]++, bufno ^= 1) {

			// m is the pointer into the buffer we are going to use.
			// "This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(" is what mikolalysenko said, it
			// obviously doesn't apply here
			// The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int m = 1 + ((dims[0] + 3) * (1 + (bufno * (dims[1] + 3))));

			for (currentPos[1] = 0; currentPos[1] < (dims[1]); currentPos[1]++, m += 2) {
				for (currentPos[0] = 0; currentPos[0] < (dims[0]); currentPos[0]++, ++m) {

					// Read in 8 field values around this vertex and store them in an array
					// Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
					int mask = 0;
					int g = 0;

					for (int z = 0; z < 2; ++z) {
						for (int y = 0; y < 2; ++y) {
							for (int x = 0; x < 2; ++g) {
								// TODO: mutableblockpos?
								final float p = potential.apply(new BlockPos(startPos[0] + currentPos[0] + x, startPos[1] + currentPos[1] + y, startPos[2] + currentPos[2] + z), cache);

								// final float p = getBlockDensity(new BlockPos(startPos[0] + currentPos[0] + x, startPos[1] + currentPos[1] + y, startPos[2] + currentPos[2] + z),
								// cache);
								grid[g] = p;
								mask |= p > 0.0F ? 1 << g : 0;
								++x;

							}
						}
					}

					// Check for early termination if cell does not intersect boundary
					if ((mask == 0) || (mask == 0xFF)) {
						continue;
					}

					IBlockState stateActual = Blocks.AIR.getDefaultState();

					final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
					getStateAndPos:
					for (int y = -1; y < 2; ++y) {
						for (int z = -1; z < 2; ++z) {
							for (int x = -1; x < 2; ++x) {
								pos.setPos(startPos[0] + currentPos[0] + x, startPos[1] + currentPos[1] + y, startPos[2] + currentPos[2] + z);
								final IBlockState tempStateActual = cache.getBlockState(pos).getActualState(cache, pos);

								// if (shouldSmooth(tempState) && (state.getBlock() != Blocks.GRASS))
								// {
								// state = tempState;
								// if ((tempState.getBlock() == Blocks.GRASS))
								// {
								// break getStateAndPos;
								// }
								// }

								if (ModUtil.shouldSmooth(tempStateActual) && (stateActual.getBlock() != Blocks.SNOW_LAYER) && (stateActual.getBlock() != Blocks.GRASS)) {
									stateActual = tempStateActual;
									if ((tempStateActual.getBlock() == Blocks.SNOW_LAYER) || (tempStateActual.getBlock() == Blocks.GRASS)) {
										break getStateAndPos;
									}
								}
							}
						}
					}

					final int[] brightnessPos = new int[]{startPos[0] + currentPos[0], startPos[1] + currentPos[1] + 1, startPos[2] + currentPos[2]};

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
					final float s = 1.0F / e_count;
					for (int i = 0; i < 3; ++i) {
						v[i] = startPos[i] + currentPos[i] + (s * v[i]);
					}

					final int tx = currentPos[0] == 16 ? 0 : currentPos[0];
					final int ty = currentPos[1] == 16 ? 0 : currentPos[1];
					final int tz = currentPos[2] == 16 ? 0 : currentPos[2];
					long i1 = (tx * 3129871) ^ (tz * 116129781L) ^ ty;
					i1 = (i1 * i1 * 42317861L) + (i1 * 11L);
					v[0] = (float) (v[0] - (((((i1 >> 16) & 15L) / 15.0F) - 0.5D) * 0.2D));
					v[1] = (float) (v[1] - (((((i1 >> 20) & 15L) / 15.0F) - 1.0D) * 0.2D));
					v[2] = (float) (v[2] - (((((i1 >> 24) & 15L) / 15.0F) - 0.5D) * 0.2D));

					// "Add vertex to buffer, store pointer to vertex index in buffer" is what mikolalysenko said, but Click_Me seems to have changed something

					buffer[m] = v;

					final BlockPos brightnessBlockPos = new BlockPos(brightnessPos[0], brightnessPos[1], brightnessPos[2]);

					final ArrayList<QuadVertexList> vertexes = new ArrayList<>();

					// Now we need to add faces together, to do this we just loop over 3 basis components
					for (int axis = 0; axis < 3; ++axis) {
						// The first three entries of the edge_mask count the crossings along the edge
						if ((edge_mask & (1 << axis)) == 0) {
							continue;
						}

						// i = axes we are point along. iu, iv = orthogonal axes
						final int iu = (axis + 1) % 3;
						final int iv = (axis + 2) % 3;

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

						final QuadVertexList vertexList;

						// Remember to flip orientation depending on the sign of the corner.
						if ((mask & 1) != 0) {
							vertexList = new QuadVertexList(new Vec3d(v0[0], v0[1], v0[2]), new Vec3d(v1[0], v1[1], v1[2]), new Vec3d(v2[0], v2[1], v2[2]), new Vec3d(v3[0], v3[1], v3[2]));

						} else {
							vertexList = new QuadVertexList(new Vec3d(v0[0], v0[1], v0[2]), new Vec3d(v3[0], v3[1], v3[2]), new Vec3d(v2[0], v2[1], v2[2]), new Vec3d(v1[0], v1[1], v1[2]));

						}

						vertexes.add(vertexList);
					}

					final BlockPosSurfaceNetInfo posInfo = new BlockPosSurfaceNetInfo(stateActual, brightnessBlockPos, vertexes);

					posInfos.put(new BlockPos(startPos[0] + currentPos[0], startPos[1] + currentPos[1], startPos[2] + currentPos[2]), posInfo);

				}

			}
		}
		return new SurfaceNet(posInfos);
	}

	public static class SurfaceNet {

		public final HashMap<BlockPos, BlockPosSurfaceNetInfo> posInfos;

		public SurfaceNet(final HashMap<BlockPos, BlockPosSurfaceNetInfo> posInfos) {
			this.posInfos = posInfos;
		}

		public BlockPosSurfaceNetInfo get(BlockPos pos) {
			return posInfos.get(pos);
		}

	}

	public static class BlockPosSurfaceNetInfo {

		public final IBlockState state;
		public final BlockPos brightnessPos;
		public final List<QuadVertexList> vertexList;

		public BlockPosSurfaceNetInfo(final IBlockState state, final BlockPos brightnessPos, final List<QuadVertexList> quadVertexList) {
			this.state = state;
			this.brightnessPos = brightnessPos;
			this.vertexList = quadVertexList;
		}

	}

	public static class QuadVertexList {

		public final Vec3d vertex1;
		public final Vec3d vertex2;
		public final Vec3d vertex3;
		public final Vec3d vertex4;

		public QuadVertexList(final Vec3d vertex1, final Vec3d vertex2, final Vec3d vertex3, final Vec3d vertex4) {
			this.vertex1 = vertex1;
			this.vertex2 = vertex2;
			this.vertex3 = vertex3;
			this.vertex4 = vertex4;
		}

		public Vec3d[] getVertexes() {
			return new Vec3d[]{this.vertex1, this.vertex2, this.vertex3, this.vertex4};
		}

	}

}
