package net.minecraft.world;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeSpliterator;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.border.WorldBorder;

public interface ICollisionReader extends IBlockReader {
   WorldBorder getWorldBorder();

   @Nullable
   IBlockReader getBlockReader(int chunkX, int chunkZ);

   default boolean checkNoEntityCollision(@Nullable Entity entityIn, VoxelShape shape) {
      return true;
   }

   default boolean func_226663_a_(BlockState p_226663_1_, BlockPos p_226663_2_, ISelectionContext p_226663_3_) {
      VoxelShape voxelshape = p_226663_1_.getCollisionShape(this, p_226663_2_, p_226663_3_);
      return voxelshape.isEmpty() || this.checkNoEntityCollision((Entity)null, voxelshape.withOffset((double)p_226663_2_.getX(), (double)p_226663_2_.getY(), (double)p_226663_2_.getZ()));
   }

   default boolean checkNoEntityCollision(Entity p_226668_1_) {
      return this.checkNoEntityCollision(p_226668_1_, VoxelShapes.create(p_226668_1_.getBoundingBox()));
   }

   default boolean hasNoCollisions(AxisAlignedBB p_226664_1_) {
      return this.func_234865_b_((Entity)null, p_226664_1_, (p_234866_0_) -> {
         return true;
      });
   }

   default boolean hasNoCollisions(Entity p_226669_1_) {
      return this.func_234865_b_(p_226669_1_, p_226669_1_.getBoundingBox(), (p_234864_0_) -> {
         return true;
      });
   }

   default boolean hasNoCollisions(Entity p_226665_1_, AxisAlignedBB p_226665_2_) {
      return this.func_234865_b_(p_226665_1_, p_226665_2_, (p_234863_0_) -> {
         return true;
      });
   }

   default boolean func_234865_b_(@Nullable Entity p_234865_1_, AxisAlignedBB p_234865_2_, Predicate<Entity> p_234865_3_) {
      return this.func_234867_d_(p_234865_1_, p_234865_2_, p_234865_3_).allMatch(VoxelShape::isEmpty);
   }

   Stream<VoxelShape> func_230318_c_(@Nullable Entity p_230318_1_, AxisAlignedBB p_230318_2_, Predicate<Entity> p_230318_3_);

   default Stream<VoxelShape> func_234867_d_(@Nullable Entity p_234867_1_, AxisAlignedBB p_234867_2_, Predicate<Entity> p_234867_3_) {
      return Stream.concat(this.getCollisionShapes(p_234867_1_, p_234867_2_), this.func_230318_c_(p_234867_1_, p_234867_2_, p_234867_3_));
   }

   // Hack this
   default Stream<VoxelShape> getCollisionShapes(@Nullable Entity p_226666_1_, AxisAlignedBB p_226666_2_) {
      return StreamSupport.stream(new VoxelShapeSpliterator(this, p_226666_1_, p_226666_2_), false);
   }

   default Stream<VoxelShape> func_241457_a_(@Nullable Entity p_241457_1_, AxisAlignedBB p_241457_2_, BiPredicate<BlockState, BlockPos> p_241457_3_) {
      return StreamSupport.stream(new VoxelShapeSpliterator(this, p_241457_1_, p_241457_2_, p_241457_3_), false);
   }
}
