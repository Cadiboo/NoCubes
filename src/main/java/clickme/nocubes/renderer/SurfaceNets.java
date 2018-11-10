package clickme.nocubes.renderer;

import net.minecraft.world.*;
import clickme.nocubes.*;
import net.minecraft.block.*;
import net.minecraft.client.renderer.*;
import net.minecraft.init.*;
import net.minecraft.client.*;
import net.minecraft.util.*;

public class SurfaceNets
{
    public static int[] cube_edges;
    public static int[] edge_table;
    
    public static float getBlockDensity(final int x, final int y, final int z, final IBlockAccess cache) {
        float dens = 0.0f;
        for (int k = 0; k < 2; ++k) {
            for (int j = 0; j < 2; ++j) {
                for (int i = 0; i < 2; ++i) {
                    final Block block = cache.func_147439_a(x - i, y - j, z - k);
                    if (NoCubes.isBlockNatural(block)) {
                        ++dens;
                    }
                    else {
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
        }
        if (pass != 0) {
            return false;
        }
        final Tessellator tess = Tessellator.field_78398_a;
        final int[] dims = { 16, 16, 16 };
        final int[] c = { cx, cy, cz };
        final int[] x = new int[3];
        final int[] r = { 1, dims[0] + 3, (dims[0] + 3) * (dims[1] + 3) };
        final float[] grid = new float[8];
        final float[][] buffer = new float[r[2] * 2][3];
        int bufno = 1;
        x[2] = 0;
        while (x[2] < dims[2] + 1) {
            int m = 1 + (dims[0] + 3) * (1 + bufno * (dims[1] + 3));
            x[1] = 0;
            while (x[1] < dims[1] + 1) {
                x[0] = 0;
                while (x[0] < dims[0] + 1) {
                    int mask = 0;
                    int g = 0;
                    for (int k = 0; k < 2; ++k) {
                        for (int j = 0; j < 2; ++j) {
                            for (int i = 0; i < 2; ++i, ++g) {
                                final float p = getBlockDensity(c[0] + x[0] + i, c[1] + x[1] + j, c[2] + x[2] + k, cache);
                                grid[g] = p;
                                mask |= ((p > 0.0f) ? (1 << g) : 0);
                            }
                        }
                    }
                    if (mask != 0) {
                        if (mask != 255) {
                            Block block = Blocks.field_150350_a;
                            int meta = 0;
                        Label_0523:
                            for (int l = -1; l < 2; ++l) {
                                for (int j2 = -1; j2 < 2; ++j2) {
                                    for (int i2 = -1; i2 < 2; ++i2) {
                                        final Block b = cache.func_147439_a(c[0] + x[0] + i2, c[1] + x[1] + l, c[2] + x[2] + j2);
                                        if (NoCubes.isBlockNatural(b) && block != Blocks.field_150431_aC && block != Blocks.field_150349_c) {
                                            block = b;
                                            meta = cache.func_72805_g(c[0] + x[0] + i2, c[1] + x[1] + l, c[2] + x[2] + j2);
                                            if (b == Blocks.field_150431_aC) {
                                                break Label_0523;
                                            }
                                            if (b == Blocks.field_150349_c) {
                                                break Label_0523;
                                            }
                                        }
                                    }
                                }
                            }
                            final int[] br = { c[0] + x[0], c[1] + x[1] + 1, c[2] + x[2] };
                        Label_0716:
                            for (int k2 = -1; k2 < 2; ++k2) {
                                for (int j3 = -2; j3 < 3; ++j3) {
                                    for (int i3 = -1; i3 < 2; ++i3) {
                                        final Block b2 = cache.func_147439_a(c[0] + x[0] + i3, c[1] + x[1] + k2, c[2] + x[2] + j3);
                                        if (!b2.func_149662_c()) {
                                            br[0] = c[0] + x[0] + i3;
                                            br[1] = c[1] + x[1] + k2;
                                            br[2] = c[2] + x[2] + j3;
                                            break Label_0716;
                                        }
                                    }
                                }
                            }
                            final IIcon icon = renderer.func_147787_a(block, 1, meta);
                            final double tu0 = icon.func_94209_e();
                            final double tu2 = icon.func_94212_f();
                            final double tv0 = icon.func_94206_g();
                            final double tv2 = icon.func_94210_h();
                            final int edgemask = SurfaceNets.edge_table[mask];
                            int ecount = 0;
                            final float[] v = { 0.0f, 0.0f, 0.0f };
                            for (int i4 = 0; i4 < 12; ++i4) {
                                if ((edgemask & 1 << i4) != 0x0) {
                                    ++ecount;
                                    final int e0 = SurfaceNets.cube_edges[i4 << 1];
                                    final int e2 = SurfaceNets.cube_edges[(i4 << 1) + 1];
                                    final float g2 = grid[e0];
                                    final float g3 = grid[e2];
                                    float t = g2 - g3;
                                    if (Math.abs(t) > 0.0f) {
                                        t = g2 / t;
                                        for (int j4 = 0, k3 = 1; j4 < 3; ++j4, k3 <<= 1) {
                                            final int a = e0 & k3;
                                            final int b3 = e2 & k3;
                                            if (a != b3) {
                                                final float[] array = v;
                                                final int n = j4;
                                                array[n] += ((a != 0) ? (1.0f - t) : t);
                                            }
                                            else {
                                                final float[] array2 = v;
                                                final int n2 = j4;
                                                array2[n2] += ((a != 0) ? 1.0f : 0.0f);
                                            }
                                        }
                                    }
                                }
                            }
                            final float s = 1.0f / ecount;
                            for (int i5 = 0; i5 < 3; ++i5) {
                                v[i5] = c[i5] + x[i5] + s * v[i5];
                            }
                            final int tx = (x[0] == 16) ? 0 : x[0];
                            final int ty = (x[1] == 16) ? 0 : x[1];
                            final int tz = (x[2] == 16) ? 0 : x[2];
                            long i6 = tx * 3129871 ^ tz * 116129781L ^ ty;
                            i6 = i6 * i6 * 42317861L + i6 * 11L;
                            final float[] array3 = v;
                            final int n3 = 0;
                            array3[n3] -= (float)(((i6 >> 16 & 0xFL) / 15.0f - 0.5) * 0.2);
                            final float[] array4 = v;
                            final int n4 = 1;
                            array4[n4] -= (float)(((i6 >> 20 & 0xFL) / 15.0f - 1.0) * 0.2);
                            final float[] array5 = v;
                            final int n5 = 2;
                            array5[n5] -= (float)(((i6 >> 24 & 0xFL) / 15.0f - 0.5) * 0.2);
                            buffer[m] = v;
                            for (int i7 = 0; i7 < 3; ++i7) {
                                if ((edgemask & 1 << i7) != 0x0) {
                                    final int iu = (i7 + 1) % 3;
                                    final int iv = (i7 + 2) % 3;
                                    if (x[iu] != 0) {
                                        if (x[iv] != 0) {
                                            final int du = r[iu];
                                            final int dv = r[iv];
                                            tess.func_78380_c(block.func_149677_c((IBlockAccess)Minecraft.func_71410_x().field_71441_e, br[0], br[1], br[2]));
                                            tess.func_78378_d(block.func_149720_d(cache, c[0] + x[0], c[1] + x[1], c[2] + x[2]));
                                            final float[] v2 = buffer[m];
                                            final float[] v3 = buffer[m - du];
                                            final float[] v4 = buffer[m - du - dv];
                                            final float[] v5 = buffer[m - dv];
                                            if ((mask & 0x1) != 0x0) {
                                                tess.func_78374_a((double)v2[0], (double)v2[1], (double)v2[2], tu0, tv2);
                                                tess.func_78374_a((double)v3[0], (double)v3[1], (double)v3[2], tu2, tv2);
                                                tess.func_78374_a((double)v4[0], (double)v4[1], (double)v4[2], tu2, tv0);
                                                tess.func_78374_a((double)v5[0], (double)v5[1], (double)v5[2], tu0, tv0);
                                            }
                                            else {
                                                tess.func_78374_a((double)v2[0], (double)v2[1], (double)v2[2], tu0, tv2);
                                                tess.func_78374_a((double)v5[0], (double)v5[1], (double)v5[2], tu2, tv2);
                                                tess.func_78374_a((double)v4[0], (double)v4[1], (double)v4[2], tu2, tv0);
                                                tess.func_78374_a((double)v3[0], (double)v3[1], (double)v3[2], tu0, tv0);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    final int[] array6 = x;
                    final int n6 = 0;
                    ++array6[n6];
                    ++m;
                }
                final int[] array7 = x;
                final int n7 = 1;
                ++array7[n7];
                m += 2;
            }
            final int[] array8 = x;
            final int n8 = 2;
            ++array8[n8];
            bufno ^= 0x1;
            r[2] = -r[2];
        }
        return true;
    }
    
    static {
        SurfaceNets.cube_edges = new int[24];
        SurfaceNets.edge_table = new int[256];
        int k = 0;
        for (int i = 0; i < 8; ++i) {
            for (int j = 1; j <= 4; j <<= 1) {
                final int p = i ^ j;
                if (i <= p) {
                    SurfaceNets.cube_edges[k++] = i;
                    SurfaceNets.cube_edges[k++] = p;
                }
            }
        }
        for (int i = 0; i < 256; ++i) {
            int em = 0;
            for (int l = 0; l < 24; l += 2) {
                final boolean a = (i & 1 << SurfaceNets.cube_edges[l]) != 0x0;
                final boolean b = (i & 1 << SurfaceNets.cube_edges[l + 1]) != 0x0;
                em |= ((a != b) ? (1 << (l >> 1)) : 0);
            }
            SurfaceNets.edge_table[i] = em;
        }
    }
}
