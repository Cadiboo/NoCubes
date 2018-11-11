package clickme.nocubes.test;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

public class SmoothBlockRenderer2 implements ISimpleBlockRenderingHandler {
   public int getRenderId() {
      return 0;
   }

   public boolean renderWorldBlock(IBlockAccess access, int x, int y, int z, Block block, int i, RenderBlocks renderer) {
      int color = block.func_149720_d(renderer.field_147845_a, x, y, z);
      float r = (float)(color >> 16 & 255) / 255.0F;
      float g = (float)(color >> 8 & 255) / 255.0F;
      float b = (float)(color & 255) / 255.0F;
      if (EntityRenderer.field_78517_a) {
         float r1 = (r * 30.0F + g * 59.0F + b * 11.0F) / 100.0F;
         float g1 = (r * 30.0F + g * 70.0F) / 100.0F;
         float b1 = (r * 30.0F + b * 70.0F) / 100.0F;
         r = r1;
         g = g1;
         b = b1;
      }

      return Minecraft.func_71379_u() && block.func_149750_m() == 0 ? this.renderStandardBlockWithAmbientOcclusion(renderer, block, x, y, z, r, g, b) : this.renderSmoothBlock(renderer, block, x, y, z, r, g, b);
   }

   public static float getSmoothBlockHeight(IBlockAccess access, Block block, int x, int y, int z) {
      long i = (long)(x * 3129871) ^ (long)y * 116129781L ^ (long)z;
      i = i * i * 42317861L + i * 11L;
      float f = ((float)(i >> 16 & 15L) / 15.0F - 0.5F) * 0.8F;
      if (block == Blocks.field_150431_aC) {
         f -= 0.75F;
      }

      boolean flag = false;

      for(int j = 0; j < 4; ++j) {
         int k = x - (j & 1);
         int l = z - (j >> 1 & 1);
         Material material = access.func_147439_a(k, y + 1, l).func_149688_o();
         if (material != Material.field_151579_a && material != Material.field_151582_l && material != Material.field_151585_k) {
            return 1.0F;
         }

         Block block1 = access.func_147439_a(k, y, l);
         Material material1 = block1.func_149688_o();
         if (material1 != Material.field_151579_a && material1 != Material.field_151582_l && material1 != Material.field_151585_k) {
            if (block1 != block) {
               return 1.0F;
            }
         } else {
            flag = true;
         }
      }

      return flag ? 0.0F : 1.0F + f;
   }

   public static float getSmoothBlockHeightForCollision(IBlockAccess access, Block block, int x, int y, int z) {
      boolean flag = false;

      for(int j = 0; j < 4; ++j) {
         int k = x - (j & 1);
         int l = z - (j >> 1 & 1);
         Material material = access.func_147439_a(k, y + 1, l).func_149688_o();
         if (material != Material.field_151579_a && material != Material.field_151582_l && material != Material.field_151585_k && material != Material.field_151586_h) {
            return 1.0F;
         }

         Block block1 = access.func_147439_a(k, y, l);
         Material material1 = block1.func_149688_o();
         if (material1 != Material.field_151579_a && material1 != Material.field_151582_l && material1 != Material.field_151585_k && material1 != Material.field_151586_h) {
            if (block1 != block) {
               return 1.0F;
            }
         } else {
            flag = true;
         }
      }

      return flag ? 0.5F : 1.0F;
   }

   public boolean renderSmoothBlock(RenderBlocks renderer, Block block, int x, int y, int z, float r, float g, float b) {
      Tessellator tessellator = Tessellator.field_78398_a;
      boolean rendered = false;
      float f3 = 0.5F;
      float f4 = 1.0F;
      float f5 = 0.8F;
      float f6 = 0.6F;
      float f7 = f4 * r;
      float f8 = f4 * g;
      float f9 = f4 * b;
      float f10 = f3 * r;
      float f11 = f5 * r;
      float f12 = f6 * r;
      float f13 = f3 * g;
      float f14 = f5 * g;
      float f15 = f6 * g;
      float f16 = f3 * b;
      float f17 = f5 * b;
      float f18 = f6 * b;
      float h0 = getSmoothBlockHeight(renderer.field_147845_a, block, x, y, z);
      float h1 = getSmoothBlockHeight(renderer.field_147845_a, block, x, y, z + 1);
      float h2 = getSmoothBlockHeight(renderer.field_147845_a, block, x + 1, y, z + 1);
      float h3 = getSmoothBlockHeight(renderer.field_147845_a, block, x + 1, y, z);
      IIcon icon;
      double u0;
      double u1;
      double v0;
      double v1;
      double x1;
      double y0;
      double z0;
      double z1;
      if (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x, y - 1, z, 0)) {
         tessellator.func_78380_c(block.func_149677_c(renderer.field_147845_a, x, y - 1, z));
         tessellator.func_78386_a(f10, f13, f16);
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 0);
         u0 = (double)icon.func_94209_e();
         u1 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v1 = (double)icon.func_94210_h();
         x1 = (double)x;
         y0 = (double)x + 1.0D;
         z0 = (double)y;
         z1 = (double)z;
         double z1 = (double)z + 1.0D;
         tessellator.func_78374_a(x1, z0, z1, u0, v1);
         tessellator.func_78374_a(x1, z0, z1, u0, v0);
         tessellator.func_78374_a(y0, z0, z1, u1, v0);
         tessellator.func_78374_a(y0, z0, z1, u1, v1);
         rendered = true;
      }

      if (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x, y + 1, z, 1)) {
         int brightness = block.func_149677_c(renderer.field_147845_a, x, y + 1, z);
         if (h0 < 1.0F) {
            brightness -= 4194304;
         }

         tessellator.func_78380_c(brightness);
         tessellator.func_78386_a(f7, f8, f9);
         IIcon icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 1);
         double u0 = (double)icon.func_94209_e();
         double u1 = (double)icon.func_94212_f();
         double v0 = (double)icon.func_94206_g();
         double v1 = (double)icon.func_94210_h();
         double x0 = (double)x;
         double x1 = (double)x + 1.0D;
         double y0 = (double)y;
         double z0 = (double)z;
         double z1 = (double)z + 1.0D;
         tessellator.func_78374_a(x1, y0 + (double)h2, z1, u1, v1);
         tessellator.func_78374_a(x1, y0 + (double)h3, z0, u1, v0);
         tessellator.func_78374_a(x0, y0 + (double)h0, z0, u0, v0);
         tessellator.func_78374_a(x0, y0 + (double)h1, z1, u0, v1);
         rendered = true;
      }

      if ((h0 != 0.0F || h3 != 0.0F) && (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x, y, z - 1, 2))) {
         tessellator.func_78380_c(block.func_149677_c(renderer.field_147845_a, x, y, z - 1));
         tessellator.func_78386_a(f11, f14, f17);
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 2);
         u0 = (double)icon.func_94209_e();
         u1 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v1 = (double)icon.func_94210_h();
         if (h3 == 0.0F) {
            v0 = v1;
         }

         x1 = (double)x;
         y0 = (double)x + 1.0D;
         z0 = (double)y;
         z1 = (double)z;
         tessellator.func_78374_a(x1, z0 + (double)h0, z1, u1, v0);
         tessellator.func_78374_a(y0, z0 + (double)h3, z1, u0, v0);
         tessellator.func_78374_a(y0, z0, z1, u0, v1);
         tessellator.func_78374_a(x1, z0, z1, u1, v1);
         rendered = true;
      }

      if ((h1 != 0.0F || h2 != 0.0F) && (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x, y, z + 1, 3))) {
         tessellator.func_78380_c(block.func_149677_c(renderer.field_147845_a, x, y, z + 1));
         tessellator.func_78386_a(f11, f14, f17);
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 3);
         u0 = (double)icon.func_94209_e();
         u1 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v1 = (double)icon.func_94210_h();
         double v2 = v0;
         if (h2 == 0.0F) {
            v2 = v1;
         }

         x1 = (double)x;
         y0 = (double)x + 1.0D;
         z0 = (double)y;
         z1 = (double)z + 1.0D;
         tessellator.func_78374_a(x1, z0 + (double)h1, z1, u0, v0);
         tessellator.func_78374_a(x1, z0, z1, u0, v1);
         tessellator.func_78374_a(y0, z0, z1, u1, v1);
         tessellator.func_78374_a(y0, z0 + (double)h2, z1, u1, v2);
         rendered = true;
      }

      if ((h0 != 0.0F || h1 != 0.0F) && (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x - 1, y, z, 4))) {
         tessellator.func_78380_c(block.func_149677_c(renderer.field_147845_a, x - 1, y, z));
         tessellator.func_78386_a(f12, f15, f18);
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 4);
         u0 = (double)icon.func_94209_e();
         u1 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v1 = (double)icon.func_94210_h();
         if (h0 == 0.0F) {
            u0 = u1;
         }

         x1 = (double)x;
         y0 = (double)y;
         z0 = (double)z;
         z1 = (double)z + 1.0D;
         tessellator.func_78374_a(x1, y0 + (double)h1, z1, u0, v1);
         tessellator.func_78374_a(x1, y0 + (double)h0, z0, u0, v0);
         tessellator.func_78374_a(x1, y0, z0, u1, v0);
         tessellator.func_78374_a(x1, y0, z1, u1, v1);
         rendered = true;
      }

      if ((h2 != 0.0F || h3 != 0.0F) && (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x + 1, y, z, 5))) {
         tessellator.func_78380_c(block.func_149677_c(renderer.field_147845_a, x + 1, y, z));
         tessellator.func_78386_a(f12, f15, f18);
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 5);
         u0 = (double)icon.func_94209_e();
         u1 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v1 = (double)icon.func_94210_h();
         if (h2 == 0.0F) {
            v0 = v1;
         }

         x1 = (double)x + 1.0D;
         y0 = (double)y;
         z0 = (double)z;
         z1 = (double)z + 1.0D;
         tessellator.func_78374_a(x1, y0, z1, u0, v1);
         tessellator.func_78374_a(x1, y0, z0, u1, v1);
         tessellator.func_78374_a(x1, y0 + (double)h3, z0, u1, v0);
         tessellator.func_78374_a(x1, y0 + (double)h2, z1, u0, v0);
         rendered = true;
      }

      return rendered;
   }

   public boolean renderStandardBlockWithAmbientOcclusion(RenderBlocks renderer, Block block, int x, int y, int z, float r, float g, float b) {
      Tessellator tessellator = Tessellator.field_78398_a;
      boolean rendered = false;
      float h0 = getSmoothBlockHeight(renderer.field_147845_a, block, x, y, z);
      float h1 = getSmoothBlockHeight(renderer.field_147845_a, block, x, y, z + 1);
      float h2 = getSmoothBlockHeight(renderer.field_147845_a, block, x + 1, y, z + 1);
      float h3 = getSmoothBlockHeight(renderer.field_147845_a, block, x + 1, y, z);
      boolean flag2;
      boolean flag3;
      boolean flag4;
      boolean flag5;
      int i1;
      float f7;
      float f3;
      float f4;
      float f5;
      float f6;
      IIcon icon;
      double u0;
      double u1;
      double v0;
      double v1;
      double x1;
      double y0;
      double z0;
      double z1;
      double z1;
      if (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x, y - 1, z, 0)) {
         renderer.field_147831_S = block.func_149677_c(renderer.field_147845_a, x - 1, y - 1, z);
         renderer.field_147825_U = block.func_149677_c(renderer.field_147845_a, x, y - 1, z - 1);
         renderer.field_147828_V = block.func_149677_c(renderer.field_147845_a, x, y - 1, z + 1);
         renderer.field_147835_X = block.func_149677_c(renderer.field_147845_a, x + 1, y - 1, z);
         renderer.field_147886_y = renderer.field_147845_a.func_147439_a(x - 1, y - 1, z).func_149685_I();
         renderer.field_147814_A = renderer.field_147845_a.func_147439_a(x, y - 1, z - 1).func_149685_I();
         renderer.field_147815_B = renderer.field_147845_a.func_147439_a(x, y - 1, z + 1).func_149685_I();
         renderer.field_147810_D = renderer.field_147845_a.func_147439_a(x + 1, y - 1, z).func_149685_I();
         flag2 = renderer.field_147845_a.func_147439_a(x + 1, y - 2, z).func_149751_l();
         flag3 = renderer.field_147845_a.func_147439_a(x - 1, y - 2, z).func_149751_l();
         flag4 = renderer.field_147845_a.func_147439_a(x, y - 2, z + 1).func_149751_l();
         flag5 = renderer.field_147845_a.func_147439_a(x, y - 2, z - 1).func_149751_l();
         if (!flag5 && !flag3) {
            renderer.field_147888_x = renderer.field_147886_y;
            renderer.field_147832_R = renderer.field_147831_S;
         } else {
            renderer.field_147888_x = renderer.field_147845_a.func_147439_a(x - 1, y - 1, z - 1).func_149685_I();
            renderer.field_147832_R = block.func_149677_c(renderer.field_147845_a, x - 1, y - 1, z - 1);
         }

         if (!flag4 && !flag3) {
            renderer.field_147884_z = renderer.field_147886_y;
            renderer.field_147826_T = renderer.field_147831_S;
         } else {
            renderer.field_147884_z = renderer.field_147845_a.func_147439_a(x - 1, y - 1, z + 1).func_149685_I();
            renderer.field_147826_T = block.func_149677_c(renderer.field_147845_a, x - 1, y - 1, z + 1);
         }

         if (!flag5 && !flag2) {
            renderer.field_147816_C = renderer.field_147810_D;
            renderer.field_147827_W = renderer.field_147835_X;
         } else {
            renderer.field_147816_C = renderer.field_147845_a.func_147439_a(x + 1, y - 1, z - 1).func_149685_I();
            renderer.field_147827_W = block.func_149677_c(renderer.field_147845_a, x + 1, y - 1, z - 1);
         }

         if (!flag4 && !flag2) {
            renderer.field_147811_E = renderer.field_147810_D;
            renderer.field_147834_Y = renderer.field_147835_X;
         } else {
            renderer.field_147811_E = renderer.field_147845_a.func_147439_a(x + 1, y - 1, z + 1).func_149685_I();
            renderer.field_147834_Y = block.func_149677_c(renderer.field_147845_a, x + 1, y - 1, z + 1);
         }

         i1 = block.func_149677_c(renderer.field_147845_a, x, y - 1, z);
         f7 = renderer.field_147845_a.func_147439_a(x, y - 1, z).func_149685_I();
         f3 = (renderer.field_147884_z + renderer.field_147886_y + renderer.field_147815_B + f7) / 4.0F;
         f4 = (renderer.field_147815_B + f7 + renderer.field_147811_E + renderer.field_147810_D) / 4.0F;
         f5 = (f7 + renderer.field_147814_A + renderer.field_147810_D + renderer.field_147816_C) / 4.0F;
         f6 = (renderer.field_147886_y + renderer.field_147888_x + f7 + renderer.field_147814_A) / 4.0F;
         renderer.field_147864_al = renderer.func_147778_a(renderer.field_147826_T, renderer.field_147831_S, renderer.field_147828_V, i1);
         renderer.field_147870_ao = renderer.func_147778_a(renderer.field_147828_V, renderer.field_147834_Y, renderer.field_147835_X, i1);
         renderer.field_147876_an = renderer.func_147778_a(renderer.field_147825_U, renderer.field_147835_X, renderer.field_147827_W, i1);
         renderer.field_147874_am = renderer.func_147778_a(renderer.field_147831_S, renderer.field_147832_R, renderer.field_147825_U, i1);
         renderer.field_147872_ap = renderer.field_147852_aq = renderer.field_147850_ar = renderer.field_147848_as = r * 0.5F;
         renderer.field_147846_at = renderer.field_147860_au = renderer.field_147858_av = renderer.field_147856_aw = g * 0.5F;
         renderer.field_147854_ax = renderer.field_147841_ay = renderer.field_147839_az = renderer.field_147833_aA = b * 0.5F;
         renderer.field_147872_ap *= f3;
         renderer.field_147846_at *= f3;
         renderer.field_147854_ax *= f3;
         renderer.field_147852_aq *= f6;
         renderer.field_147860_au *= f6;
         renderer.field_147841_ay *= f6;
         renderer.field_147850_ar *= f5;
         renderer.field_147858_av *= f5;
         renderer.field_147839_az *= f5;
         renderer.field_147848_as *= f4;
         renderer.field_147856_aw *= f4;
         renderer.field_147833_aA *= f4;
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 0);
         u0 = (double)icon.func_94209_e();
         u1 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v1 = (double)icon.func_94210_h();
         x1 = (double)x;
         y0 = (double)x + 1.0D;
         z0 = (double)y;
         z1 = (double)z;
         z1 = (double)z + 1.0D;
         tessellator.func_78386_a(renderer.field_147872_ap, renderer.field_147846_at, renderer.field_147854_ax);
         tessellator.func_78380_c(renderer.field_147864_al);
         tessellator.func_78374_a(x1, z0, z1, u0, v1);
         tessellator.func_78386_a(renderer.field_147852_aq, renderer.field_147860_au, renderer.field_147841_ay);
         tessellator.func_78380_c(renderer.field_147874_am);
         tessellator.func_78374_a(x1, z0, z1, u0, v0);
         tessellator.func_78386_a(renderer.field_147850_ar, renderer.field_147858_av, renderer.field_147839_az);
         tessellator.func_78380_c(renderer.field_147876_an);
         tessellator.func_78374_a(y0, z0, z1, u1, v0);
         tessellator.func_78386_a(renderer.field_147848_as, renderer.field_147856_aw, renderer.field_147833_aA);
         tessellator.func_78380_c(renderer.field_147870_ao);
         tessellator.func_78374_a(y0, z0, z1, u1, v1);
         rendered = true;
      }

      if (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x, y + 1, z, 1)) {
         renderer.field_147880_aa = block.func_149677_c(renderer.field_147845_a, x - 1, y + 1, z);
         renderer.field_147885_ae = block.func_149677_c(renderer.field_147845_a, x + 1, y + 1, z);
         renderer.field_147878_ac = block.func_149677_c(renderer.field_147845_a, x, y + 1, z - 1);
         renderer.field_147887_af = block.func_149677_c(renderer.field_147845_a, x, y + 1, z + 1);
         renderer.field_147813_G = renderer.field_147845_a.func_147439_a(x - 1, y + 1, z).func_149685_I();
         renderer.field_147824_K = renderer.field_147845_a.func_147439_a(x + 1, y + 1, z).func_149685_I();
         renderer.field_147822_I = renderer.field_147845_a.func_147439_a(x, y + 1, z - 1).func_149685_I();
         renderer.field_147817_L = renderer.field_147845_a.func_147439_a(x, y + 1, z + 1).func_149685_I();
         flag2 = renderer.field_147845_a.func_147439_a(x + 1, y + 2, z).func_149751_l();
         flag3 = renderer.field_147845_a.func_147439_a(x - 1, y + 2, z).func_149751_l();
         flag4 = renderer.field_147845_a.func_147439_a(x, y + 2, z + 1).func_149751_l();
         flag5 = renderer.field_147845_a.func_147439_a(x, y + 2, z - 1).func_149751_l();
         if (!flag5 && !flag3) {
            renderer.field_147812_F = renderer.field_147813_G;
            renderer.field_147836_Z = renderer.field_147880_aa;
         } else {
            renderer.field_147812_F = renderer.field_147845_a.func_147439_a(x - 1, y + 1, z - 1).func_149685_I();
            renderer.field_147836_Z = block.func_149677_c(renderer.field_147845_a, x - 1, y + 1, z - 1);
         }

         if (!flag5 && !flag2) {
            renderer.field_147823_J = renderer.field_147824_K;
            renderer.field_147879_ad = renderer.field_147885_ae;
         } else {
            renderer.field_147823_J = renderer.field_147845_a.func_147439_a(x + 1, y + 1, z - 1).func_149685_I();
            renderer.field_147879_ad = block.func_149677_c(renderer.field_147845_a, x + 1, y + 1, z - 1);
         }

         if (!flag4 && !flag3) {
            renderer.field_147821_H = renderer.field_147813_G;
            renderer.field_147881_ab = renderer.field_147880_aa;
         } else {
            renderer.field_147821_H = renderer.field_147845_a.func_147439_a(x - 1, y + 1, z + 1).func_149685_I();
            renderer.field_147881_ab = block.func_149677_c(renderer.field_147845_a, x - 1, y + 1, z + 1);
         }

         if (!flag4 && !flag2) {
            renderer.field_147818_M = renderer.field_147824_K;
            renderer.field_147882_ag = renderer.field_147885_ae;
         } else {
            renderer.field_147818_M = renderer.field_147845_a.func_147439_a(x + 1, y + 1, z + 1).func_149685_I();
            renderer.field_147882_ag = block.func_149677_c(renderer.field_147845_a, x + 1, y + 1, z + 1);
         }

         i1 = block.func_149677_c(renderer.field_147845_a, x, y + 1, z);
         f7 = renderer.field_147845_a.func_147439_a(x, y + 1, z).func_149685_I();
         f3 = (renderer.field_147821_H + renderer.field_147813_G + renderer.field_147817_L + f7) / 4.0F;
         f4 = (renderer.field_147817_L + f7 + renderer.field_147818_M + renderer.field_147824_K) / 4.0F;
         f5 = (f7 + renderer.field_147822_I + renderer.field_147824_K + renderer.field_147823_J) / 4.0F;
         f6 = (renderer.field_147813_G + renderer.field_147812_F + f7 + renderer.field_147822_I) / 4.0F;
         renderer.field_147870_ao = renderer.func_147778_a(renderer.field_147881_ab, renderer.field_147880_aa, renderer.field_147887_af, i1);
         renderer.field_147864_al = renderer.func_147778_a(renderer.field_147887_af, renderer.field_147882_ag, renderer.field_147885_ae, i1);
         renderer.field_147874_am = renderer.func_147778_a(renderer.field_147878_ac, renderer.field_147885_ae, renderer.field_147879_ad, i1);
         renderer.field_147876_an = renderer.func_147778_a(renderer.field_147880_aa, renderer.field_147836_Z, renderer.field_147878_ac, i1);
         renderer.field_147872_ap = renderer.field_147852_aq = renderer.field_147850_ar = renderer.field_147848_as = r;
         renderer.field_147846_at = renderer.field_147860_au = renderer.field_147858_av = renderer.field_147856_aw = g;
         renderer.field_147854_ax = renderer.field_147841_ay = renderer.field_147839_az = renderer.field_147833_aA = b;
         renderer.field_147872_ap *= f4;
         renderer.field_147846_at *= f4;
         renderer.field_147854_ax *= f4;
         renderer.field_147852_aq *= f5;
         renderer.field_147860_au *= f5;
         renderer.field_147841_ay *= f5;
         renderer.field_147850_ar *= f6;
         renderer.field_147858_av *= f6;
         renderer.field_147839_az *= f6;
         renderer.field_147848_as *= f3;
         renderer.field_147856_aw *= f3;
         renderer.field_147833_aA *= f3;
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 1);
         u0 = (double)icon.func_94209_e();
         u1 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v1 = (double)icon.func_94210_h();
         x1 = (double)x;
         y0 = (double)x + 1.0D;
         z0 = (double)y;
         z1 = (double)z;
         z1 = (double)z + 1.0D;
         tessellator.func_78386_a(renderer.field_147872_ap, renderer.field_147846_at, renderer.field_147854_ax);
         tessellator.func_78380_c(renderer.field_147864_al);
         tessellator.func_78374_a(y0, z0 + (double)h2, z1, u1, v1);
         tessellator.func_78386_a(renderer.field_147852_aq, renderer.field_147860_au, renderer.field_147841_ay);
         tessellator.func_78380_c(renderer.field_147874_am);
         tessellator.func_78374_a(y0, z0 + (double)h3, z1, u1, v0);
         tessellator.func_78386_a(renderer.field_147850_ar, renderer.field_147858_av, renderer.field_147839_az);
         tessellator.func_78380_c(renderer.field_147876_an);
         tessellator.func_78374_a(x1, z0 + (double)h0, z1, u0, v0);
         tessellator.func_78386_a(renderer.field_147848_as, renderer.field_147856_aw, renderer.field_147833_aA);
         tessellator.func_78380_c(renderer.field_147870_ao);
         tessellator.func_78374_a(x1, z0 + (double)h1, z1, u0, v1);
         rendered = true;
      }

      if ((h0 != 0.0F || h3 != 0.0F) && (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x, y, z - 1, 2))) {
         renderer.field_147819_N = renderer.field_147845_a.func_147439_a(x - 1, y, z - 1).func_149685_I();
         renderer.field_147814_A = renderer.field_147845_a.func_147439_a(x, y - 1, z - 1).func_149685_I();
         renderer.field_147822_I = renderer.field_147845_a.func_147439_a(x, y + 1, z - 1).func_149685_I();
         renderer.field_147820_O = renderer.field_147845_a.func_147439_a(x + 1, y, z - 1).func_149685_I();
         renderer.field_147883_ah = block.func_149677_c(renderer.field_147845_a, x - 1, y, z - 1);
         renderer.field_147825_U = block.func_149677_c(renderer.field_147845_a, x, y - 1, z - 1);
         renderer.field_147878_ac = block.func_149677_c(renderer.field_147845_a, x, y + 1, z - 1);
         renderer.field_147866_ai = block.func_149677_c(renderer.field_147845_a, x + 1, y, z - 1);
         flag2 = renderer.field_147845_a.func_147439_a(x + 1, y, z - 2).func_149751_l();
         flag3 = renderer.field_147845_a.func_147439_a(x - 1, y, z - 2).func_149751_l();
         flag4 = renderer.field_147845_a.func_147439_a(x, y + 1, z - 2).func_149751_l();
         flag5 = renderer.field_147845_a.func_147439_a(x, y - 1, z - 2).func_149751_l();
         if (!flag3 && !flag5) {
            renderer.field_147888_x = renderer.field_147819_N;
            renderer.field_147832_R = renderer.field_147883_ah;
         } else {
            renderer.field_147888_x = renderer.field_147845_a.func_147439_a(x - 1, y - 1, z - 1).func_149685_I();
            renderer.field_147832_R = block.func_149677_c(renderer.field_147845_a, x - 1, y - 1, z - 1);
         }

         if (!flag3 && !flag4) {
            renderer.field_147812_F = renderer.field_147819_N;
            renderer.field_147836_Z = renderer.field_147883_ah;
         } else {
            renderer.field_147812_F = renderer.field_147845_a.func_147439_a(x - 1, y + 1, z - 1).func_149685_I();
            renderer.field_147836_Z = block.func_149677_c(renderer.field_147845_a, x - 1, y + 1, z - 1);
         }

         if (!flag2 && !flag5) {
            renderer.field_147816_C = renderer.field_147820_O;
            renderer.field_147827_W = renderer.field_147866_ai;
         } else {
            renderer.field_147816_C = renderer.field_147845_a.func_147439_a(x + 1, y - 1, z - 1).func_149685_I();
            renderer.field_147827_W = block.func_149677_c(renderer.field_147845_a, x + 1, y - 1, z - 1);
         }

         if (!flag2 && !flag4) {
            renderer.field_147823_J = renderer.field_147820_O;
            renderer.field_147879_ad = renderer.field_147866_ai;
         } else {
            renderer.field_147823_J = renderer.field_147845_a.func_147439_a(x + 1, y + 1, z - 1).func_149685_I();
            renderer.field_147879_ad = block.func_149677_c(renderer.field_147845_a, x + 1, y + 1, z - 1);
         }

         i1 = block.func_149677_c(renderer.field_147845_a, x, y, z - 1);
         f7 = renderer.field_147845_a.func_147439_a(x, y, z - 1).func_149685_I();
         f3 = (renderer.field_147819_N + renderer.field_147812_F + f7 + renderer.field_147822_I) / 4.0F;
         f4 = (f7 + renderer.field_147822_I + renderer.field_147820_O + renderer.field_147823_J) / 4.0F;
         f5 = (renderer.field_147814_A + f7 + renderer.field_147816_C + renderer.field_147820_O) / 4.0F;
         f6 = (renderer.field_147888_x + renderer.field_147819_N + renderer.field_147814_A + f7) / 4.0F;
         renderer.field_147864_al = renderer.func_147778_a(renderer.field_147883_ah, renderer.field_147836_Z, renderer.field_147878_ac, i1);
         renderer.field_147874_am = renderer.func_147778_a(renderer.field_147878_ac, renderer.field_147866_ai, renderer.field_147879_ad, i1);
         renderer.field_147876_an = renderer.func_147778_a(renderer.field_147825_U, renderer.field_147827_W, renderer.field_147866_ai, i1);
         renderer.field_147870_ao = renderer.func_147778_a(renderer.field_147832_R, renderer.field_147883_ah, renderer.field_147825_U, i1);
         renderer.field_147872_ap = renderer.field_147852_aq = renderer.field_147850_ar = renderer.field_147848_as = r * 0.8F;
         renderer.field_147846_at = renderer.field_147860_au = renderer.field_147858_av = renderer.field_147856_aw = g * 0.8F;
         renderer.field_147854_ax = renderer.field_147841_ay = renderer.field_147839_az = renderer.field_147833_aA = b * 0.8F;
         renderer.field_147872_ap *= f3;
         renderer.field_147846_at *= f3;
         renderer.field_147854_ax *= f3;
         renderer.field_147852_aq *= f4;
         renderer.field_147860_au *= f4;
         renderer.field_147841_ay *= f4;
         renderer.field_147850_ar *= f5;
         renderer.field_147858_av *= f5;
         renderer.field_147839_az *= f5;
         renderer.field_147848_as *= f6;
         renderer.field_147856_aw *= f6;
         renderer.field_147833_aA *= f6;
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 2);
         u0 = (double)icon.func_94209_e();
         u1 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v1 = (double)icon.func_94210_h();
         if (h3 == 0.0F) {
            v0 = v1;
         }

         x1 = (double)x;
         y0 = (double)x + 1.0D;
         z0 = (double)y;
         z1 = (double)z;
         tessellator.func_78386_a(renderer.field_147872_ap, renderer.field_147846_at, renderer.field_147854_ax);
         tessellator.func_78380_c(renderer.field_147864_al);
         tessellator.func_78374_a(x1, z0 + (double)h0, z1, u1, v0);
         tessellator.func_78386_a(renderer.field_147852_aq, renderer.field_147860_au, renderer.field_147841_ay);
         tessellator.func_78380_c(renderer.field_147874_am);
         tessellator.func_78374_a(y0, z0 + (double)h3, z1, u0, v0);
         tessellator.func_78386_a(renderer.field_147850_ar, renderer.field_147858_av, renderer.field_147839_az);
         tessellator.func_78380_c(renderer.field_147876_an);
         tessellator.func_78374_a(y0, z0, z1, u0, v1);
         tessellator.func_78386_a(renderer.field_147848_as, renderer.field_147856_aw, renderer.field_147833_aA);
         tessellator.func_78380_c(renderer.field_147870_ao);
         tessellator.func_78374_a(x1, z0, z1, u1, v1);
         rendered = true;
      }

      if ((h1 != 0.0F || h2 != 0.0F) && (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x, y, z + 1, 3))) {
         renderer.field_147830_P = renderer.field_147845_a.func_147439_a(x - 1, y, z + 1).func_149685_I();
         renderer.field_147829_Q = renderer.field_147845_a.func_147439_a(x + 1, y, z + 1).func_149685_I();
         renderer.field_147815_B = renderer.field_147845_a.func_147439_a(x, y - 1, z + 1).func_149685_I();
         renderer.field_147817_L = renderer.field_147845_a.func_147439_a(x, y + 1, z + 1).func_149685_I();
         renderer.field_147868_aj = block.func_149677_c(renderer.field_147845_a, x - 1, y, z + 1);
         renderer.field_147862_ak = block.func_149677_c(renderer.field_147845_a, x + 1, y, z + 1);
         renderer.field_147828_V = block.func_149677_c(renderer.field_147845_a, x, y - 1, z + 1);
         renderer.field_147887_af = block.func_149677_c(renderer.field_147845_a, x, y + 1, z + 1);
         flag2 = renderer.field_147845_a.func_147439_a(x + 1, y, z + 2).func_149751_l();
         flag3 = renderer.field_147845_a.func_147439_a(x - 1, y, z + 2).func_149751_l();
         flag4 = renderer.field_147845_a.func_147439_a(x, y + 1, z + 2).func_149751_l();
         flag5 = renderer.field_147845_a.func_147439_a(x, y - 1, z + 2).func_149751_l();
         if (!flag3 && !flag5) {
            renderer.field_147884_z = renderer.field_147830_P;
            renderer.field_147826_T = renderer.field_147868_aj;
         } else {
            renderer.field_147884_z = renderer.field_147845_a.func_147439_a(x - 1, y - 1, z + 1).func_149685_I();
            renderer.field_147826_T = block.func_149677_c(renderer.field_147845_a, x - 1, y - 1, z + 1);
         }

         if (!flag3 && !flag4) {
            renderer.field_147821_H = renderer.field_147830_P;
            renderer.field_147881_ab = renderer.field_147868_aj;
         } else {
            renderer.field_147821_H = renderer.field_147845_a.func_147439_a(x - 1, y + 1, z + 1).func_149685_I();
            renderer.field_147881_ab = block.func_149677_c(renderer.field_147845_a, x - 1, y + 1, z + 1);
         }

         if (!flag2 && !flag5) {
            renderer.field_147811_E = renderer.field_147829_Q;
            renderer.field_147834_Y = renderer.field_147862_ak;
         } else {
            renderer.field_147811_E = renderer.field_147845_a.func_147439_a(x + 1, y - 1, z + 1).func_149685_I();
            renderer.field_147834_Y = block.func_149677_c(renderer.field_147845_a, x + 1, y - 1, z + 1);
         }

         if (!flag2 && !flag4) {
            renderer.field_147818_M = renderer.field_147829_Q;
            renderer.field_147882_ag = renderer.field_147862_ak;
         } else {
            renderer.field_147818_M = renderer.field_147845_a.func_147439_a(x + 1, y + 1, z + 1).func_149685_I();
            renderer.field_147882_ag = block.func_149677_c(renderer.field_147845_a, x + 1, y + 1, z + 1);
         }

         i1 = block.func_149677_c(renderer.field_147845_a, x, y, z + 1);
         f7 = renderer.field_147845_a.func_147439_a(x, y, z + 1).func_149685_I();
         f3 = (renderer.field_147830_P + renderer.field_147821_H + f7 + renderer.field_147817_L) / 4.0F;
         f4 = (f7 + renderer.field_147817_L + renderer.field_147829_Q + renderer.field_147818_M) / 4.0F;
         f5 = (renderer.field_147815_B + f7 + renderer.field_147811_E + renderer.field_147829_Q) / 4.0F;
         f6 = (renderer.field_147884_z + renderer.field_147830_P + renderer.field_147815_B + f7) / 4.0F;
         renderer.field_147864_al = renderer.func_147778_a(renderer.field_147868_aj, renderer.field_147881_ab, renderer.field_147887_af, i1);
         renderer.field_147870_ao = renderer.func_147778_a(renderer.field_147887_af, renderer.field_147862_ak, renderer.field_147882_ag, i1);
         renderer.field_147876_an = renderer.func_147778_a(renderer.field_147828_V, renderer.field_147834_Y, renderer.field_147862_ak, i1);
         renderer.field_147874_am = renderer.func_147778_a(renderer.field_147826_T, renderer.field_147868_aj, renderer.field_147828_V, i1);
         renderer.field_147872_ap = renderer.field_147852_aq = renderer.field_147850_ar = renderer.field_147848_as = r * 0.8F;
         renderer.field_147846_at = renderer.field_147860_au = renderer.field_147858_av = renderer.field_147856_aw = g * 0.8F;
         renderer.field_147854_ax = renderer.field_147841_ay = renderer.field_147839_az = renderer.field_147833_aA = b * 0.8F;
         renderer.field_147872_ap *= f3;
         renderer.field_147846_at *= f3;
         renderer.field_147854_ax *= f3;
         renderer.field_147852_aq *= f6;
         renderer.field_147860_au *= f6;
         renderer.field_147841_ay *= f6;
         renderer.field_147850_ar *= f5;
         renderer.field_147858_av *= f5;
         renderer.field_147839_az *= f5;
         renderer.field_147848_as *= f4;
         renderer.field_147856_aw *= f4;
         renderer.field_147833_aA *= f4;
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 3);
         u0 = (double)icon.func_94209_e();
         u1 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v1 = (double)icon.func_94210_h();
         double v2 = v0;
         if (h2 == 0.0F) {
            v2 = v1;
         }

         x1 = (double)x;
         y0 = (double)x + 1.0D;
         z0 = (double)y;
         z1 = (double)z + 1.0D;
         tessellator.func_78386_a(renderer.field_147872_ap, renderer.field_147846_at, renderer.field_147854_ax);
         tessellator.func_78380_c(renderer.field_147864_al);
         tessellator.func_78374_a(x1, z0 + (double)h1, z1, u0, v0);
         tessellator.func_78386_a(renderer.field_147852_aq, renderer.field_147860_au, renderer.field_147841_ay);
         tessellator.func_78380_c(renderer.field_147874_am);
         tessellator.func_78374_a(x1, z0, z1, u0, v1);
         tessellator.func_78386_a(renderer.field_147850_ar, renderer.field_147858_av, renderer.field_147839_az);
         tessellator.func_78380_c(renderer.field_147876_an);
         tessellator.func_78374_a(y0, z0, z1, u1, v1);
         tessellator.func_78386_a(renderer.field_147848_as, renderer.field_147856_aw, renderer.field_147833_aA);
         tessellator.func_78380_c(renderer.field_147870_ao);
         tessellator.func_78374_a(y0, z0 + (double)h2, z1, u1, v2);
         rendered = true;
      }

      if ((h0 != 0.0F || h1 != 0.0F) && (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x - 1, y, z, 4))) {
         renderer.field_147886_y = renderer.field_147845_a.func_147439_a(x - 1, y - 1, z).func_149685_I();
         renderer.field_147819_N = renderer.field_147845_a.func_147439_a(x - 1, y, z - 1).func_149685_I();
         renderer.field_147830_P = renderer.field_147845_a.func_147439_a(x - 1, y, z + 1).func_149685_I();
         renderer.field_147813_G = renderer.field_147845_a.func_147439_a(x - 1, y + 1, z).func_149685_I();
         renderer.field_147831_S = block.func_149677_c(renderer.field_147845_a, x - 1, y - 1, z);
         renderer.field_147883_ah = block.func_149677_c(renderer.field_147845_a, x - 1, y, z - 1);
         renderer.field_147868_aj = block.func_149677_c(renderer.field_147845_a, x - 1, y, z + 1);
         renderer.field_147880_aa = block.func_149677_c(renderer.field_147845_a, x - 1, y + 1, z);
         flag2 = renderer.field_147845_a.func_147439_a(x - 2, y + 1, z).func_149751_l();
         flag3 = renderer.field_147845_a.func_147439_a(x - 2, y - 1, z).func_149751_l();
         flag4 = renderer.field_147845_a.func_147439_a(x - 2, y, z - 1).func_149751_l();
         flag5 = renderer.field_147845_a.func_147439_a(x - 2, y, z + 1).func_149751_l();
         if (!flag4 && !flag3) {
            renderer.field_147888_x = renderer.field_147819_N;
            renderer.field_147832_R = renderer.field_147883_ah;
         } else {
            renderer.field_147888_x = renderer.field_147845_a.func_147439_a(x - 1, y - 1, z - 1).func_149685_I();
            renderer.field_147832_R = block.func_149677_c(renderer.field_147845_a, x - 1, y - 1, z - 1);
         }

         if (!flag5 && !flag3) {
            renderer.field_147884_z = renderer.field_147830_P;
            renderer.field_147826_T = renderer.field_147868_aj;
         } else {
            renderer.field_147884_z = renderer.field_147845_a.func_147439_a(x - 1, y - 1, z + 1).func_149685_I();
            renderer.field_147826_T = block.func_149677_c(renderer.field_147845_a, x - 1, y - 1, z + 1);
         }

         if (!flag4 && !flag2) {
            renderer.field_147812_F = renderer.field_147819_N;
            renderer.field_147836_Z = renderer.field_147883_ah;
         } else {
            renderer.field_147812_F = renderer.field_147845_a.func_147439_a(x - 1, y + 1, z - 1).func_149685_I();
            renderer.field_147836_Z = block.func_149677_c(renderer.field_147845_a, x - 1, y + 1, z - 1);
         }

         if (!flag5 && !flag2) {
            renderer.field_147821_H = renderer.field_147830_P;
            renderer.field_147881_ab = renderer.field_147868_aj;
         } else {
            renderer.field_147821_H = renderer.field_147845_a.func_147439_a(x - 1, y + 1, z + 1).func_149685_I();
            renderer.field_147881_ab = block.func_149677_c(renderer.field_147845_a, x - 1, y + 1, z + 1);
         }

         i1 = block.func_149677_c(renderer.field_147845_a, x - 1, y, z);
         f7 = renderer.field_147845_a.func_147439_a(x - 1, y, z).func_149685_I();
         f3 = (renderer.field_147886_y + renderer.field_147884_z + f7 + renderer.field_147830_P) / 4.0F;
         f4 = (f7 + renderer.field_147830_P + renderer.field_147813_G + renderer.field_147821_H) / 4.0F;
         f5 = (renderer.field_147819_N + f7 + renderer.field_147812_F + renderer.field_147813_G) / 4.0F;
         f6 = (renderer.field_147888_x + renderer.field_147886_y + renderer.field_147819_N + f7) / 4.0F;
         renderer.field_147870_ao = renderer.func_147778_a(renderer.field_147831_S, renderer.field_147826_T, renderer.field_147868_aj, i1);
         renderer.field_147864_al = renderer.func_147778_a(renderer.field_147868_aj, renderer.field_147880_aa, renderer.field_147881_ab, i1);
         renderer.field_147874_am = renderer.func_147778_a(renderer.field_147883_ah, renderer.field_147836_Z, renderer.field_147880_aa, i1);
         renderer.field_147876_an = renderer.func_147778_a(renderer.field_147832_R, renderer.field_147831_S, renderer.field_147883_ah, i1);
         renderer.field_147872_ap = renderer.field_147852_aq = renderer.field_147850_ar = renderer.field_147848_as = r * 0.6F;
         renderer.field_147846_at = renderer.field_147860_au = renderer.field_147858_av = renderer.field_147856_aw = g * 0.6F;
         renderer.field_147854_ax = renderer.field_147841_ay = renderer.field_147839_az = renderer.field_147833_aA = b * 0.6F;
         renderer.field_147872_ap *= f4;
         renderer.field_147846_at *= f4;
         renderer.field_147854_ax *= f4;
         renderer.field_147852_aq *= f5;
         renderer.field_147860_au *= f5;
         renderer.field_147841_ay *= f5;
         renderer.field_147850_ar *= f6;
         renderer.field_147858_av *= f6;
         renderer.field_147839_az *= f6;
         renderer.field_147848_as *= f3;
         renderer.field_147856_aw *= f3;
         renderer.field_147833_aA *= f3;
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 4);
         u0 = (double)icon.func_94209_e();
         u1 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v1 = (double)icon.func_94210_h();
         if (h0 == 0.0F) {
            u0 = u1;
         }

         x1 = (double)x;
         y0 = (double)y;
         z0 = (double)z;
         z1 = (double)z + 1.0D;
         tessellator.func_78386_a(renderer.field_147872_ap, renderer.field_147846_at, renderer.field_147854_ax);
         tessellator.func_78380_c(renderer.field_147864_al);
         tessellator.func_78374_a(x1, y0 + (double)h1, z1, u0, v1);
         tessellator.func_78386_a(renderer.field_147852_aq, renderer.field_147860_au, renderer.field_147841_ay);
         tessellator.func_78380_c(renderer.field_147874_am);
         tessellator.func_78374_a(x1, y0 + (double)h0, z0, u0, v0);
         tessellator.func_78386_a(renderer.field_147850_ar, renderer.field_147858_av, renderer.field_147839_az);
         tessellator.func_78380_c(renderer.field_147876_an);
         tessellator.func_78374_a(x1, y0, z0, u1, v0);
         tessellator.func_78386_a(renderer.field_147848_as, renderer.field_147856_aw, renderer.field_147833_aA);
         tessellator.func_78380_c(renderer.field_147870_ao);
         tessellator.func_78374_a(x1, y0, z1, u1, v1);
         rendered = true;
      }

      if (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x + 1, y, z, 5)) {
         renderer.field_147810_D = renderer.field_147845_a.func_147439_a(x + 1, y - 1, z).func_149685_I();
         renderer.field_147820_O = renderer.field_147845_a.func_147439_a(x + 1, y, z - 1).func_149685_I();
         renderer.field_147829_Q = renderer.field_147845_a.func_147439_a(x + 1, y, z + 1).func_149685_I();
         renderer.field_147824_K = renderer.field_147845_a.func_147439_a(x + 1, y + 1, z).func_149685_I();
         renderer.field_147835_X = block.func_149677_c(renderer.field_147845_a, x + 1, y - 1, z);
         renderer.field_147866_ai = block.func_149677_c(renderer.field_147845_a, x + 1, y, z - 1);
         renderer.field_147862_ak = block.func_149677_c(renderer.field_147845_a, x + 1, y, z + 1);
         renderer.field_147885_ae = block.func_149677_c(renderer.field_147845_a, x + 1, y + 1, z);
         flag2 = renderer.field_147845_a.func_147439_a(x + 2, y + 1, z).func_149751_l();
         flag3 = renderer.field_147845_a.func_147439_a(x + 2, y - 1, z).func_149751_l();
         flag4 = renderer.field_147845_a.func_147439_a(x + 2, y, z + 1).func_149751_l();
         flag5 = renderer.field_147845_a.func_147439_a(x + 2, y, z - 1).func_149751_l();
         if (!flag3 && !flag5) {
            renderer.field_147816_C = renderer.field_147820_O;
            renderer.field_147827_W = renderer.field_147866_ai;
         } else {
            renderer.field_147816_C = renderer.field_147845_a.func_147439_a(x + 1, y - 1, z - 1).func_149685_I();
            renderer.field_147827_W = block.func_149677_c(renderer.field_147845_a, x + 1, y - 1, z - 1);
         }

         if (!flag3 && !flag4) {
            renderer.field_147811_E = renderer.field_147829_Q;
            renderer.field_147834_Y = renderer.field_147862_ak;
         } else {
            renderer.field_147811_E = renderer.field_147845_a.func_147439_a(x + 1, y - 1, z + 1).func_149685_I();
            renderer.field_147834_Y = block.func_149677_c(renderer.field_147845_a, x + 1, y - 1, z + 1);
         }

         if (!flag2 && !flag5) {
            renderer.field_147823_J = renderer.field_147820_O;
            renderer.field_147879_ad = renderer.field_147866_ai;
         } else {
            renderer.field_147823_J = renderer.field_147845_a.func_147439_a(x + 1, y + 1, z - 1).func_149685_I();
            renderer.field_147879_ad = block.func_149677_c(renderer.field_147845_a, x + 1, y + 1, z - 1);
         }

         if (!flag2 && !flag4) {
            renderer.field_147818_M = renderer.field_147829_Q;
            renderer.field_147882_ag = renderer.field_147862_ak;
         } else {
            renderer.field_147818_M = renderer.field_147845_a.func_147439_a(x + 1, y + 1, z + 1).func_149685_I();
            renderer.field_147882_ag = block.func_149677_c(renderer.field_147845_a, x + 1, y + 1, z + 1);
         }

         i1 = block.func_149677_c(renderer.field_147845_a, x + 1, y, z);
         f7 = renderer.field_147845_a.func_147439_a(x + 1, y, z).func_149685_I();
         f3 = (renderer.field_147810_D + renderer.field_147811_E + f7 + renderer.field_147829_Q) / 4.0F;
         f4 = (renderer.field_147816_C + renderer.field_147810_D + renderer.field_147820_O + f7) / 4.0F;
         f5 = (renderer.field_147820_O + f7 + renderer.field_147823_J + renderer.field_147824_K) / 4.0F;
         f6 = (f7 + renderer.field_147829_Q + renderer.field_147824_K + renderer.field_147818_M) / 4.0F;
         renderer.field_147864_al = renderer.func_147778_a(renderer.field_147835_X, renderer.field_147834_Y, renderer.field_147862_ak, i1);
         renderer.field_147870_ao = renderer.func_147778_a(renderer.field_147862_ak, renderer.field_147885_ae, renderer.field_147882_ag, i1);
         renderer.field_147876_an = renderer.func_147778_a(renderer.field_147866_ai, renderer.field_147879_ad, renderer.field_147885_ae, i1);
         renderer.field_147874_am = renderer.func_147778_a(renderer.field_147827_W, renderer.field_147835_X, renderer.field_147866_ai, i1);
         renderer.field_147872_ap = renderer.field_147852_aq = renderer.field_147850_ar = renderer.field_147848_as = r * 0.6F;
         renderer.field_147846_at = renderer.field_147860_au = renderer.field_147858_av = renderer.field_147856_aw = g * 0.6F;
         renderer.field_147854_ax = renderer.field_147841_ay = renderer.field_147839_az = renderer.field_147833_aA = b * 0.6F;
         renderer.field_147872_ap *= f3;
         renderer.field_147846_at *= f3;
         renderer.field_147854_ax *= f3;
         renderer.field_147852_aq *= f4;
         renderer.field_147860_au *= f4;
         renderer.field_147841_ay *= f4;
         renderer.field_147850_ar *= f5;
         renderer.field_147858_av *= f5;
         renderer.field_147839_az *= f5;
         renderer.field_147848_as *= f6;
         renderer.field_147856_aw *= f6;
         renderer.field_147833_aA *= f6;
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 5);
         u0 = (double)icon.func_94209_e();
         u1 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v1 = (double)icon.func_94210_h();
         if (h2 == 0.0F) {
            v0 = v1;
         }

         x1 = (double)x + 1.0D;
         y0 = (double)y;
         z0 = (double)z;
         z1 = (double)z + 1.0D;
         tessellator.func_78386_a(renderer.field_147872_ap, renderer.field_147846_at, renderer.field_147854_ax);
         tessellator.func_78380_c(renderer.field_147864_al);
         tessellator.func_78374_a(x1, y0, z1, u0, v1);
         tessellator.func_78386_a(renderer.field_147852_aq, renderer.field_147860_au, renderer.field_147841_ay);
         tessellator.func_78380_c(renderer.field_147874_am);
         tessellator.func_78374_a(x1, y0, z0, u1, v1);
         tessellator.func_78386_a(renderer.field_147850_ar, renderer.field_147858_av, renderer.field_147839_az);
         tessellator.func_78380_c(renderer.field_147876_an);
         tessellator.func_78374_a(x1, y0 + (double)h3, z0, u1, v0);
         tessellator.func_78386_a(renderer.field_147848_as, renderer.field_147856_aw, renderer.field_147833_aA);
         tessellator.func_78380_c(renderer.field_147870_ao);
         tessellator.func_78374_a(x1, y0 + (double)h2, z1, u0, v0);
         rendered = true;
      }

      renderer.field_147863_w = false;
      return rendered;
   }

   public void renderInventoryBlock(Block block, int meta, int model, RenderBlocks renderer) {
      Tessellator tessellator = Tessellator.field_78398_a;
      if (renderer.field_147844_c) {
         int color = block.func_149741_i(meta);
         float red = (float)(color >> 16 & 255) / 255.0F;
         float green = (float)(color >> 8 & 255) / 255.0F;
         float blue = (float)(color & 255) / 255.0F;
         GL11.glColor4f(red, green, blue, 1.0F);
      }

      block.func_149683_g();
      renderer.func_147775_a(block);
      GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
      GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
      tessellator.func_78382_b();
      tessellator.func_78375_b(0.0F, -1.0F, 0.0F);
      renderer.func_147768_a(block, 0.0D, 0.0D, 0.0D, renderer.func_147787_a(block, 0, meta));
      tessellator.func_78381_a();
      tessellator.func_78382_b();
      tessellator.func_78375_b(0.0F, 1.0F, 0.0F);
      renderer.func_147806_b(block, 0.0D, 0.0D, 0.0D, renderer.func_147787_a(block, 1, meta));
      tessellator.func_78381_a();
      tessellator.func_78382_b();
      tessellator.func_78375_b(0.0F, 0.0F, -1.0F);
      renderer.func_147761_c(block, 0.0D, 0.0D, 0.0D, renderer.func_147787_a(block, 2, meta));
      tessellator.func_78381_a();
      tessellator.func_78382_b();
      tessellator.func_78375_b(0.0F, 0.0F, 1.0F);
      renderer.func_147734_d(block, 0.0D, 0.0D, 0.0D, renderer.func_147787_a(block, 3, meta));
      tessellator.func_78381_a();
      tessellator.func_78382_b();
      tessellator.func_78375_b(-1.0F, 0.0F, 0.0F);
      renderer.func_147798_e(block, 0.0D, 0.0D, 0.0D, renderer.func_147787_a(block, 4, meta));
      tessellator.func_78381_a();
      tessellator.func_78382_b();
      tessellator.func_78375_b(1.0F, 0.0F, 0.0F);
      renderer.func_147764_f(block, 0.0D, 0.0D, 0.0D, renderer.func_147787_a(block, 5, meta));
      tessellator.func_78381_a();
      GL11.glTranslatef(0.5F, 0.5F, 0.5F);
   }

   public boolean shouldRender3DInInventory(int meta) {
      return true;
   }
}
