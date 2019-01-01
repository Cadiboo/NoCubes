package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.debug.client.render.IDebugRenderAlgorithm;
import io.github.cadiboo.nocubes.debug.client.render.IDebugRenderAlgorithm.Face;
import io.github.cadiboo.nocubes.util.LightmapInfo;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.optifine.RebuildChunkBlockOptifineEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.optifine.RebuildChunkPreOptifineEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.mod.EnumEventType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.ArrayList;
import java.util.HashMap;

import static io.github.cadiboo.nocubes.config.ModConfig.debug;
import static io.github.cadiboo.nocubes.util.ModEnums.EffortLevel.OFF;
import static io.github.cadiboo.renderchunkrebuildchunkhooks.hooks.RenderChunkRebuildChunkHooksHooks.compiledChunk_setLayerUsed;
import static io.github.cadiboo.renderchunkrebuildchunkhooks.hooks.RenderChunkRebuildChunkHooksHooks.renderChunk_preRenderBlocks;

/**
 * @author Cadiboo
 */
public final class SurfaceNetsDev {

	private static final ThreadLocal<HashMap<BlockPos, HashMap<BlockPos, ArrayList<Face<Vec3>>>>> FACES_BLOCKPOS_MAP = ThreadLocal.withInitial(HashMap::new);

	private static float getBlockDensity(final boolean[] isSmoothableCache, final IBlockState[] statesCache, final int scanSize, final IBlockAccess cache, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final int x, final int y, final int z, PooledMutableBlockPos pooledMutableBlockPos) {

		float density = 0.0F;

		for (int xOffset = 0; xOffset < 2; ++xOffset) {
			for (int yOffset = 0; yOffset < 2; ++yOffset) {
				for (int zOffset = 0; zOffset < 2; ++zOffset) {

					// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
					final int index = (x + xOffset) + scanSize * ((y + yOffset) + scanSize * (z + zOffset));

					final IBlockState state = statesCache[index];
					final boolean shouldSmooth = isSmoothableCache[index];

					if (shouldSmooth) {
						pooledMutableBlockPos.setPos(
								renderChunkPosX + x + xOffset,
								renderChunkPosY + y + yOffset,
								renderChunkPosZ + z + zOffset
						);
						final AxisAlignedBB aabb = state.getBoundingBox(cache, pooledMutableBlockPos);
						density += aabb.maxY - aabb.minY;
					} else {
						density -= 1;
					}

					if (state.getBlock() == Blocks.BEDROCK) {
						density += 0.000000000000000000000000000000000000000000001F;
					}

				}
			}
		}

		return density;

	}

	public static void renderPre(final RebuildChunkPreEvent event) {

		final MutableBlockPos renderChunkPos = event.getRenderChunkPosition();
		final int renderChunkPosX = renderChunkPos.getX();
		final int renderChunkPosY = renderChunkPos.getY();
		final int renderChunkPosZ = renderChunkPos.getZ();
		final IBlockAccess cache;
		if (event.getType() == EnumEventType.FORGE_OPTIFINE) {
			cache = ((RebuildChunkPreOptifineEvent) event).getChunkCacheOF();
		} else {
			cache = event.getChunkCache();
		}
		final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
		final RenderChunk renderChunk = event.getRenderChunk();

		final float isosurfaceLevel = ModConfig.getIsosurfaceLevel();

		final int[] CUBE_EDGES = SurfaceNets.CUBE_EDGES;
		final int[] EDGE_TABLE = SurfaceNets.EDGE_TABLE;

		try {

			//		final int[] dims = {16, 16, 16};
			// make the algorithm look on the sides of chunks aswell
			// I tweaked the loop in Marching cubes, this time I just edited dims
			final int[] c = {renderChunkPosX, renderChunkPosY, renderChunkPosZ};
			final ArrayList<float[]> vertices = new ArrayList<>();

			final float[] data = new float[debug.dataSizeX * debug.dataSizeY * debug.dataSizeZ];

			for (BlockPos.MutableBlockPos mutableBlockPos : BlockPos.getAllInBoxMutable(renderChunkPos, renderChunkPos.add(debug.dataSizeX - 1, debug.dataSizeY - 1, debug.dataSizeZ - 1))) {
				final BlockPos sub = mutableBlockPos.subtract(renderChunkPos);
				final int x = sub.getX();
				final int y = sub.getY();
				final int z = sub.getZ();
				// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
				data[x + debug.dataSizeX * (y + debug.dataSizeY * z)] = ModUtil.getBlockDensity(mutableBlockPos, cache);
			}

			//Internal buffer, this may get resized at run time
			final int[] buffer;

//			var vertices = []
//    , faces = []
			int n = 0;
			final int[] x = new int[3],
					R = new int[]{1, (debug.marchTerrainSizeX + 1), (debug.marchTerrainSizeY + 1) * (debug.marchTerrainSizeZ + 1)};
			final float[] grid = new float[8];
			int buf_no = 1;

			//Resize buffer if necessary
//		if (R[2] * 2 > buffer.length) {
//			buffer = new Int32Array(R[2] * 2);
//		}
			buffer = new int[R[2] * 2];

			//March over the voxel grid
			for (x[2] = 0; x[2] < debug.marchTerrainSizeZ - 1; ++x[2], n += debug.marchTerrainSizeX, buf_no ^= 1, R[2] = -R[2]) {

				//m is the pointer into the buffer we are going to use.
				//This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(
				//The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
				int m = 1 + (debug.marchTerrainSizeX + 1) * (1 + buf_no * (debug.marchTerrainSizeX + 1));

				for (x[1] = 0; x[1] < debug.marchTerrainSizeY - 1; ++x[1], ++n, m += 2) {
					for (x[0] = 0; x[0] < debug.marchTerrainSizeX - 1; ++x[0], ++n, ++m) {
						pooledMutableBlockPos.setPos(c[0] + x[0], c[1] + x[1], c[2] + x[2]);

						//Read in 8 field values around this vertex and store them in an array
						//Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
						int mask = 0, g = 0, idx = n;
						for (int k = 0; k < 2; ++k, idx += debug.marchTerrainSizeX * (debug.marchTerrainSizeY - 2))
							for (int j = 0; j < 2; ++j, idx += debug.marchTerrainSizeX - 2)
								for (int i = 0; i < 2; ++i, ++g, ++idx) {
									// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
									// assuming i = x, j = y, k = z
//								pooledMutablePos.setPos(c[0] + x[0] + i, c[1] + x[1] + j, c[2] + x[2] + k);
									final float p = data[idx];
//								final float p = ModUtil.getBlockDensity(pooledMutablePos, cache);

//								final float p = data[(x[0] + i) + 18 * ((x[1] + j) + 18 * (x[2] + k))];

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
						float s = isosurfaceLevel / e_count;
						for (int i = 0; i < 3; ++i) {
							v[i] = c[i] + x[i] + s * v[i];
						}

						//Add vertex to buffer, store pointer to vertex index in buffer
						buffer[m] = vertices.size();
						if (ModConfig.offsetVertices)
							ModUtil.offsetVertex(v);
						vertices.add(v);

						final BlockRenderData renderData = ClientUtil.getBlockRenderData(pooledMutableBlockPos, cache);

						final BlockRenderLayer blockRenderLayer = renderData.getBlockRenderLayer();
						ForgeHooksClient.setRenderLayer(blockRenderLayer);
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
							compiledChunk_setLayerUsed(compiledChunk, blockRenderLayer);
							renderChunk_preRenderBlocks(renderChunk, bufferBuilder, renderChunkPos);
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
			}

		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			pooledMutableBlockPos.release();
		}
	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {

	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {

	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

		if (true) {
			event.setCanceled(ModUtil.shouldSmooth(event.getBlockState()));
			return;
		}

		try {

			final BlockPos pos = event.getBlockPos();

			final ArrayList<Face<Vec3>> faces = FACES_BLOCKPOS_MAP.get().get(event.getRenderChunkPosition()).get(pos);

			if (faces == null || faces.isEmpty()) {
				return;
			}

			event.setCanceled(true);

			final IBlockAccess cache;
			if (event.getType() == EnumEventType.FORGE_OPTIFINE) {
				cache = ((RebuildChunkBlockOptifineEvent) event).getChunkCacheOF();
			} else {
				cache = event.getChunkCache();
			}

			final int x = pos.getX();
			final int y = pos.getY();
			final int z = pos.getZ();

			final IBlockState state = cache.getBlockState(pos);
			final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

			final Object[] texturePosAndState = ClientUtil.getTexturePosAndState(cache, pos, state);
			final BlockPos texturePos = (BlockPos) texturePosAndState[0];
			final IBlockState textureState = (IBlockState) texturePosAndState[1];

			//real pos not texture pos
			final LightmapInfo lightmapInfo = ClientUtil.getLightmapInfo(pos, cache);
			final int lightmapSkyLight = lightmapInfo.getLightmapSkyLight();
			final int lightmapBlockLight = lightmapInfo.getLightmapBlockLight();

			event.setCanceled(true);
			event.setBlockRenderLayerUsed(event.getBlockRenderLayer(), true);
			final BufferBuilder bufferBuilder = event.getBufferBuilder();

			EnumFacing[] VALUES = EnumFacing.VALUES;
			int facingIndex = 0;

			for (IDebugRenderAlgorithm.Face<Vec3> vec3Face : faces) {
				EnumFacing facing = VALUES[facingIndex++];

				BakedQuad quad = ClientUtil.getQuad(textureState, texturePos, blockRendererDispatcher, facing);
				if (quad == null) {
					quad = blockRendererDispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(null, null, 0L).get(0);
				}
				TextureAtlasSprite sprite = quad.getSprite();
				if (ModConfig.beautifyTexturesLevel != OFF) {
					if (sprite == blockRendererDispatcher.getModelForState(Blocks.GRASS.getDefaultState()).getQuads(Blocks.GRASS.getDefaultState(), EnumFacing.NORTH, 0L).get(0).getSprite()) {
						quad = ClientUtil.getQuad(textureState, texturePos, blockRendererDispatcher, EnumFacing.UP);
						sprite = quad.getSprite();
					}
				}
				final int color = ClientUtil.getColor(quad, textureState, cache, texturePos);
				final int red = (color >> 16) & 255;
				final int green = (color >> 8) & 255;
				final int blue = color & 255;
				final int alpha = 0xFF;

				final float minU = ClientUtil.getMinU(sprite);
				final float minV = ClientUtil.getMinV(sprite);
				final float maxU = ClientUtil.getMaxU(sprite);
				final float maxV = ClientUtil.getMaxV(sprite);

				final Vec3 vertex0 = vec3Face.getVertex0();
				final Vec3 vertex1 = vec3Face.getVertex1();
				final Vec3 vertex2 = vec3Face.getVertex2();
				final Vec3 vertex3 = vec3Face.getVertex3();

				bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

			}
		} catch (Exception e) {
//			e.printStackTrace();
		}
	}

	public static void renderPost(final RebuildChunkPostEvent event) {

		FACES_BLOCKPOS_MAP.get().remove(event.getRenderChunkPosition());

	}

}
