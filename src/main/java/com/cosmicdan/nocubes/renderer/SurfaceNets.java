package com.cosmicdan.nocubes.renderer;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.cosmicdan.nocubes.Main;
import com.cosmicdan.nocubes.ModConfig;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/*
 * Original by Click_Me
 * Reverse-engineered and re-implemented (with permission) by CosmicDan
 * 
 */
public class SurfaceNets {
    public static int[] cube_edges = new int[24];
    public static int[] edge_table = new int[256];

    public static float getBlockDensity(int x, int y, int z, IBlockAccess cache) {
        float dens = 0.0F;

        for(int k = 0; k < 2; ++k) {
            for(int j = 0; j < 2; ++j) {
                for(int i = 0; i < 2; ++i) {
                    Block block = cache.getBlockState(new BlockPos(x - i, y - j, z - k)).getBlock();
                    if(Main.shouldSmooth(block)) {
                        ++dens;
                    } else {
                        --dens;
                    }
                }
            }
        }
        return dens;
    }

    public static boolean renderChunk(int pass, int cx, int cy, int cz, IBlockAccess cache, RenderGlobal renderer) {
        if(!ModConfig.MOD_ENABLED) {
            return false;
        } else if(pass != 0) {
            return false;
        } else {
            Tessellator tess = Tessellator.getInstance();
            int[] dims = new int[]{16, 16, 16};
            int[] c = new int[]{cx, cy, cz};
            int[] x = new int[3];
            int[] r = new int[]{1, dims[0] + 3, (dims[0] + 3) * (dims[1] + 3)};
            float[] grid = new float[8];
            float[][] buffer = new float[r[2] * 2][3];
            int bufno = 1;

            for(x[2] = 0; x[2] < dims[2] + 1; r[2] = -r[2]) {
                int m = 1 + (dims[0] + 3) * (1 + bufno * (dims[1] + 3));

                for(x[1] = 0; x[1] < dims[1] + 1; m += 2) {
                    for(x[0] = 0; x[0] < dims[0] + 1; ++m) {
                        int mask = 0;
                        int g = 0;

                        for(int k = 0; k < 2; ++k) {
                            for(int j = 0; j < 2; ++j) {
                                for(int i = 0; i < 2; ++g) {
                                    float p = getBlockDensity(c[0] + x[0] + i, c[1] + x[1] + j, c[2] + x[2] + k, cache);
                                    grid[g] = p;
                                    mask |= p > 0.0F?1 << g:0;
                                    ++i;
                                }
                            }
                        }

                        if(mask != 0 && mask != 255) {
                            Block block = Blocks.AIR;
                            int meta = 0;

                            label368:
                            for(int k = -1; k < 2; ++k) {
                                for(int j = -1; j < 2; ++j) {
                                    for(int i = -1; i < 2; ++i) {
                                        Block b = cache.getBlockState(new BlockPos(c[0] + x[0] + i, c[1] + x[1] + k, c[2] + x[2] + j)).getBlock();
                                        if(Main.shouldSmooth(b) && block != Blocks.SNOW_LAYER && block != Blocks.GRASS) {
                                            block = b;
//                                            meta = cache.getBlockMetadata(c[0] + x[0] + i, c[1] + x[1] + k, c[2] + x[2] + j);
                                            if(b == Blocks.SNOW_LAYER || b == Blocks.GRASS) {
                                                break label368;
                                            }
                                        }
                                    }
                                }
                            }

                            int[] br = new int[]{c[0] + x[0], c[1] + x[1] + 1, c[2] + x[2]};

                            label594:
                            for(int k = -1; k < 2; ++k) {
                                for(int j = -2; j < 3; ++j) {
                                    for(int i = -1; i < 2; ++i) {
                                    	Block b = cache.getBlockState(new BlockPos(c[0] + x[0] + i, c[1] + x[1] + k, c[2] + x[2] + j)).getBlock();
                                        if(!b.isOpaqueCube(b.getDefaultState())) {
                                            br[0] = c[0] + x[0] + i;
                                            br[1] = c[1] + x[1] + k;
                                            br[2] = c[2] + x[2] + j;
                                            break label594;
                                        }
                                    }
                                }
                            }

                            List<BakedQuad> tex = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(block.getDefaultState()).getQuads(block.getDefaultState(), EnumFacing.UP, 0l);
                            
                            BakedQuad quad = tex.get(0);
                            
                            quad.getSprite();
                            
                            TextureAtlasSprite icon = quad.getSprite();
                            double tu0 = (double)icon.getMinU();
                            double tu1 = (double)icon.getMaxU();
                            double tv0 = (double)icon.getMinV();
                            double tv1 = (double)icon.getMaxV();
                            int edgemask = edge_table[mask];
                            int ecount = 0;
                            float[] v = new float[]{0.0F, 0.0F, 0.0F};

                            for(int i = 0; i < 12; ++i) {
                                if((edgemask & 1 << i) != 0) {
                                    ++ecount;
                                    int e0 = cube_edges[i << 1];
                                    int e1 = cube_edges[(i << 1) + 1];
                                    float g0 = grid[e0];
                                    float g1 = grid[e1];
                                    float t = g0 - g1;
                                    if(Math.abs(t) > 0.0F) {
                                        t = g0 / t;
                                        int j = 0;

                                        for(int k = 1; j < 3; k <<= 1) {
                                            int a = e0 & k;
                                            int b = e1 & k;
                                            if(a != b) {
                                                v[j] += a != 0?1.0F - t:t;
                                            } else {
                                                v[j] += a != 0?1.0F:0.0F;
                                            }

                                            ++j;
                                        }
                                    }
                                }
                            }

                            float s = 1.0F / (float)ecount;

                            for(int i = 0; i < 3; ++i) {
                                v[i] = (float)(c[i] + x[i]) + s * v[i];
                            }

                            int tx = x[0] == 16?0:x[0];
                            int ty = x[1] == 16?0:x[1];
                            int tz = x[2] == 16?0:x[2];
                            long i1 = (long)(tx * 3129871) ^ (long)tz * 116129781L ^ (long)ty;
                            i1 = i1 * i1 * 42317861L + i1 * 11L;
                            v[0] = (float)((double)v[0] - ((double)((float)(i1 >> 16 & 15L) / 15.0F) - 0.5D) * 0.2D);
                            v[1] = (float)((double)v[1] - ((double)((float)(i1 >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D);
                            v[2] = (float)((double)v[2] - ((double)((float)(i1 >> 24 & 15L) / 15.0F) - 0.5D) * 0.2D);
                            buffer[m] = v;

                            for(int i = 0; i < 3; ++i) {
                                if((edgemask & 1 << i) != 0) {
                                    int iu = (i + 1) % 3;
                                    int iv = (i + 2) % 3;
                                    if(x[iu] != 0 && x[iv] != 0) {
                                        int du = r[iu];
                                        int dv = r[iv];
//                                        tess.setBrightness(block.getMixedBrightnessForBlock(Minecraft.getMinecraft().theWorld, br[0], br[1], br[2]));
//                                        tess.setBrightness(Minecraft.getMinecraft().world.getLightBrightness(new BlockPos(br[0], br[1], br[2])));
//                                      tess.setColorOpaque_I(block.colorMultiplier(cache, c[0] + x[0], c[1] + x[1], c[2] + x[2]));
//                                        tess.getBuffer().color(block.getMapColor(block.getDefaultState(), cache, new BlockPos(c[0] + x[0], c[1] + x[1], c[2] + x[2])));
                                        float[] v0 = buffer[m];
                                        float[] v1 = buffer[m - du];
                                        float[] v2 = buffer[m - du - dv];
                                        float[] v3 = buffer[m - dv];
                                        
                                        final Tessellator tessellator = Tessellator.getInstance();
                                		final BufferBuilder bufferbuilder = tessellator.getBuffer();
                                		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

                                		
                                        
                                        if((mask & 1) != 0) {
                                        	
                                        	
                                        	bufferbuilder.pos((double)v0[0], (double)v0[1], (double)v0[2]).tex( tu0, tv1);
                                        	bufferbuilder.pos((double)v1[0], (double)v1[1], (double)v1[2]).tex( tu1, tv1);
                                        	bufferbuilder.pos((double)v2[0], (double)v2[1], (double)v2[2]).tex( tu1, tv0);
                                        	bufferbuilder.pos((double)v3[0], (double)v3[1], (double)v3[2]).tex( tu0, tv0);
                                        } else {
                                        	bufferbuilder.pos((double)v0[0], (double)v0[1], (double)v0[2]).tex( tu0, tv1);
                                        	bufferbuilder.pos((double)v3[0], (double)v3[1], (double)v3[2]).tex( tu1, tv1);
                                        	bufferbuilder.pos((double)v2[0], (double)v2[1], (double)v2[2]).tex( tu1, tv0);
                                        	bufferbuilder.pos((double)v1[0], (double)v1[1], (double)v1[2]).tex( tu0, tv0);
                                        }
                                        
                                        tessellator.draw();
                                        
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

        for(int i = 0; i < 8; ++i) {
            for(int j = 1; j <= 4; j <<= 1) {
                int p = i ^ j;
                if(i <= p) {
                    cube_edges[k++] = i;
                    cube_edges[k++] = p;
                }
            }
        }

        for(int i = 0; i < 256; ++i) {
            int em = 0;

            for(int j = 0; j < 24; j += 2) {
                boolean a = (i & 1 << cube_edges[j]) != 0;
                boolean b = (i & 1 << cube_edges[j + 1]) != 0;
                em |= a != b?1 << (j >> 1):0;
            }

            edge_table[i] = em;
        }

    }
}
