package clickme.nocubes.blocks;

import clickme.nocubes.NoCubes;
import clickme.nocubes.SmoothBlockRenderer2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class GrassHook extends BlockGrass {
   public GrassHook() {
      this.func_149711_c(0.6F);
      this.func_149672_a(field_149779_h);
      this.func_149663_c("grass");
      this.func_149658_d("grass_top");
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

   @SideOnly(Side.CLIENT)
   public IIcon func_149673_e(IBlockAccess access, int x, int y, int z, int side) {
      return this.field_149761_L;
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int side, int meta) {
      return this.field_149761_L;
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister register) {
      this.field_149761_L = register.func_94245_a(this.func_149641_N());
   }
}
