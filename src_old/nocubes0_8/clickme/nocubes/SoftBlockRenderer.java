package clickme.nocubes;

import clickme.nocubes.test.SmoothBlockRenderer2;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SoftBlockRenderer {
   public boolean renderSoftBlock(Block block, int x, int y, int z, RenderBlocks renderer, IBlockAccess world) {
      Tessellator tessellator = Tessellator.field_78398_a;
      int meta = world.func_72805_g(x, y, z);
      int color = block.func_149720_d(world, x, y, z);
      float colorRed = (float)(color >> 16 & 255) / 255.0F;
      float colorGreen = (float)(color >> 8 & 255) / 255.0F;
      float colorBlue = (float)(color & 255) / 255.0F;
      float shadowBottom = 0.6F;
      float shadowTop = 1.0F;
      float shadowLeft = 0.9F;
      float shadowRight = 0.8F;
      IIcon icon;
      if (!renderer.func_147744_b()) {
         icon = renderer.func_147787_a(block, 1, meta);
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
               points[side].field_72448_b = (double)y;
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
               colorFactor = shadowTop;
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
      return material == Material.field_151579_a || material == Material.field_151585_k || material == Material.field_151582_l || NoCubes.isBlockLiquid(block);
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
         if (!isBlockAirOrPlant(block) && !NoCubes.isBlockSoft(block)) {
            return true;
         }

         Block block1 = world.func_147439_a(x1, (int)point.field_72448_b - 1, z1);
         if (!isBlockAirOrPlant(block1) && !NoCubes.isBlockSoft(block1)) {
            return true;
         }
      }

      return false;
   }

   public boolean renderLiquidBlock(Block block, int x, int y, int z, RenderBlocks renderer, IBlockAccess world) {
      boolean rendered = renderer.func_147721_p(block, x, y, z);
      if (NoCubes.isBlockLiquid(world.func_147439_a(x, y + 1, z))) {
         return rendered;
      } else {
         int brightness = block.func_149677_c(world, x, y, z);
         if (NoCubes.isBlockSoft(world.func_147439_a(x + 1, y, z))) {
            this.renderGhostLiquid(block, x + 1, y, z, brightness, renderer, world);
         }

         if (NoCubes.isBlockSoft(world.func_147439_a(x, y, z + 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x - 1, y, z + 1))) {
            this.renderGhostLiquid(block, x, y, z + 1, brightness, renderer, world);
         }

         if (NoCubes.isBlockSoft(world.func_147439_a(x - 1, y, z)) && !NoCubes.isBlockLiquid(world.func_147439_a(x - 2, y, z)) && !NoCubes.isBlockLiquid(world.func_147439_a(x - 1, y, z - 1))) {
            this.renderGhostLiquid(block, x - 1, y, z, brightness, renderer, world);
         }

         if (NoCubes.isBlockSoft(world.func_147439_a(x, y, z - 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x - 1, y, z - 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x, y, z - 2)) && !NoCubes.isBlockLiquid(world.func_147439_a(x + 1, y, z - 1))) {
            this.renderGhostLiquid(block, x, y, z - 1, brightness, renderer, world);
         }

         if (NoCubes.isBlockSoft(world.func_147439_a(x + 1, y, z + 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x, y, z + 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x + 1, y, z)) && !NoCubes.isBlockLiquid(world.func_147439_a(x + 2, y, z + 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x + 1, y, z + 2))) {
            this.renderGhostLiquid(block, x + 1, y, z + 1, brightness, renderer, world);
         }

         if (NoCubes.isBlockSoft(world.func_147439_a(x + 1, y, z - 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x, y, z - 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x + 1, y, z - 2)) && !NoCubes.isBlockLiquid(world.func_147439_a(x + 2, y, z - 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x + 1, y, z)) && !NoCubes.isBlockLiquid(world.func_147439_a(x, y, z - 2))) {
            this.renderGhostLiquid(block, x + 1, y, z - 1, brightness, renderer, world);
         }

         if (NoCubes.isBlockSoft(world.func_147439_a(x - 1, y, z - 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x - 2, y, z - 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x - 1, y, z - 2)) && !NoCubes.isBlockLiquid(world.func_147439_a(x, y, z - 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x - 1, y, z)) && !NoCubes.isBlockLiquid(world.func_147439_a(x - 2, y, z - 2)) && !NoCubes.isBlockLiquid(world.func_147439_a(x - 2, y, z))) {
            this.renderGhostLiquid(block, x - 1, y, z - 1, brightness, renderer, world);
         }

         if (NoCubes.isBlockSoft(world.func_147439_a(x - 1, y, z + 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x - 2, y, z + 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x - 1, y, z)) && !NoCubes.isBlockLiquid(world.func_147439_a(x, y, z + 1)) && !NoCubes.isBlockLiquid(world.func_147439_a(x - 1, y, z + 2)) && !NoCubes.isBlockLiquid(world.func_147439_a(x - 2, y, z)) && !NoCubes.isBlockLiquid(world.func_147439_a(x - 2, y, z + 2)) && !NoCubes.isBlockLiquid(world.func_147439_a(x, y, z + 2))) {
            this.renderGhostLiquid(block, x - 1, y, z + 1, brightness, renderer, world);
         }

         return rendered;
      }
   }

   public boolean doesPointIntersectWithLiquid(int x, int y, int z, IBlockAccess world) {
      if (NoCubes.isBlockLiquid(world.func_147439_a(x, y, z))) {
         return true;
      } else if (NoCubes.isBlockLiquid(world.func_147439_a(x - 1, y, z))) {
         return true;
      } else if (NoCubes.isBlockLiquid(world.func_147439_a(x, y, z - 1))) {
         return true;
      } else if (NoCubes.isBlockLiquid(world.func_147439_a(x - 1, y, z - 1))) {
         return true;
      } else if (NoCubes.isBlockLiquid(world.func_147439_a(x, y + 1, z))) {
         return true;
      } else if (NoCubes.isBlockLiquid(world.func_147439_a(x - 1, y + 1, z))) {
         return true;
      } else if (NoCubes.isBlockLiquid(world.func_147439_a(x, y + 1, z - 1))) {
         return true;
      } else {
         return NoCubes.isBlockLiquid(world.func_147439_a(x - 1, y + 1, z - 1));
      }
   }

   public boolean renderGhostLiquid(Block block, int x, int y, int z, int brightness, RenderBlocks renderer, IBlockAccess world) {
      Tessellator tessellator = Tessellator.field_78398_a;
      Material material = block.func_149688_o();
      double height0 = 0.7D;
      double height1 = 0.7D;
      double height2 = 0.7D;
      double height3 = 0.7D;
      if (this.doesPointIntersectWithLiquid(x, y, z, world)) {
         height0 = (double)renderer.func_147729_a(x, y, z, material);
      }

      if (this.doesPointIntersectWithLiquid(x, y, z + 1, world)) {
         height1 = (double)renderer.func_147729_a(x, y, z + 1, material);
      }

      if (this.doesPointIntersectWithLiquid(x + 1, y, z + 1, world)) {
         height2 = (double)renderer.func_147729_a(x + 1, y, z + 1, material);
      }

      if (this.doesPointIntersectWithLiquid(x + 1, y, z, world)) {
         height3 = (double)renderer.func_147729_a(x + 1, y, z, material);
      }

      height0 -= 0.0010000000474974513D;
      height1 -= 0.0010000000474974513D;
      height2 -= 0.0010000000474974513D;
      height3 -= 0.0010000000474974513D;
      IIcon icon = renderer.func_147777_a(block, 1);
      double minU = (double)icon.func_94214_a(0.0D);
      double minV = (double)icon.func_94207_b(0.0D);
      double maxU = (double)icon.func_94214_a(16.0D);
      double maxV = (double)icon.func_94207_b(16.0D);
      tessellator.func_78380_c(brightness);
      tessellator.func_78378_d(block.func_149720_d(world, x, y, z));
      tessellator.func_78374_a((double)(x + 0), (double)y + height0, (double)(z + 0), minU, minV);
      tessellator.func_78374_a((double)(x + 0), (double)y + height1, (double)(z + 1), minU, maxV);
      tessellator.func_78374_a((double)(x + 1), (double)y + height2, (double)(z + 1), maxU, maxV);
      tessellator.func_78374_a((double)(x + 1), (double)y + height3, (double)(z + 0), maxU, minV);
      return true;
   }

   public static boolean shouldHookRenderer(Block block) {
      return NoCubes.isNoCubesEnabled && (NoCubes.isBlockSoft(block) || NoCubes.isBlockLiquid(block));
   }

   public boolean directRenderHook(Block block, int x, int y, int z, RenderBlocks renderer) {
      block.func_149719_a(renderer.field_147845_a, x, y, z);
      renderer.func_147775_a(block);
      IBlockAccess world = renderer.field_147845_a;
      return NoCubes.isBlockLiquid(block) ? this.renderLiquidBlock(block, x, y, z, renderer, world) : this.renderSoftBlock(block, x, y, z, renderer, world);
   }

   public static void inject(Block block, World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity entity) {
      float f = SmoothBlockRenderer2.getSmoothBlockHeightForCollision(world, block, x, y, z);
      float f1 = SmoothBlockRenderer2.getSmoothBlockHeightForCollision(world, block, x, y, z + 1);
      float f2 = SmoothBlockRenderer2.getSmoothBlockHeightForCollision(world, block, x + 1, y, z + 1);
      float f3 = SmoothBlockRenderer2.getSmoothBlockHeightForCollision(world, block, x + 1, y, z);
      addBBoundsToList(x, y, z, 0.0F, 0.0F, 0.0F, 0.5F, f, 0.5F, aabb, list);
      addBBoundsToList(x, y, z, 0.0F, 0.0F, 0.5F, 0.5F, f1, 1.0F, aabb, list);
      addBBoundsToList(x, y, z, 0.5F, 0.0F, 0.5F, 1.0F, f2, 1.0F, aabb, list);
      addBBoundsToList(x, y, z, 0.5F, 0.0F, 0.0F, 1.0F, f3, 0.5F, aabb, list);
   }

   public static void addBBoundsToList(int x, int y, int z, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, AxisAlignedBB aabb, List list) {
      AxisAlignedBB aabb1 = AxisAlignedBB.func_72332_a().func_72299_a((double)x + (double)minX, (double)y + (double)minY, (double)z + (double)minZ, (double)x + (double)maxX, (double)y + (double)maxY, (double)z + (double)maxZ);
      if (aabb1 != null && aabb.func_72326_a(aabb1)) {
         list.add(aabb1);
      }

   }
}
