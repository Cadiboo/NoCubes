package net.minecraft.client.renderer.color;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.ShearableDoublePlantBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.FoliageColors;
import net.minecraft.world.GrassColors;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings("all") // AT is weird, its ATed at runtime
@OnlyIn(Dist.CLIENT)
public class BlockColors {
   // FORGE: Use RegistryDelegates as non-Vanilla block ids are not constant
   public final java.util.Map<net.minecraftforge.registries.IRegistryDelegate<Block>, IBlockColor> colors = new java.util.HashMap<>();
   public final Map<Block, Set<IProperty<?>>> field_225311_b = Maps.newHashMap();

   public static BlockColors init() {
      BlockColors blockcolors = new BlockColors();
      blockcolors.register((state, reader, pos, p_210234_3_) -> {
         return reader != null && pos != null ? BiomeColors.getGrassColor(reader, state.get(ShearableDoublePlantBlock.field_208063_b) == DoubleBlockHalf.UPPER ? pos.down() : pos) : -1;
      }, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
      blockcolors.func_225308_a(ShearableDoublePlantBlock.field_208063_b, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
      blockcolors.register((p_210225_0_, p_210225_1_, p_210225_2_, p_210225_3_) -> {
         return p_210225_1_ != null && p_210225_2_ != null ? BiomeColors.getGrassColor(p_210225_1_, p_210225_2_) : GrassColors.get(0.5D, 1.0D);
      }, Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.GRASS, Blocks.POTTED_FERN);
      blockcolors.register((p_210227_0_, p_210227_1_, p_210227_2_, p_210227_3_) -> {
         return FoliageColors.getSpruce();
      }, Blocks.SPRUCE_LEAVES);
      blockcolors.register((p_210232_0_, p_210232_1_, p_210232_2_, p_210232_3_) -> {
         return FoliageColors.getBirch();
      }, Blocks.BIRCH_LEAVES);
      blockcolors.register((p_210229_0_, p_210229_1_, p_210229_2_, p_210229_3_) -> {
         return p_210229_1_ != null && p_210229_2_ != null ? BiomeColors.getFoliageColor(p_210229_1_, p_210229_2_) : FoliageColors.getDefault();
      }, Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.VINE);
      blockcolors.register((p_210226_0_, p_210226_1_, p_210226_2_, p_210226_3_) -> {
         return p_210226_1_ != null && p_210226_2_ != null ? BiomeColors.getWaterColor(p_210226_1_, p_210226_2_) : -1;
      }, Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.CAULDRON);
      blockcolors.register((p_210231_0_, p_210231_1_, p_210231_2_, p_210231_3_) -> {
         return RedstoneWireBlock.colorMultiplier(p_210231_0_.get(RedstoneWireBlock.POWER));
      }, Blocks.REDSTONE_WIRE);
      blockcolors.func_225308_a(RedstoneWireBlock.POWER, Blocks.REDSTONE_WIRE);
      blockcolors.register((p_210230_0_, p_210230_1_, p_210230_2_, p_210230_3_) -> {
         return p_210230_1_ != null && p_210230_2_ != null ? BiomeColors.getGrassColor(p_210230_1_, p_210230_2_) : -1;
      }, Blocks.SUGAR_CANE);
      blockcolors.register((p_210224_0_, p_210224_1_, p_210224_2_, p_210224_3_) -> {
         return 14731036;
      }, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
      blockcolors.register((p_210233_0_, p_210233_1_, p_210233_2_, p_210233_3_) -> {
         int i = p_210233_0_.get(StemBlock.AGE);
         int j = i * 32;
         int k = 255 - i * 8;
         int l = i * 4;
         return j << 16 | k << 8 | l;
      }, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
      blockcolors.func_225308_a(StemBlock.AGE, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
      blockcolors.register((p_210228_0_, p_210228_1_, p_210228_2_, p_210228_3_) -> {
         return p_210228_1_ != null && p_210228_2_ != null ? 2129968 : 7455580;
      }, Blocks.LILY_PAD);
      net.minecraftforge.client.ForgeHooksClient.onBlockColorsInit(blockcolors);
      return blockcolors;
   }

   public int getColorOrMaterialColor(BlockState state, World p_189991_2_, BlockPos p_189991_3_) {
      IBlockColor iblockcolor = this.colors.get(state.getBlock().delegate);
      if (iblockcolor != null) {
         return iblockcolor.getColor(state, (IEnviromentBlockReader)null, (BlockPos)null, 0);
      } else {
         MaterialColor materialcolor = state.getMaterialColor(p_189991_2_, p_189991_3_);
         return materialcolor != null ? materialcolor.colorValue : -1;
      }
   }

   public int getColor(BlockState p_216860_1_, @Nullable IEnviromentBlockReader p_216860_2_, @Nullable BlockPos p_216860_3_, int p_216860_4_) {
      IBlockColor iblockcolor = this.colors.get(p_216860_1_.getBlock().delegate);
      return iblockcolor == null ? -1 : iblockcolor.getColor(p_216860_1_, p_216860_2_, p_216860_3_, p_216860_4_);
   }

   public void register(IBlockColor blockColor, Block... blocksIn) {
      for(Block block : blocksIn) {
         this.colors.put(block.delegate, blockColor);
      }

   }

   private void func_225309_a(Set<IProperty<?>> p_225309_1_, Block... p_225309_2_) {
      for(Block block : p_225309_2_) {
         this.field_225311_b.put(block, p_225309_1_);
      }

   }

   private void func_225308_a(IProperty<?> p_225308_1_, Block... p_225308_2_) {
      this.func_225309_a(ImmutableSet.of(p_225308_1_), p_225308_2_);
   }

   public Set<IProperty<?>> func_225310_a(Block p_225310_1_) {
      return this.field_225311_b.getOrDefault(p_225310_1_, ImmutableSet.of());
   }
}
