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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.ArrayList;
import java.util.HashMap;

import static io.github.cadiboo.nocubes.util.ModEnums.EffortLevel.OFF;

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

		int mutableIndex = 0;

		final float isosurfaceLevel = ModConfig.getIsosurfaceLevel();

		final int[] CUBE_EDGES = SurfaceNets.CUBE_EDGES;
		final int[] EDGE_TABLE = SurfaceNets.EDGE_TABLE;

		//chunk size = 16, this is chunk size + 1 block on every positive axis side
		final int chunkSizeX = 16;
		final int chunkSizeY = 16;
		final int chunkSizeZ = 16;
		final int scanSizeX = chunkSizeX + 1;
		final int scanSizeY = chunkSizeY + 1;
		final int scanSizeZ = chunkSizeZ + 1;

		final boolean[] isSmoothable = new boolean[(scanSizeX + 1) * (scanSizeY + 1) * (scanSizeZ + 1)];
		final IBlockState[] states = new IBlockState[(scanSizeX + 1) * (scanSizeY + 1) * (scanSizeZ + 1)];

		//http://ngildea.blogspot.com/2014/09/dual-contouring-chunked-terrain.html

		try {

			// -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17
			// transverse the chunk + 2 blocks on every positive axis side, plus 1 block on every side
			for (int xOffset = -1; xOffset < scanSizeX + 1; xOffset++) {
				for (int yOffset = -1; yOffset < scanSizeY + 1; yOffset++) {
					for (int zOffset = -1; zOffset < scanSizeZ + 1; zOffset++) {

						final int x = (xOffset + 1);
						final int y = (yOffset + 1);
						final int z = (zOffset + 1);

						// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
						final int index = x + scanSizeX * (y + scanSizeY * z);

						final IBlockState potentiallySmoothableBlockState = cache.getBlockState(pooledMutableBlockPos.setPos(
								renderChunkPosX + xOffset,
								renderChunkPosY + yOffset,
								renderChunkPosZ + zOffset
						));

						states[index] = potentiallySmoothableBlockState;
						isSmoothable[index] = ModUtil.shouldSmooth(potentiallySmoothableBlockState);

					}
				}
			}

			final float[] densities = new float[(scanSizeX + 1) * (scanSizeY + 1) * (scanSizeZ + 1)];

			mutableIndex = 0;
			// transverse the chunk + 2 blocks on every positive axis side
			for (int x = 0; x < scanSizeX + 1; x++) {
				for (int y = 0; y < scanSizeY + 1; y++) {
					for (int z = 0; z < scanSizeZ + 1; z++) {
//						mutableIndex++;

						//
//						// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
//						final int index = x + scanSizeX * (y + scanSizeY * z);

						densities[mutableIndex] = getBlockDensity(isSmoothable, states, scanSizeX, cache, renderChunkPosX, renderChunkPosY, renderChunkPosZ, x, y, z, pooledMutableBlockPos);

					}
				}
			}

			final float[] neighbourDensityGrid = new float[8];

			final int[] r_noClue = new int[]{1, scanSizeX, scanSizeX * scanSizeY};
			int bufNo = 0;
			final int[] buffer = new int[r_noClue[2] * 2];

			final ArrayList<Vec3> vertices = new ArrayList<>();

			final HashMap<BlockPos, ArrayList<Face<Vec3>>> map = new HashMap<>();

			// transverse the chunk + 1 block on every positive axis side
			for (int posX = 0; posX < scanSizeX; posX++) {
				bufNo ^= 1;
				int bufferPointer = 1 + (scanSizeX + 1) * (1 + bufNo * (scanSizeY + 1));
				for (int posY = 0; posY < scanSizeY; posY++) {
					for (int posZ = 0; posZ < scanSizeZ; posZ++) {

						int mask = 0b00000000;
						int neighbourDensityGridIndex = 0;
						//every corner of the block
						for (int xOffset = 0; xOffset < 2; xOffset++) {
							for (int yOffset = 0; yOffset < 2; yOffset++) {
								for (int zOffset = 0; zOffset < 2; zOffset++) {
									final int x = posX + xOffset;
									final int y = posY + yOffset;
									final int z = posZ + zOffset;

									// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
									final int index = x + scanSizeX * (y + scanSizeY * z);

									float density = densities[index];
									neighbourDensityGrid[neighbourDensityGridIndex] = density;
									mask |= (density < 0) ? (1 << neighbourDensityGridIndex) : 0;

									neighbourDensityGridIndex++;
								}
							}
						}

						//Check for early termination if cell does not intersect boundary
						if (mask == 0 || mask == 0xFF) {
							continue;
						}

						//Sum up edge intersections
						final int edgeMask = EDGE_TABLE[mask];
						final Vec3 vertex = new Vec3();
						int edgeCrossingCount = 0;

						//For every edge of the cube...
						for (int edgeIndex = 0; edgeIndex < 12; ++edgeIndex) {

							//Use edge mask to check if it is crossed
							if ((edgeMask & (1 << edgeIndex)) == 0) {
								continue;
							}

							//If it did, increment number of edge crossings
							++edgeCrossingCount;

							//Now find the point of intersection
							int edge0 = CUBE_EDGES[edgeIndex << 1];       //Unpack vertices
							int edge1 = CUBE_EDGES[(edgeIndex << 1) + 1];
							float density0 = neighbourDensityGrid[edge0];                 //Unpack grid values
							float density1 = neighbourDensityGrid[edge1];
							float pointOfIntersection = density0 - density1;                 //Compute point of intersection
							if (Math.abs(pointOfIntersection) > 1e-6) {
								pointOfIntersection = density0 / pointOfIntersection;
							} else {
								continue;
							}

							final int xA = edge0 & 1;
							final int xB = edge1 & 1;
							if (xA != xB) {
								vertex.xCoord += xA != 0 ? 1.0 - pointOfIntersection : pointOfIntersection;
							} else {
								vertex.xCoord += xA != 0 ? 1.0 : 0;
							}

							final int yA = edge0 & 2;
							final int yB = edge1 & 2;
							if (yA != yB) {
								vertex.yCoord += yA != 0 ? 1.0 - pointOfIntersection : pointOfIntersection;
							} else {
								vertex.yCoord += yA != 0 ? 1.0 : 0;
							}

							final int zA = edge0 & 3;
							final int zB = edge1 & 3;
							if (zA != zB) {
								vertex.zCoord += zA != 0 ? 1.0 - pointOfIntersection : pointOfIntersection;
							} else {
								vertex.zCoord += zA != 0 ? 1.0 : 0;
							}

						}

						//Now we just average the edge intersections and add them to coordinate
						float s = isosurfaceLevel / edgeCrossingCount;
						vertex.xCoord = renderChunkPosX + posX + s * vertex.xCoord;
						vertex.yCoord = renderChunkPosY + posY + s * vertex.yCoord;
						vertex.zCoord = renderChunkPosZ + posZ + s * vertex.zCoord;

						//Add vertex to buffer, store pointer to vertex index in buffer
						buffer[bufferPointer] = vertices.size();
						if (ModConfig.offsetVertices)
							ModUtil.offsetVertex(vertex);
						vertices.add(vertex);

						final ArrayList<Face<Vec3>> faces = new ArrayList<>();

						for (int axisIndex = 0; axisIndex < 3; ++axisIndex) {

							//The first three entries of the edge_mask count the crossings along the edge
							if ((edgeMask & (1 << axisIndex)) == 0) {
								continue;
							}

							int otherAxisIndex0 = (axisIndex + 1) % 3;
							int otherAxisIndex1 = (axisIndex + 2) % 3;

							switch (otherAxisIndex0) {
								default:
								case 0:
									if (posX == 0) continue;
									break;
								case 1:
									if (posY == 0) continue;
									break;
								case 2:
									if (posZ == 0) continue;
									break;
							}
							switch (otherAxisIndex1) {
								default:
								case 0:
									if (posX == 0) continue;
									break;
								case 1:
									if (posY == 0) continue;
									break;
								case 2:
									if (posZ == 0) continue;
									break;
							}

							int otherAxisEdge0 = r_noClue[otherAxisIndex0];
							int otherAxisEdge1 = r_noClue[otherAxisIndex1];

							if ((mask & 1) == 0) {
//							faces.add([buffer[m], buffer[m - du], buffer[m - du - dv], buffer[m - dv]]);

								faces.add(new Face<Vec3>(
										vertices.get(buffer[bufferPointer]),
										vertices.get(buffer[bufferPointer - otherAxisEdge0]),
										vertices.get(buffer[bufferPointer - otherAxisEdge0 - otherAxisEdge1]),
										vertices.get(buffer[bufferPointer - otherAxisEdge1])
								));

							} else {
//							faces.add([buffer[m], buffer[m - dv], buffer[m - du - dv], buffer[m - du]]);

								faces.add(new Face<Vec3>(
										vertices.get(buffer[bufferPointer]),
										vertices.get(buffer[bufferPointer - otherAxisEdge1]),
										vertices.get(buffer[bufferPointer - otherAxisEdge0 - otherAxisEdge1]),
										vertices.get(buffer[bufferPointer - otherAxisEdge0])
								));
							}

						}

						map.put(new BlockPos(renderChunkPosX + posX, renderChunkPosX + posY, renderChunkPosX + posZ), faces);

					}
				}
			}

			FACES_BLOCKPOS_MAP.get().put(renderChunkPos, map);

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
			e.printStackTrace();
		}
	}

	public static void renderPost(final RebuildChunkPostEvent event) {

		FACES_BLOCKPOS_MAP.get().remove(event.getRenderChunkPosition());

	}

}
