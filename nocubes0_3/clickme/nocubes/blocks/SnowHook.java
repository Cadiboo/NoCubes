package clickme.nocubes.blocks;

import clickme.nocubes.NoCubes;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class SnowHook extends Block {
   public SnowHook() {
      super(Material.field_151597_y);
      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);
      this.func_149675_a(true);
      this.func_149647_a(CreativeTabs.field_78031_c);
      this.func_149711_c(0.1F);
      this.func_149672_a(field_149773_n);
      this.func_149663_c("snow");
      this.func_149713_g(0);
      this.func_149658_d("snow");
   }

   public int func_149645_b() {
      return NoCubes.renderId;
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      return null;
   }

   public Item func_149650_a(int i, Random random, int j) {
      return Items.field_151126_ay;
   }

   public int func_149745_a(Random random) {
      return 1;
   }

   public void func_149674_a(World world, int x, int y, int z, Random random) {
      if (world.func_72972_b(EnumSkyBlock.Block, x, y, z) > 11) {
         world.func_147468_f(x, y, z);
      }

   }
}
