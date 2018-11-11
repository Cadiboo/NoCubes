package clickme.nocubes.test;

import cpw.mods.fml.client.registry.*;
import net.minecraft.world.*;
import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.init.*;
import net.minecraft.block.material.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.*;
import org.lwjgl.opengl.*;

public class SmoothBlockRenderer2 implements ISimpleBlockRenderingHandler
{
    public int getRenderId()
    {
        return 0;
    }

    public boolean renderWorldBlock(final IBlockAccess access, final int x, final int y, final int z, final Block block, final int i, final RenderBlocks renderer)
    {
        final int color = block.colorMultiplier(renderer.blockAccess, x, y, z);
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        if(EntityRenderer.anaglyphEnable)
        {
            final float r2 = (r * 30.0f + g * 59.0f + b * 11.0f) / 100.0f;
            final float g2 = (r * 30.0f + g * 70.0f) / 100.0f;
            final float b2 = (r * 30.0f + b * 70.0f) / 100.0f;
            r = r2;
            g = g2;
            b = b2;
        }
        return (Minecraft.isAmbientOcclusionEnabled() && block.getLightValue() == 0) ? this.renderStandardBlockWithAmbientOcclusion(renderer, block, x, y, z, r, g, b) : this.renderSmoothBlock(renderer, block, x, y, z, r, g, b);
    }

    public static float getSmoothBlockHeight(final IBlockAccess access, final Block block, final int x, final int y, final int z)
    {
        long i = x * 3129871 ^ y * 116129781L ^ z;
        i = i * i * 42317861L + i * 11L;
        float f = ((i >> 16 & 0xFL) / 15.0f - 0.5f) * 0.8f;
        if(block == Blocks.snow_layer)
        {
            f -= 0.75f;
        }
        boolean flag = false;
        for(int j = 0; j < 4; ++j)
        {
            final int k = x - (j & 0x1);
            final int l = z - (j >> 1 & 0x1);
            final Material material = access.getBlock(k, y + 1, l).getMaterial();
            if(material != Material.air && material != Material.vine && material != Material.plants)
            {
                return 1.0f;
            }
            final Block block2 = access.getBlock(k, y, l);
            final Material material2 = block2.getMaterial();
            if(material2 == Material.air || material2 == Material.vine || material2 == Material.plants)
            {
                flag = true;
            }
            else if(block2 != block)
            {
                return 1.0f;
            }
        }
        return flag ? 0.0f : (1.0f + f);
    }

    public static float getSmoothBlockHeightForCollision(final IBlockAccess access, final Block block, final int x, final int y, final int z)
    {
        boolean flag = false;
        for(int j = 0; j < 4; ++j)
        {
            final int k = x - (j & 0x1);
            final int l = z - (j >> 1 & 0x1);
            final Material material = access.getBlock(k, y + 1, l).getMaterial();
            if(material != Material.air && material != Material.vine && material != Material.plants && material != Material.water)
            {
                return 1.0f;
            }
            final Block block2 = access.getBlock(k, y, l);
            final Material material2 = block2.getMaterial();
            if(material2 == Material.air || material2 == Material.vine || material2 == Material.plants || material2 == Material.water)
            {
                flag = true;
            }
            else if(block2 != block)
            {
                return 1.0f;
            }
        }
        return flag ? 0.5f : 1.0f;
    }

    public boolean renderSmoothBlock(final RenderBlocks renderer, final Block block, final int x, final int y, final int z, final float r, final float g, final float b)
    {
        final Tessellator tessellator = Tessellator.instance;
        boolean rendered = false;
        final float f3 = 0.5f;
        final float f4 = 1.0f;
        final float f5 = 0.8f;
        final float f6 = 0.6f;
        final float f7 = f4 * r;
        final float f8 = f4 * g;
        final float f9 = f4 * b;
        final float f10 = f3 * r;
        final float f11 = f5 * r;
        final float f12 = f6 * r;
        final float f13 = f3 * g;
        final float f14 = f5 * g;
        final float f15 = f6 * g;
        final float f16 = f3 * b;
        final float f17 = f5 * b;
        final float f18 = f6 * b;
        final float h0 = getSmoothBlockHeight(renderer.blockAccess, block, x, y, z);
        final float h2 = getSmoothBlockHeight(renderer.blockAccess, block, x, y, z + 1);
        final float h3 = getSmoothBlockHeight(renderer.blockAccess, block, x + 1, y, z + 1);
        final float h4 = getSmoothBlockHeight(renderer.blockAccess, block, x + 1, y, z);
        if(renderer.renderAllFaces || block.shouldSideBeRendered(renderer.blockAccess, x, y - 1, z, 0))
        {
            tessellator.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, x, y - 1, z));
            tessellator.setColorOpaque_F(f10, f13, f16);
            final IIcon icon = renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 0);
            final double u0 = icon.getMinU();
            final double u2 = icon.getMaxU();
            final double v0 = icon.getMinV();
            final double v2 = icon.getMaxV();
            final double u3 = u2;
            final double u4 = u0;
            final double v3 = v0;
            final double v4 = v2;
            final double x2 = x;
            final double x3 = x + 1.0;
            final double y2 = y;
            final double z2 = z;
            final double z3 = z + 1.0;
            tessellator.addVertexWithUV(x2, y2, z3, u4, v4);
            tessellator.addVertexWithUV(x2, y2, z2, u0, v0);
            tessellator.addVertexWithUV(x3, y2, z2, u3, v3);
            tessellator.addVertexWithUV(x3, y2, z3, u2, v2);
            rendered = true;
        }
        if(renderer.renderAllFaces || block.shouldSideBeRendered(renderer.blockAccess, x, y + 1, z, 1))
        {
            int brightness = block.getMixedBrightnessForBlock(renderer.blockAccess, x, y + 1, z);
            if(h0 < 1.0f)
            {
                brightness -= 4194304;
            }
            tessellator.setBrightness(brightness);
            tessellator.setColorOpaque_F(f7, f8, f9);
            final IIcon icon2 = renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 1);
            final double u5 = icon2.getMinU();
            final double u6 = icon2.getMaxU();
            final double v5 = icon2.getMinV();
            final double v6 = icon2.getMaxV();
            final double u7 = u6;
            final double u8 = u5;
            final double v7 = v5;
            final double v8 = v6;
            final double x4 = x;
            final double x5 = x + 1.0;
            final double y3 = y;
            final double z4 = z;
            final double z5 = z + 1.0;
            tessellator.addVertexWithUV(x5, y3 + h3, z5, u6, v6);
            tessellator.addVertexWithUV(x5, y3 + h4, z4, u7, v7);
            tessellator.addVertexWithUV(x4, y3 + h0, z4, u5, v5);
            tessellator.addVertexWithUV(x4, y3 + h2, z5, u8, v8);
            rendered = true;
        }
        if((h0 != 0.0f || h4 != 0.0f) && (renderer.renderAllFaces || block.shouldSideBeRendered(renderer.blockAccess, x, y, z - 1, 2)))
        {
            tessellator.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, x, y, z - 1));
            tessellator.setColorOpaque_F(f11, f14, f17);
            final IIcon icon = renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 2);
            final double u0 = icon.getMinU();
            final double u2 = icon.getMaxU();
            double v0 = icon.getMinV();
            final double v2 = icon.getMaxV();
            final double u3 = u2;
            final double u4 = u0;
            final double v3 = v0;
            final double v4 = v2;
            if(h4 == 0.0f)
            {
                v0 = v2;
            }
            final double x2 = x;
            final double x3 = x + 1.0;
            final double y2 = y;
            final double z2 = z;
            tessellator.addVertexWithUV(x2, y2 + h0, z2, u3, v3);
            tessellator.addVertexWithUV(x3, y2 + h4, z2, u0, v0);
            tessellator.addVertexWithUV(x3, y2, z2, u4, v4);
            tessellator.addVertexWithUV(x2, y2, z2, u2, v2);
            rendered = true;
        }
        if((h2 != 0.0f || h3 != 0.0f) && (renderer.renderAllFaces || block.shouldSideBeRendered(renderer.blockAccess, x, y, z + 1, 3)))
        {
            tessellator.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, x, y, z + 1));
            tessellator.setColorOpaque_F(f11, f14, f17);
            final IIcon icon = renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 3);
            final double u0 = icon.getMinU();
            final double u2 = icon.getMaxU();
            final double v0 = icon.getMinV();
            final double v2 = icon.getMaxV();
            final double u3 = u2;
            final double u4 = u0;
            double v3 = v0;
            final double v4 = v2;
            if(h3 == 0.0f)
            {
                v3 = v2;
            }
            final double x2 = x;
            final double x3 = x + 1.0;
            final double y2 = y;
            final double z6 = z + 1.0;
            tessellator.addVertexWithUV(x2, y2 + h2, z6, u0, v0);
            tessellator.addVertexWithUV(x2, y2, z6, u4, v4);
            tessellator.addVertexWithUV(x3, y2, z6, u2, v2);
            tessellator.addVertexWithUV(x3, y2 + h3, z6, u3, v3);
            rendered = true;
        }
        if((h0 != 0.0f || h2 != 0.0f) && (renderer.renderAllFaces || block.shouldSideBeRendered(renderer.blockAccess, x - 1, y, z, 4)))
        {
            tessellator.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y, z));
            tessellator.setColorOpaque_F(f12, f15, f18);
            final IIcon icon = renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 4);
            double u0 = icon.getMinU();
            final double u2 = icon.getMaxU();
            final double v0 = icon.getMinV();
            final double v2 = icon.getMaxV();
            final double u3 = u2;
            final double u4 = u0;
            final double v3 = v0;
            final double v4 = v2;
            if(h0 == 0.0f)
            {
                u0 = u2;
            }
            final double x2 = x;
            final double y4 = y;
            final double z7 = z;
            final double z6 = z + 1.0;
            tessellator.addVertexWithUV(x2, y4 + h2, z6, u4, v4);
            tessellator.addVertexWithUV(x2, y4 + h0, z7, u0, v0);
            tessellator.addVertexWithUV(x2, y4, z7, u3, v3);
            tessellator.addVertexWithUV(x2, y4, z6, u2, v2);
            rendered = true;
        }
        if((h3 != 0.0f || h4 != 0.0f) && (renderer.renderAllFaces || block.shouldSideBeRendered(renderer.blockAccess, x + 1, y, z, 5)))
        {
            tessellator.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y, z));
            tessellator.setColorOpaque_F(f12, f15, f18);
            final IIcon icon = renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 5);
            final double u0 = icon.getMinU();
            final double u2 = icon.getMaxU();
            double v0 = icon.getMinV();
            final double v2 = icon.getMaxV();
            final double u3 = u2;
            final double u4 = u0;
            final double v3 = v0;
            final double v4 = v2;
            if(h3 == 0.0f)
            {
                v0 = v2;
            }
            final double x6 = x + 1.0;
            final double y4 = y;
            final double z7 = z;
            final double z6 = z + 1.0;
            tessellator.addVertexWithUV(x6, y4, z6, u4, v4);
            tessellator.addVertexWithUV(x6, y4, z7, u2, v2);
            tessellator.addVertexWithUV(x6, y4 + h4, z7, u3, v3);
            tessellator.addVertexWithUV(x6, y4 + h3, z6, u0, v0);
            rendered = true;
        }
        return rendered;
    }

    public boolean renderStandardBlockWithAmbientOcclusion(final RenderBlocks renderer, final Block block, final int x, final int y, final int z, final float r, final float g, final float b)
    {
        final Tessellator tessellator = Tessellator.instance;
        boolean rendered = false;
        final float h0 = getSmoothBlockHeight(renderer.blockAccess, block, x, y, z);
        final float h2 = getSmoothBlockHeight(renderer.blockAccess, block, x, y, z + 1);
        final float h3 = getSmoothBlockHeight(renderer.blockAccess, block, x + 1, y, z + 1);
        final float h4 = getSmoothBlockHeight(renderer.blockAccess, block, x + 1, y, z);
        if(renderer.renderAllFaces || block.shouldSideBeRendered(renderer.blockAccess, x, y - 1, z, 0))
        {
            renderer.aoBrightnessXYNN = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y - 1, z);
            renderer.aoBrightnessYZNN = block.getMixedBrightnessForBlock(renderer.blockAccess, x, y - 1, z - 1);
            renderer.aoBrightnessYZNP = block.getMixedBrightnessForBlock(renderer.blockAccess, x, y - 1, z + 1);
            renderer.aoBrightnessXYPN = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y - 1, z);
            renderer.aoLightValueScratchXYNN = renderer.blockAccess.getBlock(x - 1, y - 1, z).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchYZNN = renderer.blockAccess.getBlock(x, y - 1, z - 1).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchYZNP = renderer.blockAccess.getBlock(x, y - 1, z + 1).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchXYPN = renderer.blockAccess.getBlock(x + 1, y - 1, z).getAmbientOcclusionLightValue();
            final boolean flag2 = renderer.blockAccess.getBlock(x + 1, y - 2, z).getCanBlockGrass();
            final boolean flag3 = renderer.blockAccess.getBlock(x - 1, y - 2, z).getCanBlockGrass();
            final boolean flag4 = renderer.blockAccess.getBlock(x, y - 2, z + 1).getCanBlockGrass();
            final boolean flag5 = renderer.blockAccess.getBlock(x, y - 2, z - 1).getCanBlockGrass();
            if(!flag5 && !flag3)
            {
                renderer.aoLightValueScratchXYZNNN = renderer.aoLightValueScratchXYNN;
                renderer.aoBrightnessXYZNNN = renderer.aoBrightnessXYNN;
            }
            else
            {
                renderer.aoLightValueScratchXYZNNN = renderer.blockAccess.getBlock(x - 1, y - 1, z - 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZNNN = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y - 1, z - 1);
            }
            if(!flag4 && !flag3)
            {
                renderer.aoLightValueScratchXYZNNP = renderer.aoLightValueScratchXYNN;
                renderer.aoBrightnessXYZNNP = renderer.aoBrightnessXYNN;
            }
            else
            {
                renderer.aoLightValueScratchXYZNNP = renderer.blockAccess.getBlock(x - 1, y - 1, z + 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZNNP = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y - 1, z + 1);
            }
            if(!flag5 && !flag2)
            {
                renderer.aoLightValueScratchXYZPNN = renderer.aoLightValueScratchXYPN;
                renderer.aoBrightnessXYZPNN = renderer.aoBrightnessXYPN;
            }
            else
            {
                renderer.aoLightValueScratchXYZPNN = renderer.blockAccess.getBlock(x + 1, y - 1, z - 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZPNN = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y - 1, z - 1);
            }
            if(!flag4 && !flag2)
            {
                renderer.aoLightValueScratchXYZPNP = renderer.aoLightValueScratchXYPN;
                renderer.aoBrightnessXYZPNP = renderer.aoBrightnessXYPN;
            }
            else
            {
                renderer.aoLightValueScratchXYZPNP = renderer.blockAccess.getBlock(x + 1, y - 1, z + 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZPNP = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y - 1, z + 1);
            }
            final int i1 = block.getMixedBrightnessForBlock(renderer.blockAccess, x, y - 1, z);
            final float f7 = renderer.blockAccess.getBlock(x, y - 1, z).getAmbientOcclusionLightValue();
            final float f8 = (renderer.aoLightValueScratchXYZNNP + renderer.aoLightValueScratchXYNN + renderer.aoLightValueScratchYZNP + f7) / 4.0f;
            final float f9 = (renderer.aoLightValueScratchYZNP + f7 + renderer.aoLightValueScratchXYZPNP + renderer.aoLightValueScratchXYPN) / 4.0f;
            final float f10 = (f7 + renderer.aoLightValueScratchYZNN + renderer.aoLightValueScratchXYPN + renderer.aoLightValueScratchXYZPNN) / 4.0f;
            final float f11 = (renderer.aoLightValueScratchXYNN + renderer.aoLightValueScratchXYZNNN + f7 + renderer.aoLightValueScratchYZNN) / 4.0f;
            renderer.brightnessTopLeft = renderer.getAoBrightness(renderer.aoBrightnessXYZNNP, renderer.aoBrightnessXYNN, renderer.aoBrightnessYZNP, i1);
            renderer.brightnessTopRight = renderer.getAoBrightness(renderer.aoBrightnessYZNP, renderer.aoBrightnessXYZPNP, renderer.aoBrightnessXYPN, i1);
            renderer.brightnessBottomRight = renderer.getAoBrightness(renderer.aoBrightnessYZNN, renderer.aoBrightnessXYPN, renderer.aoBrightnessXYZPNN, i1);
            renderer.brightnessBottomLeft = renderer.getAoBrightness(renderer.aoBrightnessXYNN, renderer.aoBrightnessXYZNNN, renderer.aoBrightnessYZNN, i1);
            final float n = r * 0.5f;
            renderer.colorRedTopRight = n;
            renderer.colorRedBottomRight = n;
            renderer.colorRedBottomLeft = n;
            renderer.colorRedTopLeft = n;
            final float n2 = g * 0.5f;
            renderer.colorGreenTopRight = n2;
            renderer.colorGreenBottomRight = n2;
            renderer.colorGreenBottomLeft = n2;
            renderer.colorGreenTopLeft = n2;
            final float n3 = b * 0.5f;
            renderer.colorBlueTopRight = n3;
            renderer.colorBlueBottomRight = n3;
            renderer.colorBlueBottomLeft = n3;
            renderer.colorBlueTopLeft = n3;
            renderer.colorRedTopLeft *= f8;
            renderer.colorGreenTopLeft *= f8;
            renderer.colorBlueTopLeft *= f8;
            renderer.colorRedBottomLeft *= f11;
            renderer.colorGreenBottomLeft *= f11;
            renderer.colorBlueBottomLeft *= f11;
            renderer.colorRedBottomRight *= f10;
            renderer.colorGreenBottomRight *= f10;
            renderer.colorBlueBottomRight *= f10;
            renderer.colorRedTopRight *= f9;
            renderer.colorGreenTopRight *= f9;
            renderer.colorBlueTopRight *= f9;
            final IIcon icon = renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 0);
            final double u0 = icon.getMinU();
            final double u2 = icon.getMaxU();
            final double v0 = icon.getMinV();
            final double v2 = icon.getMaxV();
            final double u3 = u2;
            final double u4 = u0;
            final double v3 = v0;
            final double v4 = v2;
            final double x2 = x;
            final double x3 = x + 1.0;
            final double y2 = y;
            final double z2 = z;
            final double z3 = z + 1.0;
            tessellator.setColorOpaque_F(renderer.colorRedTopLeft, renderer.colorGreenTopLeft, renderer.colorBlueTopLeft);
            tessellator.setBrightness(renderer.brightnessTopLeft);
            tessellator.addVertexWithUV(x2, y2, z3, u4, v4);
            tessellator.setColorOpaque_F(renderer.colorRedBottomLeft, renderer.colorGreenBottomLeft, renderer.colorBlueBottomLeft);
            tessellator.setBrightness(renderer.brightnessBottomLeft);
            tessellator.addVertexWithUV(x2, y2, z2, u0, v0);
            tessellator.setColorOpaque_F(renderer.colorRedBottomRight, renderer.colorGreenBottomRight, renderer.colorBlueBottomRight);
            tessellator.setBrightness(renderer.brightnessBottomRight);
            tessellator.addVertexWithUV(x3, y2, z2, u3, v3);
            tessellator.setColorOpaque_F(renderer.colorRedTopRight, renderer.colorGreenTopRight, renderer.colorBlueTopRight);
            tessellator.setBrightness(renderer.brightnessTopRight);
            tessellator.addVertexWithUV(x3, y2, z3, u2, v2);
            rendered = true;
        }
        if(renderer.renderAllFaces || block.shouldSideBeRendered(renderer.blockAccess, x, y + 1, z, 1))
        {
            renderer.aoBrightnessXYNP = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y + 1, z);
            renderer.aoBrightnessXYPP = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y + 1, z);
            renderer.aoBrightnessYZPN = block.getMixedBrightnessForBlock(renderer.blockAccess, x, y + 1, z - 1);
            renderer.aoBrightnessYZPP = block.getMixedBrightnessForBlock(renderer.blockAccess, x, y + 1, z + 1);
            renderer.aoLightValueScratchXYNP = renderer.blockAccess.getBlock(x - 1, y + 1, z).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchXYPP = renderer.blockAccess.getBlock(x + 1, y + 1, z).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchYZPN = renderer.blockAccess.getBlock(x, y + 1, z - 1).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchYZPP = renderer.blockAccess.getBlock(x, y + 1, z + 1).getAmbientOcclusionLightValue();
            final boolean flag2 = renderer.blockAccess.getBlock(x + 1, y + 2, z).getCanBlockGrass();
            final boolean flag3 = renderer.blockAccess.getBlock(x - 1, y + 2, z).getCanBlockGrass();
            final boolean flag4 = renderer.blockAccess.getBlock(x, y + 2, z + 1).getCanBlockGrass();
            final boolean flag5 = renderer.blockAccess.getBlock(x, y + 2, z - 1).getCanBlockGrass();
            if(!flag5 && !flag3)
            {
                renderer.aoLightValueScratchXYZNPN = renderer.aoLightValueScratchXYNP;
                renderer.aoBrightnessXYZNPN = renderer.aoBrightnessXYNP;
            }
            else
            {
                renderer.aoLightValueScratchXYZNPN = renderer.blockAccess.getBlock(x - 1, y + 1, z - 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZNPN = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y + 1, z - 1);
            }
            if(!flag5 && !flag2)
            {
                renderer.aoLightValueScratchXYZPPN = renderer.aoLightValueScratchXYPP;
                renderer.aoBrightnessXYZPPN = renderer.aoBrightnessXYPP;
            }
            else
            {
                renderer.aoLightValueScratchXYZPPN = renderer.blockAccess.getBlock(x + 1, y + 1, z - 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZPPN = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y + 1, z - 1);
            }
            if(!flag4 && !flag3)
            {
                renderer.aoLightValueScratchXYZNPP = renderer.aoLightValueScratchXYNP;
                renderer.aoBrightnessXYZNPP = renderer.aoBrightnessXYNP;
            }
            else
            {
                renderer.aoLightValueScratchXYZNPP = renderer.blockAccess.getBlock(x - 1, y + 1, z + 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZNPP = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y + 1, z + 1);
            }
            if(!flag4 && !flag2)
            {
                renderer.aoLightValueScratchXYZPPP = renderer.aoLightValueScratchXYPP;
                renderer.aoBrightnessXYZPPP = renderer.aoBrightnessXYPP;
            }
            else
            {
                renderer.aoLightValueScratchXYZPPP = renderer.blockAccess.getBlock(x + 1, y + 1, z + 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZPPP = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y + 1, z + 1);
            }
            final int i1 = block.getMixedBrightnessForBlock(renderer.blockAccess, x, y + 1, z);
            final float f7 = renderer.blockAccess.getBlock(x, y + 1, z).getAmbientOcclusionLightValue();
            final float f12 = (renderer.aoLightValueScratchXYZNPP + renderer.aoLightValueScratchXYNP + renderer.aoLightValueScratchYZPP + f7) / 4.0f;
            final float f13 = (renderer.aoLightValueScratchYZPP + f7 + renderer.aoLightValueScratchXYZPPP + renderer.aoLightValueScratchXYPP) / 4.0f;
            final float f14 = (f7 + renderer.aoLightValueScratchYZPN + renderer.aoLightValueScratchXYPP + renderer.aoLightValueScratchXYZPPN) / 4.0f;
            final float f15 = (renderer.aoLightValueScratchXYNP + renderer.aoLightValueScratchXYZNPN + f7 + renderer.aoLightValueScratchYZPN) / 4.0f;
            renderer.brightnessTopRight = renderer.getAoBrightness(renderer.aoBrightnessXYZNPP, renderer.aoBrightnessXYNP, renderer.aoBrightnessYZPP, i1);
            renderer.brightnessTopLeft = renderer.getAoBrightness(renderer.aoBrightnessYZPP, renderer.aoBrightnessXYZPPP, renderer.aoBrightnessXYPP, i1);
            renderer.brightnessBottomLeft = renderer.getAoBrightness(renderer.aoBrightnessYZPN, renderer.aoBrightnessXYPP, renderer.aoBrightnessXYZPPN, i1);
            renderer.brightnessBottomRight = renderer.getAoBrightness(renderer.aoBrightnessXYNP, renderer.aoBrightnessXYZNPN, renderer.aoBrightnessYZPN, i1);
            renderer.colorRedTopRight = r;
            renderer.colorRedBottomRight = r;
            renderer.colorRedBottomLeft = r;
            renderer.colorRedTopLeft = r;
            renderer.colorGreenTopRight = g;
            renderer.colorGreenBottomRight = g;
            renderer.colorGreenBottomLeft = g;
            renderer.colorGreenTopLeft = g;
            renderer.colorBlueTopRight = b;
            renderer.colorBlueBottomRight = b;
            renderer.colorBlueBottomLeft = b;
            renderer.colorBlueTopLeft = b;
            renderer.colorRedTopLeft *= f13;
            renderer.colorGreenTopLeft *= f13;
            renderer.colorBlueTopLeft *= f13;
            renderer.colorRedBottomLeft *= f14;
            renderer.colorGreenBottomLeft *= f14;
            renderer.colorBlueBottomLeft *= f14;
            renderer.colorRedBottomRight *= f15;
            renderer.colorGreenBottomRight *= f15;
            renderer.colorBlueBottomRight *= f15;
            renderer.colorRedTopRight *= f12;
            renderer.colorGreenTopRight *= f12;
            renderer.colorBlueTopRight *= f12;
            final IIcon icon = renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 1);
            final double u0 = icon.getMinU();
            final double u2 = icon.getMaxU();
            final double v0 = icon.getMinV();
            final double v2 = icon.getMaxV();
            final double u3 = u2;
            final double u4 = u0;
            final double v3 = v0;
            final double v4 = v2;
            final double x2 = x;
            final double x3 = x + 1.0;
            final double y2 = y;
            final double z2 = z;
            final double z3 = z + 1.0;
            tessellator.setColorOpaque_F(renderer.colorRedTopLeft, renderer.colorGreenTopLeft, renderer.colorBlueTopLeft);
            tessellator.setBrightness(renderer.brightnessTopLeft);
            tessellator.addVertexWithUV(x3, y2 + h3, z3, u2, v2);
            tessellator.setColorOpaque_F(renderer.colorRedBottomLeft, renderer.colorGreenBottomLeft, renderer.colorBlueBottomLeft);
            tessellator.setBrightness(renderer.brightnessBottomLeft);
            tessellator.addVertexWithUV(x3, y2 + h4, z2, u3, v3);
            tessellator.setColorOpaque_F(renderer.colorRedBottomRight, renderer.colorGreenBottomRight, renderer.colorBlueBottomRight);
            tessellator.setBrightness(renderer.brightnessBottomRight);
            tessellator.addVertexWithUV(x2, y2 + h0, z2, u0, v0);
            tessellator.setColorOpaque_F(renderer.colorRedTopRight, renderer.colorGreenTopRight, renderer.colorBlueTopRight);
            tessellator.setBrightness(renderer.brightnessTopRight);
            tessellator.addVertexWithUV(x2, y2 + h2, z3, u4, v4);
            rendered = true;
        }
        if((h0 != 0.0f || h4 != 0.0f) && (renderer.renderAllFaces || block.shouldSideBeRendered(renderer.blockAccess, x, y, z - 1, 2)))
        {
            renderer.aoLightValueScratchXZNN = renderer.blockAccess.getBlock(x - 1, y, z - 1).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchYZNN = renderer.blockAccess.getBlock(x, y - 1, z - 1).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchYZPN = renderer.blockAccess.getBlock(x, y + 1, z - 1).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchXZPN = renderer.blockAccess.getBlock(x + 1, y, z - 1).getAmbientOcclusionLightValue();
            renderer.aoBrightnessXZNN = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y, z - 1);
            renderer.aoBrightnessYZNN = block.getMixedBrightnessForBlock(renderer.blockAccess, x, y - 1, z - 1);
            renderer.aoBrightnessYZPN = block.getMixedBrightnessForBlock(renderer.blockAccess, x, y + 1, z - 1);
            renderer.aoBrightnessXZPN = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y, z - 1);
            final boolean flag2 = renderer.blockAccess.getBlock(x + 1, y, z - 2).getCanBlockGrass();
            final boolean flag3 = renderer.blockAccess.getBlock(x - 1, y, z - 2).getCanBlockGrass();
            final boolean flag4 = renderer.blockAccess.getBlock(x, y + 1, z - 2).getCanBlockGrass();
            final boolean flag5 = renderer.blockAccess.getBlock(x, y - 1, z - 2).getCanBlockGrass();
            if(!flag3 && !flag5)
            {
                renderer.aoLightValueScratchXYZNNN = renderer.aoLightValueScratchXZNN;
                renderer.aoBrightnessXYZNNN = renderer.aoBrightnessXZNN;
            }
            else
            {
                renderer.aoLightValueScratchXYZNNN = renderer.blockAccess.getBlock(x - 1, y - 1, z - 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZNNN = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y - 1, z - 1);
            }
            if(!flag3 && !flag4)
            {
                renderer.aoLightValueScratchXYZNPN = renderer.aoLightValueScratchXZNN;
                renderer.aoBrightnessXYZNPN = renderer.aoBrightnessXZNN;
            }
            else
            {
                renderer.aoLightValueScratchXYZNPN = renderer.blockAccess.getBlock(x - 1, y + 1, z - 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZNPN = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y + 1, z - 1);
            }
            if(!flag2 && !flag5)
            {
                renderer.aoLightValueScratchXYZPNN = renderer.aoLightValueScratchXZPN;
                renderer.aoBrightnessXYZPNN = renderer.aoBrightnessXZPN;
            }
            else
            {
                renderer.aoLightValueScratchXYZPNN = renderer.blockAccess.getBlock(x + 1, y - 1, z - 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZPNN = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y - 1, z - 1);
            }
            if(!flag2 && !flag4)
            {
                renderer.aoLightValueScratchXYZPPN = renderer.aoLightValueScratchXZPN;
                renderer.aoBrightnessXYZPPN = renderer.aoBrightnessXZPN;
            }
            else
            {
                renderer.aoLightValueScratchXYZPPN = renderer.blockAccess.getBlock(x + 1, y + 1, z - 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZPPN = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y + 1, z - 1);
            }
            final int i1 = block.getMixedBrightnessForBlock(renderer.blockAccess, x, y, z - 1);
            final float f7 = renderer.blockAccess.getBlock(x, y, z - 1).getAmbientOcclusionLightValue();
            final float f8 = (renderer.aoLightValueScratchXZNN + renderer.aoLightValueScratchXYZNPN + f7 + renderer.aoLightValueScratchYZPN) / 4.0f;
            final float f16 = (f7 + renderer.aoLightValueScratchYZPN + renderer.aoLightValueScratchXZPN + renderer.aoLightValueScratchXYZPPN) / 4.0f;
            final float f10 = (renderer.aoLightValueScratchYZNN + f7 + renderer.aoLightValueScratchXYZPNN + renderer.aoLightValueScratchXZPN) / 4.0f;
            final float f17 = (renderer.aoLightValueScratchXYZNNN + renderer.aoLightValueScratchXZNN + renderer.aoLightValueScratchYZNN + f7) / 4.0f;
            renderer.brightnessTopLeft = renderer.getAoBrightness(renderer.aoBrightnessXZNN, renderer.aoBrightnessXYZNPN, renderer.aoBrightnessYZPN, i1);
            renderer.brightnessBottomLeft = renderer.getAoBrightness(renderer.aoBrightnessYZPN, renderer.aoBrightnessXZPN, renderer.aoBrightnessXYZPPN, i1);
            renderer.brightnessBottomRight = renderer.getAoBrightness(renderer.aoBrightnessYZNN, renderer.aoBrightnessXYZPNN, renderer.aoBrightnessXZPN, i1);
            renderer.brightnessTopRight = renderer.getAoBrightness(renderer.aoBrightnessXYZNNN, renderer.aoBrightnessXZNN, renderer.aoBrightnessYZNN, i1);
            final float n4 = r * 0.8f;
            renderer.colorRedTopRight = n4;
            renderer.colorRedBottomRight = n4;
            renderer.colorRedBottomLeft = n4;
            renderer.colorRedTopLeft = n4;
            final float n5 = g * 0.8f;
            renderer.colorGreenTopRight = n5;
            renderer.colorGreenBottomRight = n5;
            renderer.colorGreenBottomLeft = n5;
            renderer.colorGreenTopLeft = n5;
            final float n6 = b * 0.8f;
            renderer.colorBlueTopRight = n6;
            renderer.colorBlueBottomRight = n6;
            renderer.colorBlueBottomLeft = n6;
            renderer.colorBlueTopLeft = n6;
            renderer.colorRedTopLeft *= f8;
            renderer.colorGreenTopLeft *= f8;
            renderer.colorBlueTopLeft *= f8;
            renderer.colorRedBottomLeft *= f16;
            renderer.colorGreenBottomLeft *= f16;
            renderer.colorBlueBottomLeft *= f16;
            renderer.colorRedBottomRight *= f10;
            renderer.colorGreenBottomRight *= f10;
            renderer.colorBlueBottomRight *= f10;
            renderer.colorRedTopRight *= f17;
            renderer.colorGreenTopRight *= f17;
            renderer.colorBlueTopRight *= f17;
            final IIcon icon = renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 2);
            final double u0 = icon.getMinU();
            final double u2 = icon.getMaxU();
            double v0 = icon.getMinV();
            final double v2 = icon.getMaxV();
            final double u3 = u2;
            final double u4 = u0;
            final double v3 = v0;
            final double v4 = v2;
            if(h4 == 0.0f)
            {
                v0 = v2;
            }
            final double x2 = x;
            final double x3 = x + 1.0;
            final double y2 = y;
            final double z2 = z;
            tessellator.setColorOpaque_F(renderer.colorRedTopLeft, renderer.colorGreenTopLeft, renderer.colorBlueTopLeft);
            tessellator.setBrightness(renderer.brightnessTopLeft);
            tessellator.addVertexWithUV(x2, y2 + h0, z2, u3, v3);
            tessellator.setColorOpaque_F(renderer.colorRedBottomLeft, renderer.colorGreenBottomLeft, renderer.colorBlueBottomLeft);
            tessellator.setBrightness(renderer.brightnessBottomLeft);
            tessellator.addVertexWithUV(x3, y2 + h4, z2, u0, v0);
            tessellator.setColorOpaque_F(renderer.colorRedBottomRight, renderer.colorGreenBottomRight, renderer.colorBlueBottomRight);
            tessellator.setBrightness(renderer.brightnessBottomRight);
            tessellator.addVertexWithUV(x3, y2, z2, u4, v4);
            tessellator.setColorOpaque_F(renderer.colorRedTopRight, renderer.colorGreenTopRight, renderer.colorBlueTopRight);
            tessellator.setBrightness(renderer.brightnessTopRight);
            tessellator.addVertexWithUV(x2, y2, z2, u2, v2);
            rendered = true;
        }
        if((h2 != 0.0f || h3 != 0.0f) && (renderer.renderAllFaces || block.shouldSideBeRendered(renderer.blockAccess, x, y, z + 1, 3)))
        {
            renderer.aoLightValueScratchXZNP = renderer.blockAccess.getBlock(x - 1, y, z + 1).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchXZPP = renderer.blockAccess.getBlock(x + 1, y, z + 1).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchYZNP = renderer.blockAccess.getBlock(x, y - 1, z + 1).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchYZPP = renderer.blockAccess.getBlock(x, y + 1, z + 1).getAmbientOcclusionLightValue();
            renderer.aoBrightnessXZNP = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y, z + 1);
            renderer.aoBrightnessXZPP = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y, z + 1);
            renderer.aoBrightnessYZNP = block.getMixedBrightnessForBlock(renderer.blockAccess, x, y - 1, z + 1);
            renderer.aoBrightnessYZPP = block.getMixedBrightnessForBlock(renderer.blockAccess, x, y + 1, z + 1);
            final boolean flag2 = renderer.blockAccess.getBlock(x + 1, y, z + 2).getCanBlockGrass();
            final boolean flag3 = renderer.blockAccess.getBlock(x - 1, y, z + 2).getCanBlockGrass();
            final boolean flag4 = renderer.blockAccess.getBlock(x, y + 1, z + 2).getCanBlockGrass();
            final boolean flag5 = renderer.blockAccess.getBlock(x, y - 1, z + 2).getCanBlockGrass();
            if(!flag3 && !flag5)
            {
                renderer.aoLightValueScratchXYZNNP = renderer.aoLightValueScratchXZNP;
                renderer.aoBrightnessXYZNNP = renderer.aoBrightnessXZNP;
            }
            else
            {
                renderer.aoLightValueScratchXYZNNP = renderer.blockAccess.getBlock(x - 1, y - 1, z + 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZNNP = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y - 1, z + 1);
            }
            if(!flag3 && !flag4)
            {
                renderer.aoLightValueScratchXYZNPP = renderer.aoLightValueScratchXZNP;
                renderer.aoBrightnessXYZNPP = renderer.aoBrightnessXZNP;
            }
            else
            {
                renderer.aoLightValueScratchXYZNPP = renderer.blockAccess.getBlock(x - 1, y + 1, z + 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZNPP = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y + 1, z + 1);
            }
            if(!flag2 && !flag5)
            {
                renderer.aoLightValueScratchXYZPNP = renderer.aoLightValueScratchXZPP;
                renderer.aoBrightnessXYZPNP = renderer.aoBrightnessXZPP;
            }
            else
            {
                renderer.aoLightValueScratchXYZPNP = renderer.blockAccess.getBlock(x + 1, y - 1, z + 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZPNP = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y - 1, z + 1);
            }
            if(!flag2 && !flag4)
            {
                renderer.aoLightValueScratchXYZPPP = renderer.aoLightValueScratchXZPP;
                renderer.aoBrightnessXYZPPP = renderer.aoBrightnessXZPP;
            }
            else
            {
                renderer.aoLightValueScratchXYZPPP = renderer.blockAccess.getBlock(x + 1, y + 1, z + 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZPPP = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y + 1, z + 1);
            }
            final int i1 = block.getMixedBrightnessForBlock(renderer.blockAccess, x, y, z + 1);
            final float f7 = renderer.blockAccess.getBlock(x, y, z + 1).getAmbientOcclusionLightValue();
            final float f8 = (renderer.aoLightValueScratchXZNP + renderer.aoLightValueScratchXYZNPP + f7 + renderer.aoLightValueScratchYZPP) / 4.0f;
            final float f9 = (f7 + renderer.aoLightValueScratchYZPP + renderer.aoLightValueScratchXZPP + renderer.aoLightValueScratchXYZPPP) / 4.0f;
            final float f10 = (renderer.aoLightValueScratchYZNP + f7 + renderer.aoLightValueScratchXYZPNP + renderer.aoLightValueScratchXZPP) / 4.0f;
            final float f11 = (renderer.aoLightValueScratchXYZNNP + renderer.aoLightValueScratchXZNP + renderer.aoLightValueScratchYZNP + f7) / 4.0f;
            renderer.brightnessTopLeft = renderer.getAoBrightness(renderer.aoBrightnessXZNP, renderer.aoBrightnessXYZNPP, renderer.aoBrightnessYZPP, i1);
            renderer.brightnessTopRight = renderer.getAoBrightness(renderer.aoBrightnessYZPP, renderer.aoBrightnessXZPP, renderer.aoBrightnessXYZPPP, i1);
            renderer.brightnessBottomRight = renderer.getAoBrightness(renderer.aoBrightnessYZNP, renderer.aoBrightnessXYZPNP, renderer.aoBrightnessXZPP, i1);
            renderer.brightnessBottomLeft = renderer.getAoBrightness(renderer.aoBrightnessXYZNNP, renderer.aoBrightnessXZNP, renderer.aoBrightnessYZNP, i1);
            final float n7 = r * 0.8f;
            renderer.colorRedTopRight = n7;
            renderer.colorRedBottomRight = n7;
            renderer.colorRedBottomLeft = n7;
            renderer.colorRedTopLeft = n7;
            final float n8 = g * 0.8f;
            renderer.colorGreenTopRight = n8;
            renderer.colorGreenBottomRight = n8;
            renderer.colorGreenBottomLeft = n8;
            renderer.colorGreenTopLeft = n8;
            final float n9 = b * 0.8f;
            renderer.colorBlueTopRight = n9;
            renderer.colorBlueBottomRight = n9;
            renderer.colorBlueBottomLeft = n9;
            renderer.colorBlueTopLeft = n9;
            renderer.colorRedTopLeft *= f8;
            renderer.colorGreenTopLeft *= f8;
            renderer.colorBlueTopLeft *= f8;
            renderer.colorRedBottomLeft *= f11;
            renderer.colorGreenBottomLeft *= f11;
            renderer.colorBlueBottomLeft *= f11;
            renderer.colorRedBottomRight *= f10;
            renderer.colorGreenBottomRight *= f10;
            renderer.colorBlueBottomRight *= f10;
            renderer.colorRedTopRight *= f9;
            renderer.colorGreenTopRight *= f9;
            renderer.colorBlueTopRight *= f9;
            final IIcon icon = renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 3);
            final double u0 = icon.getMinU();
            final double u2 = icon.getMaxU();
            final double v0 = icon.getMinV();
            final double v2 = icon.getMaxV();
            final double u3 = u2;
            final double u4 = u0;
            double v3 = v0;
            final double v4 = v2;
            if(h3 == 0.0f)
            {
                v3 = v2;
            }
            final double x2 = x;
            final double x3 = x + 1.0;
            final double y2 = y;
            final double z4 = z + 1.0;
            tessellator.setColorOpaque_F(renderer.colorRedTopLeft, renderer.colorGreenTopLeft, renderer.colorBlueTopLeft);
            tessellator.setBrightness(renderer.brightnessTopLeft);
            tessellator.addVertexWithUV(x2, y2 + h2, z4, u0, v0);
            tessellator.setColorOpaque_F(renderer.colorRedBottomLeft, renderer.colorGreenBottomLeft, renderer.colorBlueBottomLeft);
            tessellator.setBrightness(renderer.brightnessBottomLeft);
            tessellator.addVertexWithUV(x2, y2, z4, u4, v4);
            tessellator.setColorOpaque_F(renderer.colorRedBottomRight, renderer.colorGreenBottomRight, renderer.colorBlueBottomRight);
            tessellator.setBrightness(renderer.brightnessBottomRight);
            tessellator.addVertexWithUV(x3, y2, z4, u2, v2);
            tessellator.setColorOpaque_F(renderer.colorRedTopRight, renderer.colorGreenTopRight, renderer.colorBlueTopRight);
            tessellator.setBrightness(renderer.brightnessTopRight);
            tessellator.addVertexWithUV(x3, y2 + h3, z4, u3, v3);
            rendered = true;
        }
        if((h0 != 0.0f || h2 != 0.0f) && (renderer.renderAllFaces || block.shouldSideBeRendered(renderer.blockAccess, x - 1, y, z, 4)))
        {
            renderer.aoLightValueScratchXYNN = renderer.blockAccess.getBlock(x - 1, y - 1, z).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchXZNN = renderer.blockAccess.getBlock(x - 1, y, z - 1).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchXZNP = renderer.blockAccess.getBlock(x - 1, y, z + 1).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchXYNP = renderer.blockAccess.getBlock(x - 1, y + 1, z).getAmbientOcclusionLightValue();
            renderer.aoBrightnessXYNN = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y - 1, z);
            renderer.aoBrightnessXZNN = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y, z - 1);
            renderer.aoBrightnessXZNP = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y, z + 1);
            renderer.aoBrightnessXYNP = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y + 1, z);
            final boolean flag2 = renderer.blockAccess.getBlock(x - 2, y + 1, z).getCanBlockGrass();
            final boolean flag3 = renderer.blockAccess.getBlock(x - 2, y - 1, z).getCanBlockGrass();
            final boolean flag4 = renderer.blockAccess.getBlock(x - 2, y, z - 1).getCanBlockGrass();
            final boolean flag5 = renderer.blockAccess.getBlock(x - 2, y, z + 1).getCanBlockGrass();
            if(!flag4 && !flag3)
            {
                renderer.aoLightValueScratchXYZNNN = renderer.aoLightValueScratchXZNN;
                renderer.aoBrightnessXYZNNN = renderer.aoBrightnessXZNN;
            }
            else
            {
                renderer.aoLightValueScratchXYZNNN = renderer.blockAccess.getBlock(x - 1, y - 1, z - 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZNNN = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y - 1, z - 1);
            }
            if(!flag5 && !flag3)
            {
                renderer.aoLightValueScratchXYZNNP = renderer.aoLightValueScratchXZNP;
                renderer.aoBrightnessXYZNNP = renderer.aoBrightnessXZNP;
            }
            else
            {
                renderer.aoLightValueScratchXYZNNP = renderer.blockAccess.getBlock(x - 1, y - 1, z + 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZNNP = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y - 1, z + 1);
            }
            if(!flag4 && !flag2)
            {
                renderer.aoLightValueScratchXYZNPN = renderer.aoLightValueScratchXZNN;
                renderer.aoBrightnessXYZNPN = renderer.aoBrightnessXZNN;
            }
            else
            {
                renderer.aoLightValueScratchXYZNPN = renderer.blockAccess.getBlock(x - 1, y + 1, z - 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZNPN = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y + 1, z - 1);
            }
            if(!flag5 && !flag2)
            {
                renderer.aoLightValueScratchXYZNPP = renderer.aoLightValueScratchXZNP;
                renderer.aoBrightnessXYZNPP = renderer.aoBrightnessXZNP;
            }
            else
            {
                renderer.aoLightValueScratchXYZNPP = renderer.blockAccess.getBlock(x - 1, y + 1, z + 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZNPP = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y + 1, z + 1);
            }
            final int i1 = block.getMixedBrightnessForBlock(renderer.blockAccess, x - 1, y, z);
            final float f7 = renderer.blockAccess.getBlock(x - 1, y, z).getAmbientOcclusionLightValue();
            final float f12 = (renderer.aoLightValueScratchXYNN + renderer.aoLightValueScratchXYZNNP + f7 + renderer.aoLightValueScratchXZNP) / 4.0f;
            final float f13 = (f7 + renderer.aoLightValueScratchXZNP + renderer.aoLightValueScratchXYNP + renderer.aoLightValueScratchXYZNPP) / 4.0f;
            final float f14 = (renderer.aoLightValueScratchXZNN + f7 + renderer.aoLightValueScratchXYZNPN + renderer.aoLightValueScratchXYNP) / 4.0f;
            final float f15 = (renderer.aoLightValueScratchXYZNNN + renderer.aoLightValueScratchXYNN + renderer.aoLightValueScratchXZNN + f7) / 4.0f;
            renderer.brightnessTopRight = renderer.getAoBrightness(renderer.aoBrightnessXYNN, renderer.aoBrightnessXYZNNP, renderer.aoBrightnessXZNP, i1);
            renderer.brightnessTopLeft = renderer.getAoBrightness(renderer.aoBrightnessXZNP, renderer.aoBrightnessXYNP, renderer.aoBrightnessXYZNPP, i1);
            renderer.brightnessBottomLeft = renderer.getAoBrightness(renderer.aoBrightnessXZNN, renderer.aoBrightnessXYZNPN, renderer.aoBrightnessXYNP, i1);
            renderer.brightnessBottomRight = renderer.getAoBrightness(renderer.aoBrightnessXYZNNN, renderer.aoBrightnessXYNN, renderer.aoBrightnessXZNN, i1);
            final float n10 = r * 0.6f;
            renderer.colorRedTopRight = n10;
            renderer.colorRedBottomRight = n10;
            renderer.colorRedBottomLeft = n10;
            renderer.colorRedTopLeft = n10;
            final float n11 = g * 0.6f;
            renderer.colorGreenTopRight = n11;
            renderer.colorGreenBottomRight = n11;
            renderer.colorGreenBottomLeft = n11;
            renderer.colorGreenTopLeft = n11;
            final float n12 = b * 0.6f;
            renderer.colorBlueTopRight = n12;
            renderer.colorBlueBottomRight = n12;
            renderer.colorBlueBottomLeft = n12;
            renderer.colorBlueTopLeft = n12;
            renderer.colorRedTopLeft *= f13;
            renderer.colorGreenTopLeft *= f13;
            renderer.colorBlueTopLeft *= f13;
            renderer.colorRedBottomLeft *= f14;
            renderer.colorGreenBottomLeft *= f14;
            renderer.colorBlueBottomLeft *= f14;
            renderer.colorRedBottomRight *= f15;
            renderer.colorGreenBottomRight *= f15;
            renderer.colorBlueBottomRight *= f15;
            renderer.colorRedTopRight *= f12;
            renderer.colorGreenTopRight *= f12;
            renderer.colorBlueTopRight *= f12;
            final IIcon icon = renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 4);
            double u0 = icon.getMinU();
            final double u2 = icon.getMaxU();
            final double v0 = icon.getMinV();
            final double v2 = icon.getMaxV();
            final double u3 = u2;
            final double u4 = u0;
            final double v3 = v0;
            final double v4 = v2;
            if(h0 == 0.0f)
            {
                u0 = u2;
            }
            final double x2 = x;
            final double y3 = y;
            final double z5 = z;
            final double z4 = z + 1.0;
            tessellator.setColorOpaque_F(renderer.colorRedTopLeft, renderer.colorGreenTopLeft, renderer.colorBlueTopLeft);
            tessellator.setBrightness(renderer.brightnessTopLeft);
            tessellator.addVertexWithUV(x2, y3 + h2, z4, u4, v4);
            tessellator.setColorOpaque_F(renderer.colorRedBottomLeft, renderer.colorGreenBottomLeft, renderer.colorBlueBottomLeft);
            tessellator.setBrightness(renderer.brightnessBottomLeft);
            tessellator.addVertexWithUV(x2, y3 + h0, z5, u0, v0);
            tessellator.setColorOpaque_F(renderer.colorRedBottomRight, renderer.colorGreenBottomRight, renderer.colorBlueBottomRight);
            tessellator.setBrightness(renderer.brightnessBottomRight);
            tessellator.addVertexWithUV(x2, y3, z5, u3, v3);
            tessellator.setColorOpaque_F(renderer.colorRedTopRight, renderer.colorGreenTopRight, renderer.colorBlueTopRight);
            tessellator.setBrightness(renderer.brightnessTopRight);
            tessellator.addVertexWithUV(x2, y3, z4, u2, v2);
            rendered = true;
        }
        if(renderer.renderAllFaces || block.shouldSideBeRendered(renderer.blockAccess, x + 1, y, z, 5))
        {
            renderer.aoLightValueScratchXYPN = renderer.blockAccess.getBlock(x + 1, y - 1, z).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchXZPN = renderer.blockAccess.getBlock(x + 1, y, z - 1).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchXZPP = renderer.blockAccess.getBlock(x + 1, y, z + 1).getAmbientOcclusionLightValue();
            renderer.aoLightValueScratchXYPP = renderer.blockAccess.getBlock(x + 1, y + 1, z).getAmbientOcclusionLightValue();
            renderer.aoBrightnessXYPN = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y - 1, z);
            renderer.aoBrightnessXZPN = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y, z - 1);
            renderer.aoBrightnessXZPP = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y, z + 1);
            renderer.aoBrightnessXYPP = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y + 1, z);
            final boolean flag2 = renderer.blockAccess.getBlock(x + 2, y + 1, z).getCanBlockGrass();
            final boolean flag3 = renderer.blockAccess.getBlock(x + 2, y - 1, z).getCanBlockGrass();
            final boolean flag4 = renderer.blockAccess.getBlock(x + 2, y, z + 1).getCanBlockGrass();
            final boolean flag5 = renderer.blockAccess.getBlock(x + 2, y, z - 1).getCanBlockGrass();
            if(!flag3 && !flag5)
            {
                renderer.aoLightValueScratchXYZPNN = renderer.aoLightValueScratchXZPN;
                renderer.aoBrightnessXYZPNN = renderer.aoBrightnessXZPN;
            }
            else
            {
                renderer.aoLightValueScratchXYZPNN = renderer.blockAccess.getBlock(x + 1, y - 1, z - 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZPNN = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y - 1, z - 1);
            }
            if(!flag3 && !flag4)
            {
                renderer.aoLightValueScratchXYZPNP = renderer.aoLightValueScratchXZPP;
                renderer.aoBrightnessXYZPNP = renderer.aoBrightnessXZPP;
            }
            else
            {
                renderer.aoLightValueScratchXYZPNP = renderer.blockAccess.getBlock(x + 1, y - 1, z + 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZPNP = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y - 1, z + 1);
            }
            if(!flag2 && !flag5)
            {
                renderer.aoLightValueScratchXYZPPN = renderer.aoLightValueScratchXZPN;
                renderer.aoBrightnessXYZPPN = renderer.aoBrightnessXZPN;
            }
            else
            {
                renderer.aoLightValueScratchXYZPPN = renderer.blockAccess.getBlock(x + 1, y + 1, z - 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZPPN = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y + 1, z - 1);
            }
            if(!flag2 && !flag4)
            {
                renderer.aoLightValueScratchXYZPPP = renderer.aoLightValueScratchXZPP;
                renderer.aoBrightnessXYZPPP = renderer.aoBrightnessXZPP;
            }
            else
            {
                renderer.aoLightValueScratchXYZPPP = renderer.blockAccess.getBlock(x + 1, y + 1, z + 1).getAmbientOcclusionLightValue();
                renderer.aoBrightnessXYZPPP = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y + 1, z + 1);
            }
            final int i1 = block.getMixedBrightnessForBlock(renderer.blockAccess, x + 1, y, z);
            final float f7 = renderer.blockAccess.getBlock(x + 1, y, z).getAmbientOcclusionLightValue();
            final float f8 = (renderer.aoLightValueScratchXYPN + renderer.aoLightValueScratchXYZPNP + f7 + renderer.aoLightValueScratchXZPP) / 4.0f;
            final float f16 = (renderer.aoLightValueScratchXYZPNN + renderer.aoLightValueScratchXYPN + renderer.aoLightValueScratchXZPN + f7) / 4.0f;
            final float f10 = (renderer.aoLightValueScratchXZPN + f7 + renderer.aoLightValueScratchXYZPPN + renderer.aoLightValueScratchXYPP) / 4.0f;
            final float f17 = (f7 + renderer.aoLightValueScratchXZPP + renderer.aoLightValueScratchXYPP + renderer.aoLightValueScratchXYZPPP) / 4.0f;
            renderer.brightnessTopLeft = renderer.getAoBrightness(renderer.aoBrightnessXYPN, renderer.aoBrightnessXYZPNP, renderer.aoBrightnessXZPP, i1);
            renderer.brightnessTopRight = renderer.getAoBrightness(renderer.aoBrightnessXZPP, renderer.aoBrightnessXYPP, renderer.aoBrightnessXYZPPP, i1);
            renderer.brightnessBottomRight = renderer.getAoBrightness(renderer.aoBrightnessXZPN, renderer.aoBrightnessXYZPPN, renderer.aoBrightnessXYPP, i1);
            renderer.brightnessBottomLeft = renderer.getAoBrightness(renderer.aoBrightnessXYZPNN, renderer.aoBrightnessXYPN, renderer.aoBrightnessXZPN, i1);
            final float n13 = r * 0.6f;
            renderer.colorRedTopRight = n13;
            renderer.colorRedBottomRight = n13;
            renderer.colorRedBottomLeft = n13;
            renderer.colorRedTopLeft = n13;
            final float n14 = g * 0.6f;
            renderer.colorGreenTopRight = n14;
            renderer.colorGreenBottomRight = n14;
            renderer.colorGreenBottomLeft = n14;
            renderer.colorGreenTopLeft = n14;
            final float n15 = b * 0.6f;
            renderer.colorBlueTopRight = n15;
            renderer.colorBlueBottomRight = n15;
            renderer.colorBlueBottomLeft = n15;
            renderer.colorBlueTopLeft = n15;
            renderer.colorRedTopLeft *= f8;
            renderer.colorGreenTopLeft *= f8;
            renderer.colorBlueTopLeft *= f8;
            renderer.colorRedBottomLeft *= f16;
            renderer.colorGreenBottomLeft *= f16;
            renderer.colorBlueBottomLeft *= f16;
            renderer.colorRedBottomRight *= f10;
            renderer.colorGreenBottomRight *= f10;
            renderer.colorBlueBottomRight *= f10;
            renderer.colorRedTopRight *= f17;
            renderer.colorGreenTopRight *= f17;
            renderer.colorBlueTopRight *= f17;
            final IIcon icon = renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 5);
            final double u0 = icon.getMinU();
            final double u2 = icon.getMaxU();
            double v0 = icon.getMinV();
            final double v2 = icon.getMaxV();
            final double u3 = u2;
            final double u4 = u0;
            final double v3 = v0;
            final double v4 = v2;
            if(h3 == 0.0f)
            {
                v0 = v2;
            }
            final double x4 = x + 1.0;
            final double y3 = y;
            final double z5 = z;
            final double z4 = z + 1.0;
            tessellator.setColorOpaque_F(renderer.colorRedTopLeft, renderer.colorGreenTopLeft, renderer.colorBlueTopLeft);
            tessellator.setBrightness(renderer.brightnessTopLeft);
            tessellator.addVertexWithUV(x4, y3, z4, u4, v4);
            tessellator.setColorOpaque_F(renderer.colorRedBottomLeft, renderer.colorGreenBottomLeft, renderer.colorBlueBottomLeft);
            tessellator.setBrightness(renderer.brightnessBottomLeft);
            tessellator.addVertexWithUV(x4, y3, z5, u2, v2);
            tessellator.setColorOpaque_F(renderer.colorRedBottomRight, renderer.colorGreenBottomRight, renderer.colorBlueBottomRight);
            tessellator.setBrightness(renderer.brightnessBottomRight);
            tessellator.addVertexWithUV(x4, y3 + h4, z5, u3, v3);
            tessellator.setColorOpaque_F(renderer.colorRedTopRight, renderer.colorGreenTopRight, renderer.colorBlueTopRight);
            tessellator.setBrightness(renderer.brightnessTopRight);
            tessellator.addVertexWithUV(x4, y3 + h3, z4, u0, v0);
            rendered = true;
        }
        renderer.enableAO = false;
        return rendered;
    }

    public void renderInventoryBlock(final Block block, final int meta, final int model, final RenderBlocks renderer)
    {
        final Tessellator tessellator = Tessellator.instance;
        if(renderer.useInventoryTint)
        {
            final int color = block.getRenderColor(meta);
            final float red = (color >> 16 & 0xFF) / 255.0f;
            final float green = (color >> 8 & 0xFF) / 255.0f;
            final float blue = (color & 0xFF) / 255.0f;
            GL11.glColor4f(red, green, blue, 1.0f);
        }
        block.setBlockBoundsForItemRender();
        renderer.setRenderBoundsFromBlock(block);
        GL11.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
        GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0f, -1.0f, 0.0f);
        renderer.renderFaceYNeg(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 0, meta));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0f, 1.0f, 0.0f);
        renderer.renderFaceYPos(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 1, meta));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0f, 0.0f, -1.0f);
        renderer.renderFaceZNeg(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 2, meta));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0f, 0.0f, 1.0f);
        renderer.renderFaceZPos(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 3, meta));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0f, 0.0f, 0.0f);
        renderer.renderFaceXNeg(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 4, meta));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0f, 0.0f, 0.0f);
        renderer.renderFaceXPos(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 5, meta));
        tessellator.draw();
        GL11.glTranslatef(0.5f, 0.5f, 0.5f);
    }

    public boolean shouldRender3DInInventory(final int meta)
    {
        return true;
    }
}
