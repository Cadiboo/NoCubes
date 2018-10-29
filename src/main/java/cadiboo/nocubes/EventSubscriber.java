package cadiboo.nocubes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import org.apache.logging.log4j.LogManager;

import cadiboo.nocubes.config.ModConfig;
import cadiboo.nocubes.util.ModEnums.RenderAlgorithm;
import cadiboo.nocubes.util.ModUtil;
import cadiboo.nocubes.util.ModUtil.BlockPosSurfaceNetInfo;
import cadiboo.nocubes.util.ModUtil.QuadVertexList;
import cadiboo.nocubes.util.ModUtil.SurfaceNet;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlocksEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class EventSubscriber {

	public static final Random FRAGMENT_RANDOM = new Random();

	@SubscribeEvent(receiveCanceled = true)
	public static void rebuildChunkBlocksVANILLA_MODDED(final RebuildChunkBlocksEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (!ModConfig.getActiveRenderingAlgorithms().contains(RenderAlgorithm.VANILLA_MODDED)) {
			return;
		}

		event.setCanceled(true);

		for (final BlockPos.MutableBlockPos currentBlockPos : event.getChunkBlockPositions()) {

			final IBlockState blockState = event.getWorldView().getBlockState(currentBlockPos);
			final Block block = blockState.getBlock();

			for (final BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {

				if (!block.canRenderInLayer(blockState, blockRenderLayer)) {
					continue;
				}

				net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockRenderLayer);

				if (block.getDefaultState().getRenderType() != EnumBlockRenderType.INVISIBLE) {

					final BufferBuilder bufferbuilder = event.startOrContinueLayer(blockRenderLayer);

					final boolean used = event.getBlockRendererDispatcher().renderBlock(blockState, currentBlockPos, event.getWorldView(), bufferbuilder);

					event.setBlockRenderLayerUsedWithOrOpperation(blockRenderLayer, used);

				}

			}

			net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);

		}

		ModUtil.smoothWater(event);

	}

	@SubscribeEvent(receiveCanceled = true, priority = EventPriority.LOWEST)
	public static void rebuildChunkBlocksVANILLA(final RebuildChunkBlocksEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (!ModConfig.getActiveRenderingAlgorithms().contains(RenderAlgorithm.VANILLA)) {
			return;
		}

		event.setCanceled(false);

	}

	@SubscribeEvent(receiveCanceled = true)
	public static void rebuildChunkBlocksFRAGMENTED(final RebuildChunkBlocksEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (!ModConfig.getActiveRenderingAlgorithms().contains(RenderAlgorithm.FRAGMENTED)) {
			return;
		}

		event.setCanceled(true);

		for (final BlockPos.MutableBlockPos blockpos$mutableblockpos : event.getChunkBlockPositions()) {
			final IBlockState iblockstate = event.getWorldView().getBlockState(blockpos$mutableblockpos);
			final Block block = iblockstate.getBlock();

			for (final BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
				if (!block.canRenderInLayer(iblockstate, blockRenderLayer)) {
					continue;
				}
				net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockRenderLayer);

				if (block.getDefaultState().getRenderType() != EnumBlockRenderType.INVISIBLE) {
					final BufferBuilder bufferbuilder = event.startOrContinueLayer(blockRenderLayer);

					BlockPos offsetPos = blockpos$mutableblockpos;

					for (final EnumFacing facing : ModConfig.getFragmentFacings()) {
						offsetPos = offsetPos.offset(facing, FRAGMENT_RANDOM.nextInt(ModConfig.getFragmentRange() + 1));
					}

					final boolean used = event.getBlockRendererDispatcher().renderBlock(iblockstate, offsetPos, event.getWorldView(), bufferbuilder);

					event.setBlockRenderLayerUsedWithOrOpperation(blockRenderLayer, used);

				}
			}
			net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
		}

		ModUtil.smoothWater(event);

	}

	@SubscribeEvent(receiveCanceled = true)
	public static void rebuildChunkBlocksFACING(final RebuildChunkBlocksEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (!ModConfig.getActiveRenderingAlgorithms().contains(RenderAlgorithm.FACING)) {
			return;
		}

		event.setCanceled(true);

		for (final BlockPos.MutableBlockPos blockpos$mutableblockpos : event.getChunkBlockPositions()) {
			final IBlockState iblockstate = event.getWorldView().getBlockState(blockpos$mutableblockpos);
			final Block block = iblockstate.getBlock();

			for (final BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
				if (!block.canRenderInLayer(iblockstate, blockRenderLayer)) {
					continue;
				}
				net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockRenderLayer);

				if (block.getDefaultState().getRenderType() != EnumBlockRenderType.INVISIBLE) {
					final BufferBuilder bufferbuilder = event.startOrContinueLayer(blockRenderLayer);

					for (final EnumFacing side : ModConfig.getFacingFacings()) {
						final boolean used = ModUtil.renderBlockEnumFacing(iblockstate, blockpos$mutableblockpos, event.getWorldView(), bufferbuilder, event.getBlockRendererDispatcher(), true, side, false);

						event.setBlockRenderLayerUsedWithOrOpperation(blockRenderLayer, used);
					}

				}
			}
			net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
		}

		ModUtil.smoothWater(event);

	}

	@SubscribeEvent(receiveCanceled = true)
	public static void rebuildChunkBlocksMARCHING_CUBES(final RebuildChunkBlocksEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (!ModConfig.getActiveRenderingAlgorithms().contains(RenderAlgorithm.MARCHING_CUBES)) {
			return;
		}

		event.setCanceled(true);

		final ChunkCache cache = event.getWorldView();

		// do stuff

		// https://github.com/PrimozLavric/MarchingCubes/blob/9b119d9a0024aae0e3cc917659fc814028699b18/MarchingCubesJava/src/MarchingCubes.java #marchingCubesInt

		final int[] dims = new int[] { 16, 16, 16 };

		final int[] c = new int[] { event.getRenderChunkPosition().getX(), event.getRenderChunkPosition().getY(), event.getRenderChunkPosition().getZ() };

		final ArrayList<float[]> vertices = new ArrayList<>();
		final ArrayList<int[]> faces = new ArrayList<>();
		int n = 0;
		final float[] grid = new float[8];
		final int[] edges = new int[12];
		final int[] x = new int[3];

		// March over the volume
		for (x[2] = 0; x[2] < (dims[2] - 1); ++x[2], n += dims[0]) {
			for (x[1] = 0; x[1] < (dims[1] - 1); ++x[1], ++n) {
				for (x[0] = 0; x[0] < (dims[0] - 1); ++x[0], ++n) {
					// For each cell, compute cube mask
					int cube_index = 0;
					for (int i = 0; i < 8; ++i) {
						final int[] v = ModUtil.MARCHING_CUBES_CUBE_VERTS[i];

//						Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
//						Flat[n + v[0] + (dims[0] * (v[1] + (dims[1] * v[2])))]
//						final float s = data[n + v[0] + (dims[0] * (v[1] + (dims[1] * v[2])))];
//						final float s = ModUtil.getBlockDensity(new BlockPos(n + v[0] + (dims[0] * (v[1] + (dims[1] * v[2])))), cache);

						final float s = ModUtil.getBlockDensity(new BlockPos(c[0] + x[0] + v[0], c[1] + x[1] + v[1], c[2] + x[2] + v[2]), cache);

						grid[i] = s;
						cube_index |= (s > 0) ? 1 << i : 0;
					}
					// Compute vertices
					final int edge_mask = ModUtil.MARCHING_CUBES_EDGE_TABLE[cube_index];
					if (edge_mask == 0) {
						continue;
					}
					for (int i = 0; i < 12; ++i) {
						if ((edge_mask & (1 << i)) == 0) {
							continue;
						}
						edges[i] = vertices.size();
						final float[] nv = new float[] { 0, 0, 0 };
						final int[] e = ModUtil.MARCHING_CUBES_EDGE_INDEX[i];
						final int[] p0 = ModUtil.MARCHING_CUBES_CUBE_VERTS[e[0]];
						final int[] p1 = ModUtil.MARCHING_CUBES_CUBE_VERTS[e[1]];
						final float a = grid[e[0]];
						final float b = grid[e[1]];
						final float d = a - b;
						int t = 0;
						if (Math.abs(d) > 1e-6) {
							t = (int) (a / d);
						}
						for (int j = 0; j < 3; ++j) {
							nv[j] = (x[j] + p0[j]) + (t * (p1[j] - p0[j]));
						}

						vertices.add(nv);
					}

					// Add faces
					final int[] f = ModUtil.MARCHING_CUBES_TRI_TABLE[cube_index];
					for (int i = 0; i < f.length; i += 3) {
						faces.add(new int[] { edges[f[i + 0]], edges[f[i + 1]], edges[f[i + 2]] });
					}
				}
			}
		}

		final BufferBuilder bufferBuilderTest = event.startOrContinueLayer(BlockRenderLayer.SOLID);
		event.setBlockRenderLayerUsedWithOrOpperation(BlockRenderLayer.SOLID, true);

		final int red = 0xFF;
		final int green = 0xFF;
		final int blue = 0xFF;
		final int alpha = 0xFF;
		final double minU = 0;
		final double maxU = 1;
		final double minV = 0;
		final double maxV = 1;

		final int lightmapSkyLight = 15 << 4, lightmapBlockLight = 0;

		for (int i = 0; i < vertices.size(); i++) {
			LogManager.getLogger().info(vertices.size() / 4f);
			final float[] v0 = vertices.get(i + 0);
			bufferBuilderTest.pos(c[0] + v0[0], c[1] + v0[1], c[2] + v0[2]).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
		}

//		for (int i = 0; i < (vertices.size() - 3); i += 4) {
//			final float[] v0 = vertices.get(i + 0);
//			final float[] v1 = vertices.get(i + 1);
//			final float[] v2 = vertices.get(i + 2);
//			final float[] v3 = vertices.get(i + 3);
//
//			bufferBuilderTest.pos(v0[0], v0[1], v0[2]).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//			bufferBuilderTest.pos(v1[0], v1[1], v1[2]).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//			bufferBuilderTest.pos(v2[0], v2[1], v2[2]).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//			bufferBuilderTest.pos(v3[0], v3[1], v3[2]).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//
//		}

		for (final BlockPos pos : event.getChunkBlockPositions()) {
			final IBlockState state = cache.getBlockState(pos);

			if (ModUtil.shouldSmooth(state)) {
				continue;
			}

			final Block block = state.getBlock();

			for (final BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
				if (!block.canRenderInLayer(state, blockRenderLayer)) {
					continue;
				}
				net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockRenderLayer);

				if (state.getRenderType() != EnumBlockRenderType.INVISIBLE) {
					final BufferBuilder bufferBuilder = event.startOrContinueLayer(blockRenderLayer);

					final boolean wasLayerUsed = event.getBlockRendererDispatcher().renderBlock(state, pos, cache, bufferBuilder);

					event.setBlockRenderLayerUsedWithOrOpperation(blockRenderLayer, wasLayerUsed);

				}
			}
			net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
		}

		ModUtil.smoothWater(event);

	}

	@SubscribeEvent(receiveCanceled = true)
	public static void rebuildChunkBlocksSURFACE_NETS(final RebuildChunkBlocksEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (!ModConfig.getActiveRenderingAlgorithms().contains(RenderAlgorithm.SURFACE_NETS)) {
			return;
		}

		event.setCanceled(true);

		final ChunkCache cache = event.getWorldView();
		final BlockPos position = event.getRenderChunkPosition();
		// final BiFunction<BlockPos, ChunkCache, Float> potential = (pos, cache2) -> {
		// return getBlockDensity(position, cache2);
		// };

		// do stuff

		// dims: "A 3D vector of integers representing the resolution of the isosurface". Resolution in our context means size
		final int[] dims = new int[] { 16, 16, 16 };
		final int[] startPos = new int[] { position.getX(), position.getY(), position.getZ() };
		final int[] currentPos = new int[3];
		final int[] edgesIThink = new int[] { 1, dims[0] + 3, (dims[0] + 3) * (dims[1] + 3) };
		final float[] grid = new float[8];
		final float[][] buffer = new float[edgesIThink[2] * 2][3];
		int bufno = 1;

		// "Resize buffer if necessary" is what mikolalysenko said, but Click_Me seems to have removed this code. This is probably because the buffer should never (and actually
		// can't be in java) be resized

		// March over the voxel grid
		for (currentPos[2] = 0; currentPos[2] < (dims[2] + 1); edgesIThink[2] = -edgesIThink[2], ++currentPos[2], bufno ^= 1) {

			// m is the pointer into the buffer we are going to use.
			// "This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(" is what mikolalysenko said, it
			// obviously doesn't apply here
			// The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int m = 1 + ((dims[0] + 3) * (1 + (bufno * (dims[1] + 3))));

			for (currentPos[1] = 0; currentPos[1] < (dims[1] + 1); ++currentPos[1], m += 2) {
				for (currentPos[0] = 0; currentPos[0] < (dims[0] + 1); ++currentPos[0], ++m) {

					// Read in 8 field values around this vertex and store them in an array
					// Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
					int mask = 0;
					int g = 0;

					for (int z = 0; z < 2; ++z) {
						for (int y = 0; y < 2; ++y) {
							for (int x = 0; x < 2; ++g) {
								// TODO: mutableblockpos?
								// final float p = potential.apply(new BlockPos(c[0] + x[0] + i, c[1] + x[1] + j, c[2] + x[2] + k), cache);

								final float p = ModUtil.getBlockDensity(new BlockPos(startPos[0] + currentPos[0] + x, startPos[1] + currentPos[1] + y, startPos[2] + currentPos[2] + z), cache);
								grid[g] = p;
								mask |= p > 0.0F ? 1 << g : 0;
								++x;

							}
						}
					}

					// Check for early termination if cell does not intersect boundary
					if ((mask == 0) || (mask == 0xff)) {
						continue;
					}

					IBlockState state = Blocks.AIR.getDefaultState();

					final MutableBlockPos pos = new MutableBlockPos();
					getStateAndPos: for (int y = -1; y < 2; ++y) {
						for (int z = -1; z < 2; ++z) {
							for (int x = -1; x < 2; ++x) {
								pos.setPos(startPos[0] + currentPos[0] + x, startPos[1] + currentPos[1] + y, startPos[2] + currentPos[2] + z);
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

					final int[] brightnessPos = new int[] { startPos[0] + currentPos[0], startPos[1] + currentPos[1] + 1, startPos[2] + currentPos[2] };

					getBrightnessPos: for (int y = -1; y < 2; ++y) {
						for (int z = -2; z < 3; ++z) {
							for (int x = -1; x < 2; ++x) {
								// TODO: mutableblockpos?
								final IBlockState tempState = cache.getBlockState(new BlockPos(startPos[0] + currentPos[0] + x, startPos[1] + currentPos[1] + y, startPos[2] + currentPos[2] + z));
								if (!tempState.isOpaqueCube()) {
									brightnessPos[0] = startPos[0] + currentPos[0] + x;
									brightnessPos[1] = startPos[1] + currentPos[1] + y;
									brightnessPos[2] = startPos[2] + currentPos[2] + z;
									break getBrightnessPos;
								}
							}
						}
					}

					// Sum up edge intersections
					final int edge_mask = ModUtil.SURFACE_NETS_EDGE_TABLE[mask];
					int e_count = 0;
					final float[] v = new float[] { 0.0F, 0.0F, 0.0F };

					// For every edge of the cube...
					for (int i = 0; i < 12; ++i) {

						// Use edge mask to check if it is crossed
						if ((edge_mask & (1 << i)) == 0) {
							continue;
						}

						// If it did, increment number of edge crossings
						++e_count;

						// Now find the point of intersection
						final int e0 = ModUtil.SURFACE_NETS_CUBE_EDGES[i << 1]; // Unpack vertices
						final int e1 = ModUtil.SURFACE_NETS_CUBE_EDGES[(i << 1) + 1];
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

					// get model
					final IBakedModel model = event.getBlockRendererDispatcher().getModelForState(state);
					if (model == null) {
						continue;
					}

					List<BakedQuad> quads = model.getQuads(state, EnumFacing.UP, MathHelper.getPositionRandom(pos));
					if ((quads == null) || quads.isEmpty()) {
						quads = model.getQuads(state, null, MathHelper.getPositionRandom(pos));
						if ((quads == null) || quads.isEmpty()) {
							continue;
						}
					}
					final BakedQuad quad = quads.get(0);
					if (quad == null) {
						continue;
					}

					final TextureAtlasSprite sprite = quad.getSprite();
					if (sprite == null) {
						continue;
					}

					final float redFloat;
					final float greenFloat;
					final float blueFloat;

					if (quad.hasTintIndex()) {
						final int colorMultiplier = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, cache, pos, 0);
						redFloat = ((colorMultiplier >> 16) & 255) / 255.0F;
						greenFloat = ((colorMultiplier >> 8) & 255) / 255.0F;
						blueFloat = (colorMultiplier & 255) / 255.0F;
					} else {
						redFloat = 1;
						greenFloat = 1;
						blueFloat = 1;
					}

					final BlockRenderLayer blockRenderLayer = state.getBlock().getRenderLayer();
					final BufferBuilder bufferBuilder = event.startOrContinueLayer(blockRenderLayer);
					event.setBlockRenderLayerUsedWithOrOpperation(blockRenderLayer, true);

					final int red = (int) (0xFF * redFloat);
					final int green = (int) (0xFF * greenFloat);
					final int blue = (int) (0xFF * blueFloat);
					final int alpha = 0xFF;
					final double minU = sprite.getMinU();
					final double maxU = sprite.getMaxU();
					final double minV = sprite.getMinV();
					final double maxV = sprite.getMaxV();

					final BlockPos brightnessBlockPos = new BlockPos(brightnessPos[0], brightnessPos[1], brightnessPos[2]);
					final IBlockState brightnessState = cache.getBlockState(brightnessBlockPos);

					final int brightness = brightnessState.getPackedLightmapCoords(cache, brightnessBlockPos);
					final int lightmapSkyLight = (brightness >> 16) & 65535;
					final int lightmapBlockLight = brightness & 65535;

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

						// Remember to flip orientation depending on the sign of the corner.
						if ((mask & 1) != 0) {
							bufferBuilder.pos(v0[0], v0[1], v0[2]).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(v1[0], v1[1], v1[2]).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(v2[0], v2[1], v2[2]).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(v3[0], v3[1], v3[2]).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
						} else {
							bufferBuilder.pos(v0[0], v0[1], v0[2]).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(v3[0], v3[1], v3[2]).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(v2[0], v2[1], v2[2]).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(v1[0], v1[1], v1[2]).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
						}
					}

				}

			}
		}

		for (final BlockPos pos : event.getChunkBlockPositions()) {
			final IBlockState state = cache.getBlockState(pos);

			final Block block = state.getBlock();

			if (ModUtil.shouldSmooth(state)) {
				continue;
			}

			for (final BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
				if (!block.canRenderInLayer(state, blockRenderLayer)) {
					continue;
				}
				net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockRenderLayer);

				if (state.getRenderType() != EnumBlockRenderType.INVISIBLE) {
					final BufferBuilder bufferBuilder = event.startOrContinueLayer(blockRenderLayer);

					final boolean wasLayerUsed = event.getBlockRendererDispatcher().renderBlock(state, pos, cache, bufferBuilder);

					event.setBlockRenderLayerUsedWithOrOpperation(blockRenderLayer, wasLayerUsed);

				}
			}
			net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
		}

		ModUtil.smoothWater(event);

	}

	@SubscribeEvent(receiveCanceled = true)
	public static void rebuildChunkBlocksSURFACE_NETS_OOP(final RebuildChunkBlocksEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (!ModConfig.getActiveRenderingAlgorithms().contains(RenderAlgorithm.SURFACE_NETS_OOP)) {
			return;
		}

		event.setCanceled(true);

		final ChunkCache cache = event.getWorldView();
		final BlockPos position = event.getRenderChunkPosition();
		final BiFunction<BlockPos, IBlockAccess, Float> potential = (positionOfPotentiallySmoothableBlock, blockAccess) -> {
			return ModUtil.getBlockDensity(positionOfPotentiallySmoothableBlock, blockAccess);
		};

		final SurfaceNet surfaceNet = ModUtil.generateSurfaceNet(position, cache, potential);

		for (final BlockPosSurfaceNetInfo posInfo : surfaceNet.posInfos) {
			final BlockPos pos = posInfo.pos;
			final IBlockState state = posInfo.state;
			final BlockPos brightnessBlockPos = posInfo.brightnessPos;
			final Block block = state.getBlock();

			// get model
			final IBakedModel model = event.getBlockRendererDispatcher().getModelForState(state);
			if (model == null) {
				continue;
			}

			// get north quads OR null quads
			List<BakedQuad> quads = model.getQuads(state, EnumFacing.UP, MathHelper.getPositionRandom(pos));
			if ((quads == null) || quads.isEmpty()) {
				quads = model.getQuads(state, null, MathHelper.getPositionRandom(pos));
				if ((quads == null) || quads.isEmpty()) {
					continue;
				}
			}

			// get first quad
			final BakedQuad quad = quads.get(0);
			if (quad == null) {
				continue;
			}

			// get sprite
			final TextureAtlasSprite sprite = quad.getSprite();
			if (sprite == null) {
				continue;
			}

			final float redFloat;
			final float greenFloat;
			final float blueFloat;

			if (quad.hasTintIndex()) {
				final int colorMultiplier = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, cache, pos, 0);
				redFloat = ((colorMultiplier >> 16) & 255) / 255.0F;
				greenFloat = ((colorMultiplier >> 8) & 255) / 255.0F;
				blueFloat = (colorMultiplier & 255) / 255.0F;
			} else {
				redFloat = 1;
				greenFloat = 1;
				blueFloat = 1;
			}

			final int red = (int) (0xFF * redFloat);
			final int green = (int) (0xFF * greenFloat);
			final int blue = (int) (0xFF * blueFloat);
			final int alpha = 0xFF;
			final double minU = sprite.getMinU();
			final double maxU = sprite.getMaxU();
			final double minV = sprite.getMinV();
			final double maxV = sprite.getMaxV();

			final IBlockState brightnessState = cache.getBlockState(brightnessBlockPos);

			final int brightness = brightnessState.getPackedLightmapCoords(cache, brightnessBlockPos);
			final int lightmapSkyLight = (brightness >> 16) & 65535;
			final int lightmapBlockLight = brightness & 65535;

			for (final BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
				if (!block.canRenderInLayer(state, blockRenderLayer)) {
					continue;
				}
				net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockRenderLayer);

				if (state.getRenderType() != EnumBlockRenderType.INVISIBLE) {
					final BufferBuilder bufferBuilder = event.startOrContinueLayer(blockRenderLayer);

					boolean wasLayerUsed = false;
					for (final QuadVertexList vertexList : posInfo.vertexList) {

						bufferBuilder.pos(vertexList.vertex1.x, vertexList.vertex1.y, vertexList.vertex1.z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
						bufferBuilder.pos(vertexList.vertex2.x, vertexList.vertex2.y, vertexList.vertex2.z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
						bufferBuilder.pos(vertexList.vertex3.x, vertexList.vertex3.y, vertexList.vertex3.z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
						bufferBuilder.pos(vertexList.vertex4.x, vertexList.vertex4.y, vertexList.vertex4.z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
						wasLayerUsed = true;
					}
					event.setBlockRenderLayerUsedWithOrOpperation(blockRenderLayer, wasLayerUsed);
				}
			}
			net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
		}

		for (final BlockPos.MutableBlockPos blockpos$mutableblockpos : event.getChunkBlockPositions()) {
			final IBlockState state = event.getWorldView().getBlockState(blockpos$mutableblockpos);
			final Block block = state.getBlock();

			if (ModUtil.shouldSmooth(state)) {
				continue;
			}

			for (final BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
				if (!block.canRenderInLayer(state, blockRenderLayer)) {
					continue;
				}
				net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockRenderLayer);

				if (block.getDefaultState().getRenderType() != EnumBlockRenderType.INVISIBLE) {
					final BufferBuilder bufferbuilder = event.startOrContinueLayer(blockRenderLayer);

					final boolean used = event.getBlockRendererDispatcher().renderBlock(state, blockpos$mutableblockpos, event.getWorldView(), bufferbuilder);

					event.setBlockRenderLayerUsedWithOrOpperation(blockRenderLayer, used);

				}
			}
			net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
		}

		ModUtil.smoothWater(event);

	}

}
