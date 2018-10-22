package com.cosmicdan.nocubes.renderer;

import com.cosmicdan.nocubes.Main;
import com.cosmicdan.nocubes.ModConfig;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

/*
 * Original by Click_Me
 * Reverse-engineered and re-implemented (with permission) by CosmicDan
 * Refactored and updated to 1.12.x by Cadiboo
 */
public class SurfaceNets {
	public static int[]	cube_edges	= new int[24];
	public static int[]	edge_table	= new int[256];

	public static float getBlockDensity(final int x, final int y, final int z, final IBlockAccess cache) {
		float dens = 0.0F;

		for (int k = 0; k < 2; ++k) {
			for (int j = 0; j < 2; ++j) {
				for (int i = 0; i < 2; ++i) {
					final Block block = cache.getBlock(x - i, y - j, z - k);
					if (Main.shouldSmooth(block)) {
						++dens;
					} else {
						--dens;
					}
				}
			}
		}
		return dens;
	}

	public static boolean renderChunk(final int pass, final int cx, final int cy, final int cz, final IBlockAccess cache, final RenderBlocks renderer) {
		if (!ModConfig.MOD_ENABLED) {
			return false;
		} else if (pass != 0) {
			return false;
		} else {
			final Tessellator tess = Tessellator.instance;
			final int[] dims = new int[] { 16, 16, 16 };
			final int[] c = new int[] { cx, cy, cz };
			final int[] x = new int[3];
			final int[] r = new int[] { 1, dims[0] + 3, (dims[0] + 3) * (dims[1] + 3) };
			final float[] grid = new float[8];
			final float[][] buffer = new float[r[2] * 2][3];
			int bufno = 1;

			for (x[2] = 0; x[2] < (dims[2] + 1); r[2] = -r[2]) {
				int m = 1 + ((dims[0] + 3) * (1 + (bufno * (dims[1] + 3))));

				for (x[1] = 0; x[1] < (dims[1] + 1); m += 2) {
					for (x[0] = 0; x[0] < (dims[0] + 1); ++m) {
						int mask = 0;
						int g = 0;

						for (int k = 0; k < 2; ++k) {
							for (int j = 0; j < 2; ++j) {
								for (int i = 0; i < 2; ++g) {
									final float p = getBlockDensity(c[0] + x[0] + i, c[1] + x[1] + j, c[2] + x[2] + k, cache);
									grid[g] = p;
									mask |= p > 0.0F ? 1 << g : 0;
									++i;
								}
							}
						}

						if ((mask != 0) && (mask != 255)) {
							Block block = Blocks.air;
							int meta = 0;

							label368: for (int k = -1; k < 2; ++k) {
								for (int j = -1; j < 2; ++j) {
									for (int i = -1; i < 2; ++i) {
										final Block b = cache.getBlock(c[0] + x[0] + i, c[1] + x[1] + k, c[2] + x[2] + j);
										if (Main.shouldSmooth(b) && (block != Blocks.snow_layer) && (block != Blocks.grass)) {
											block = b;
											meta = cache.getBlockMetadata(c[0] + x[0] + i, c[1] + x[1] + k, c[2] + x[2] + j);
											if ((b == Blocks.snow_layer) || (b == Blocks.grass)) {
												break label368;
											}
										}
									}
								}
							}

							final int[] br = new int[] { c[0] + x[0], c[1] + x[1] + 1, c[2] + x[2] };

							label594: for (int k = -1; k < 2; ++k) {
								for (int j = -2; j < 3; ++j) {
									for (int i = -1; i < 2; ++i) {
										final Block b = cache.getBlock(c[0] + x[0] + i, c[1] + x[1] + k, c[2] + x[2] + j);
										if (!b.isOpaqueCube()) {
											br[0] = c[0] + x[0] + i;
											br[1] = c[1] + x[1] + k;
											br[2] = c[2] + x[2] + j;
											break label594;
										}
									}
								}
							}

							final IIcon icon = renderer.getBlockIconFromSideAndMetadata(block, 1, meta);
							final double tu0 = (double) icon.getMinU();
							final double tu1 = (double) icon.getMaxU();
							final double tv0 = (double) icon.getMinV();
							final double tv1 = (double) icon.getMaxV();
							final int edgemask = edge_table[mask];
							int ecount = 0;
							final float[] v = new float[] { 0.0F, 0.0F, 0.0F };

							for (int i = 0; i < 12; ++i) {
								if ((edgemask & (1 << i)) != 0) {
									++ecount;
									final int e0 = cube_edges[i << 1];
									final int e1 = cube_edges[(i << 1) + 1];
									final float g0 = grid[e0];
									final float g1 = grid[e1];
									float t = g0 - g1;
									if (Math.abs(t) > 0.0F) {
										t = g0 / t;
										int j = 0;

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
							}

							final float s = 1.0F / ecount;

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
							buffer[m] = v;

							for (int i = 0; i < 3; ++i) {
								if ((edgemask & (1 << i)) != 0) {
									final int iu = (i + 1) % 3;
									final int iv = (i + 2) % 3;
									if ((x[iu] != 0) && (x[iv] != 0)) {
										final int du = r[iu];
										final int dv = r[iv];
										tess.setBrightness(block.getMixedBrightnessForBlock(Minecraft.getMinecraft().theWorld, br[0], br[1], br[2]));
										tess.setColorOpaque_I(block.colorMultiplier(cache, c[0] + x[0], c[1] + x[1], c[2] + x[2]));
										final float[] v0 = buffer[m];
										final float[] v1 = buffer[m - du];
										final float[] v2 = buffer[m - du - dv];
										final float[] v3 = buffer[m - dv];
										if ((mask & 1) != 0) {
											tess.addVertexWithUV((double) v0[0], (double) v0[1], (double) v0[2], tu0, tv1);
											tess.addVertexWithUV((double) v1[0], (double) v1[1], (double) v1[2], tu1, tv1);
											tess.addVertexWithUV((double) v2[0], (double) v2[1], (double) v2[2], tu1, tv0);
											tess.addVertexWithUV((double) v3[0], (double) v3[1], (double) v3[2], tu0, tv0);
										} else {
											tess.addVertexWithUV((double) v0[0], (double) v0[1], (double) v0[2], tu0, tv1);
											tess.addVertexWithUV((double) v3[0], (double) v3[1], (double) v3[2], tu1, tv1);
											tess.addVertexWithUV((double) v2[0], (double) v2[1], (double) v2[2], tu1, tv0);
											tess.addVertexWithUV((double) v1[0], (double) v1[1], (double) v1[2], tu0, tv0);
										}
									}
								}
							}
						}

						++x[0];
					}

					++x[1];
				}

				++x[2];
				bufno ^= 1;
			}

			return true;
		}
	}

	static {
		int k = 0;

		for (int i = 0; i < 8; ++i) {
			for (int j = 1; j <= 4; j <<= 1) {
				final int p = i ^ j;
				if (i <= p) {
					cube_edges[k++] = i;
					cube_edges[k++] = p;
				}
			}
		}

		for (int i = 0; i < 256; ++i) {
			int em = 0;

			for (int j = 0; j < 24; j += 2) {
				final boolean a = (i & (1 << cube_edges[j])) != 0;
				final boolean b = (i & (1 << cube_edges[j + 1])) != 0;
				em |= a != b ? 1 << (j >> 1) : 0;
			}

			edge_table[i] = em;
		}

	}
}
