package net.minecraft.util.math.shapes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.math.DoubleMath;
import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.AxisRotation;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class VoxelShapes {
   private static final VoxelShape BLOCK = Util.make(() -> {
      VoxelShapePart voxelshapepart = new BitSetVoxelShapePart(1, 1, 1);
      voxelshapepart.setFull(0, 0, 0, true, true);
      return new VoxelShapeCube(voxelshapepart);
   });
   public static final VoxelShape INFINITY = box(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
   private static final VoxelShape EMPTY = new VoxelShapeArray(new BitSetVoxelShapePart(0, 0, 0), (DoubleList)(new DoubleArrayList(new double[]{0.0D})), (DoubleList)(new DoubleArrayList(new double[]{0.0D})), (DoubleList)(new DoubleArrayList(new double[]{0.0D})));

   public static VoxelShape empty() {
      return EMPTY;
   }

   public static VoxelShape block() {
      return BLOCK;
   }

   public static VoxelShape box(double p_197873_0_, double p_197873_2_, double p_197873_4_, double p_197873_6_, double p_197873_8_, double p_197873_10_) {
      return create(new AxisAlignedBB(p_197873_0_, p_197873_2_, p_197873_4_, p_197873_6_, p_197873_8_, p_197873_10_));
   }

   public static VoxelShape create(AxisAlignedBB p_197881_0_) {
      int i = findBits(p_197881_0_.minX, p_197881_0_.maxX);
      int j = findBits(p_197881_0_.minY, p_197881_0_.maxY);
      int k = findBits(p_197881_0_.minZ, p_197881_0_.maxZ);
      if (i >= 0 && j >= 0 && k >= 0) {
         if (i == 0 && j == 0 && k == 0) {
            return p_197881_0_.contains(0.5D, 0.5D, 0.5D) ? block() : empty();
         } else {
            int l = 1 << i;
            int i1 = 1 << j;
            int j1 = 1 << k;
            int k1 = (int)Math.round(p_197881_0_.minX * (double)l);
            int l1 = (int)Math.round(p_197881_0_.maxX * (double)l);
            int i2 = (int)Math.round(p_197881_0_.minY * (double)i1);
            int j2 = (int)Math.round(p_197881_0_.maxY * (double)i1);
            int k2 = (int)Math.round(p_197881_0_.minZ * (double)j1);
            int l2 = (int)Math.round(p_197881_0_.maxZ * (double)j1);
            BitSetVoxelShapePart bitsetvoxelshapepart = new BitSetVoxelShapePart(l, i1, j1, k1, i2, k2, l1, j2, l2);

            for(long i3 = (long)k1; i3 < (long)l1; ++i3) {
               for(long j3 = (long)i2; j3 < (long)j2; ++j3) {
                  for(long k3 = (long)k2; k3 < (long)l2; ++k3) {
                     bitsetvoxelshapepart.setFull((int)i3, (int)j3, (int)k3, false, true);
                  }
               }
            }

            return new VoxelShapeCube(bitsetvoxelshapepart);
         }
      } else {
         return new VoxelShapeArray(BLOCK.shape, new double[]{p_197881_0_.minX, p_197881_0_.maxX}, new double[]{p_197881_0_.minY, p_197881_0_.maxY}, new double[]{p_197881_0_.minZ, p_197881_0_.maxZ});
      }
   }

   private static int findBits(double p_197885_0_, double p_197885_2_) {
      if (!(p_197885_0_ < -1.0E-7D) && !(p_197885_2_ > 1.0000001D)) {
         for(int i = 0; i <= 3; ++i) {
            double d0 = p_197885_0_ * (double)(1 << i);
            double d1 = p_197885_2_ * (double)(1 << i);
            boolean flag = Math.abs(d0 - Math.floor(d0)) < 1.0E-7D;
            boolean flag1 = Math.abs(d1 - Math.floor(d1)) < 1.0E-7D;
            if (flag && flag1) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   protected static long lcm(int p_197877_0_, int p_197877_1_) {
      return (long)p_197877_0_ * (long)(p_197877_1_ / IntMath.gcd(p_197877_0_, p_197877_1_));
   }

   public static VoxelShape or(VoxelShape p_197872_0_, VoxelShape p_197872_1_) {
      return join(p_197872_0_, p_197872_1_, IBooleanFunction.OR);
   }

   public static VoxelShape or(VoxelShape p_216384_0_, VoxelShape... p_216384_1_) {
      return Arrays.stream(p_216384_1_).reduce(p_216384_0_, VoxelShapes::or);
   }

   public static VoxelShape join(VoxelShape p_197878_0_, VoxelShape p_197878_1_, IBooleanFunction p_197878_2_) {
      return joinUnoptimized(p_197878_0_, p_197878_1_, p_197878_2_).optimize();
   }

   public static VoxelShape joinUnoptimized(VoxelShape p_197882_0_, VoxelShape p_197882_1_, IBooleanFunction p_197882_2_) {
      if (p_197882_2_.apply(false, false)) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException());
      } else if (p_197882_0_ == p_197882_1_) {
         return p_197882_2_.apply(true, true) ? p_197882_0_ : empty();
      } else {
         boolean flag = p_197882_2_.apply(true, false);
         boolean flag1 = p_197882_2_.apply(false, true);
         if (p_197882_0_.isEmpty()) {
            return flag1 ? p_197882_1_ : empty();
         } else if (p_197882_1_.isEmpty()) {
            return flag ? p_197882_0_ : empty();
         } else {
            IDoubleListMerger idoublelistmerger = createIndexMerger(1, p_197882_0_.getCoords(Direction.Axis.X), p_197882_1_.getCoords(Direction.Axis.X), flag, flag1);
            IDoubleListMerger idoublelistmerger1 = createIndexMerger(idoublelistmerger.getList().size() - 1, p_197882_0_.getCoords(Direction.Axis.Y), p_197882_1_.getCoords(Direction.Axis.Y), flag, flag1);
            IDoubleListMerger idoublelistmerger2 = createIndexMerger((idoublelistmerger.getList().size() - 1) * (idoublelistmerger1.getList().size() - 1), p_197882_0_.getCoords(Direction.Axis.Z), p_197882_1_.getCoords(Direction.Axis.Z), flag, flag1);
            BitSetVoxelShapePart bitsetvoxelshapepart = BitSetVoxelShapePart.join(p_197882_0_.shape, p_197882_1_.shape, idoublelistmerger, idoublelistmerger1, idoublelistmerger2, p_197882_2_);
            return (VoxelShape)(idoublelistmerger instanceof DoubleCubeMergingList && idoublelistmerger1 instanceof DoubleCubeMergingList && idoublelistmerger2 instanceof DoubleCubeMergingList ? new VoxelShapeCube(bitsetvoxelshapepart) : new VoxelShapeArray(bitsetvoxelshapepart, idoublelistmerger.getList(), idoublelistmerger1.getList(), idoublelistmerger2.getList()));
         }
      }
   }

   public static boolean joinIsNotEmpty(VoxelShape p_197879_0_, VoxelShape p_197879_1_, IBooleanFunction p_197879_2_) {
      if (p_197879_2_.apply(false, false)) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException());
      } else if (p_197879_0_ == p_197879_1_) {
         return p_197879_2_.apply(true, true);
      } else if (p_197879_0_.isEmpty()) {
         return p_197879_2_.apply(false, !p_197879_1_.isEmpty());
      } else if (p_197879_1_.isEmpty()) {
         return p_197879_2_.apply(!p_197879_0_.isEmpty(), false);
      } else {
         boolean flag = p_197879_2_.apply(true, false);
         boolean flag1 = p_197879_2_.apply(false, true);

         for(Direction.Axis direction$axis : AxisRotation.AXIS_VALUES) {
            if (p_197879_0_.max(direction$axis) < p_197879_1_.min(direction$axis) - 1.0E-7D) {
               return flag || flag1;
            }

            if (p_197879_1_.max(direction$axis) < p_197879_0_.min(direction$axis) - 1.0E-7D) {
               return flag || flag1;
            }
         }

         IDoubleListMerger idoublelistmerger = createIndexMerger(1, p_197879_0_.getCoords(Direction.Axis.X), p_197879_1_.getCoords(Direction.Axis.X), flag, flag1);
         IDoubleListMerger idoublelistmerger1 = createIndexMerger(idoublelistmerger.getList().size() - 1, p_197879_0_.getCoords(Direction.Axis.Y), p_197879_1_.getCoords(Direction.Axis.Y), flag, flag1);
         IDoubleListMerger idoublelistmerger2 = createIndexMerger((idoublelistmerger.getList().size() - 1) * (idoublelistmerger1.getList().size() - 1), p_197879_0_.getCoords(Direction.Axis.Z), p_197879_1_.getCoords(Direction.Axis.Z), flag, flag1);
         return joinIsNotEmpty(idoublelistmerger, idoublelistmerger1, idoublelistmerger2, p_197879_0_.shape, p_197879_1_.shape, p_197879_2_);
      }
   }

   private static boolean joinIsNotEmpty(IDoubleListMerger p_197874_0_, IDoubleListMerger p_197874_1_, IDoubleListMerger p_197874_2_, VoxelShapePart p_197874_3_, VoxelShapePart p_197874_4_, IBooleanFunction p_197874_5_) {
      return !p_197874_0_.forMergedIndexes((p_199861_5_, p_199861_6_, p_199861_7_) -> {
         return p_197874_1_.forMergedIndexes((p_199860_6_, p_199860_7_, p_199860_8_) -> {
            return p_197874_2_.forMergedIndexes((p_199862_7_, p_199862_8_, p_199862_9_) -> {
               return !p_197874_5_.apply(p_197874_3_.isFullWide(p_199861_5_, p_199860_6_, p_199862_7_), p_197874_4_.isFullWide(p_199861_6_, p_199860_7_, p_199862_8_));
            });
         });
      });
   }

   public static double collide(Direction.Axis axis, AxisAlignedBB aabb, Stream<VoxelShape> allShapes, double motion) {
      Iterator<VoxelShape> iterator = allShapes.iterator();
      while (iterator.hasNext()) {
         if (Math.abs(motion) < 0.0000001)
            return 0.0D;
         motion = iterator.next().collide(axis, aabb, motion);
      }
      return motion;
   }

   public static double collide(Direction.Axis axis, AxisAlignedBB aabb, IWorldReader world, double motion, ISelectionContext ctx, Stream<VoxelShape> nonBlockShapes) {
      return collide(aabb, world, motion, ctx, AxisRotation.between(axis, Direction.Axis.Z), nonBlockShapes);
   }

   private static double collide(AxisAlignedBB aabb, IWorldReader world, double motion, ISelectionContext ctx, AxisRotation _rotation_, Stream<VoxelShape> nonBlockShapes) {
      if (aabb.getXsize() < 0.000001 || aabb.getYsize() < 0.000001 || aabb.getZsize() < 0.000001)
         return motion;
      if (Math.abs(motion) < 0.0000001)
         return 0.0D;

      AxisRotation _inverse_ = _rotation_.inverse();
      Direction.Axis cycledX = _inverse_.cycle(Direction.Axis.X);
      Direction.Axis cycledY = _inverse_.cycle(Direction.Axis.Y);
      Direction.Axis cycledZ = _inverse_.cycle(Direction.Axis.Z);
      BlockPos.Mutable pos = new BlockPos.Mutable();

      int minX = MathHelper.floor(aabb.min(cycledX) - 0.0000001) - 1;
      int maxX = MathHelper.floor(aabb.max(cycledX) + 0.0000001) + 1;
      int minY = MathHelper.floor(aabb.min(cycledY) - 0.0000001) - 1;
      int maxY = MathHelper.floor(aabb.max(cycledY) + 0.0000001) + 1;
      double d0 = aabb.min(cycledZ) - 0.0000001;
      double d1 = aabb.max(cycledZ) + 0.0000001;
      boolean motionInitiallyPositive = motion > 0.0D;
      int minZ = motionInitiallyPositive ? MathHelper.floor(aabb.max(cycledZ) - 0.0000001) - 1 : MathHelper.floor(aabb.min(cycledZ) + 0.0000001) + 1;
      int maxZ = lastC(motion, d0, d1);
      int zIncrement = motionInitiallyPositive ? 1 : -1;
      int z = minZ;

	   motion = io.github.cadiboo.nocubes.hooks.Hooks.collide(aabb, world, motion, ctx, _rotation_, _inverse_, pos, minX, maxX, minY, maxY, minZ, maxZ);
	   if (Math.abs(motion) < 1.0E-7D)
		   return 0.0D;

	   while (true) {
         if (motionInitiallyPositive) {
            if (z > maxZ)
               break;
         } else if (z < maxZ)
            break;

         for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
               int boundariesTouched = 0;
               if (x == minX || x == maxX)
                  ++boundariesTouched;
               if (y == minY || y == maxY)
                  ++boundariesTouched;
               if (z == minZ || z == maxZ)
                  ++boundariesTouched;
               if (boundariesTouched >= 3)
                  continue;

               pos.set(_inverse_, x, y, z);
               BlockState blockstate = world.getBlockState(pos);
               if (!io.github.cadiboo.nocubes.hooks.Hooks.canBlockStateCollide(blockstate))
                  continue;
               if (boundariesTouched == 1 && !blockstate.hasLargeCollisionShape())
                  continue;
               if (boundariesTouched == 2 && !blockstate.is(Blocks.MOVING_PISTON))
                  continue;

               motion = blockstate.getCollisionShape(world, pos, ctx).collide(cycledZ, aabb.move(-pos.getX(), -pos.getY(), -pos.getZ()), motion);
               if (Math.abs(motion) < 0.0000001)
                  return 0.0D;
               maxZ = lastC(motion, d0, d1);
            }
         }
         z += zIncrement;
      }

      double[] motionRef = {motion};
      nonBlockShapes.forEach((shape) -> motionRef[0] = shape.collide(cycledZ, aabb, motionRef[0]));
      return motionRef[0];
   }

   private static int lastC(double p_216385_0_, double p_216385_2_, double p_216385_4_) {
      return p_216385_0_ > 0.0D ? MathHelper.floor(p_216385_4_ + p_216385_0_) + 1 : MathHelper.floor(p_216385_2_ + p_216385_0_) - 1;
   }

   @OnlyIn(Dist.CLIENT)
   public static boolean blockOccudes(VoxelShape p_197875_0_, VoxelShape p_197875_1_, Direction p_197875_2_) {
      if (p_197875_0_ == block() && p_197875_1_ == block()) {
         return true;
      } else if (p_197875_1_.isEmpty()) {
         return false;
      } else {
         Direction.Axis direction$axis = p_197875_2_.getAxis();
         Direction.AxisDirection direction$axisdirection = p_197875_2_.getAxisDirection();
         VoxelShape voxelshape = direction$axisdirection == Direction.AxisDirection.POSITIVE ? p_197875_0_ : p_197875_1_;
         VoxelShape voxelshape1 = direction$axisdirection == Direction.AxisDirection.POSITIVE ? p_197875_1_ : p_197875_0_;
         IBooleanFunction ibooleanfunction = direction$axisdirection == Direction.AxisDirection.POSITIVE ? IBooleanFunction.ONLY_FIRST : IBooleanFunction.ONLY_SECOND;
         return DoubleMath.fuzzyEquals(voxelshape.max(direction$axis), 1.0D, 1.0E-7D) && DoubleMath.fuzzyEquals(voxelshape1.min(direction$axis), 0.0D, 1.0E-7D) && !joinIsNotEmpty(new SplitVoxelShape(voxelshape, direction$axis, voxelshape.shape.getSize(direction$axis) - 1), new SplitVoxelShape(voxelshape1, direction$axis, 0), ibooleanfunction);
      }
   }

   public static VoxelShape getFaceShape(VoxelShape p_216387_0_, Direction p_216387_1_) {
      if (p_216387_0_ == block()) {
         return block();
      } else {
         Direction.Axis direction$axis = p_216387_1_.getAxis();
         boolean flag;
         int i;
         if (p_216387_1_.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            flag = DoubleMath.fuzzyEquals(p_216387_0_.max(direction$axis), 1.0D, 1.0E-7D);
            i = p_216387_0_.shape.getSize(direction$axis) - 1;
         } else {
            flag = DoubleMath.fuzzyEquals(p_216387_0_.min(direction$axis), 0.0D, 1.0E-7D);
            i = 0;
         }

         return (VoxelShape)(!flag ? empty() : new SplitVoxelShape(p_216387_0_, direction$axis, i));
      }
   }

   public static boolean mergedFaceOccludes(VoxelShape p_204642_0_, VoxelShape p_204642_1_, Direction p_204642_2_) {
      if (p_204642_0_ != block() && p_204642_1_ != block()) {
         Direction.Axis direction$axis = p_204642_2_.getAxis();
         Direction.AxisDirection direction$axisdirection = p_204642_2_.getAxisDirection();
         VoxelShape voxelshape = direction$axisdirection == Direction.AxisDirection.POSITIVE ? p_204642_0_ : p_204642_1_;
         VoxelShape voxelshape1 = direction$axisdirection == Direction.AxisDirection.POSITIVE ? p_204642_1_ : p_204642_0_;
         if (!DoubleMath.fuzzyEquals(voxelshape.max(direction$axis), 1.0D, 1.0E-7D)) {
            voxelshape = empty();
         }

         if (!DoubleMath.fuzzyEquals(voxelshape1.min(direction$axis), 0.0D, 1.0E-7D)) {
            voxelshape1 = empty();
         }

         return !joinIsNotEmpty(block(), joinUnoptimized(new SplitVoxelShape(voxelshape, direction$axis, voxelshape.shape.getSize(direction$axis) - 1), new SplitVoxelShape(voxelshape1, direction$axis, 0), IBooleanFunction.OR), IBooleanFunction.ONLY_FIRST);
      } else {
         return true;
      }
   }

   public static boolean faceShapeOccludes(VoxelShape p_223416_0_, VoxelShape p_223416_1_) {
      if (p_223416_0_ != block() && p_223416_1_ != block()) {
         if (p_223416_0_.isEmpty() && p_223416_1_.isEmpty()) {
            return false;
         } else {
            return !joinIsNotEmpty(block(), joinUnoptimized(p_223416_0_, p_223416_1_, IBooleanFunction.OR), IBooleanFunction.ONLY_FIRST);
         }
      } else {
         return true;
      }
   }

   @VisibleForTesting
   protected static IDoubleListMerger createIndexMerger(int p_199410_0_, DoubleList p_199410_1_, DoubleList p_199410_2_, boolean p_199410_3_, boolean p_199410_4_) {
      int i = p_199410_1_.size() - 1;
      int j = p_199410_2_.size() - 1;
      if (p_199410_1_ instanceof DoubleRangeList && p_199410_2_ instanceof DoubleRangeList) {
         long k = lcm(i, j);
         if ((long)p_199410_0_ * k <= 256L) {
            return new DoubleCubeMergingList(i, j);
         }
      }

      if (p_199410_1_.getDouble(i) < p_199410_2_.getDouble(0) - 1.0E-7D) {
         return new NonOverlappingMerger(p_199410_1_, p_199410_2_, false);
      } else if (p_199410_2_.getDouble(j) < p_199410_1_.getDouble(0) - 1.0E-7D) {
         return new NonOverlappingMerger(p_199410_2_, p_199410_1_, true);
      } else if (i == j && Objects.equals(p_199410_1_, p_199410_2_)) {
         if (p_199410_1_ instanceof SimpleDoubleMerger) {
            return (IDoubleListMerger)p_199410_1_;
         } else {
            return (IDoubleListMerger)(p_199410_2_ instanceof SimpleDoubleMerger ? (IDoubleListMerger)p_199410_2_ : new SimpleDoubleMerger(p_199410_1_));
         }
      } else {
         return new IndirectMerger(p_199410_1_, p_199410_2_, p_199410_3_, p_199410_4_);
      }
   }

   public interface ILineConsumer {
      void consume(double p_consume_1_, double p_consume_3_, double p_consume_5_, double p_consume_7_, double p_consume_9_, double p_consume_11_);
   }
}
