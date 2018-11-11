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
         float r2 = (r * 30.0F + g * 59.0F + b * 11.0F) / 100.0F;
         float g2 = (r * 30.0F + g * 70.0F) / 100.0F;
         float b2 = (r * 30.0F + b * 70.0F) / 100.0F;
         r = r2;
         g = g2;
         b = b2;
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

         Block block2 = access.func_147439_a(k, y, l);
         Material material2 = block2.func_149688_o();
         if (material2 != Material.field_151579_a && material2 != Material.field_151582_l && material2 != Material.field_151585_k) {
            if (block2 != block) {
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

         Block block2 = access.func_147439_a(k, y, l);
         Material material2 = block2.func_149688_o();
         if (material2 != Material.field_151579_a && material2 != Material.field_151582_l && material2 != Material.field_151585_k && material2 != Material.field_151586_h) {
            if (block2 != block) {
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
      float f7 = 1.0F * r;
      float f8 = 1.0F * g;
      float f9 = 1.0F * b;
      float f10 = 0.5F * r;
      float f11 = 0.8F * r;
      float f12 = 0.6F * r;
      float f13 = 0.5F * g;
      float f14 = 0.8F * g;
      float f15 = 0.6F * g;
      float f16 = 0.5F * b;
      float f17 = 0.8F * b;
      float f18 = 0.6F * b;
      float h0 = getSmoothBlockHeight(renderer.field_147845_a, block, x, y, z);
      float h2 = getSmoothBlockHeight(renderer.field_147845_a, block, x, y, z + 1);
      float h3 = getSmoothBlockHeight(renderer.field_147845_a, block, x + 1, y, z + 1);
      float h4 = getSmoothBlockHeight(renderer.field_147845_a, block, x + 1, y, z);
      IIcon icon;
      double u0;
      double u2;
      double v0;
      double v2;
      double x6;
      double y4;
      double z7;
      double z6;
      if (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x, y - 1, z, 0)) {
         tessellator.func_78380_c(block.func_149677_c(renderer.field_147845_a, x, y - 1, z));
         tessellator.func_78386_a(f10, f13, f16);
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 0);
         u0 = (double)icon.func_94209_e();
         u2 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v2 = (double)icon.func_94210_h();
         x6 = (double)x;
         y4 = (double)x + 1.0D;
         z7 = (double)y;
         z6 = (double)z;
         double z3 = (double)z + 1.0D;
         tessellator.func_78374_a(x6, z7, z3, u0, v2);
         tessellator.func_78374_a(x6, z7, z6, u0, v0);
         tessellator.func_78374_a(y4, z7, z6, u2, v0);
         tessellator.func_78374_a(y4, z7, z3, u2, v2);
         rendered = true;
      }

      if (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x, y + 1, z, 1)) {
         int brightness = block.func_149677_c(renderer.field_147845_a, x, y + 1, z);
         if (h0 < 1.0F) {
            brightness -= 4194304;
         }

         tessellator.func_78380_c(brightness);
         tessellator.func_78386_a(f7, f8, f9);
         IIcon icon2 = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 1);
         double u5 = (double)icon2.func_94209_e();
         double u6 = (double)icon2.func_94212_f();
         double v5 = (double)icon2.func_94206_g();
         double v6 = (double)icon2.func_94210_h();
         double x4 = (double)x;
         double x5 = (double)x + 1.0D;
         double y3 = (double)y;
         double z4 = (double)z;
         double z5 = (double)z + 1.0D;
         tessellator.func_78374_a(x5, y3 + (double)h3, z5, u6, v6);
         tessellator.func_78374_a(x5, y3 + (double)h4, z4, u6, v5);
         tessellator.func_78374_a(x4, y3 + (double)h0, z4, u5, v5);
         tessellator.func_78374_a(x4, y3 + (double)h2, z5, u5, v6);
         rendered = true;
      }

      if ((h0 != 0.0F || h4 != 0.0F) && (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x, y, z - 1, 2))) {
         tessellator.func_78380_c(block.func_149677_c(renderer.field_147845_a, x, y, z - 1));
         tessellator.func_78386_a(f11, f14, f17);
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 2);
         u0 = (double)icon.func_94209_e();
         u2 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v2 = (double)icon.func_94210_h();
         if (h4 == 0.0F) {
            v0 = v2;
         }

         x6 = (double)x;
         y4 = (double)x + 1.0D;
         z7 = (double)y;
         z6 = (double)z;
         tessellator.func_78374_a(x6, z7 + (double)h0, z6, u2, v0);
         tessellator.func_78374_a(y4, z7 + (double)h4, z6, u0, v0);
         tessellator.func_78374_a(y4, z7, z6, u0, v2);
         tessellator.func_78374_a(x6, z7, z6, u2, v2);
         rendered = true;
      }

      if ((h2 != 0.0F || h3 != 0.0F) && (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x, y, z + 1, 3))) {
         tessellator.func_78380_c(block.func_149677_c(renderer.field_147845_a, x, y, z + 1));
         tessellator.func_78386_a(f11, f14, f17);
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 3);
         u0 = (double)icon.func_94209_e();
         u2 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v2 = (double)icon.func_94210_h();
         double v3 = v0;
         if (h3 == 0.0F) {
            v3 = v2;
         }

         x6 = (double)x;
         y4 = (double)x + 1.0D;
         z7 = (double)y;
         z6 = (double)z + 1.0D;
         tessellator.func_78374_a(x6, z7 + (double)h2, z6, u0, v0);
         tessellator.func_78374_a(x6, z7, z6, u0, v2);
         tessellator.func_78374_a(y4, z7, z6, u2, v2);
         tessellator.func_78374_a(y4, z7 + (double)h3, z6, u2, v3);
         rendered = true;
      }

      if ((h0 != 0.0F || h2 != 0.0F) && (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x - 1, y, z, 4))) {
         tessellator.func_78380_c(block.func_149677_c(renderer.field_147845_a, x - 1, y, z));
         tessellator.func_78386_a(f12, f15, f18);
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 4);
         u0 = (double)icon.func_94209_e();
         u2 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v2 = (double)icon.func_94210_h();
         if (h0 == 0.0F) {
            u0 = u2;
         }

         x6 = (double)x;
         y4 = (double)y;
         z7 = (double)z;
         z6 = (double)z + 1.0D;
         tessellator.func_78374_a(x6, y4 + (double)h2, z6, u0, v2);
         tessellator.func_78374_a(x6, y4 + (double)h0, z7, u0, v0);
         tessellator.func_78374_a(x6, y4, z7, u2, v0);
         tessellator.func_78374_a(x6, y4, z6, u2, v2);
         rendered = true;
      }

      if ((h3 != 0.0F || h4 != 0.0F) && (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x + 1, y, z, 5))) {
         tessellator.func_78380_c(block.func_149677_c(renderer.field_147845_a, x + 1, y, z));
         tessellator.func_78386_a(f12, f15, f18);
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 5);
         u0 = (double)icon.func_94209_e();
         u2 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v2 = (double)icon.func_94210_h();
         if (h3 == 0.0F) {
            v0 = v2;
         }

         x6 = (double)x + 1.0D;
         y4 = (double)y;
         z7 = (double)z;
         z6 = (double)z + 1.0D;
         tessellator.func_78374_a(x6, y4, z6, u0, v2);
         tessellator.func_78374_a(x6, y4, z7, u2, v2);
         tessellator.func_78374_a(x6, y4 + (double)h4, z7, u2, v0);
         tessellator.func_78374_a(x6, y4 + (double)h3, z6, u0, v0);
         rendered = true;
      }

      return rendered;
   }

   public boolean renderStandardBlockWithAmbientOcclusion(RenderBlocks renderer, Block block, int x, int y, int z, float r, float g, float b) {
      Tessellator tessellator = Tessellator.field_78398_a;
      boolean rendered = false;
      float h0 = getSmoothBlockHeight(renderer.field_147845_a, block, x, y, z);
      float h2 = getSmoothBlockHeight(renderer.field_147845_a, block, x, y, z + 1);
      float h3 = getSmoothBlockHeight(renderer.field_147845_a, block, x + 1, y, z + 1);
      float h4 = getSmoothBlockHeight(renderer.field_147845_a, block, x + 1, y, z);
      boolean flag2;
      boolean flag3;
      boolean flag4;
      boolean flag5;
      int i1;
      float f7;
      float f8;
      float f16;
      float f10;
      float f17;
      float n13;
      float n14;
      float n15;
      IIcon icon;
      double u0;
      double u2;
      double v0;
      double v2;
      double x4;
      double y3;
      double z5;
      double z4;
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
         f8 = (renderer.field_147884_z + renderer.field_147886_y + renderer.field_147815_B + f7) / 4.0F;
         f16 = (renderer.field_147815_B + f7 + renderer.field_147811_E + renderer.field_147810_D) / 4.0F;
         f10 = (f7 + renderer.field_147814_A + renderer.field_147810_D + renderer.field_147816_C) / 4.0F;
         f17 = (renderer.field_147886_y + renderer.field_147888_x + f7 + renderer.field_147814_A) / 4.0F;
         renderer.field_147864_al = renderer.func_147778_a(renderer.field_147826_T, renderer.field_147831_S, renderer.field_147828_V, i1);
         renderer.field_147870_ao = renderer.func_147778_a(renderer.field_147828_V, renderer.field_147834_Y, renderer.field_147835_X, i1);
         renderer.field_147876_an = renderer.func_147778_a(renderer.field_147825_U, renderer.field_147835_X, renderer.field_147827_W, i1);
         renderer.field_147874_am = renderer.func_147778_a(renderer.field_147831_S, renderer.field_147832_R, renderer.field_147825_U, i1);
         n13 = r * 0.5F;
         renderer.field_147848_as = n13;
         renderer.field_147850_ar = n13;
         renderer.field_147852_aq = n13;
         renderer.field_147872_ap = n13;
         n14 = g * 0.5F;
         renderer.field_147856_aw = n14;
         renderer.field_147858_av = n14;
         renderer.field_147860_au = n14;
         renderer.field_147846_at = n14;
         n15 = b * 0.5F;
         renderer.field_147833_aA = n15;
         renderer.field_147839_az = n15;
         renderer.field_147841_ay = n15;
         renderer.field_147854_ax = n15;
         renderer.field_147872_ap *= f8;
         renderer.field_147846_at *= f8;
         renderer.field_147854_ax *= f8;
         renderer.field_147852_aq *= f17;
         renderer.field_147860_au *= f17;
         renderer.field_147841_ay *= f17;
         renderer.field_147850_ar *= f10;
         renderer.field_147858_av *= f10;
         renderer.field_147839_az *= f10;
         renderer.field_147848_as *= f16;
         renderer.field_147856_aw *= f16;
         renderer.field_147833_aA *= f16;
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 0);
         u0 = (double)icon.func_94209_e();
         u2 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v2 = (double)icon.func_94210_h();
         x4 = (double)x;
         y3 = (double)x + 1.0D;
         z5 = (double)y;
         z4 = (double)z;
         double z3 = (double)z + 1.0D;
         tessellator.func_78386_a(renderer.field_147872_ap, renderer.field_147846_at, renderer.field_147854_ax);
         tessellator.func_78380_c(renderer.field_147864_al);
         tessellator.func_78374_a(x4, z5, z3, u0, v2);
         tessellator.func_78386_a(renderer.field_147852_aq, renderer.field_147860_au, renderer.field_147841_ay);
         tessellator.func_78380_c(renderer.field_147874_am);
         tessellator.func_78374_a(x4, z5, z4, u0, v0);
         tessellator.func_78386_a(renderer.field_147850_ar, renderer.field_147858_av, renderer.field_147839_az);
         tessellator.func_78380_c(renderer.field_147876_an);
         tessellator.func_78374_a(y3, z5, z4, u2, v0);
         tessellator.func_78386_a(renderer.field_147848_as, renderer.field_147856_aw, renderer.field_147833_aA);
         tessellator.func_78380_c(renderer.field_147870_ao);
         tessellator.func_78374_a(y3, z5, z3, u2, v2);
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
         f8 = (renderer.field_147821_H + renderer.field_147813_G + renderer.field_147817_L + f7) / 4.0F;
         f16 = (renderer.field_147817_L + f7 + renderer.field_147818_M + renderer.field_147824_K) / 4.0F;
         f10 = (f7 + renderer.field_147822_I + renderer.field_147824_K + renderer.field_147823_J) / 4.0F;
         f17 = (renderer.field_147813_G + renderer.field_147812_F + f7 + renderer.field_147822_I) / 4.0F;
         renderer.field_147870_ao = renderer.func_147778_a(renderer.field_147881_ab, renderer.field_147880_aa, renderer.field_147887_af, i1);
         renderer.field_147864_al = renderer.func_147778_a(renderer.field_147887_af, renderer.field_147882_ag, renderer.field_147885_ae, i1);
         renderer.field_147874_am = renderer.func_147778_a(renderer.field_147878_ac, renderer.field_147885_ae, renderer.field_147879_ad, i1);
         renderer.field_147876_an = renderer.func_147778_a(renderer.field_147880_aa, renderer.field_147836_Z, renderer.field_147878_ac, i1);
         renderer.field_147848_as = r;
         renderer.field_147850_ar = r;
         renderer.field_147852_aq = r;
         renderer.field_147872_ap = r;
         renderer.field_147856_aw = g;
         renderer.field_147858_av = g;
         renderer.field_147860_au = g;
         renderer.field_147846_at = g;
         renderer.field_147833_aA = b;
         renderer.field_147839_az = b;
         renderer.field_147841_ay = b;
         renderer.field_147854_ax = b;
         renderer.field_147872_ap *= f16;
         renderer.field_147846_at *= f16;
         renderer.field_147854_ax *= f16;
         renderer.field_147852_aq *= f10;
         renderer.field_147860_au *= f10;
         renderer.field_147841_ay *= f10;
         renderer.field_147850_ar *= f17;
         renderer.field_147858_av *= f17;
         renderer.field_147839_az *= f17;
         renderer.field_147848_as *= f8;
         renderer.field_147856_aw *= f8;
         renderer.field_147833_aA *= f8;
         IIcon icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 1);
         double u0 = (double)icon.func_94209_e();
         double u2 = (double)icon.func_94212_f();
         double v0 = (double)icon.func_94206_g();
         double v2 = (double)icon.func_94210_h();
         double x2 = (double)x;
         double x3 = (double)x + 1.0D;
         double y2 = (double)y;
         double z2 = (double)z;
         double z3 = (double)z + 1.0D;
         tessellator.func_78386_a(renderer.field_147872_ap, renderer.field_147846_at, renderer.field_147854_ax);
         tessellator.func_78380_c(renderer.field_147864_al);
         tessellator.func_78374_a(x3, y2 + (double)h3, z3, u2, v2);
         tessellator.func_78386_a(renderer.field_147852_aq, renderer.field_147860_au, renderer.field_147841_ay);
         tessellator.func_78380_c(renderer.field_147874_am);
         tessellator.func_78374_a(x3, y2 + (double)h4, z2, u2, v0);
         tessellator.func_78386_a(renderer.field_147850_ar, renderer.field_147858_av, renderer.field_147839_az);
         tessellator.func_78380_c(renderer.field_147876_an);
         tessellator.func_78374_a(x2, y2 + (double)h0, z2, u0, v0);
         tessellator.func_78386_a(renderer.field_147848_as, renderer.field_147856_aw, renderer.field_147833_aA);
         tessellator.func_78380_c(renderer.field_147870_ao);
         tessellator.func_78374_a(x2, y2 + (double)h2, z3, u0, v2);
         rendered = true;
      }

      if ((h0 != 0.0F || h4 != 0.0F) && (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x, y, z - 1, 2))) {
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
         f8 = (renderer.field_147819_N + renderer.field_147812_F + f7 + renderer.field_147822_I) / 4.0F;
         f16 = (f7 + renderer.field_147822_I + renderer.field_147820_O + renderer.field_147823_J) / 4.0F;
         f10 = (renderer.field_147814_A + f7 + renderer.field_147816_C + renderer.field_147820_O) / 4.0F;
         f17 = (renderer.field_147888_x + renderer.field_147819_N + renderer.field_147814_A + f7) / 4.0F;
         renderer.field_147864_al = renderer.func_147778_a(renderer.field_147883_ah, renderer.field_147836_Z, renderer.field_147878_ac, i1);
         renderer.field_147874_am = renderer.func_147778_a(renderer.field_147878_ac, renderer.field_147866_ai, renderer.field_147879_ad, i1);
         renderer.field_147876_an = renderer.func_147778_a(renderer.field_147825_U, renderer.field_147827_W, renderer.field_147866_ai, i1);
         renderer.field_147870_ao = renderer.func_147778_a(renderer.field_147832_R, renderer.field_147883_ah, renderer.field_147825_U, i1);
         n13 = r * 0.8F;
         renderer.field_147848_as = n13;
         renderer.field_147850_ar = n13;
         renderer.field_147852_aq = n13;
         renderer.field_147872_ap = n13;
         n14 = g * 0.8F;
         renderer.field_147856_aw = n14;
         renderer.field_147858_av = n14;
         renderer.field_147860_au = n14;
         renderer.field_147846_at = n14;
         n15 = b * 0.8F;
         renderer.field_147833_aA = n15;
         renderer.field_147839_az = n15;
         renderer.field_147841_ay = n15;
         renderer.field_147854_ax = n15;
         renderer.field_147872_ap *= f8;
         renderer.field_147846_at *= f8;
         renderer.field_147854_ax *= f8;
         renderer.field_147852_aq *= f16;
         renderer.field_147860_au *= f16;
         renderer.field_147841_ay *= f16;
         renderer.field_147850_ar *= f10;
         renderer.field_147858_av *= f10;
         renderer.field_147839_az *= f10;
         renderer.field_147848_as *= f17;
         renderer.field_147856_aw *= f17;
         renderer.field_147833_aA *= f17;
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 2);
         u0 = (double)icon.func_94209_e();
         u2 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v2 = (double)icon.func_94210_h();
         if (h4 == 0.0F) {
            v0 = v2;
         }

         x4 = (double)x;
         y3 = (double)x + 1.0D;
         z5 = (double)y;
         z4 = (double)z;
         tessellator.func_78386_a(renderer.field_147872_ap, renderer.field_147846_at, renderer.field_147854_ax);
         tessellator.func_78380_c(renderer.field_147864_al);
         tessellator.func_78374_a(x4, z5 + (double)h0, z4, u2, v0);
         tessellator.func_78386_a(renderer.field_147852_aq, renderer.field_147860_au, renderer.field_147841_ay);
         tessellator.func_78380_c(renderer.field_147874_am);
         tessellator.func_78374_a(y3, z5 + (double)h4, z4, u0, v0);
         tessellator.func_78386_a(renderer.field_147850_ar, renderer.field_147858_av, renderer.field_147839_az);
         tessellator.func_78380_c(renderer.field_147876_an);
         tessellator.func_78374_a(y3, z5, z4, u0, v2);
         tessellator.func_78386_a(renderer.field_147848_as, renderer.field_147856_aw, renderer.field_147833_aA);
         tessellator.func_78380_c(renderer.field_147870_ao);
         tessellator.func_78374_a(x4, z5, z4, u2, v2);
         rendered = true;
      }

      if ((h2 != 0.0F || h3 != 0.0F) && (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x, y, z + 1, 3))) {
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
         f8 = (renderer.field_147830_P + renderer.field_147821_H + f7 + renderer.field_147817_L) / 4.0F;
         f16 = (f7 + renderer.field_147817_L + renderer.field_147829_Q + renderer.field_147818_M) / 4.0F;
         f10 = (renderer.field_147815_B + f7 + renderer.field_147811_E + renderer.field_147829_Q) / 4.0F;
         f17 = (renderer.field_147884_z + renderer.field_147830_P + renderer.field_147815_B + f7) / 4.0F;
         renderer.field_147864_al = renderer.func_147778_a(renderer.field_147868_aj, renderer.field_147881_ab, renderer.field_147887_af, i1);
         renderer.field_147870_ao = renderer.func_147778_a(renderer.field_147887_af, renderer.field_147862_ak, renderer.field_147882_ag, i1);
         renderer.field_147876_an = renderer.func_147778_a(renderer.field_147828_V, renderer.field_147834_Y, renderer.field_147862_ak, i1);
         renderer.field_147874_am = renderer.func_147778_a(renderer.field_147826_T, renderer.field_147868_aj, renderer.field_147828_V, i1);
         n13 = r * 0.8F;
         renderer.field_147848_as = n13;
         renderer.field_147850_ar = n13;
         renderer.field_147852_aq = n13;
         renderer.field_147872_ap = n13;
         n14 = g * 0.8F;
         renderer.field_147856_aw = n14;
         renderer.field_147858_av = n14;
         renderer.field_147860_au = n14;
         renderer.field_147846_at = n14;
         n15 = b * 0.8F;
         renderer.field_147833_aA = n15;
         renderer.field_147839_az = n15;
         renderer.field_147841_ay = n15;
         renderer.field_147854_ax = n15;
         renderer.field_147872_ap *= f8;
         renderer.field_147846_at *= f8;
         renderer.field_147854_ax *= f8;
         renderer.field_147852_aq *= f17;
         renderer.field_147860_au *= f17;
         renderer.field_147841_ay *= f17;
         renderer.field_147850_ar *= f10;
         renderer.field_147858_av *= f10;
         renderer.field_147839_az *= f10;
         renderer.field_147848_as *= f16;
         renderer.field_147856_aw *= f16;
         renderer.field_147833_aA *= f16;
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 3);
         u0 = (double)icon.func_94209_e();
         u2 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v2 = (double)icon.func_94210_h();
         double v3 = v0;
         if (h3 == 0.0F) {
            v3 = v2;
         }

         x4 = (double)x;
         y3 = (double)x + 1.0D;
         z5 = (double)y;
         z4 = (double)z + 1.0D;
         tessellator.func_78386_a(renderer.field_147872_ap, renderer.field_147846_at, renderer.field_147854_ax);
         tessellator.func_78380_c(renderer.field_147864_al);
         tessellator.func_78374_a(x4, z5 + (double)h2, z4, u0, v0);
         tessellator.func_78386_a(renderer.field_147852_aq, renderer.field_147860_au, renderer.field_147841_ay);
         tessellator.func_78380_c(renderer.field_147874_am);
         tessellator.func_78374_a(x4, z5, z4, u0, v2);
         tessellator.func_78386_a(renderer.field_147850_ar, renderer.field_147858_av, renderer.field_147839_az);
         tessellator.func_78380_c(renderer.field_147876_an);
         tessellator.func_78374_a(y3, z5, z4, u2, v2);
         tessellator.func_78386_a(renderer.field_147848_as, renderer.field_147856_aw, renderer.field_147833_aA);
         tessellator.func_78380_c(renderer.field_147870_ao);
         tessellator.func_78374_a(y3, z5 + (double)h3, z4, u2, v3);
         rendered = true;
      }

      if ((h0 != 0.0F || h2 != 0.0F) && (renderer.field_147837_f || block.func_149646_a(renderer.field_147845_a, x - 1, y, z, 4))) {
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
         f8 = (renderer.field_147886_y + renderer.field_147884_z + f7 + renderer.field_147830_P) / 4.0F;
         f16 = (f7 + renderer.field_147830_P + renderer.field_147813_G + renderer.field_147821_H) / 4.0F;
         f10 = (renderer.field_147819_N + f7 + renderer.field_147812_F + renderer.field_147813_G) / 4.0F;
         f17 = (renderer.field_147888_x + renderer.field_147886_y + renderer.field_147819_N + f7) / 4.0F;
         renderer.field_147870_ao = renderer.func_147778_a(renderer.field_147831_S, renderer.field_147826_T, renderer.field_147868_aj, i1);
         renderer.field_147864_al = renderer.func_147778_a(renderer.field_147868_aj, renderer.field_147880_aa, renderer.field_147881_ab, i1);
         renderer.field_147874_am = renderer.func_147778_a(renderer.field_147883_ah, renderer.field_147836_Z, renderer.field_147880_aa, i1);
         renderer.field_147876_an = renderer.func_147778_a(renderer.field_147832_R, renderer.field_147831_S, renderer.field_147883_ah, i1);
         n13 = r * 0.6F;
         renderer.field_147848_as = n13;
         renderer.field_147850_ar = n13;
         renderer.field_147852_aq = n13;
         renderer.field_147872_ap = n13;
         n14 = g * 0.6F;
         renderer.field_147856_aw = n14;
         renderer.field_147858_av = n14;
         renderer.field_147860_au = n14;
         renderer.field_147846_at = n14;
         n15 = b * 0.6F;
         renderer.field_147833_aA = n15;
         renderer.field_147839_az = n15;
         renderer.field_147841_ay = n15;
         renderer.field_147854_ax = n15;
         renderer.field_147872_ap *= f16;
         renderer.field_147846_at *= f16;
         renderer.field_147854_ax *= f16;
         renderer.field_147852_aq *= f10;
         renderer.field_147860_au *= f10;
         renderer.field_147841_ay *= f10;
         renderer.field_147850_ar *= f17;
         renderer.field_147858_av *= f17;
         renderer.field_147839_az *= f17;
         renderer.field_147848_as *= f8;
         renderer.field_147856_aw *= f8;
         renderer.field_147833_aA *= f8;
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 4);
         u0 = (double)icon.func_94209_e();
         u2 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v2 = (double)icon.func_94210_h();
         if (h0 == 0.0F) {
            u0 = u2;
         }

         x4 = (double)x;
         y3 = (double)y;
         z5 = (double)z;
         z4 = (double)z + 1.0D;
         tessellator.func_78386_a(renderer.field_147872_ap, renderer.field_147846_at, renderer.field_147854_ax);
         tessellator.func_78380_c(renderer.field_147864_al);
         tessellator.func_78374_a(x4, y3 + (double)h2, z4, u0, v2);
         tessellator.func_78386_a(renderer.field_147852_aq, renderer.field_147860_au, renderer.field_147841_ay);
         tessellator.func_78380_c(renderer.field_147874_am);
         tessellator.func_78374_a(x4, y3 + (double)h0, z5, u0, v0);
         tessellator.func_78386_a(renderer.field_147850_ar, renderer.field_147858_av, renderer.field_147839_az);
         tessellator.func_78380_c(renderer.field_147876_an);
         tessellator.func_78374_a(x4, y3, z5, u2, v0);
         tessellator.func_78386_a(renderer.field_147848_as, renderer.field_147856_aw, renderer.field_147833_aA);
         tessellator.func_78380_c(renderer.field_147870_ao);
         tessellator.func_78374_a(x4, y3, z4, u2, v2);
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
         f8 = (renderer.field_147810_D + renderer.field_147811_E + f7 + renderer.field_147829_Q) / 4.0F;
         f16 = (renderer.field_147816_C + renderer.field_147810_D + renderer.field_147820_O + f7) / 4.0F;
         f10 = (renderer.field_147820_O + f7 + renderer.field_147823_J + renderer.field_147824_K) / 4.0F;
         f17 = (f7 + renderer.field_147829_Q + renderer.field_147824_K + renderer.field_147818_M) / 4.0F;
         renderer.field_147864_al = renderer.func_147778_a(renderer.field_147835_X, renderer.field_147834_Y, renderer.field_147862_ak, i1);
         renderer.field_147870_ao = renderer.func_147778_a(renderer.field_147862_ak, renderer.field_147885_ae, renderer.field_147882_ag, i1);
         renderer.field_147876_an = renderer.func_147778_a(renderer.field_147866_ai, renderer.field_147879_ad, renderer.field_147885_ae, i1);
         renderer.field_147874_am = renderer.func_147778_a(renderer.field_147827_W, renderer.field_147835_X, renderer.field_147866_ai, i1);
         n13 = r * 0.6F;
         renderer.field_147848_as = n13;
         renderer.field_147850_ar = n13;
         renderer.field_147852_aq = n13;
         renderer.field_147872_ap = n13;
         n14 = g * 0.6F;
         renderer.field_147856_aw = n14;
         renderer.field_147858_av = n14;
         renderer.field_147860_au = n14;
         renderer.field_147846_at = n14;
         n15 = b * 0.6F;
         renderer.field_147833_aA = n15;
         renderer.field_147839_az = n15;
         renderer.field_147841_ay = n15;
         renderer.field_147854_ax = n15;
         renderer.field_147872_ap *= f8;
         renderer.field_147846_at *= f8;
         renderer.field_147854_ax *= f8;
         renderer.field_147852_aq *= f16;
         renderer.field_147860_au *= f16;
         renderer.field_147841_ay *= f16;
         renderer.field_147850_ar *= f10;
         renderer.field_147858_av *= f10;
         renderer.field_147839_az *= f10;
         renderer.field_147848_as *= f17;
         renderer.field_147856_aw *= f17;
         renderer.field_147833_aA *= f17;
         icon = renderer.func_147793_a(block, renderer.field_147845_a, x, y, z, 5);
         u0 = (double)icon.func_94209_e();
         u2 = (double)icon.func_94212_f();
         v0 = (double)icon.func_94206_g();
         v2 = (double)icon.func_94210_h();
         if (h3 == 0.0F) {
            v0 = v2;
         }

         x4 = (double)x + 1.0D;
         y3 = (double)y;
         z5 = (double)z;
         z4 = (double)z + 1.0D;
         tessellator.func_78386_a(renderer.field_147872_ap, renderer.field_147846_at, renderer.field_147854_ax);
         tessellator.func_78380_c(renderer.field_147864_al);
         tessellator.func_78374_a(x4, y3, z4, u0, v2);
         tessellator.func_78386_a(renderer.field_147852_aq, renderer.field_147860_au, renderer.field_147841_ay);
         tessellator.func_78380_c(renderer.field_147874_am);
         tessellator.func_78374_a(x4, y3, z5, u2, v2);
         tessellator.func_78386_a(renderer.field_147850_ar, renderer.field_147858_av, renderer.field_147839_az);
         tessellator.func_78380_c(renderer.field_147876_an);
         tessellator.func_78374_a(x4, y3 + (double)h4, z5, u2, v0);
         tessellator.func_78386_a(renderer.field_147848_as, renderer.field_147856_aw, renderer.field_147833_aA);
         tessellator.func_78380_c(renderer.field_147870_ao);
         tessellator.func_78374_a(x4, y3 + (double)h3, z4, u0, v0);
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
