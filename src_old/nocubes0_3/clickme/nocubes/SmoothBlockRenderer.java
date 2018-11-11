package clickme.nocubes;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

public class SmoothBlockRenderer implements ISimpleBlockRenderingHandler {
   public void renderInventoryBlock(Block block, int meta, int model, RenderBlocks renderer) {
      Tessellator tessellator = Tessellator.field_78398_a;
      if (renderer.field_147844_c) {
         int color = block.func_149741_i(meta);
         float colorRed = (float)(color >> 16 & 255) / 255.0F;
         float colorGreen = (float)(color >> 8 & 255) / 255.0F;
         float colorBlue = (float)(color & 255) / 255.0F;
         GL11.glColor4f(colorRed, colorGreen, colorBlue, 1.0F);
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

   public int getRenderId() {
      return NoCubes.renderId;
   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int model, RenderBlocks renderer) {
      Tessellator tessellator = Tessellator.field_78398_a;
      int color = block.func_149720_d(world, x, y, z);
      float colorRed = (float)(color >> 16 & 255) / 255.0F;
      float colorGreen = (float)(color >> 8 & 255) / 255.0F;
      float colorBlue = (float)(color & 255) / 255.0F;
      float shadowBottom = 0.8F;
      float shadowTop = 1.0F;
      float shadowLeft = 0.95F;
      float shadowRight = 0.9F;
      IIcon icon;
      if (!renderer.func_147744_b()) {
         icon = renderer.func_147777_a(block, 0);
      } else {
         icon = renderer.field_147840_d;
      }

      double minU = (double)icon.func_94209_e();
      double minV = (double)icon.func_94206_g();
      double maxU = (double)icon.func_94212_f();
      double maxV = (double)icon.func_94210_h();
      Vec3[] points = new Vec3[]{world.func_82732_R().func_72345_a(0.0D, 0.0D, 0.0D), world.func_82732_R().func_72345_a(1.0D, 0.0D, 0.0D), world.func_82732_R().func_72345_a(1.0D, 0.0D, 1.0D), world.func_82732_R().func_72345_a(0.0D, 0.0D, 1.0D), world.func_82732_R().func_72345_a(0.0D, 1.0D, 0.0D), world.func_82732_R().func_72345_a(1.0D, 1.0D, 0.0D), world.func_82732_R().func_72345_a(1.0D, 1.0D, 1.0D), world.func_82732_R().func_72345_a(0.0D, 1.0D, 1.0D)};

      int side;
      for(side = 0; side < 8; ++side) {
         points[side].field_72450_a += (double)x;
         points[side].field_72448_b += (double)y;
         points[side].field_72449_c += (double)z;
         if (!doesPointIntersectWithManufactured(world, points[side])) {
            if (side < 4 && doesPointBottomIntersectWithAir(world, points[side])) {
               points[side].field_72448_b = (double)y + 1.0D;
            } else if (side >= 4 && doesPointTopIntersectWithAir(world, points[side])) {
               points[side].field_72448_b = (double)y + 0.0D;
            }

            points[side] = this.givePointRoughness(points[side]);
         }
      }

      for(side = 0; side < 6; ++side) {
         int facingX = x;
         int facingY = y;
         int facingZ = z;
         if (side == 0) {
            facingY = y - 1;
         } else if (side == 1) {
            facingY = y + 1;
         } else if (side == 2) {
            facingZ = z - 1;
         } else if (side == 3) {
            facingX = x + 1;
         } else if (side == 4) {
            facingZ = z + 1;
         } else if (side == 5) {
            facingX = x - 1;
         }

         if (renderer.field_147837_f || block.func_149646_a(world, facingX, facingY, facingZ, side)) {
            float colorFactor = 1.0F;
            Vec3 vertex0 = null;
            Vec3 vertex1 = null;
            Vec3 vertex2 = null;
            Vec3 vertex3 = null;
            if (side == 0) {
               colorFactor = shadowBottom;
               vertex0 = points[0];
               vertex1 = points[1];
               vertex2 = points[2];
               vertex3 = points[3];
            } else if (side == 1) {
               if (points[1].field_72448_b == points[5].field_72448_b) {
                  colorFactor = shadowLeft;
               } else if (points[2].field_72448_b == points[6].field_72448_b) {
                  colorFactor = shadowLeft;
               } else {
                  colorFactor = shadowTop;
               }

               vertex0 = points[7];
               vertex1 = points[6];
               vertex2 = points[5];
               vertex3 = points[4];
            } else if (side == 2) {
               colorFactor = shadowLeft;
               vertex0 = points[1];
               vertex1 = points[0];
               vertex2 = points[4];
               vertex3 = points[5];
            } else if (side == 3) {
               colorFactor = shadowRight;
               vertex0 = points[2];
               vertex1 = points[1];
               vertex2 = points[5];
               vertex3 = points[6];
            } else if (side == 4) {
               colorFactor = shadowLeft;
               vertex0 = points[3];
               vertex1 = points[2];
               vertex2 = points[6];
               vertex3 = points[7];
            } else if (side == 5) {
               colorFactor = shadowRight;
               vertex0 = points[0];
               vertex1 = points[3];
               vertex2 = points[7];
               vertex3 = points[4];
            }

            tessellator.func_78380_c(block.func_149677_c(world, facingX, facingY, facingZ));
            tessellator.func_78386_a(shadowTop * colorFactor * colorRed, shadowTop * colorFactor * colorGreen, shadowTop * colorFactor * colorBlue);
            tessellator.func_78374_a(vertex0.field_72450_a, vertex0.field_72448_b, vertex0.field_72449_c, minU, maxV);
            tessellator.func_78374_a(vertex1.field_72450_a, vertex1.field_72448_b, vertex1.field_72449_c, maxU, maxV);
            tessellator.func_78374_a(vertex2.field_72450_a, vertex2.field_72448_b, vertex2.field_72449_c, maxU, minV);
            tessellator.func_78374_a(vertex3.field_72450_a, vertex3.field_72448_b, vertex3.field_72449_c, minU, minV);
         }
      }

      return true;
   }

   private Vec3 givePointRoughness(Vec3 point) {
      long i = (long)(point.field_72450_a * 3129871.0D) ^ (long)point.field_72448_b * 116129781L ^ (long)point.field_72449_c;
      i = i * i * 42317861L + i * 11L;
      point.field_72450_a += (double)(((float)(i >> 16 & 15L) / 15.0F - 0.5F) * 0.5F);
      point.field_72448_b += (double)(((float)(i >> 20 & 15L) / 15.0F - 0.5F) * 0.5F);
      point.field_72449_c += (double)(((float)(i >> 24 & 15L) / 15.0F - 0.5F) * 0.5F);
      return point;
   }

   public static boolean isBlockAirOrPlant(Block block) {
      Material material = block.func_149688_o();
      return material == Material.field_151579_a || material == Material.field_151585_k || material == Material.field_151582_l;
   }

   public static boolean doesPointTopIntersectWithAir(IBlockAccess world, Vec3 point) {
      boolean intersects = false;

      for(int i = 0; i < 4; ++i) {
         int x1 = (int)(point.field_72450_a - (double)(i & 1));
         int z1 = (int)(point.field_72449_c - (double)(i >> 1 & 1));
         if (!isBlockAirOrPlant(world.func_147439_a(x1, (int)point.field_72448_b, z1))) {
            return false;
         }

         if (isBlockAirOrPlant(world.func_147439_a(x1, (int)point.field_72448_b - 1, z1))) {
            intersects = true;
         }
      }

      return intersects;
   }

   public static boolean doesPointBottomIntersectWithAir(IBlockAccess world, Vec3 point) {
      boolean intersects = false;
      boolean notOnly = false;

      for(int i = 0; i < 4; ++i) {
         int x1 = (int)(point.field_72450_a - (double)(i & 1));
         int z1 = (int)(point.field_72449_c - (double)(i >> 1 & 1));
         if (!isBlockAirOrPlant(world.func_147439_a(x1, (int)point.field_72448_b - 1, z1))) {
            return false;
         }

         if (!isBlockAirOrPlant(world.func_147439_a(x1, (int)point.field_72448_b + 1, z1))) {
            notOnly = true;
         }

         if (isBlockAirOrPlant(world.func_147439_a(x1, (int)point.field_72448_b, z1))) {
            intersects = true;
         }
      }

      return intersects && notOnly;
   }

   public static boolean doesPointIntersectWithManufactured(IBlockAccess world, Vec3 point) {
      for(int i = 0; i < 4; ++i) {
         int x1 = (int)(point.field_72450_a - (double)(i & 1));
         int z1 = (int)(point.field_72449_c - (double)(i >> 1 & 1));
         Block block = world.func_147439_a(x1, (int)point.field_72448_b, z1);
         if (!isBlockAirOrPlant(block) && !NoCubes.isBlockSmoothed(block)) {
            return true;
         }

         Block block1 = world.func_147439_a(x1, (int)point.field_72448_b - 1, z1);
         if (!isBlockAirOrPlant(block1) && !NoCubes.isBlockSmoothed(block1)) {
            return true;
         }
      }

      return false;
   }
}
