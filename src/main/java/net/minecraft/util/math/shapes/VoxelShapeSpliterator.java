package net.minecraft.util.math.shapes;

import java.util.*;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.CubeCoordinateIterator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ICollisionReader;
import net.minecraft.world.border.WorldBorder;

public class VoxelShapeSpliterator extends AbstractSpliterator<VoxelShape> {
   @Nullable
   private final Entity source;
   private final AxisAlignedBB box;
   private final ISelectionContext context;
   private final CubeCoordinateIterator cursor;
   private final BlockPos.Mutable pos;
   private final VoxelShape entityShape;
   private final ICollisionReader collisionGetter;
   private boolean needsBorderCheck;
   private final BiPredicate<BlockState, BlockPos> predicate;
   private Deque<VoxelShape> nocubesShapes = null;

   public VoxelShapeSpliterator(ICollisionReader p_i231606_1_, @Nullable Entity p_i231606_2_, AxisAlignedBB p_i231606_3_) {
      this(p_i231606_1_, p_i231606_2_, p_i231606_3_, (p_241459_0_, p_241459_1_) -> {
         return true;
      });
   }

   public VoxelShapeSpliterator(ICollisionReader p_i241238_1_, @Nullable Entity p_i241238_2_, AxisAlignedBB p_i241238_3_, BiPredicate<BlockState, BlockPos> p_i241238_4_) {
      super(Long.MAX_VALUE, Spliterator.NONNULL | Spliterator.IMMUTABLE);
      this.context = p_i241238_2_ == null ? ISelectionContext.empty() : ISelectionContext.of(p_i241238_2_);
      this.pos = new BlockPos.Mutable();
      this.entityShape = VoxelShapes.create(p_i241238_3_);
      this.collisionGetter = p_i241238_1_;
      this.needsBorderCheck = p_i241238_2_ != null;
      this.source = p_i241238_2_;
      this.box = p_i241238_3_;
      this.predicate = p_i241238_4_;
      int i = MathHelper.floor(p_i241238_3_.minX - 1.0E-7D) - 1;
      int j = MathHelper.floor(p_i241238_3_.maxX + 1.0E-7D) + 1;
      int k = MathHelper.floor(p_i241238_3_.minY - 1.0E-7D) - 1;
      int l = MathHelper.floor(p_i241238_3_.maxY + 1.0E-7D) + 1;
      int i1 = MathHelper.floor(p_i241238_3_.minZ - 1.0E-7D) - 1;
      int j1 = MathHelper.floor(p_i241238_3_.maxZ + 1.0E-7D) + 1;
      this.cursor = new CubeCoordinateIterator(i, k, i1, j, l, j1);
   }

   public boolean tryAdvance(Consumer<? super VoxelShape> p_tryAdvance_1_) {
	   if (this.needsBorderCheck) if (this.worldBorderCheck(p_tryAdvance_1_))
	   	return true;
	   if (this.nocubesShapes == null)
		   this.nocubesShapes = io.github.cadiboo.nocubes.hooks.Hooks.createNoCubesIntersectingCollisionList(this.collisionGetter, this.box, this.pos);
	   while (!this.nocubesShapes.isEmpty()) {
		   VoxelShape shape = this.nocubesShapes.pop();
//		   if (!this.box.intersects(
//		   	  shape.min(Direction.Axis.X), shape.min(Direction.Axis.Y), shape.min(Direction.Axis.Z),
//		   	  shape.max(Direction.Axis.X), shape.max(Direction.Axis.Y), shape.max(Direction.Axis.Z)
//		   ))
//			   continue;
		   if (!VoxelShapes.joinIsNotEmpty(shape, this.entityShape, IBooleanFunction.AND))
			   continue;
		   p_tryAdvance_1_.accept(shape);
		   return true;
	   }
	   return this.collisionCheck(p_tryAdvance_1_);
   }

   boolean collisionCheck(Consumer<? super VoxelShape> p_234878_1_) {
      while(true) {
         if (this.cursor.advance()) {
            int i = this.cursor.nextX();
            int j = this.cursor.nextY();
            int k = this.cursor.nextZ();
            int numBoundariesTouched = this.cursor.getNextType();
            if (numBoundariesTouched == 3) {
               continue;
            }

            IBlockReader iblockreader = this.getChunk(i, k);
            if (iblockreader == null) {
               continue;
            }

            this.pos.set(i, j, k);
            BlockState blockstate = iblockreader.getBlockState(this.pos);
			 if (!this.predicate.test(blockstate, this.pos))
				 continue;
			 if (!io.github.cadiboo.nocubes.hooks.Hooks.canBlockStateCollide(blockstate))
				 continue;
			 if (numBoundariesTouched == 1 && !blockstate.hasLargeCollisionShape())
				 continue;
			 if (numBoundariesTouched == 2 && !blockstate.is(Blocks.MOVING_PISTON))
				 continue;

            VoxelShape voxelshape = blockstate.getCollisionShape(this.collisionGetter, this.pos, this.context);
//            if (voxelshape == VoxelShapes.block()) {
//               if (!this.box.intersects(i, j, k, (double)i + 1.0D, (double)j + 1.0D, (double)k + 1.0D)) {
//                  continue;
//               }
//
//               p_234878_1_.accept(voxelshape.move(i, j, k));
//               return true;
//            }

            VoxelShape voxelshape1 = voxelshape.move(i, j, k);
            if (!VoxelShapes.joinIsNotEmpty(voxelshape1, this.entityShape, IBooleanFunction.AND)) {
               continue;
            }

            p_234878_1_.accept(voxelshape1);
            return true;
         }

         return false;
      }
   }

   @Nullable
   private IBlockReader getChunk(int p_234876_1_, int p_234876_2_) {
      int i = p_234876_1_ >> 4;
      int j = p_234876_2_ >> 4;
      return this.collisionGetter.getChunkForCollisions(i, j);
   }

   boolean worldBorderCheck(Consumer<? super VoxelShape> p_234879_1_) {
      Objects.requireNonNull(this.source);
      this.needsBorderCheck = false;
      WorldBorder worldborder = this.collisionGetter.getWorldBorder();
      AxisAlignedBB axisalignedbb = this.source.getBoundingBox();
      if (!isBoxFullyWithinWorldBorder(worldborder, axisalignedbb)) {
         VoxelShape voxelshape = worldborder.getCollisionShape();
         if (!isOutsideBorder(voxelshape, axisalignedbb) && isCloseToBorder(voxelshape, axisalignedbb)) {
            p_234879_1_.accept(voxelshape);
            return true;
         }
      }

      return false;
   }

   private static boolean isCloseToBorder(VoxelShape p_241460_0_, AxisAlignedBB p_241460_1_) {
      return VoxelShapes.joinIsNotEmpty(p_241460_0_, VoxelShapes.create(p_241460_1_.inflate(1.0E-7D)), IBooleanFunction.AND);
   }

   private static boolean isOutsideBorder(VoxelShape p_241461_0_, AxisAlignedBB p_241461_1_) {
      return VoxelShapes.joinIsNotEmpty(p_241461_0_, VoxelShapes.create(p_241461_1_.deflate(1.0E-7D)), IBooleanFunction.AND);
   }

   public static boolean isBoxFullyWithinWorldBorder(WorldBorder p_234877_0_, AxisAlignedBB p_234877_1_) {
      double d0 = (double)MathHelper.floor(p_234877_0_.getMinX());
      double d1 = (double)MathHelper.floor(p_234877_0_.getMinZ());
      double d2 = (double)MathHelper.ceil(p_234877_0_.getMaxX());
      double d3 = (double)MathHelper.ceil(p_234877_0_.getMaxZ());
      return p_234877_1_.minX > d0 && p_234877_1_.minX < d2 && p_234877_1_.minZ > d1 && p_234877_1_.minZ < d3 && p_234877_1_.maxX > d0 && p_234877_1_.maxX < d2 && p_234877_1_.maxZ > d1 && p_234877_1_.maxZ < d3;
   }
}
