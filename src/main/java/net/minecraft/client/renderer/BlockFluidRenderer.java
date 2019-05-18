package net.minecraft.client.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Blocks;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockFluidRenderer {
   public final TextureAtlasSprite[] atlasSpritesLava = new TextureAtlasSprite[2];
   public final TextureAtlasSprite[] atlasSpritesWater = new TextureAtlasSprite[2];
   public TextureAtlasSprite atlasSpriteWaterOverlay;

   public BlockFluidRenderer() {
      this.initAtlasSprites();
   }

   protected void initAtlasSprites() {
      TextureMap texturemap = Minecraft.getInstance().getTextureMap();
      this.atlasSpritesLava[0] = Minecraft.getInstance().getModelManager().getBlockModelShapes().getModel(Blocks.LAVA.getDefaultState()).getParticleTexture();
      this.atlasSpritesLava[1] = texturemap.getSprite(ModelBakery.LOCATION_LAVA_FLOW);
      this.atlasSpritesWater[0] = Minecraft.getInstance().getModelManager().getBlockModelShapes().getModel(Blocks.WATER.getDefaultState()).getParticleTexture();
      this.atlasSpritesWater[1] = texturemap.getSprite(ModelBakery.LOCATION_WATER_FLOW);
      this.atlasSpriteWaterOverlay = texturemap.getSprite(ModelBakery.LOCATION_WATER_OVERLAY);
   }

   public static boolean isAdjacentFluidSameAs(IBlockReader worldIn, BlockPos pos, EnumFacing side, IFluidState state) {
      BlockPos blockpos = pos.offset(side);
      // NoCubes Start
      if (io.github.cadiboo.nocubes.config.Config.renderExtendedFluids) {
         final IBlockState blockState = worldIn.getBlockState(blockpos);
         if (blockState.nocubes_isTerrainSmoothable() || blockState.nocubes_isLeavesSmoothable()) {
            return io.github.cadiboo.nocubes.hooks.Hooks.smoothableIsAdjacentFluidSameAs(worldIn, side, blockpos);
         }
      }
      // NoCubes End
      IFluidState ifluidstate = worldIn.getFluidState(blockpos);
      return ifluidstate.getFluid().isEquivalentTo(state.getFluid());
   }

   public static boolean func_209556_a(IBlockReader reader, BlockPos pos, EnumFacing face, float heightIn) {
      BlockPos blockpos = pos.offset(face);
      IBlockState iblockstate = reader.getBlockState(blockpos);
      if (iblockstate.isSolid()) {
         VoxelShape voxelshape = VoxelShapes.create(0.0D, 0.0D, 0.0D, 1.0D, (double)heightIn, 1.0D);
         VoxelShape voxelshape1 = iblockstate.getRenderShape(reader, blockpos);
         return VoxelShapes.isCubeSideCovered(voxelshape, voxelshape1, face);
      } else {
         return false;
      }
   }

   public boolean render(IWorldReader worldIn, BlockPos pos, BufferBuilder buffer, IFluidState state) {
      boolean flag = state.isTagged(FluidTags.LAVA);
      TextureAtlasSprite[] atextureatlassprite = flag ? this.atlasSpritesLava : this.atlasSpritesWater;
      int i = flag ? 16777215 : BiomeColors.getWaterColor(worldIn, pos);
      float f = (float)(i >> 16 & 255) / 255.0F;
      float f1 = (float)(i >> 8 & 255) / 255.0F;
      float f2 = (float)(i & 255) / 255.0F;
      boolean flag1 = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.UP, state);
      boolean flag2 = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.DOWN, state) && !func_209556_a(worldIn, pos, EnumFacing.DOWN, 0.8888889F);
      boolean flag3 = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.NORTH, state);
      boolean flag4 = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.SOUTH, state);
      boolean flag5 = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.WEST, state);
      boolean flag6 = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.EAST, state);
      if (!flag1 && !flag2 && !flag6 && !flag5 && !flag3 && !flag4) {
         return false;
      } else {
         boolean flag7 = false;
         float f3 = 0.5F;
         float f4 = 1.0F;
         float f5 = 0.8F;
         float f6 = 0.6F;
         float f7 = this.getFluidHeight(worldIn, pos, state.getFluid());
         float f8 = this.getFluidHeight(worldIn, pos.south(), state.getFluid());
         float f9 = this.getFluidHeight(worldIn, pos.east().south(), state.getFluid());
         float f10 = this.getFluidHeight(worldIn, pos.east(), state.getFluid());
         double d0 = (double)pos.getX();
         double d1 = (double)pos.getY();
         double d2 = (double)pos.getZ();
         float f11 = 0.001F;
         if (flag1 && !func_209556_a(worldIn, pos, EnumFacing.UP, Math.min(Math.min(f7, f8), Math.min(f9, f10)))) {
            flag7 = true;
            f7 -= 0.001F;
            f8 -= 0.001F;
            f9 -= 0.001F;
            f10 -= 0.001F;
            Vec3d vec3d = state.getFlow(worldIn, pos);
            float f12;
            float f13;
            float f14;
            float f15;
            float f16;
            float f17;
            float f18;
            float f19;
            if (vec3d.x == 0.0D && vec3d.z == 0.0D) {
               TextureAtlasSprite textureatlassprite1 = atextureatlassprite[0];
               f12 = textureatlassprite1.getInterpolatedU(0.0D);
               f16 = textureatlassprite1.getInterpolatedV(0.0D);
               f13 = f12;
               f17 = textureatlassprite1.getInterpolatedV(16.0D);
               f14 = textureatlassprite1.getInterpolatedU(16.0D);
               f18 = f17;
               f15 = f14;
               f19 = f16;
            } else {
               TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
               float f20 = (float)MathHelper.atan2(vec3d.z, vec3d.x) - ((float)Math.PI / 2F);
               float f21 = MathHelper.sin(f20) * 0.25F;
               float f22 = MathHelper.cos(f20) * 0.25F;
               float f23 = 8.0F;
               f12 = textureatlassprite.getInterpolatedU((double)(8.0F + (-f22 - f21) * 16.0F));
               f16 = textureatlassprite.getInterpolatedV((double)(8.0F + (-f22 + f21) * 16.0F));
               f13 = textureatlassprite.getInterpolatedU((double)(8.0F + (-f22 + f21) * 16.0F));
               f17 = textureatlassprite.getInterpolatedV((double)(8.0F + (f22 + f21) * 16.0F));
               f14 = textureatlassprite.getInterpolatedU((double)(8.0F + (f22 + f21) * 16.0F));
               f18 = textureatlassprite.getInterpolatedV((double)(8.0F + (f22 - f21) * 16.0F));
               f15 = textureatlassprite.getInterpolatedU((double)(8.0F + (f22 - f21) * 16.0F));
               f19 = textureatlassprite.getInterpolatedV((double)(8.0F + (-f22 - f21) * 16.0F));
            }

            int i2 = this.getCombinedLightUpMax(worldIn, pos);
            int j2 = i2 >> 16 & '\uffff';
            int k2 = i2 & '\uffff';
            float f42 = 1.0F * f;
            float f43 = 1.0F * f1;
            float f24 = 1.0F * f2;
            buffer.pos(d0 + 0.0D, d1 + (double)f7, d2 + 0.0D).color(f42, f43, f24, 1.0F).tex((double)f12, (double)f16).lightmap(j2, k2).endVertex();
            buffer.pos(d0 + 0.0D, d1 + (double)f8, d2 + 1.0D).color(f42, f43, f24, 1.0F).tex((double)f13, (double)f17).lightmap(j2, k2).endVertex();
            buffer.pos(d0 + 1.0D, d1 + (double)f9, d2 + 1.0D).color(f42, f43, f24, 1.0F).tex((double)f14, (double)f18).lightmap(j2, k2).endVertex();
            buffer.pos(d0 + 1.0D, d1 + (double)f10, d2 + 0.0D).color(f42, f43, f24, 1.0F).tex((double)f15, (double)f19).lightmap(j2, k2).endVertex();
            if (state.shouldRenderSides(worldIn, pos.up())) {
               buffer.pos(d0 + 0.0D, d1 + (double)f7, d2 + 0.0D).color(f42, f43, f24, 1.0F).tex((double)f12, (double)f16).lightmap(j2, k2).endVertex();
               buffer.pos(d0 + 1.0D, d1 + (double)f10, d2 + 0.0D).color(f42, f43, f24, 1.0F).tex((double)f15, (double)f19).lightmap(j2, k2).endVertex();
               buffer.pos(d0 + 1.0D, d1 + (double)f9, d2 + 1.0D).color(f42, f43, f24, 1.0F).tex((double)f14, (double)f18).lightmap(j2, k2).endVertex();
               buffer.pos(d0 + 0.0D, d1 + (double)f8, d2 + 1.0D).color(f42, f43, f24, 1.0F).tex((double)f13, (double)f17).lightmap(j2, k2).endVertex();
            }
         }

         if (flag2) {
            float f33 = atextureatlassprite[0].getMinU();
            float f34 = atextureatlassprite[0].getMaxU();
            float f36 = atextureatlassprite[0].getMinV();
            float f38 = atextureatlassprite[0].getMaxV();
            int j1 = this.getCombinedLightUpMax(worldIn, pos.down());
            int k1 = j1 >> 16 & '\uffff';
            int l1 = j1 & '\uffff';
            float f39 = 0.5F * f;
            float f40 = 0.5F * f1;
            float f41 = 0.5F * f2;
            buffer.pos(d0, d1, d2 + 1.0D).color(f39, f40, f41, 1.0F).tex((double)f33, (double)f38).lightmap(k1, l1).endVertex();
            buffer.pos(d0, d1, d2).color(f39, f40, f41, 1.0F).tex((double)f33, (double)f36).lightmap(k1, l1).endVertex();
            buffer.pos(d0 + 1.0D, d1, d2).color(f39, f40, f41, 1.0F).tex((double)f34, (double)f36).lightmap(k1, l1).endVertex();
            buffer.pos(d0 + 1.0D, d1, d2 + 1.0D).color(f39, f40, f41, 1.0F).tex((double)f34, (double)f38).lightmap(k1, l1).endVertex();
            flag7 = true;
         }

         for(int i1 = 0; i1 < 4; ++i1) {
            float f35;
            float f37;
            double d3;
            double d4;
            double d5;
            double d6;
            EnumFacing enumfacing;
            boolean flag8;
            if (i1 == 0) {
               f35 = f7;
               f37 = f10;
               d3 = d0;
               d5 = d0 + 1.0D;
               d4 = d2 + (double)0.001F;
               d6 = d2 + (double)0.001F;
               enumfacing = EnumFacing.NORTH;
               flag8 = flag3;
            } else if (i1 == 1) {
               f35 = f9;
               f37 = f8;
               d3 = d0 + 1.0D;
               d5 = d0;
               d4 = d2 + 1.0D - (double)0.001F;
               d6 = d2 + 1.0D - (double)0.001F;
               enumfacing = EnumFacing.SOUTH;
               flag8 = flag4;
            } else if (i1 == 2) {
               f35 = f8;
               f37 = f7;
               d3 = d0 + (double)0.001F;
               d5 = d0 + (double)0.001F;
               d4 = d2 + 1.0D;
               d6 = d2;
               enumfacing = EnumFacing.WEST;
               flag8 = flag5;
            } else {
               f35 = f10;
               f37 = f9;
               d3 = d0 + 1.0D - (double)0.001F;
               d5 = d0 + 1.0D - (double)0.001F;
               d4 = d2;
               d6 = d2 + 1.0D;
               enumfacing = EnumFacing.EAST;
               flag8 = flag6;
            }

            if (flag8 && !func_209556_a(worldIn, pos, enumfacing, Math.max(f35, f37))) {
               flag7 = true;
               BlockPos blockpos = pos.offset(enumfacing);
               TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
               if (!flag) {
                  IBlockState blockstate = worldIn.getBlockState(blockpos);
                  if (blockstate.getBlockFaceShape(worldIn, blockpos, enumfacing) == net.minecraft.block.state.BlockFaceShape.SOLID) {
                     textureatlassprite2 = this.atlasSpriteWaterOverlay;
                  }
               }

               float f44 = textureatlassprite2.getInterpolatedU(0.0D);
               float f25 = textureatlassprite2.getInterpolatedU(8.0D);
               float f26 = textureatlassprite2.getInterpolatedV((double)((1.0F - f35) * 16.0F * 0.5F));
               float f27 = textureatlassprite2.getInterpolatedV((double)((1.0F - f37) * 16.0F * 0.5F));
               float f28 = textureatlassprite2.getInterpolatedV(8.0D);
               int j = this.getCombinedLightUpMax(worldIn, blockpos);
               int k = j >> 16 & '\uffff';
               int l = j & '\uffff';
               float f29 = i1 < 2 ? 0.8F : 0.6F;
               float f30 = 1.0F * f29 * f;
               float f31 = 1.0F * f29 * f1;
               float f32 = 1.0F * f29 * f2;
               buffer.pos(d3, d1 + (double)f35, d4).color(f30, f31, f32, 1.0F).tex((double)f44, (double)f26).lightmap(k, l).endVertex();
               buffer.pos(d5, d1 + (double)f37, d6).color(f30, f31, f32, 1.0F).tex((double)f25, (double)f27).lightmap(k, l).endVertex();
               buffer.pos(d5, d1 + 0.0D, d6).color(f30, f31, f32, 1.0F).tex((double)f25, (double)f28).lightmap(k, l).endVertex();
               buffer.pos(d3, d1 + 0.0D, d4).color(f30, f31, f32, 1.0F).tex((double)f44, (double)f28).lightmap(k, l).endVertex();
               if (textureatlassprite2 != this.atlasSpriteWaterOverlay) {
                  buffer.pos(d3, d1 + 0.0D, d4).color(f30, f31, f32, 1.0F).tex((double)f44, (double)f28).lightmap(k, l).endVertex();
                  buffer.pos(d5, d1 + 0.0D, d6).color(f30, f31, f32, 1.0F).tex((double)f25, (double)f28).lightmap(k, l).endVertex();
                  buffer.pos(d5, d1 + (double)f37, d6).color(f30, f31, f32, 1.0F).tex((double)f25, (double)f27).lightmap(k, l).endVertex();
                  buffer.pos(d3, d1 + (double)f35, d4).color(f30, f31, f32, 1.0F).tex((double)f44, (double)f26).lightmap(k, l).endVertex();
               }
            }
         }

         return flag7;
      }
   }

   public int getCombinedLightUpMax(IWorldReader reader, BlockPos pos) {
      int i = reader.getCombinedLight(pos, 0);
      int j = reader.getCombinedLight(pos.up(), 0);
      int k = i & 255;
      int l = j & 255;
      int i1 = i >> 16 & 255;
      int j1 = j >> 16 & 255;
      return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
   }

   public float getFluidHeight(IWorldReaderBase reader, BlockPos pos, Fluid fluidIn) {
      int i = 0;
      float f = 0.0F;

      for(int j = 0; j < 4; ++j) {
         BlockPos blockpos = pos.add(-(j & 1), 0, -(j >> 1 & 1));
         if (reader.getFluidState(blockpos.up()).getFluid().isEquivalentTo(fluidIn)) {
            return 1.0F;
         }

         IFluidState ifluidstate = reader.getFluidState(blockpos);
         if (ifluidstate.getFluid().isEquivalentTo(fluidIn)) {
            if (ifluidstate.getHeight() >= 0.8F) {
               f += ifluidstate.getHeight() * 10.0F;
               i += 10;
            } else {
               f += ifluidstate.getHeight();
               ++i;
            }
         } else if (!reader.getBlockState(blockpos).getMaterial().isSolid()) {
            ++i;
         }
      }

      return f / (float)i;
   }
}
