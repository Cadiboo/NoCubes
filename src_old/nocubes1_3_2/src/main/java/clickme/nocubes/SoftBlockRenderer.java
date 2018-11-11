package clickme.nocubes;

import net.minecraft.block.*;
import net.minecraft.client.renderer.*;
import net.minecraft.block.material.*;
import net.minecraft.world.*;
import net.minecraft.util.*;
import java.util.*;
import net.minecraft.entity.*;
import clickme.nocubes.test.*;

public class SoftBlockRenderer
{
    public boolean renderSoftBlock(final Block block, final int x, final int y, final int z, final RenderBlocks renderer, final IBlockAccess world)
    {
        final Tessellator tessellator = Tessellator.instance;
        final int meta = world.getBlockMetadata(x, y, z);
        final int color = block.colorMultiplier(world, x, y, z);
        final float colorRed = (color >> 16 & 0xFF) / 255.0f;
        final float colorGreen = (color >> 8 & 0xFF) / 255.0f;
        final float colorBlue = (color & 0xFF) / 255.0f;
        final float shadowBottom = 0.6f;
        final float shadowTop = 1.0f;
        final float shadowLeft = 0.9f;
        final float shadowRight = 0.8f;
        IIcon icon;
        if(!renderer.hasOverrideBlockTexture())
        {
            icon = renderer.getBlockIconFromSideAndMetadata(block, 1, meta);
        }
        else
        {
            icon = renderer.overrideBlockTexture;
        }
        final double minU = icon.getMinU();
        final double minV = icon.getMinV();
        final double maxU = icon.getMaxU();
        final double maxV = icon.getMaxV();
        final Vec3[] points = {Vec3.createVectorHelper(0.0, 0.0, 0.0), Vec3.createVectorHelper(1.0, 0.0, 0.0), Vec3.createVectorHelper(1.0, 0.0, 1.0), Vec3.createVectorHelper(0.0, 0.0, 1.0), Vec3.createVectorHelper(0.0, 1.0, 0.0), Vec3.createVectorHelper(1.0, 1.0, 0.0), Vec3.createVectorHelper(1.0, 1.0, 1.0), Vec3.createVectorHelper(0.0, 1.0, 1.0)};
        for(int point = 0; point < 8; ++point)
        {
            final Vec3 vec3 = points[point];
            vec3.xCoord += x;
            final Vec3 vec4 = points[point];
            vec4.yCoord += y;
            final Vec3 vec5 = points[point];
            vec5.zCoord += z;
            if(!doesPointIntersectWithManufactured(world, points[point]))
            {
                if(point < 4 && doesPointBottomIntersectWithAir(world, points[point]))
                {
                    points[point].yCoord = y + 1.0;
                }
                else if(point >= 4 && doesPointTopIntersectWithAir(world, points[point]))
                {
                    points[point].yCoord = y;
                }
                points[point] = this.givePointRoughness(points[point]);
            }
        }
        for(int side = 0; side < 6; ++side)
        {
            int facingX = x;
            int facingY = y;
            int facingZ = z;
            if(side == 0)
            {
                --facingY;
            }
            else if(side == 1)
            {
                ++facingY;
            }
            else if(side == 2)
            {
                --facingZ;
            }
            else if(side == 3)
            {
                ++facingX;
            }
            else if(side == 4)
            {
                ++facingZ;
            }
            else if(side == 5)
            {
                --facingX;
            }
            if(renderer.renderAllFaces || block.shouldSideBeRendered(world, facingX, facingY, facingZ, side))
            {
                float colorFactor = 1.0f;
                Vec3 vertex0 = null;
                Vec3 vertex2 = null;
                Vec3 vertex3 = null;
                Vec3 vertex4 = null;
                if(side == 0)
                {
                    colorFactor = shadowBottom;
                    vertex0 = points[0];
                    vertex2 = points[1];
                    vertex3 = points[2];
                    vertex4 = points[3];
                }
                else if(side == 1)
                {
                    colorFactor = shadowTop;
                    vertex0 = points[7];
                    vertex2 = points[6];
                    vertex3 = points[5];
                    vertex4 = points[4];
                }
                else if(side == 2)
                {
                    colorFactor = shadowLeft;
                    vertex0 = points[1];
                    vertex2 = points[0];
                    vertex3 = points[4];
                    vertex4 = points[5];
                }
                else if(side == 3)
                {
                    colorFactor = shadowRight;
                    vertex0 = points[2];
                    vertex2 = points[1];
                    vertex3 = points[5];
                    vertex4 = points[6];
                }
                else if(side == 4)
                {
                    colorFactor = shadowLeft;
                    vertex0 = points[3];
                    vertex2 = points[2];
                    vertex3 = points[6];
                    vertex4 = points[7];
                }
                else if(side == 5)
                {
                    colorFactor = shadowRight;
                    vertex0 = points[0];
                    vertex2 = points[3];
                    vertex3 = points[7];
                    vertex4 = points[4];
                }
                tessellator.setBrightness(block.getMixedBrightnessForBlock(world, facingX, facingY, facingZ));
                tessellator.setColorOpaque_F(shadowTop * colorFactor * colorRed, shadowTop * colorFactor * colorGreen, shadowTop * colorFactor * colorBlue);
                tessellator.addVertexWithUV(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord, minU, maxV);
                tessellator.addVertexWithUV(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord, maxU, maxV);
                tessellator.addVertexWithUV(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord, maxU, minV);
                tessellator.addVertexWithUV(vertex4.xCoord, vertex4.yCoord, vertex4.zCoord, minU, minV);
            }
        }
        return true;
    }

    private Vec3 givePointRoughness(final Vec3 point)
    {
        long i = (long)(point.xCoord * 3129871.0) ^ (long)point.yCoord * 116129781L ^ (long)point.zCoord;
        i = i * i * 42317861L + i * 11L;
        point.xCoord += ((i >> 16 & 0xFL) / 15.0f - 0.5f) * 0.5f;
        point.yCoord += ((i >> 20 & 0xFL) / 15.0f - 0.5f) * 0.5f;
        point.zCoord += ((i >> 24 & 0xFL) / 15.0f - 0.5f) * 0.5f;
        return point;
    }

    public static boolean isBlockAirOrPlant(final Block block)
    {
        final Material material = block.getMaterial();
        return material == Material.air || material == Material.plants || material == Material.vine || NoCubes.isBlockLiquid(block);
    }

    public static boolean doesPointTopIntersectWithAir(final IBlockAccess world, final Vec3 point)
    {
        boolean intersects = false;
        for(int i = 0; i < 4; ++i)
        {
            final int x1 = (int)(point.xCoord - (i & 0x1));
            final int z1 = (int)(point.zCoord - (i >> 1 & 0x1));
            if(!isBlockAirOrPlant(world.getBlock(x1, (int)point.yCoord, z1)))
            {
                return false;
            }
            if(isBlockAirOrPlant(world.getBlock(x1, (int)point.yCoord - 1, z1)))
            {
                intersects = true;
            }
        }
        return intersects;
    }

    public static boolean doesPointBottomIntersectWithAir(final IBlockAccess world, final Vec3 point)
    {
        boolean intersects = false;
        boolean notOnly = false;
        for(int i = 0; i < 4; ++i)
        {
            final int x1 = (int)(point.xCoord - (i & 0x1));
            final int z1 = (int)(point.zCoord - (i >> 1 & 0x1));
            if(!isBlockAirOrPlant(world.getBlock(x1, (int)point.yCoord - 1, z1)))
            {
                return false;
            }
            if(!isBlockAirOrPlant(world.getBlock(x1, (int)point.yCoord + 1, z1)))
            {
                notOnly = true;
            }
            if(isBlockAirOrPlant(world.getBlock(x1, (int)point.yCoord, z1)))
            {
                intersects = true;
            }
        }
        return intersects && notOnly;
    }

    public static boolean doesPointIntersectWithManufactured(final IBlockAccess world, final Vec3 point)
    {
        for(int i = 0; i < 4; ++i)
        {
            final int x1 = (int)(point.xCoord - (i & 0x1));
            final int z1 = (int)(point.zCoord - (i >> 1 & 0x1));
            final Block block = world.getBlock(x1, (int)point.yCoord, z1);
            if(!isBlockAirOrPlant(block) && !NoCubes.isBlockSoft(block))
            {
                return true;
            }
            final Block block2 = world.getBlock(x1, (int)point.yCoord - 1, z1);
            if(!isBlockAirOrPlant(block2) && !NoCubes.isBlockSoft(block2))
            {
                return true;
            }
        }
        return false;
    }

    public boolean renderLiquidBlock(final Block block, final int x, final int y, final int z, final RenderBlocks renderer, final IBlockAccess world)
    {
        final boolean rendered = renderer.renderBlockLiquid(block, x, y, z);
        if(NoCubes.isBlockLiquid(world.getBlock(x, y + 1, z)))
        {
            return rendered;
        }
        final int brightness = block.getMixedBrightnessForBlock(world, x, y, z);
        if(NoCubes.isBlockSoft(world.getBlock(x + 1, y, z)))
        {
            this.renderGhostLiquid(block, x + 1, y, z, brightness, renderer, world);
        }
        if(NoCubes.isBlockSoft(world.getBlock(x, y, z + 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z + 1)))
        {
            this.renderGhostLiquid(block, x, y, z + 1, brightness, renderer, world);
        }
        if(NoCubes.isBlockSoft(world.getBlock(x - 1, y, z)) && !NoCubes.isBlockLiquid(world.getBlock(x - 2, y, z)) && !NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z - 1)))
        {
            this.renderGhostLiquid(block, x - 1, y, z, brightness, renderer, world);
        }
        if(NoCubes.isBlockSoft(world.getBlock(x, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x, y, z - 2)) && !NoCubes.isBlockLiquid(world.getBlock(x + 1, y, z - 1)))
        {
            this.renderGhostLiquid(block, x, y, z - 1, brightness, renderer, world);
        }
        if(NoCubes.isBlockSoft(world.getBlock(x + 1, y, z + 1)) && !NoCubes.isBlockLiquid(world.getBlock(x, y, z + 1)) && !NoCubes.isBlockLiquid(world.getBlock(x + 1, y, z)) && !NoCubes.isBlockLiquid(world.getBlock(x + 2, y, z + 1)) && !NoCubes.isBlockLiquid(world.getBlock(x + 1, y, z + 2)))
        {
            this.renderGhostLiquid(block, x + 1, y, z + 1, brightness, renderer, world);
        }
        if(NoCubes.isBlockSoft(world.getBlock(x + 1, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x + 1, y, z - 2)) && !NoCubes.isBlockLiquid(world.getBlock(x + 2, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x + 1, y, z)) && !NoCubes.isBlockLiquid(world.getBlock(x, y, z - 2)))
        {
            this.renderGhostLiquid(block, x + 1, y, z - 1, brightness, renderer, world);
        }
        if(NoCubes.isBlockSoft(world.getBlock(x - 1, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 2, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z - 2)) && !NoCubes.isBlockLiquid(world.getBlock(x, y, z - 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z)) && !NoCubes.isBlockLiquid(world.getBlock(x - 2, y, z - 2)) && !NoCubes.isBlockLiquid(world.getBlock(x - 2, y, z)))
        {
            this.renderGhostLiquid(block, x - 1, y, z - 1, brightness, renderer, world);
        }
        if(NoCubes.isBlockSoft(world.getBlock(x - 1, y, z + 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 2, y, z + 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z)) && !NoCubes.isBlockLiquid(world.getBlock(x, y, z + 1)) && !NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z + 2)) && !NoCubes.isBlockLiquid(world.getBlock(x - 2, y, z)) && !NoCubes.isBlockLiquid(world.getBlock(x - 2, y, z + 2)) && !NoCubes.isBlockLiquid(world.getBlock(x, y, z + 2)))
        {
            this.renderGhostLiquid(block, x - 1, y, z + 1, brightness, renderer, world);
        }
        return rendered;
    }

    public boolean doesPointIntersectWithLiquid(final int x, final int y, final int z, final IBlockAccess world)
    {
        return NoCubes.isBlockLiquid(world.getBlock(x, y, z)) || NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z)) || NoCubes.isBlockLiquid(world.getBlock(x, y, z - 1)) || NoCubes.isBlockLiquid(world.getBlock(x - 1, y, z - 1)) || NoCubes.isBlockLiquid(world.getBlock(x, y + 1, z)) || NoCubes.isBlockLiquid(world.getBlock(x - 1, y + 1, z)) || NoCubes.isBlockLiquid(world.getBlock(x, y + 1, z - 1)) || NoCubes.isBlockLiquid(world.getBlock(x - 1, y + 1, z - 1));
    }

    public boolean renderGhostLiquid(final Block block, final int x, final int y, final int z, final int brightness, final RenderBlocks renderer, final IBlockAccess world)
    {
        final Tessellator tessellator = Tessellator.instance;
        final Material material = block.getMaterial();
        double height0 = 0.7;
        double height2 = 0.7;
        double height3 = 0.7;
        double height4 = 0.7;
        if(this.doesPointIntersectWithLiquid(x, y, z, world))
        {
            height0 = renderer.getLiquidHeight(x, y, z, material);
        }
        if(this.doesPointIntersectWithLiquid(x, y, z + 1, world))
        {
            height2 = renderer.getLiquidHeight(x, y, z + 1, material);
        }
        if(this.doesPointIntersectWithLiquid(x + 1, y, z + 1, world))
        {
            height3 = renderer.getLiquidHeight(x + 1, y, z + 1, material);
        }
        if(this.doesPointIntersectWithLiquid(x + 1, y, z, world))
        {
            height4 = renderer.getLiquidHeight(x + 1, y, z, material);
        }
        height0 -= 0.0010000000474974513;
        height2 -= 0.0010000000474974513;
        height3 -= 0.0010000000474974513;
        height4 -= 0.0010000000474974513;
        final IIcon icon = renderer.getBlockIconFromSide(block, 1);
        final double minU = icon.getInterpolatedU(0.0);
        final double minV = icon.getInterpolatedV(0.0);
        final double maxU = icon.getInterpolatedU(16.0);
        final double maxV = icon.getInterpolatedV(16.0);
        tessellator.setBrightness(brightness);
        tessellator.setColorOpaque_I(block.colorMultiplier(world, x, y, z));
        tessellator.addVertexWithUV((double)(x + 0), y + height0, (double)(z + 0), minU, minV);
        tessellator.addVertexWithUV((double)(x + 0), y + height2, (double)(z + 1), minU, maxV);
        tessellator.addVertexWithUV((double)(x + 1), y + height3, (double)(z + 1), maxU, maxV);
        tessellator.addVertexWithUV((double)(x + 1), y + height4, (double)(z + 0), maxU, minV);
        return true;
    }

    public static boolean shouldHookRenderer(final Block block)
    {
        return NoCubes.isNoCubesEnabled && (NoCubes.isBlockSoft(block) || NoCubes.isBlockLiquid(block));
    }

    public boolean directRenderHook(final Block block, final int x, final int y, final int z, final RenderBlocks renderer)
    {
        block.setBlockBoundsBasedOnState(renderer.blockAccess, x, y, z);
        renderer.setRenderBoundsFromBlock(block);
        final IBlockAccess world = renderer.blockAccess;
        if(NoCubes.isBlockLiquid(block))
        {
            return this.renderLiquidBlock(block, x, y, z, renderer, world);
        }
        return this.renderSoftBlock(block, x, y, z, renderer, world);
    }

    public static void inject(final Block block, final World world, final int x, final int y, final int z, final AxisAlignedBB aabb, final List list, final Entity entity)
    {
        final float f = SmoothBlockRenderer2.getSmoothBlockHeightForCollision((IBlockAccess)world, block, x, y, z);
        final float f2 = SmoothBlockRenderer2.getSmoothBlockHeightForCollision((IBlockAccess)world, block, x, y, z + 1);
        final float f3 = SmoothBlockRenderer2.getSmoothBlockHeightForCollision((IBlockAccess)world, block, x + 1, y, z + 1);
        final float f4 = SmoothBlockRenderer2.getSmoothBlockHeightForCollision((IBlockAccess)world, block, x + 1, y, z);
        addBBoundsToList(x, y, z, 0.0f, 0.0f, 0.0f, 0.5f, f, 0.5f, aabb, list);
        addBBoundsToList(x, y, z, 0.0f, 0.0f, 0.5f, 0.5f, f2, 1.0f, aabb, list);
        addBBoundsToList(x, y, z, 0.5f, 0.0f, 0.5f, 1.0f, f3, 1.0f, aabb, list);
        addBBoundsToList(x, y, z, 0.5f, 0.0f, 0.0f, 1.0f, f4, 0.5f, aabb, list);
    }

    public static void addBBoundsToList(final int x, final int y, final int z, final float minX, final float minY, final float minZ, final float maxX, final float maxY, final float maxZ, final AxisAlignedBB aabb, final List list)
    {
        final AxisAlignedBB aabb2 = AxisAlignedBB.getBoundingBox(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ);
        if(aabb2 != null && aabb.intersectsWith(aabb2))
        {
            list.add(aabb2);
        }
    }
}
