package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IProperty;
import net.minecraft.state.IStateHolder;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;
import net.minecraft.tags.Tag;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.EmptyBlockReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockState extends StateHolder<Block, BlockState> implements IStateHolder<BlockState>, net.minecraftforge.common.extensions.IForgeBlockState {
   @Nullable
   private BlockState.Cache field_215707_c;
   private final int lightLevel;
   private final boolean field_215709_e;

   public BlockState(Block blockIn, ImmutableMap<IProperty<?>, Comparable<?>> properties) {
      super(blockIn, properties);
      this.lightLevel = blockIn.getLightValue(this);
      this.field_215709_e = blockIn.func_220074_n(this);
   }

   public void func_215692_c() {
      if (!this.getBlock().isVariableOpacity()) {
         this.field_215707_c = new BlockState.Cache(this);
      }

   }

   public Block getBlock() {
      return this.object;
   }

   public Material getMaterial() {
      return this.getBlock().getMaterial(this);
   }

   public boolean canEntitySpawn(IBlockReader worldIn, BlockPos pos, EntityType<?> type) {
      return this.getBlock().canEntitySpawn(this, worldIn, pos, type);
   }

   public boolean propagatesSkylightDown(IBlockReader worldIn, BlockPos pos) {
      return this.field_215707_c != null ? this.field_215707_c.field_222500_d : this.getBlock().propagatesSkylightDown(this, worldIn, pos);
   }

   public int getOpacity(IBlockReader worldIn, BlockPos pos) {
      return this.field_215707_c != null ? this.field_215707_c.field_222501_e : this.getBlock().getOpacity(this, worldIn, pos);
   }

   public VoxelShape func_215702_a(IBlockReader worldIn, BlockPos pos, Direction p_215702_3_) {
      return this.field_215707_c != null && this.field_215707_c.field_222502_f != null ? this.field_215707_c.field_222502_f[p_215702_3_.ordinal()] : VoxelShapes.func_216387_a(this.getRenderShape(worldIn, pos), p_215702_3_);
   }

   public boolean func_215704_f() {
      return this.field_215707_c == null || this.field_215707_c.field_222503_g;
   }

   public boolean func_215691_g() {
      return this.field_215709_e;
   }

   public int getLightValue() {
      return this.lightLevel;
   }

   /** @deprecated use {@link BlockState#isAir(IBlockReader, BlockPos) */
   @Deprecated
   public boolean isAir() {
      return this.getBlock().isAir(this);
   }

   /** @deprecated use {@link BlockState#rotate(IWorld, BlockPos, Rotation) */
   @Deprecated
   public MaterialColor getMaterialColor(IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().getMaterialColor(this, worldIn, pos);
   }

   /**
    * Returns the blockstate with the given rotation. If inapplicable, returns itself.
    */
   public BlockState rotate(Rotation rot) {
      return this.getBlock().rotate(this, rot);
   }

   /**
    * Returns the blockstate mirrored in the given way. If inapplicable, returns itself.
    */
   public BlockState mirror(Mirror mirrorIn) {
      return this.getBlock().mirror(this, mirrorIn);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean hasCustomBreakingProgress() {
      return this.getBlock().hasCustomBreakingProgress(this);
   }

   public BlockRenderType getRenderType() {
      return this.getBlock().getRenderType(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getPackedLightmapCoords(IEnviromentBlockReader p_215684_1_, BlockPos pos) {
      return this.getBlock().getPackedLightmapCoords(this, p_215684_1_, pos);
   }

   @OnlyIn(Dist.CLIENT)
   public float func_215703_d(IBlockReader p_215703_1_, BlockPos pos) {
      return this.getBlock().func_220080_a(this, p_215703_1_, pos);
   }

   public boolean isNormalCube(IBlockReader p_215686_1_, BlockPos pos) {
      return this.getBlock().isNormalCube(this, p_215686_1_, pos);
   }

   public boolean canProvidePower() {
      return this.getBlock().canProvidePower(this);
   }

   public int getWeakPower(IBlockReader blockAccess, BlockPos pos, Direction side) {
      return this.getBlock().getWeakPower(this, blockAccess, pos, side);
   }

   public boolean hasComparatorInputOverride() {
      return this.getBlock().hasComparatorInputOverride(this);
   }

   public int getComparatorInputOverride(World worldIn, BlockPos pos) {
      return this.getBlock().getComparatorInputOverride(this, worldIn, pos);
   }

   public float getBlockHardness(IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().getBlockHardness(this, worldIn, pos);
   }

   public float getPlayerRelativeBlockHardness(PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().getPlayerRelativeBlockHardness(this, player, worldIn, pos);
   }

   public int getStrongPower(IBlockReader blockAccess, BlockPos pos, Direction side) {
      return this.getBlock().getStrongPower(this, blockAccess, pos, side);
   }

   public PushReaction getPushReaction() {
      return this.getBlock().getPushReaction(this);
   }

   public boolean isOpaqueCube(IBlockReader worldIn, BlockPos pos) {
      return this.field_215707_c != null ? this.field_215707_c.field_222499_c : this.getBlock().isOpaqueCube(this, worldIn, pos);
   }

   public boolean isSolid() {
      // NoCubes Start
      if (io.github.cadiboo.nocubes.config.Config.renderSmoothTerrain && this.nocubes_isTerrainSmoothable()) return false;
      if (io.github.cadiboo.nocubes.config.Config.renderSmoothLeaves && this.nocubes_isLeavesSmoothable()) return false;
      // NoCubes End
      return this.field_215707_c != null ? this.field_215707_c.field_222498_b : this.getBlock().isSolid(this);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isSideInvisible(BlockState state, Direction face) {
      return this.getBlock().isSideInvisible(this, state, face);
   }

   public VoxelShape getShape(IBlockReader worldIn, BlockPos pos) {
      return this.getShape(worldIn, pos, ISelectionContext.dummy());
   }

   public VoxelShape getShape(IBlockReader p_215700_1_, BlockPos p_215700_2_, ISelectionContext p_215700_3_) {
      return this.getBlock().getShape(this, p_215700_1_, p_215700_2_, p_215700_3_);
   }

   public VoxelShape getCollisionShape(IBlockReader worldIn, BlockPos pos) {
      return this.field_215707_c != null ? this.field_215707_c.field_223626_g : this.getCollisionShape(worldIn, pos, ISelectionContext.dummy());
   }

   public VoxelShape getCollisionShape(IBlockReader p_215685_1_, BlockPos pos, ISelectionContext p_215685_3_) {
      return this.getBlock().getCollisionShape(this, p_215685_1_, pos, p_215685_3_);
   }

   public VoxelShape getRenderShape(IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().getRenderShape(this, worldIn, pos);
   }

   public VoxelShape getRaytraceShape(IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().getRaytraceShape(this, worldIn, pos);
   }

   public final boolean func_215682_a(IBlockReader p_215682_1_, BlockPos p_215682_2_, Entity p_215682_3_) {
      return Block.doesSideFillSquare(this.getCollisionShape(p_215682_1_, p_215682_2_, ISelectionContext.forEntity(p_215682_3_)), Direction.UP);
   }

   public Vec3d getOffset(IBlockReader access, BlockPos pos) {
      return this.getBlock().getOffset(this, access, pos);
   }

   /**
    * Called on both Client and Server when World#addBlockEvent is called. On the Server, this may perform additional
    * changes to the world, like pistons replacing the block with an extended base. On the client, the update may
    * involve replacing tile entities, playing sounds, or performing other visual actions to reflect the server side
    * changes.
    */
   public boolean onBlockEventReceived(World worldIn, BlockPos pos, int id, int param) {
      return this.getBlock().eventReceived(this, worldIn, pos, id, param);
   }

   public void neighborChanged(World p_215697_1_, BlockPos p_215697_2_, Block p_215697_3_, BlockPos p_215697_4_, boolean p_215697_5_) {
      this.getBlock().neighborChanged(this, p_215697_1_, p_215697_2_, p_215697_3_, p_215697_4_, p_215697_5_);
   }

   /**
    * For all neighbors, have them react to this block's existence, potentially updating their states as needed. For
    * example, fences make their connections to this block if possible and observers pulse if this block was placed in
    * front of their detector
    */
   public void updateNeighbors(IWorld worldIn, BlockPos pos, int flags) {
      this.getBlock().updateNeighbors(this, worldIn, pos, flags);
   }

   /**
    * Performs validations on the block state and possibly neighboring blocks to validate whether the incoming state is
    * valid to stay in the world. Currently used only by redstone wire to update itself if neighboring blocks have
    * changed and to possibly break itself.
    */
   public void updateDiagonalNeighbors(IWorld worldIn, BlockPos pos, int flags) {
      this.getBlock().updateDiagonalNeighbors(this, worldIn, pos, flags);
   }

   public void onBlockAdded(World p_215705_1_, BlockPos p_215705_2_, BlockState p_215705_3_, boolean p_215705_4_) {
      this.getBlock().onBlockAdded(this, p_215705_1_, p_215705_2_, p_215705_3_, p_215705_4_);
   }

   public void onReplaced(World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
      this.getBlock().onReplaced(this, worldIn, pos, newState, isMoving);
   }

   public void tick(World worldIn, BlockPos pos, Random random) {
      this.getBlock().tick(this, worldIn, pos, random);
   }

   public void randomTick(World worldIn, BlockPos pos, Random random) {
      this.getBlock().randomTick(this, worldIn, pos, random);
   }

   public void onEntityCollision(World worldIn, BlockPos pos, Entity entityIn) {
      this.getBlock().onEntityCollision(this, worldIn, pos, entityIn);
   }

   public void spawnAdditionalDrops(World p_215706_1_, BlockPos p_215706_2_, ItemStack p_215706_3_) {
      this.getBlock().spawnAdditionalDrops(this, p_215706_1_, p_215706_2_, p_215706_3_);
   }

   public List<ItemStack> getDrops(LootContext.Builder p_215693_1_) {
      return this.getBlock().getDrops(this, p_215693_1_);
   }

   public boolean onBlockActivated(World p_215687_1_, PlayerEntity p_215687_2_, Hand p_215687_3_, BlockRayTraceResult p_215687_4_) {
      return this.getBlock().onBlockActivated(this, p_215687_1_, p_215687_4_.getPos(), p_215687_2_, p_215687_3_, p_215687_4_);
   }

   public void onBlockClicked(World worldIn, BlockPos pos, PlayerEntity player) {
      this.getBlock().onBlockClicked(this, worldIn, pos, player);
   }

   public boolean causesSuffocation(IBlockReader p_215696_1_, BlockPos p_215696_2_) {
      // NoCubes Start
      if (io.github.cadiboo.nocubes.config.Config.terrainCollisions && this.nocubes_isTerrainSmoothable()) return false;
      // NoCubes End
      return this.getBlock().causesSuffocation(this, p_215696_1_, p_215696_2_);
   }

   public BlockState updatePostPlacement(Direction face, BlockState queried, IWorld worldIn, BlockPos currentPos, BlockPos offsetPos) {
      return this.getBlock().updatePostPlacement(this, face, queried, worldIn, currentPos, offsetPos);
   }

   public boolean allowsMovement(IBlockReader worldIn, BlockPos pos, PathType type) {
      return this.getBlock().allowsMovement(this, worldIn, pos, type);
   }

   public boolean isReplaceable(BlockItemUseContext useContext) {
      return this.getBlock().isReplaceable(this, useContext);
   }

   public boolean isValidPosition(IWorldReader worldIn, BlockPos pos) {
      return this.getBlock().isValidPosition(this, worldIn, pos);
   }

   public boolean blockNeedsPostProcessing(IBlockReader worldIn, BlockPos pos) {
      return this.getBlock().needsPostProcessing(this, worldIn, pos);
   }

   @Nullable
   public INamedContainerProvider getContainer(World p_215699_1_, BlockPos p_215699_2_) {
      return this.getBlock().getContainer(this, p_215699_1_, p_215699_2_);
   }

   public boolean isIn(Tag<Block> tagIn) {
      return this.getBlock().isIn(tagIn);
   }

   public IFluidState getFluidState() {
      return this.getBlock().getFluidState(this);
   }

   public boolean ticksRandomly() {
      return this.getBlock().ticksRandomly(this);
   }

   @OnlyIn(Dist.CLIENT)
   public long getPositionRandom(BlockPos pos) {
      return this.getBlock().getPositionRandom(this, pos);
   }

   public SoundType getSoundType() {
      return this.getBlock().getSoundType(this);
   }

   public void onProjectileCollision(World worldIn, BlockState state, BlockRayTraceResult hit, Entity projectile) {
      this.getBlock().onProjectileCollision(worldIn, state, hit, projectile);
   }

   public static <T> Dynamic<T> serialize(DynamicOps<T> p_215689_0_, BlockState p_215689_1_) {
      ImmutableMap<IProperty<?>, Comparable<?>> immutablemap = p_215689_1_.getValues();
      T t;
      if (immutablemap.isEmpty()) {
         t = p_215689_0_.createMap(ImmutableMap.of(p_215689_0_.createString("Name"), p_215689_0_.createString(Registry.BLOCK.getKey(p_215689_1_.getBlock()).toString())));
      } else {
         t = p_215689_0_.createMap(ImmutableMap.of(p_215689_0_.createString("Name"), p_215689_0_.createString(Registry.BLOCK.getKey(p_215689_1_.getBlock()).toString()), p_215689_0_.createString("Properties"), p_215689_0_.createMap(immutablemap.entrySet().stream().map((p_215683_1_) -> {
            return Pair.of(p_215689_0_.createString(p_215683_1_.getKey().getName()), p_215689_0_.createString(IStateHolder.func_215670_b(p_215683_1_.getKey(), p_215683_1_.getValue())));
         }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))));
      }

      return new Dynamic<>(p_215689_0_, t);
   }

   public static <T> BlockState deserialize(Dynamic<T> p_215698_0_) {
      DefaultedRegistry defaultedregistry = Registry.BLOCK;
      Optional<T> optional = p_215698_0_.getElement("Name");
      DynamicOps<T> dynamicops = p_215698_0_.getOps();
      Block block = (Block)defaultedregistry.getOrDefault(new ResourceLocation(optional.flatMap(dynamicops::getStringValue).orElse("minecraft:air")));
      Map<String, String> map = p_215698_0_.get("Properties").asMap((p_215701_0_) -> {
         return p_215701_0_.asString("");
      }, (p_215694_0_) -> {
         return p_215694_0_.asString("");
      });
      BlockState blockstate = block.getDefaultState();
      StateContainer<Block, BlockState> statecontainer = block.getStateContainer();

      for(Entry<String, String> entry : map.entrySet()) {
         String s = entry.getKey();
         IProperty<?> iproperty = statecontainer.getProperty(s);
         if (iproperty != null) {
            blockstate = IStateHolder.func_215671_a(blockstate, iproperty, s, p_215698_0_.toString(), entry.getValue());
         }
      }

      return blockstate;
   }

   static final class Cache {
      private static final Direction[] field_222497_a = Direction.values();
      private final boolean field_222498_b;
      private final boolean field_222499_c;
      private final boolean field_222500_d;
      private final int field_222501_e;
      private final VoxelShape[] field_222502_f;
      private final VoxelShape field_223626_g;
      private final boolean field_222503_g;

      private Cache(BlockState p_i50627_1_) {
         Block block = p_i50627_1_.getBlock();
         this.field_222498_b = block.isSolid(p_i50627_1_);
         this.field_222499_c = block.isOpaqueCube(p_i50627_1_, EmptyBlockReader.INSTANCE, BlockPos.ZERO);
         this.field_222500_d = block.propagatesSkylightDown(p_i50627_1_, EmptyBlockReader.INSTANCE, BlockPos.ZERO);
         this.field_222501_e = block.getOpacity(p_i50627_1_, EmptyBlockReader.INSTANCE, BlockPos.ZERO);
         if (!p_i50627_1_.isSolid()) {
            this.field_222502_f = null;
         } else {
            this.field_222502_f = new VoxelShape[field_222497_a.length];
            VoxelShape voxelshape = block.getRenderShape(p_i50627_1_, EmptyBlockReader.INSTANCE, BlockPos.ZERO);

            for(Direction direction : field_222497_a) {
               this.field_222502_f[direction.ordinal()] = VoxelShapes.func_216387_a(voxelshape, direction);
            }
         }

         this.field_223626_g = block.getCollisionShape(p_i50627_1_, EmptyBlockReader.INSTANCE, BlockPos.ZERO, ISelectionContext.dummy());
         this.field_222503_g = Arrays.stream(Direction.Axis.values()).anyMatch((p_222491_1_) -> {
            return this.field_223626_g.getStart(p_222491_1_) < 0.0D || this.field_223626_g.getEnd(p_222491_1_) > 1.0D;
         });
      }
   }

   // ******** NoCubes Start ******** //

   public boolean nocubes_isTerrainSmoothable = false;
   public boolean nocubes_isLeavesSmoothable = false;

   /**
    * does NOT take into account whether NoCubes is enabled or not
    */
   public boolean nocubes_isTerrainSmoothable() {
      return this.nocubes_isTerrainSmoothable;
   }

   public void nocubes_setTerrainSmoothable(final boolean newIsTerrainSmoothable) {
      this.nocubes_isTerrainSmoothable = newIsTerrainSmoothable;
   }

   /**
    * does NOT take into account whether NoCubes is enabled or not
    */
   public boolean nocubes_isLeavesSmoothable() {
      return this.nocubes_isLeavesSmoothable;
   }

   public void nocubes_setLeavesSmoothable(final boolean newIsLeavesSmoothable) {
      this.nocubes_isLeavesSmoothable = newIsLeavesSmoothable;
   }

}
