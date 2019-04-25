package net.minecraft.fluid;

import net.minecraft.block.state.IBlockState;
import net.minecraft.particles.IParticleData;
import net.minecraft.state.IStateHolder;
import net.minecraft.tags.Tag;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Random;

public interface IFluidState extends IStateHolder<IFluidState>, net.minecraftforge.common.extensions.IForgeFluidState {
   Fluid getFluid();

   default boolean isSource() {
      return this.getFluid().isSource(this);
   }

   default boolean isEmpty() {
      return this.getFluid().isEmpty();
   }

   default float getHeight() {
      return this.getFluid().getHeight(this);
   }

   default int getLevel() {
      return this.getFluid().getLevel(this);
   }

   @OnlyIn(Dist.CLIENT)
   default boolean shouldRenderSides(IBlockReader worldIn, BlockPos pos) {
      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            BlockPos blockpos = pos.add(i, 0, j);
            IFluidState ifluidstate = worldIn.getFluidState(blockpos);
            if (!ifluidstate.getFluid().isEquivalentTo(this.getFluid()) && !worldIn.getBlockState(blockpos).isOpaqueCube(worldIn, blockpos)) {
               return true;
            }
         }
      }

      return false;
   }

   default void tick(World worldIn, BlockPos pos) {
      this.getFluid().tick(worldIn, pos, this);
   }

   @OnlyIn(Dist.CLIENT)
   default void animateTick(World p_206881_1_, BlockPos p_206881_2_, Random p_206881_3_) {
      this.getFluid().animateTick(p_206881_1_, p_206881_2_, this, p_206881_3_);
   }

   default boolean ticksRandomly() {
      return this.getFluid().ticksRandomly();
   }

   default void randomTick(World worldIn, BlockPos pos, Random random) {
      this.getFluid().randomTick(worldIn, pos, this, random);
   }

   default Vec3d getFlow(IWorldReaderBase p_206887_1_, BlockPos p_206887_2_) {
      return this.getFluid().getFlow(p_206887_1_, p_206887_2_, this);
   }

   default IBlockState getBlockState() {
      return this.getFluid().getBlockState(this);
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   default IParticleData getDripParticleData() {
      return this.getFluid().getDripParticleData();
   }

   /**
    * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
    * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
    */
   @OnlyIn(Dist.CLIENT)
   default BlockRenderLayer getRenderLayer() {
      return this.getFluid().getRenderLayer();
   }

   default boolean isTagged(Tag<Fluid> tagIn) {
      return this.getFluid().isIn(tagIn);
   }

   default float getExplosionResistance() {
      return this.getFluid().getExplosionResistance();
   }

   default boolean canOtherFlowInto(Fluid fluidIn, EnumFacing direction) {
      return this.getFluid().canOtherFlowInto(this, fluidIn, direction);
   }
}
