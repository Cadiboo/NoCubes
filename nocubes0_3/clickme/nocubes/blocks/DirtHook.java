package clickme.nocubes.blocks;

import clickme.nocubes.NoCubes;
import clickme.nocubes.SmoothBlockRenderer2;
import java.util.List;
import net.minecraft.block.BlockDirt;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class DirtHook extends BlockDirt {
   public DirtHook() {
      this.func_149711_c(0.5F);
      this.func_149672_a(field_149767_g);
      this.func_149663_c("dirt");
      this.func_149658_d("dirt");
   }

   public int func_149645_b() {
      return NoCubes.renderId;
   }

   public void func_149683_g() {
      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
   }

   public void func_149743_a(World world, int x, int y, int z, AxisAlignedBB bb, List list, Entity entity) {
      float f = SmoothBlockRenderer2.getSmoothBlockHeightForCollision(world, this, x, y, z);
      float f1 = SmoothBlockRenderer2.getSmoothBlockHeightForCollision(world, this, x, y, z + 1);
      float f2 = SmoothBlockRenderer2.getSmoothBlockHeightForCollision(world, this, x + 1, y, z + 1);
      float f3 = SmoothBlockRenderer2.getSmoothBlockHeightForCollision(world, this, x + 1, y, z);
      this.func_149676_a(0.0F, 0.0F, 0.0F, 0.5F, f, 0.5F);
      super.func_149743_a(world, x, y, z, bb, list, entity);
      this.func_149676_a(0.0F, 0.0F, 0.5F, 0.5F, f1, 1.0F);
      super.func_149743_a(world, x, y, z, bb, list, entity);
      this.func_149676_a(0.5F, 0.0F, 0.5F, 1.0F, f2, 1.0F);
      super.func_149743_a(world, x, y, z, bb, list, entity);
      this.func_149676_a(0.5F, 0.0F, 0.0F, 1.0F, f3, 0.5F);
      super.func_149743_a(world, x, y, z, bb, list, entity);
      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
   }
}
