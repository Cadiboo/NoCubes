package cadiboo.nocubes.renderer;

import cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;

public class SurfaceNets {

	public static final int[] CUBE_EDGES = new int[24];
	public static final int[] EDGE_TABLE = new int[256];

	//Precompute edge table, like Paul Bourke does.
	// This saves a bit of time when computing the centroid of each boundary cell
	static {

		//Initialize the cube_edges table
		// This is just the vertex number of each cube
		int k = 0;
		for (int i = 0; i < 8; ++ i) {
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

		for (int i = 0; i < 256; ++ i) {
			int em = 0;
			for (int j = 0; j < 24; j += 2) {
				final boolean a = (i & (1 << CUBE_EDGES[j])) != 0;
				final boolean b = (i & (1 << CUBE_EDGES[j + 1])) != 0;
				em |= a != b ? 1 << (j >> 1) : 0;
			}
			EDGE_TABLE[i] = em;
		}

	}

	public static boolean renderBlock(IBlockState state, final BlockPos pos, final ChunkCache cache, final BufferBuilder bufferBuilder, final BlockRendererDispatcher blockRendererDispatcher) {

		// For every edge crossing the boundary, create an (n-1) cell.  (Face in 3D)
		// For every face crossing the boundary, create an (n-2) cell. (Edge in 3D)
		// …
		// For every d-dimensional cell, create an (n-d) cell.
		// …
		// For every n-cell, create a vertex.

		//Read in 8 field values around this vertex and store them in an array
		//Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later

		// 8-bit mask (1 bit for every cube corner)
		int mask = 0b00000000;
		// grid of densities (1 slot for every cube neighbour)
		float[] densityGrid = { 0, 0, 0, 0, 0, 0, 0, 0 };

		int pointIndex = 0;
		for (BlockPos.MutableBlockPos edgePos : BlockPos.getAllInBoxMutable(pos.add(- 1, - 1, - 1), pos.add(1, 1, 1))) {
			if (edgePos.equals(pos)) {
				continue;
			}
			pointIndex++;
			final float density = ModUtil.getBlockDensity(edgePos, cache);
			densityGrid[pointIndex] = density;
			mask |= density > 0.0F ? 1 << pointIndex : 0;
		}

		//Check for early termination if cell does not intersect boundary
		if (mask == 0b00000000 || mask == 0b11111111) {
			return false;
		}

		//Sum up edge intersections
		int edge_mask = EDGE_TABLE[mask];
		float[] v = { 0.0f, 0.0f, 0.0f };
		int e_count = 0;

		//For every edge of the cube...
		for (int i = 0; i < 12; ++ i) {

			//Use edge mask to check if it is crossed
			// if (! (edge_mask & (1 << i))) {
			if ((edge_mask & (1 << i)) != 0) {
				continue;
			}

			//If it did, increment number of edge crossings
			++ e_count;

			//Now find the point of intersection
			int e0 = CUBE_EDGES[i << 1]; //Unpack vertices
			int e1 = CUBE_EDGES[(i << 1) + 1];
			float g0 = densityGrid[e0]; //Unpack grid values
			float g1 = densityGrid[e1];
			float t = g0 - g1; //Compute point of intersection
			if (Math.abs(t) > 1e-6) {
				t = g0 / t;
			} else {
				continue;
			}

			// Interpolate vertices and add up intersections (this can be done without multiplying)
			//			for (int j = 0, k = 1; j < 3; ++ j, k <<= 1) {
			//				int a = e0 & k, b = e1 & k;
			//				if (a ! = b){
			//					v[j] += a ? 1.0 - t : t;
			//				} else{
			//					v[j] += a ? 1.0 : 0;
			//				}
			//			}
			for (int j = 0, k = 1; j < 3; ++ j, k <<= 1) {
				int a = e0 & k;
				int b = e1 & k;
				if (a != b) {
					v[j] += a != 0 ? 1.0F - t : t;
				} else {
					v[j] += a != 0 ? 1.0F : 0.0F;
				}

				++ j;
			}

		}

		//Now we just average the edge intersections and add them to coordinate
		float s = 1.0f / e_count;
		for (int i = 0; i < 3; ++ i) {
//			v[i] = x[i] + s * v[i];
		}

		//		//Add vertex to buffer, store pointer to vertex index in buffer
		//		buffer[m] = vertices.length;
		//		vertices.push(v);

		//Now we need to add faces together, to do this we just loop over 3 basis components
		for (int i = 0; i < 3; ++ i) {
			//The first three entries of the edge_mask count the crossings along the edge
			// if (! (edge_mask & (1 << i))) {
			if ((edge_mask & (1 << i)) == 0) {
				continue;
			}

			// i = axes we are point along.  iu, iv = orthogonal axes
			int iu = (i + 1) % 3, iv = (i + 2) % 3;

			//			//If we are on a boundary, skip it
			//			if (x[iu] == 0 || x[iv] == 0) {
			//				continue;
			//			}
			//
			//			//Otherwise, look up adjacent edges in buffer
			//			int du = R[iu], dv = R[iv];
			//
			//			//Remember to flip orientation depending on the sign of the corner.
			//			if (mask & 1) {
			//				faces.push([buffer[m], buffer[m - du], buffer[m - du - dv], buffer[m - dv]]);
			//			} else {
			//				faces.push([buffer[m], buffer[m - dv], buffer[m - du - dv], buffer[m - du]]);
			//			}
		}

		return false;
	}

	//	public static float getBlockDensity(final int x, final int y, final int z, final IBlockAccess cache) {
	//		float dens = 0.0F;
	//
	//		for (int k = 0; k < 2; ++k) {
	//			for (int j = 0; j < 2; ++j) {
	//				for (int i = 0; i < 2; ++i) {
	//					final Block block = cache.getBlock(x - i, y - j, z - k);
	//					if (NoCubes.isBlockNatural(block)) {
	//						++dens;
	//					} else {
	//						--dens;
	//					}
	//				}
	//			}
	//		}
	//
	//		return dens;
	//	}
	//
	//	public static boolean renderChunk(final int pass, final int cx, final int cy, final int cz, final IBlockAccess cache) {
	//		if (!NoCubes.isNoCubesEnabled) {
	//			return false;
	//		} else if (pass != 0) {
	//			return false;
	//		} else {
	//			final Tessellator tess = Tessellator.getInstance();
	//			final int[] dims = new int[] { 16, 16, 16 };
	//			final int[] c = new int[] { cx, cy, cz };
	//			final int[] x = new int[3];
	//			final int[] r = new int[] { 1, dims[0] + 3, (dims[0] + 3) * (dims[1] + 3) };
	//			final float[] grid = new float[8];
	//			final float[][] buffer = new float[r[2] * 2][3];
	//			int bufno = 1;
	//
	//			for (x[2] = 0; x[2] < (dims[2] + 1); r[2] = -r[2]) {
	//				int m = 1 + ((dims[0] + 3) * (1 + (bufno * (dims[1] + 3))));
	//
	//				for (x[1] = 0; x[1] < (dims[1] + 1); m += 2) {
	//					for (x[0] = 0; x[0] < (dims[0] + 1); ++m) {
	//						int mask = 0;
	//						int g = 0;
	//
	//						int meta;
	//						for (int k = 0; k < 2; ++k) {
	//							for (meta = 0; meta < 2; ++meta) {
	//								for (k = 0; k < 2; ++g) {
	//									final float p = getBlockDensity(c[0] + x[0] + k, c[1] + x[1] + meta, c[2] + x[2] + k, cache);
	//									grid[g] = p;
	//									mask |= p > 0.0F ? 1 << g : 0;
	//									++k;
	//								}
	//							}
	//						}
	//
	//						if ((mask != 0) && (mask != 255)) {
	//							Block block = Blocks.AIR;
	//							meta = 0;
	//
	//							int j;
	//							int k;
	//							label216: for (k = -1; k < 2; ++k) {
	//								for (k = -1; k < 2; ++k) {
	//									for (j = -1; j < 2; ++j) {
	//										final Block b = cache.getBlock(c[0] + x[0] + j, c[1] + x[1] + k, c[2] + x[2] + k);
	//										if (NoCubes.isBlockNatural(b) && (block != Blocks.SNOW_LAYER) && (block != Blocks.GRASS)) {
	//											block = b;
	//											meta = cache.getBlockMetadata(c[0] + x[0] + j, c[1] + x[1] + k, c[2] + x[2] + k);
	//											if ((b == Blocks.SNOW_LAYER) || (b == Blocks.GRASS)) {
	//												break label216;
	//											}
	//										}
	//									}
	//								}
	//							}
	//
	//							final int[] br = new int[] { c[0] + x[0], c[1] + x[1] + 1, c[2] + x[2] };
	//
	//							label193: for (k = -1; k < 2; ++k) {
	//								for (j = -2; j < 3; ++j) {
	//									for (int i = -1; i < 2; ++i) {
	//										final Block b = cache.getBlock(c[0] + x[0] + i, c[1] + x[1] + k, c[2] + x[2] + j);
	//										if (!b.isOpaqueCube()) {
	//											br[0] = c[0] + x[0] + i;
	//											br[1] = c[1] + x[1] + k;
	//											br[2] = c[2] + x[2] + j;
	//											break label193;
	//										}
	//									}
	//								}
	//							}
	//
	//							final IIcon icon = renderer.getBlockIconFromSideAndMetadata(block, 1, meta);
	//							final double tu0 = (double) icon.func_94209_e();
	//							final double tu1 = (double) icon.func_94212_f();
	//							final double tv0 = (double) icon.func_94206_g();
	//							final double tv1 = (double) icon.func_94210_h();
	//							final int edgemask = EDGE_TABLE[mask];
	//							int ecount = 0;
	//							final float[] v = new float[] { 0.0F, 0.0F, 0.0F };
	//							int i = 0;
	//
	//							label176: while (true) {
	//								int e0;
	//								int e1;
	//								int j;
	//								int iu;
	//								int iv;
	//								int du;
	//								if (i >= 12) {
	//									final float s = 1.0F / ecount;
	//
	//									for (e0 = 0; e0 < 3; ++e0) {
	//										v[e0] = c[e0] + x[e0] + (s * v[e0]);
	//									}
	//
	//									e0 = x[0] == 16 ? 0 : x[0];
	//									e1 = x[1] == 16 ? 0 : x[1];
	//									final int tz = x[2] == 16 ? 0 : x[2];
	//									long i1 = (e0 * 3129871) ^ (tz * 116129781L) ^ e1;
	//									i1 = (i1 * i1 * 42317861L) + (i1 * 11L);
	//									v[0] = (float) (v[0] - (((((i1 >> 16) & 15L) / 15.0F) - 0.5D) * 0.2D));
	//									v[1] = (float) (v[1] - (((((i1 >> 20) & 15L) / 15.0F) - 1.0D) * 0.2D));
	//									v[2] = (float) (v[2] - (((((i1 >> 24) & 15L) / 15.0F) - 0.5D) * 0.2D));
	//									buffer[m] = v;
	//									j = 0;
	//
	//									while (true) {
	//										if (j >= 3) {
	//											break label176;
	//										}
	//
	//										if ((edgemask & (1 << j)) != 0) {
	//											iu = (j + 1) % 3;
	//											iv = (j + 2) % 3;
	//											if ((x[iu] != 0) && (x[iv] != 0)) {
	//												du = r[iu];
	//												final int dv = r[iv];
	//												tess.func_78380_c(block.getMixedBrightnessForBlock(Minecraft.getMinecraft().theWorld, br[0], br[1], br[2]));
	//												tess.func_78378_d(block.colorMultiplier(cache, c[0] + x[0], c[1] + x[1], c[2] + x[2]));
	//												final float[] v0 = buffer[m];
	//												final float[] v1 = buffer[m - du];
	//												final float[] v2 = buffer[m - du - dv];
	//												final float[] v3 = buffer[m - dv];
	//												if ((mask & 1) != 0) {
	//													tess.addVertexWithUV((double) v0[0], (double) v0[1], (double) v0[2], tu0, tv1);
	//													tess.addVertexWithUV((double) v1[0], (double) v1[1], (double) v1[2], tu1, tv1);
	//													tess.addVertexWithUV((double) v2[0], (double) v2[1], (double) v2[2], tu1, tv0);
	//													tess.addVertexWithUV((double) v3[0], (double) v3[1], (double) v3[2], tu0, tv0);
	//												} else {
	//													tess.addVertexWithUV((double) v0[0], (double) v0[1], (double) v0[2], tu0, tv1);
	//													tess.addVertexWithUV((double) v3[0], (double) v3[1], (double) v3[2], tu1, tv1);
	//													tess.addVertexWithUV((double) v2[0], (double) v2[1], (double) v2[2], tu1, tv0);
	//													tess.addVertexWithUV((double) v1[0], (double) v1[1], (double) v1[2], tu0, tv0);
	//												}
	//											}
	//										}
	//
	//										++j;
	//									}
	//								}
	//
	//								if ((edgemask & (1 << i)) != 0) {
	//									++ecount;
	//									e0 = CUBE_EDGES[i << 1];
	//									e1 = CUBE_EDGES[(i << 1) + 1];
	//									final float g0 = grid[e0];
	//									final float g1 = grid[e1];
	//									float t = g0 - g1;
	//									if (Math.abs(t) > 0.0F) {
	//										t = g0 / t;
	//										j = 0;
	//
	//										for (iu = 1; j < 3; iu <<= 1) {
	//											iv = e0 & iu;
	//											du = e1 & iu;
	//											if (iv != du) {
	//												v[j] += iv != 0 ? 1.0F - t : t;
	//											} else {
	//												v[j] += iv != 0 ? 1.0F : 0.0F;
	//											}
	//
	//											++j;
	//										}
	//									}
	//								}
	//
	//								++i;
	//							}
	//						}
	//
	//						++x[0];
	//					}
	//
	//					++x[1];
	//				}
	//
	//				++x[2];
	//				bufno ^= 1;
	//			}
	//
	//			return true;
	//		}
	//	}
	//
	//	static {
	//		int k = 0;
	//
	//		int i;
	//		int em;
	//		int j;
	//		for (i = 0; i < 8; ++i) {
	//			for (em = 1; em <= 4; em <<= 1) {
	//				j = i ^ em;
	//				if (i <= j) {
	//					CUBE_EDGES[k++] = i;
	//					CUBE_EDGES[k++] = j;
	//				}
	//			}
	//		}
	//
	//		for (i = 0; i < 256; ++i) {
	//			em = 0;
	//
	//			for (j = 0; j < 24; j += 2) {
	//				final boolean a = (i & (1 << CUBE_EDGES[j])) != 0;
	//				final boolean b = (i & (1 << CUBE_EDGES[j + 1])) != 0;
	//				em |= a != b ? 1 << (j >> 1) : 0;
	//			}
	//
	//			EDGE_TABLE[i] = em;
	//		}
	//
	//	}
}
