package cadiboo.nocubes.renderer;

import cadiboo.nocubes.NoCubes;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class SurfaceNets {
	public static int[]	cube_edges	= new int[24];
	public static int[]	edge_table	= new int[256];

	public static float getBlockDensity(final int x, final int y, final int z, final IBlockAccess cache) {
		float dens = 0.0F;

		for (int k = 0; k < 2; ++k) {
			for (int j = 0; j < 2; ++j) {
				for (int i = 0; i < 2; ++i) {
					final Block block = cache.func_147439_a(x - i, y - j, z - k);
					if (NoCubes.isBlockNatural(block)) {
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
		if (!NoCubes.isNoCubesEnabled) {
			return false;
		} else if (pass != 0) {
			return false;
		} else {
			final Tessellator tess = Tessellator.field_78398_a;
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

						int meta;
						final int k;
						for (int k = 0; k < 2; ++k) {
							for (meta = 0; meta < 2; ++meta) {
								for (k = 0; k < 2; ++g) {
									final float p = getBlockDensity(c[0] + x[0] + k, c[1] + x[1] + meta, c[2] + x[2] + k, cache);
									grid[g] = p;
									mask |= p > 0.0F ? 1 << g : 0;
									++k;
								}
							}
						}

						if ((mask != 0) && (mask != 255)) {
							Block block = Blocks.field_150350_a;
							meta = 0;

							int j;
							int k;
							label216: for (k = -1; k < 2; ++k) {
								for (k = -1; k < 2; ++k) {
									for (j = -1; j < 2; ++j) {
										final Block b = cache.func_147439_a(c[0] + x[0] + j, c[1] + x[1] + k, c[2] + x[2] + k);
										if (NoCubes.isBlockNatural(b) && (block != Blocks.field_150431_aC) && (block != Blocks.field_150349_c)) {
											block = b;
											meta = cache.func_72805_g(c[0] + x[0] + j, c[1] + x[1] + k, c[2] + x[2] + k);
											if ((b == Blocks.field_150431_aC) || (b == Blocks.field_150349_c)) {
												break label216;
											}
										}
									}
								}
							}

							final int[] br = new int[] { c[0] + x[0], c[1] + x[1] + 1, c[2] + x[2] };

							label193: for (k = -1; k < 2; ++k) {
								for (j = -2; j < 3; ++j) {
									for (int i = -1; i < 2; ++i) {
										final Block b = cache.func_147439_a(c[0] + x[0] + i, c[1] + x[1] + k, c[2] + x[2] + j);
										if (!b.func_149662_c()) {
											br[0] = c[0] + x[0] + i;
											br[1] = c[1] + x[1] + k;
											br[2] = c[2] + x[2] + j;
											break label193;
										}
									}
								}
							}

							final IIcon icon = renderer.func_147787_a(block, 1, meta);
							final double tu0 = (double) icon.func_94209_e();
							final double tu1 = (double) icon.func_94212_f();
							final double tv0 = (double) icon.func_94206_g();
							final double tv1 = (double) icon.func_94210_h();
							final int edgemask = edge_table[mask];
							int ecount = 0;
							final float[] v = new float[] { 0.0F, 0.0F, 0.0F };
							int i = 0;

							label176: while (true) {
								int e0;
								int e1;
								int j;
								int iu;
								int iv;
								int du;
								if (i >= 12) {
									final float s = 1.0F / ecount;

									for (e0 = 0; e0 < 3; ++e0) {
										v[e0] = c[e0] + x[e0] + (s * v[e0]);
									}

									e0 = x[0] == 16 ? 0 : x[0];
									e1 = x[1] == 16 ? 0 : x[1];
									final int tz = x[2] == 16 ? 0 : x[2];
									long i1 = e0 * 3129871 ^ (tz * 116129781L) ^ e1;
									i1 = (i1 * i1 * 42317861L) + (i1 * 11L);
									v[0] = (float) (v[0] - ((((i1 >> 16) & 15L) / 15.0F - 0.5D) * 0.2D));
									v[1] = (float) (v[1] - ((((i1 >> 20) & 15L) / 15.0F - 1.0D) * 0.2D));
									v[2] = (float) (v[2] - ((((i1 >> 24) & 15L) / 15.0F - 0.5D) * 0.2D));
									buffer[m] = v;
									j = 0;

									while (true) {
										if (j >= 3) {
											break label176;
										}

										if ((edgemask & (1 << j)) != 0) {
											iu = (j + 1) % 3;
											iv = (j + 2) % 3;
											if ((x[iu] != 0) && (x[iv] != 0)) {
												du = r[iu];
												final int dv = r[iv];
												tess.func_78380_c(block.func_149677_c(Minecraft.func_71410_x().field_71441_e, br[0], br[1], br[2]));
												tess.func_78378_d(block.func_149720_d(cache, c[0] + x[0], c[1] + x[1], c[2] + x[2]));
												final float[] v0 = buffer[m];
												final float[] v1 = buffer[m - du];
												final float[] v2 = buffer[m - du - dv];
												final float[] v3 = buffer[m - dv];
												if ((mask & 1) != 0) {
													tess.func_78374_a((double) v0[0], (double) v0[1], (double) v0[2], tu0, tv1);
													tess.func_78374_a((double) v1[0], (double) v1[1], (double) v1[2], tu1, tv1);
													tess.func_78374_a((double) v2[0], (double) v2[1], (double) v2[2], tu1, tv0);
													tess.func_78374_a((double) v3[0], (double) v3[1], (double) v3[2], tu0, tv0);
												} else {
													tess.func_78374_a((double) v0[0], (double) v0[1], (double) v0[2], tu0, tv1);
													tess.func_78374_a((double) v3[0], (double) v3[1], (double) v3[2], tu1, tv1);
													tess.func_78374_a((double) v2[0], (double) v2[1], (double) v2[2], tu1, tv0);
													tess.func_78374_a((double) v1[0], (double) v1[1], (double) v1[2], tu0, tv0);
												}
											}
										}

										++j;
									}
								}

								if ((edgemask & (1 << i)) != 0) {
									++ecount;
									e0 = cube_edges[i << 1];
									e1 = cube_edges[(i << 1) + 1];
									final float g0 = grid[e0];
									final float g1 = grid[e1];
									float t = g0 - g1;
									if (Math.abs(t) > 0.0F) {
										t = g0 / t;
										j = 0;

										for (iu = 1; j < 3; iu <<= 1) {
											iv = e0 & iu;
											du = e1 & iu;
											if (iv != du) {
												v[j] += iv != 0 ? 1.0F - t : t;
											} else {
												v[j] += iv != 0 ? 1.0F : 0.0F;
											}

											++j;
										}
									}
								}

								++i;
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

		int i;
		int em;
		int j;
		for (i = 0; i < 8; ++i) {
			for (em = 1; em <= 4; em <<= 1) {
				j = i ^ em;
				if (i <= j) {
					cube_edges[k++] = i;
					cube_edges[k++] = j;
				}
			}
		}

		for (i = 0; i < 256; ++i) {
			em = 0;

			for (j = 0; j < 24; j += 2) {
				final boolean a = (i & (1 << cube_edges[j])) != 0;
				final boolean b = (i & (1 << cube_edges[j + 1])) != 0;
				em |= a != b ? 1 << (j >> 1) : 0;
			}

			edge_table[i] = em;
		}

	}
}
