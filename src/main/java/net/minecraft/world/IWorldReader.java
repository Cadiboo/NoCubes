package net.minecraft.world;

import com.google.common.collect.Streams;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.CubeCoordinateIterator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface IWorldReader extends IEnviromentBlockReader {
   /**
    * Checks to see if an air block exists at the provided location. Note that this only checks to see if the blocks
    * material is set to air, meaning it is possible for non-vanilla blocks to still pass this check.
    */
   default boolean isAirBlock(BlockPos pos) {
      return this.getBlockState(pos).isAir(this, pos);
   }

   default boolean canBlockSeeSky(BlockPos pos) {
      if (pos.getY() >= this.getSeaLevel()) {
         return this.func_217337_f(pos);
      } else {
         BlockPos blockpos = new BlockPos(pos.getX(), this.getSeaLevel(), pos.getZ());
         if (!this.func_217337_f(blockpos)) {
            return false;
         } else {
            for(BlockPos blockpos1 = blockpos.down(); blockpos1.getY() > pos.getY(); blockpos1 = blockpos1.down()) {
               BlockState blockstate = this.getBlockState(blockpos1);
               if (blockstate.getOpacity(this, blockpos1) > 0 && !blockstate.getMaterial().isLiquid()) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   int getLightSubtracted(BlockPos pos, int amount);

   @Nullable
   IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull);

   @Deprecated
   boolean chunkExists(int chunkX, int chunkZ);

   BlockPos getHeight(Heightmap.Type heightmapType, BlockPos pos);

   int getHeight(Heightmap.Type heightmapType, int x, int z);

   default float getBrightness(BlockPos pos) {
      return this.getDimension().getLightBrightnessTable()[this.getLight(pos)];
   }

   int getSkylightSubtracted();

   WorldBorder getWorldBorder();

   boolean checkNoEntityCollision(@Nullable Entity entityIn, VoxelShape shape);

   default int getStrongPower(BlockPos pos, Direction direction) {
      return this.getBlockState(pos).getStrongPower(this, pos, direction);
   }

   boolean isRemote();

   int getSeaLevel();

   default IChunk getChunk(BlockPos p_217349_1_) {
      return this.getChunk(p_217349_1_.getX() >> 4, p_217349_1_.getZ() >> 4);
   }

   default IChunk getChunk(int chunkX, int chunkZ) {
      return this.getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
   }

   default IChunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus) {
      return this.getChunk(chunkX, chunkZ, requiredStatus, true);
   }

   default ChunkStatus getChunkStatus() {
      return ChunkStatus.EMPTY;
   }

   default boolean func_217350_a(BlockState p_217350_1_, BlockPos p_217350_2_, ISelectionContext p_217350_3_) {
      VoxelShape voxelshape = p_217350_1_.getCollisionShape(this, p_217350_2_, p_217350_3_);
      return voxelshape.isEmpty() || this.checkNoEntityCollision((Entity)null, voxelshape.withOffset((double)p_217350_2_.getX(), (double)p_217350_2_.getY(), (double)p_217350_2_.getZ()));
   }

   default boolean func_217346_i(Entity p_217346_1_) {
      return this.checkNoEntityCollision(p_217346_1_, VoxelShapes.create(p_217346_1_.getBoundingBox()));
   }

   default boolean areCollisionShapesEmpty(AxisAlignedBB p_217351_1_) {
      return this.isCollisionBoxesEmpty((Entity)null, p_217351_1_, Collections.emptySet());
   }

   default boolean areCollisionShapesEmpty(Entity p_217345_1_) {
      return this.isCollisionBoxesEmpty(p_217345_1_, p_217345_1_.getBoundingBox(), Collections.emptySet());
   }

   default boolean isCollisionBoxesEmpty(Entity entityIn, AxisAlignedBB aabb) {
      return this.isCollisionBoxesEmpty(entityIn, aabb, Collections.emptySet());
   }

   default boolean isCollisionBoxesEmpty(@Nullable Entity entityIn, AxisAlignedBB aabb, Set<Entity> entitiesToIgnore) {
      return this.getCollisionShapes(entityIn, aabb, entitiesToIgnore).allMatch(VoxelShape::isEmpty);
   }

   default Stream<VoxelShape> func_223439_a(@Nullable Entity entityIn, AxisAlignedBB aabb, Set<Entity> entitiesToIgnore) {
      return Stream.empty();
   }

   default Stream<VoxelShape> getCollisionShapes(@Nullable Entity enitityIn, AxisAlignedBB aabb, Set<Entity> entitiesToIgnore) {
      return Streams.concat(this.getCollisionShapes(enitityIn, aabb), this.func_223439_a(enitityIn, aabb, entitiesToIgnore));
   }

   default Stream<VoxelShape> getCollisionShapes(@Nullable final Entity entityIn, AxisAlignedBB aabb) {
      int i = MathHelper.floor(aabb.minX - 1.0E-7D) - 1;
      int j = MathHelper.floor(aabb.maxX + 1.0E-7D) + 1;
      int k = MathHelper.floor(aabb.minY - 1.0E-7D) - 1;
      int l = MathHelper.floor(aabb.maxY + 1.0E-7D) + 1;
      int i1 = MathHelper.floor(aabb.minZ - 1.0E-7D) - 1;
      int j1 = MathHelper.floor(aabb.maxZ + 1.0E-7D) + 1;
      final ISelectionContext iselectioncontext = entityIn == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(entityIn);
      // NoCubes Start
      return io.github.cadiboo.nocubes.hooks.Hooks.getCollisionShapes(this, entityIn, aabb, i, j, k, l, i1, j1, iselectioncontext);
      // NoCubes End
//      final CubeCoordinateIterator cubecoordinateiterator = new CubeCoordinateIterator(i, k, i1, j, l, j1);
//      final BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
//      final VoxelShape voxelshape = VoxelShapes.create(aabb);
//      return StreamSupport.stream(new AbstractSpliterator<VoxelShape>(Long.MAX_VALUE, 1280) {
//         boolean isEntityNull = entityIn == null;
//
//         public boolean tryAdvance(Consumer<? super VoxelShape> p_tryAdvance_1_) {
//            if (!this.isEntityNull) {
//               this.isEntityNull = true;
//               VoxelShape voxelshape1 = IWorldReader.this.getWorldBorder().getShape();
//               boolean flag = VoxelShapes.compare(voxelshape1, VoxelShapes.create(entityIn.getBoundingBox().shrink(1.0E-7D)), IBooleanFunction.AND);
//               boolean flag1 = VoxelShapes.compare(voxelshape1, VoxelShapes.create(entityIn.getBoundingBox().grow(1.0E-7D)), IBooleanFunction.AND);
//               if (!flag && flag1) {
//                  p_tryAdvance_1_.accept(voxelshape1);
//                  return true;
//               }
//            }
//
//            VoxelShape voxelshape3;
//            while(true) {
//               if (!cubecoordinateiterator.hasNext()) {
//                  return false;
//               }
//
//               int j2 = cubecoordinateiterator.getX();
//               int k2 = cubecoordinateiterator.getY();
//               int l2 = cubecoordinateiterator.getZ();
//               int k1 = cubecoordinateiterator.func_223473_e();
//               if (k1 != 3) {
//                  int l1 = j2 >> 4;
//                  int i2 = l2 >> 4;
//                  IChunk ichunk = IWorldReader.this.getChunk(l1, i2, IWorldReader.this.getChunkStatus(), false);
//                  if (ichunk != null) {
//                     blockpos$mutableblockpos.setPos(j2, k2, l2);
//                     BlockState blockstate = ichunk.getBlockState(blockpos$mutableblockpos);
//                     if ((k1 != 1 || blockstate.func_215704_f()) && (k1 != 2 || blockstate.getBlock() == Blocks.MOVING_PISTON)) {
//                        VoxelShape voxelshape2 = blockstate.getCollisionShape(IWorldReader.this, blockpos$mutableblockpos, iselectioncontext);
//                        voxelshape3 = voxelshape2.withOffset((double)j2, (double)k2, (double)l2);
//                        if (VoxelShapes.compare(voxelshape, voxelshape3, IBooleanFunction.AND)) {
//                           break;
//                        }
//                     }
//                  }
//               }
//            }
//
//            p_tryAdvance_1_.accept(voxelshape3);
//            return true;
//         }
//      }, false);
   }

   default boolean hasWater(BlockPos pos) {
      return this.getFluidState(pos).isTagged(FluidTags.WATER);
   }

   /**
    * Checks if any of the blocks within the aabb are liquids.
    */
   default boolean containsAnyLiquid(AxisAlignedBB bb) {
      int i = MathHelper.floor(bb.minX);
      int j = MathHelper.ceil(bb.maxX);
      int k = MathHelper.floor(bb.minY);
      int l = MathHelper.ceil(bb.maxY);
      int i1 = MathHelper.floor(bb.minZ);
      int j1 = MathHelper.ceil(bb.maxZ);

      try (BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain()) {
         for(int k1 = i; k1 < j; ++k1) {
            for(int l1 = k; l1 < l; ++l1) {
               for(int i2 = i1; i2 < j1; ++i2) {
                  BlockState blockstate = this.getBlockState(blockpos$pooledmutableblockpos.setPos(k1, l1, i2));
                  if (!blockstate.getFluidState().isEmpty()) {
                     boolean flag = true;
                     return flag;
                  }
               }
            }
         }

         return false;
      }
   }

   default int getLight(BlockPos pos) {
      return this.getNeighborAwareLightSubtracted(pos, this.getSkylightSubtracted());
   }

   default int getNeighborAwareLightSubtracted(BlockPos pos, int amount) {
      return pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000 ? this.getLightSubtracted(pos, amount) : 15;
   }

   @Deprecated
   default boolean isBlockLoaded(BlockPos pos) {
      return this.chunkExists(pos.getX() >> 4, pos.getZ() >> 4);
   }

   default boolean isAreaLoaded(BlockPos center, int range) {
      return this.isAreaLoaded(center.add(-range, -range, -range), center.add(range, range, range));
   }

   @Deprecated
   default boolean isAreaLoaded(BlockPos from, BlockPos to) {
      return this.isAreaLoaded(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
   }

   @Deprecated
   default boolean isAreaLoaded(int p_217344_1_, int p_217344_2_, int p_217344_3_, int p_217344_4_, int p_217344_5_, int p_217344_6_) {
      if (p_217344_5_ >= 0 && p_217344_2_ < 256) {
         p_217344_1_ = p_217344_1_ >> 4;
         p_217344_3_ = p_217344_3_ >> 4;
         p_217344_4_ = p_217344_4_ >> 4;
         p_217344_6_ = p_217344_6_ >> 4;

         for(int i = p_217344_1_; i <= p_217344_4_; ++i) {
            for(int j = p_217344_3_; j <= p_217344_6_; ++j) {
               if (!this.chunkExists(i, j)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   Dimension getDimension();
}
