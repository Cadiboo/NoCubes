package net.minecraft.block.state;

import io.github.cadiboo.nocubes.NoCubes;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IStateHolder;
import net.minecraft.tags.Tag;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public interface IBlockState extends IStateHolder<IBlockState>, net.minecraftforge.common.extensions.IForgeBlockState {
   ThreadLocal<Object2ByteMap<IBlockState>> PROPAGATES_SKYLIGHT_DOWN_CACHE = ThreadLocal.withInitial(() -> {
      Object2ByteOpenHashMap<IBlockState> object2byteopenhashmap = new Object2ByteOpenHashMap<>();
      object2byteopenhashmap.defaultReturnValue((byte)127);
      return object2byteopenhashmap;
   });
   ThreadLocal<Object2ByteMap<IBlockState>> OPACITY_CACHE = ThreadLocal.withInitial(() -> {
      Object2ByteOpenHashMap<IBlockState> object2byteopenhashmap = new Object2ByteOpenHashMap<>();
      object2byteopenhashmap.defaultReturnValue((byte)127);
      return object2byteopenhashmap;
   });
   ThreadLocal<Object2ByteMap<IBlockState>> OPAQUE_CUBE_CACHE = ThreadLocal.withInitial(() -> {
      Object2ByteOpenHashMap<IBlockState> object2byteopenhashmap = new Object2ByteOpenHashMap<>();
      object2byteopenhashmap.defaultReturnValue((byte)127);
      return object2byteopenhashmap;
   });

   Block getBlock();

   default Material getMaterial() {
      return this.getBlock().getMaterial(this);
   }

   default boolean canEntitySpawn(Entity entityIn) {
      return this.getBlock().canEntitySpawn(this, entityIn);
   }

   default boolean propagatesSkylightDown(IBlockReader worldIn, BlockPos pos) {
      Block block = this.getBlock();
      Object2ByteMap<IBlockState> object2bytemap = block.isVariableOpacity() ? null : PROPAGATES_SKYLIGHT_DOWN_CACHE.get();
      if (object2bytemap != null) {
         byte b0 = object2bytemap.getByte(this);
         if (b0 != object2bytemap.defaultReturnValue()) {
            return b0 != 0;
         }
      }

      boolean flag = block.propagatesSkylightDown(this, worldIn, pos);
      if (object2bytemap != null) {
         object2bytemap.put(this, (byte)(flag ? 1 : 0));
      }

      return flag;
   }

   default int getOpacity(IBlockReader worldIn, BlockPos pos) {
      Block block = this.getBlock();
      Object2ByteMap<IBlockState> object2bytemap = block.isVariableOpacity() ? null : OPACITY_CACHE.get();
      if (object2bytemap != null) {
         byte b0 = object2bytemap.getByte(this);
         if (b0 != object2bytemap.defaultReturnValue()) {
            return b0;
         }
      }

      int i = block.getOpacity(this, worldIn, pos);
      if (object2bytemap != null) {
         object2bytemap.put(this, (byte)Math.min(i, worldIn.getMaxLightLevel()));
      }

      return i;
   }

   default int getLightValue() {
      return this.getBlock().getLightValue(this);
   }

   /** @deprecated use {@link IBlockState#isAir(IBlockReader, BlockPos) */
   @Deprecated
   default boolean isAir() {
      return this.getBlock().isAir(this);
   }

   default boolean useNeighborBrightness(IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().useNeighborBrightness(this, worldIn, pos);
   }

   default MaterialColor getMaterialColor(IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().getMaterialColor(this, worldIn, pos);
   }

   /** @deprecated use {@link IBlockState#rotate(IWorld, BlockPos, Rotation) */
   /**
    * Returns the blockstate with the given rotation. If inapplicable, returns itself.
    */
   @Deprecated
   default IBlockState rotate(Rotation rot) {
      return this.getBlock().rotate(this, rot);
   }

   /**
    * Returns the blockstate mirrored in the given way. If inapplicable, returns itself.
    */
   default IBlockState mirror(Mirror mirrorIn) {
      return this.getBlock().mirror(this, mirrorIn);
   }

   default boolean isFullCube() {
      return this.getBlock().isFullCube(this);
   }

   @OnlyIn(Dist.CLIENT)
   default boolean hasCustomBreakingProgress() {
      return this.getBlock().hasCustomBreakingProgress(this);
   }

   default EnumBlockRenderType getRenderType() {
      return this.getBlock().getRenderType(this);
   }

   @OnlyIn(Dist.CLIENT)
   default int getPackedLightmapCoords(IWorldReader source, BlockPos pos) {
      return this.getBlock().getPackedLightmapCoords(this, source, pos);
   }

   @OnlyIn(Dist.CLIENT)
   default float getAmbientOcclusionLightValue() {
      return this.getBlock().getAmbientOcclusionLightValue(this);
   }

   default boolean isBlockNormalCube() {
      return this.getBlock().isBlockNormalCube(this);
   }

   default boolean isNormalCube() {
      return this.getBlock().isNormalCube(this);
   }

   default boolean canProvidePower() {
      return this.getBlock().canProvidePower(this);
   }

   default int getWeakPower(IBlockReader blockAccess, BlockPos pos, EnumFacing side) {
      return this.getBlock().getWeakPower(this, blockAccess, pos, side);
   }

   default boolean hasComparatorInputOverride() {
      return this.getBlock().hasComparatorInputOverride(this);
   }

   default int getComparatorInputOverride(World worldIn, BlockPos pos) {
      return this.getBlock().getComparatorInputOverride(this, worldIn, pos);
   }

   default float getBlockHardness(IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().getBlockHardness(this, worldIn, pos);
   }

   default float getPlayerRelativeBlockHardness(EntityPlayer player, IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().getPlayerRelativeBlockHardness(this, player, worldIn, pos);
   }

   default int getStrongPower(IBlockReader blockAccess, BlockPos pos, EnumFacing side) {
      return this.getBlock().getStrongPower(this, blockAccess, pos, side);
   }

   default EnumPushReaction getPushReaction() {
      return this.getBlock().getPushReaction(this);
   }

   default boolean isOpaqueCube(IBlockReader worldIn, BlockPos pos) {
      // NoCubes Start
      if (this.nocubes_isTerrainSmoothable() && io.github.cadiboo.nocubes.NoCubes.isEnabled()) {
         return false;
      }
      // NoCubes End
      Block block = this.getBlock();
      Object2ByteMap<IBlockState> object2bytemap = block.isVariableOpacity() ? null : OPAQUE_CUBE_CACHE.get();
      if (object2bytemap != null) {
         byte b0 = object2bytemap.getByte(this);
         if (b0 != object2bytemap.defaultReturnValue()) {
            return b0 != 0;
         }
      }

      boolean flag = block.isOpaqueCube(this, worldIn, pos);
      if (object2bytemap != null) {
         object2bytemap.put(this, (byte)(flag ? 1 : 0));
      }

      return flag;
   }

   default boolean isSolid() {
      if (nocubes_isTerrainSmoothable() && NoCubes.isEnabled()) return false;
      return this.getBlock().isSolid(this);
   }

   @OnlyIn(Dist.CLIENT)
   default boolean isSideInvisible(IBlockState state, EnumFacing face) {
      return this.getBlock().isSideInvisible(this, state, face);
   }

   default VoxelShape getShape(IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().getShape(this, worldIn, pos);
   }

   default VoxelShape getCollisionShape(IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().getCollisionShape(this, worldIn, pos);
   }

   default VoxelShape getRenderShape(IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().getRenderShape(this, worldIn, pos);
   }

   default VoxelShape getRaytraceShape(IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().getRaytraceShape(this, worldIn, pos);
   }

   /**
    * Determines if the block is solid enough on the top side to support other blocks, like redstone components.
    */
   default boolean isTopSolid() {
      return this.getBlock().isTopSolid(this);
   }

   default Vec3d getOffset(IBlockReader access, BlockPos pos) {
      return this.getBlock().getOffset(this, access, pos);
   }

   /**
    * Called on both Client and Server when World#addBlockEvent is called. On the Server, this may perform additional
    * changes to the world, like pistons replacing the block with an extended base. On the client, the update may
    * involve replacing tile entities, playing sounds, or performing other visual actions to reflect the server side
    * changes.
    */
   default boolean onBlockEventReceived(World worldIn, BlockPos pos, int id, int param) {
      return this.getBlock().eventReceived(this, worldIn, pos, id, param);
   }

   /**
    * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
    * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
    * block, etc.
    */
   default void neighborChanged(World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
      this.getBlock().neighborChanged(this, worldIn, pos, blockIn, fromPos);
   }

   /**
    * For all neighbors, have them react to this block's existence, potentially updating their states as needed. For
    * example, fences make their connections to this block if possible and observers pulse if this block was placed in
    * front of their detector
    */
   default void updateNeighbors(IWorld worldIn, BlockPos pos, int flags) {
      this.getBlock().updateNeighbors(this, worldIn, pos, flags);
   }

   /**
    * Performs validations on the block state and possibly neighboring blocks to validate whether the incoming state is
    * valid to stay in the world. Currently used only by redstone wire to update itself if neighboring blocks have
    * changed and to possibly break itself.
    */
   default void updateDiagonalNeighbors(IWorld worldIn, BlockPos pos, int flags) {
      this.getBlock().updateDiagonalNeighbors(this, worldIn, pos, flags);
   }

   default void onBlockAdded(World worldIn, BlockPos pos, IBlockState oldState) {
      this.getBlock().onBlockAdded(this, worldIn, pos, oldState);
   }

   default void onReplaced(World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
      this.getBlock().onReplaced(this, worldIn, pos, newState, isMoving);
   }

   default void tick(World worldIn, BlockPos pos, Random random) {
      this.getBlock().tick(this, worldIn, pos, random);
   }

   default void randomTick(World worldIn, BlockPos pos, Random random) {
      this.getBlock().randomTick(this, worldIn, pos, random);
   }

   default void onEntityCollision(World worldIn, BlockPos pos, Entity entityIn) {
      this.getBlock().onEntityCollision(this, worldIn, pos, entityIn);
   }

   default void dropBlockAsItem(World worldIn, BlockPos pos, int fortune) {
      this.dropBlockAsItemWithChance(worldIn, pos, 1.0F, fortune);
   }

   default void dropBlockAsItemWithChance(World worldIn, BlockPos pos, float chancePerItem, int fortune) {
      this.getBlock().dropBlockAsItemWithChance(this, worldIn, pos, chancePerItem, fortune);
   }

   default boolean onBlockActivated(World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
      return this.getBlock().onBlockActivated(this, worldIn, pos, player, hand, side, hitX, hitY, hitZ);
   }

   default void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer player) {
      this.getBlock().onBlockClicked(this, worldIn, pos, player);
   }

   default boolean causesSuffocation() {
      return this.getBlock().causesSuffocation(this);
   }

   default BlockFaceShape getBlockFaceShape(IBlockReader worldIn, BlockPos pos, EnumFacing facing) {
      return this.getBlock().getBlockFaceShape(worldIn, this, pos, facing);
   }

   default IBlockState updatePostPlacement(EnumFacing face, IBlockState queried, IWorld worldIn, BlockPos currentPos, BlockPos offsetPos) {
      return this.getBlock().updatePostPlacement(this, face, queried, worldIn, currentPos, offsetPos);
   }

   default boolean allowsMovement(IBlockReader worldIn, BlockPos pos, PathType type) {
      return this.getBlock().allowsMovement(this, worldIn, pos, type);
   }

   default boolean isReplaceable(BlockItemUseContext useContext) {
      return this.getBlock().isReplaceable(this, useContext);
   }

   default boolean isValidPosition(IWorldReaderBase worldIn, BlockPos pos) {
      return this.getBlock().isValidPosition(this, worldIn, pos);
   }

   default boolean blockNeedsPostProcessing(IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().needsPostProcessing(this, worldIn, pos);
   }

   default boolean isIn(Tag<Block> tagIn) {
      return this.getBlock().isIn(tagIn);
   }

   default IFluidState getFluidState() {
      return this.getBlock().getFluidState(this);
   }

   default boolean ticksRandomly() {
      return this.getBlock().ticksRandomly(this);
   }

   @OnlyIn(Dist.CLIENT)
   default long getPositionRandom(BlockPos pos) {
      return this.getBlock().getPositionRandom(this, pos);
   }

	// ******** NoCubes Start ******** //

	/**
	 * does NOT take into account whether NoCubes is enabled or not
	 */
	default boolean nocubes_isTerrainSmoothable() {
		return false;
	}

	default void nocubes_setTerrainSmoothable(final boolean isTerrainSmoothable) {
		return;
	}

   /**
	 * does NOT take into account whether NoCubes is enabled or not
	 */
	default boolean nocubes_isLeavesSmoothable() {
		return false;
	}

	default void nocubes_setLeavesSmoothable(final boolean isLeavesSmoothable) {
		return;
	}

}
