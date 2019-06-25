package net.minecraft.world;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockMaterialMatcher;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkHolder;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class World extends net.minecraftforge.common.capabilities.CapabilityProvider<World> implements IEnviromentBlockReader, IWorld, AutoCloseable, net.minecraftforge.common.extensions.IForgeWorld {
   protected static final Logger LOGGER = LogManager.getLogger();
   private static final Direction[] FACING_VALUES = Direction.values();
   public final List<TileEntity> loadedTileEntityList = Lists.newArrayList();
   public final List<TileEntity> tickableTileEntities = Lists.newArrayList();
   protected final List<TileEntity> addedTileEntityList = Lists.newArrayList();
   protected final java.util.Set<TileEntity> tileEntitiesToBeRemoved = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>()); // Forge: faster "contains" makes removal much more efficient
   private final long cloudColour = 16777215L;
   private final Thread mainThread;
   private int skylightSubtracted;
   /**
    * Contains the current Linear Congruential Generator seed for block updates. Used with an A value of 3 and a C value
    * of 0x3c6ef35f, producing a highly planar series of values ill-suited for choosing random blocks in a 16x128x16
    * field.
    */
   protected int updateLCG = (new Random()).nextInt();
   protected final int DIST_HASH_MAGIC = 1013904223;
   public float prevRainingStrength;
   public float rainingStrength;
   public float prevThunderingStrength;
   public float thunderingStrength;
   private int lastLightningBolt;
   public final Random rand = new Random();
   public final Dimension dimension;
   public final AbstractChunkProvider chunkProvider;
   protected final WorldInfo worldInfo;
   private final IProfiler profiler;
   public final boolean isRemote;
   protected boolean processingLoadedTiles;
   private final WorldBorder worldBorder;
   public boolean restoringBlockSnapshots = false;
   public boolean captureBlockSnapshots = false;
   public java.util.ArrayList<net.minecraftforge.common.util.BlockSnapshot> capturedBlockSnapshots = new java.util.ArrayList<net.minecraftforge.common.util.BlockSnapshot>();

   protected World(WorldInfo info, DimensionType dimType, BiFunction<World, Dimension, AbstractChunkProvider> provider, IProfiler profilerIn, boolean remote) {
      super(World.class);
      this.profiler = profilerIn;
      this.worldInfo = info;
      this.dimension = dimType.create(this);
      this.chunkProvider = provider.apply(this, this.dimension);
      this.isRemote = remote;
      this.worldBorder = this.dimension.createWorldBorder();
      this.mainThread = Thread.currentThread();
   }

   public Biome getBiome(BlockPos pos) {
      return this.dimension.getBiome(pos);
   }

   public Biome getBiomeBody(BlockPos pos) {
      AbstractChunkProvider abstractchunkprovider = this.func_72863_F();
      Chunk chunk = abstractchunkprovider.getChunk(pos.getX() >> 4, pos.getZ() >> 4, false);
      if (chunk != null) {
         return chunk.getBiome(pos);
      } else {
         ChunkGenerator<?> chunkgenerator = this.func_72863_F().getChunkGenerator();
         return chunkgenerator == null ? Biomes.PLAINS : chunkgenerator.getBiomeProvider().getBiome(pos);
      }
   }

   public boolean isRemote() {
      return this.isRemote;
   }

   @Nullable
   public MinecraftServer getServer() {
      return null;
   }

   /**
    * Sets a new spawn location by finding an uncovered block at a random (x,z) location in the chunk.
    */
   @OnlyIn(Dist.CLIENT)
   public void setInitialSpawnLocation() {
      this.setSpawnPoint(new BlockPos(8, 64, 8));
   }

   public BlockState getGroundAboveSeaLevel(BlockPos pos) {
      BlockPos blockpos;
      for(blockpos = new BlockPos(pos.getX(), this.getSeaLevel(), pos.getZ()); !this.isAirBlock(blockpos.up()); blockpos = blockpos.up()) {
         ;
      }

      return this.getBlockState(blockpos);
   }

   /**
    * Check if the given BlockPos has valid coordinates
    */
   public static boolean isValid(BlockPos pos) {
      return !isOutsideBuildHeight(pos) && pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000;
   }

   public static boolean isOutsideBuildHeight(BlockPos pos) {
      return isYOutOfBounds(pos.getY());
   }

   public static boolean isYOutOfBounds(int y) {
      return y < 0 || y >= 256;
   }

   public Chunk getChunkAt(BlockPos pos) {
      return this.func_212866_a_(pos.getX() >> 4, pos.getZ() >> 4);
   }

   public Chunk func_212866_a_(int chunkX, int chunkZ) {
      return (Chunk)this.getChunk(chunkX, chunkZ, ChunkStatus.FULL);
   }

   public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
      IChunk ichunk = this.chunkProvider.getChunk(x, z, requiredStatus, nonnull);
      if (ichunk == null && nonnull) {
         throw new IllegalStateException("Should always be able to create a chunk!");
      } else {
         return ichunk;
      }
   }

   /**
    * Sets a block state into this world.Flags are as follows:
    * 1 will cause a block update.
    * 2 will send the change to clients.
    * 4 will prevent the block from being re-rendered.
    * 8 will force any re-renders to run on the main thread instead
    * 16 will prevent neighbor reactions (e.g. fences connecting, observers pulsing).
    * 32 will prevent neighbor reactions from spawning drops.
    * 64 will signify the block is being moved.
    * Flags can be OR-ed
    */
   public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
      if (isOutsideBuildHeight(pos)) {
         return false;
      } else if (!this.isRemote && this.worldInfo.getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES) {
         return false;
      } else {
         Chunk chunk = this.getChunkAt(pos);
         Block block = newState.getBlock();

         pos = pos.toImmutable(); // Forge - prevent mutable BlockPos leaks
         net.minecraftforge.common.util.BlockSnapshot blockSnapshot = null;
         if (this.captureBlockSnapshots && !this.isRemote) {
            blockSnapshot = net.minecraftforge.common.util.BlockSnapshot.getBlockSnapshot(this, pos, flags);
            this.capturedBlockSnapshots.add(blockSnapshot);
         }

         BlockState old = getBlockState(pos);
         int oldLight = old.getLightValue(this, pos);
         int oldOpacity = old.getOpacity(this, pos);

         BlockState blockstate = chunk.setBlockState(pos, newState, (flags & 64) != 0);
         if (blockstate == null) {
            if (blockSnapshot != null) this.capturedBlockSnapshots.remove(blockSnapshot);
            return false;
         } else {
            BlockState blockstate1 = this.getBlockState(pos);
            if (blockstate1 != blockstate && (blockstate1.getOpacity(this, pos) != oldOpacity || blockstate1.getLightValue() != oldLight || blockstate1.func_215691_g() || blockstate.func_215691_g())) {
               this.profiler.startSection("queueCheckLight");
               this.func_72863_F().func_212863_j_().checkBlock(pos);
               this.profiler.endSection();
            }

            if (blockSnapshot == null) { // Don't notify clients or update physics while capturing blockstates
               this.markAndNotifyBlock(pos, chunk, blockstate, newState, flags);
            }
            return true;
         }
      }
   }

   // Split off from original setBlockState(BlockPos, BlockState, int) method in order to directly send client and physic updates
   public void markAndNotifyBlock(BlockPos pos, @Nullable Chunk chunk, BlockState blockstate, BlockState newState, int flags)
   {
      Block block = newState.getBlock();
      BlockState blockstate1 = getBlockState(pos);
      {
         {
            if (blockstate1 == newState) {
               if (blockstate != blockstate1) {
                  this.markForRerender(pos);
               }

               if ((flags & 2) != 0 && (!this.isRemote || (flags & 4) == 0) && (this.isRemote || chunk == null || chunk.func_217321_u() != null && chunk.func_217321_u().isAtLeast(ChunkHolder.LocationType.TICKING))) {
                  this.notifyBlockUpdate(pos, blockstate, newState, flags);
               }

               if (!this.isRemote && (flags & 1) != 0) {
                  this.notifyNeighbors(pos, blockstate.getBlock());
                  if (newState.hasComparatorInputOverride()) {
                     this.updateComparatorOutputLevel(pos, block);
                  }
               }

               if ((flags & 16) == 0) {
                  int i = flags & -2;
                  blockstate.updateDiagonalNeighbors(this, pos, i);
                  newState.updateNeighbors(this, pos, i);
                  newState.updateDiagonalNeighbors(this, pos, i);
               }

               this.func_217393_a(pos, blockstate, blockstate1);
            }
         }
      }
   }

   public void func_217393_a(BlockPos p_217393_1_, BlockState p_217393_2_, BlockState p_217393_3_) {
   }

   public boolean removeBlock(BlockPos pos, boolean isMoving) {
      IFluidState ifluidstate = this.getFluidState(pos);
      return this.setBlockState(pos, ifluidstate.getBlockState(), 3 | (isMoving ? 64 : 0));
   }

   /**
    * Sets a block to air, but also plays the sound and particles and can spawn drops
    */
   public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
      BlockState blockstate = this.getBlockState(pos);
      if (blockstate.isAir(this, pos)) {
         return false;
      } else {
         IFluidState ifluidstate = this.getFluidState(pos);
         this.playEvent(2001, pos, Block.getStateId(blockstate));
         if (dropBlock) {
            TileEntity tileentity = blockstate.hasTileEntity() ? this.getTileEntity(pos) : null;
            Block.spawnDrops(blockstate, this, pos, tileentity);
         }

         return this.setBlockState(pos, ifluidstate.getBlockState(), 3);
      }
   }

   /**
    * Convenience method to update the block on both the client and server
    */
   public boolean setBlockState(BlockPos pos, BlockState state) {
      return this.setBlockState(pos, state, 3);
   }

   /**
    * Flags are as in setBlockState
    */
   public abstract void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags);

   public void notifyNeighbors(BlockPos pos, Block blockIn) {
      if (this.worldInfo.getGenerator() != WorldType.DEBUG_ALL_BLOCK_STATES) {
         this.notifyNeighborsOfStateChange(pos, blockIn);
      }

   }

   public void markForRerender(BlockPos pos) {
   }

   public void notifyNeighborsOfStateChange(BlockPos pos, Block blockIn) {
      if(net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(this, pos, this.getBlockState(pos), java.util.EnumSet.allOf(Direction.class), false).isCanceled())
         return;
      this.neighborChanged(pos.west(), blockIn, pos);
      this.neighborChanged(pos.east(), blockIn, pos);
      this.neighborChanged(pos.down(), blockIn, pos);
      this.neighborChanged(pos.up(), blockIn, pos);
      this.neighborChanged(pos.north(), blockIn, pos);
      this.neighborChanged(pos.south(), blockIn, pos);
   }

   public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, Direction skipSide) {
      java.util.EnumSet<Direction> directions = java.util.EnumSet.allOf(Direction.class);
      directions.remove(skipSide);
      if (net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(this, pos, this.getBlockState(pos), directions, false).isCanceled())
         return;

      if (skipSide != Direction.WEST) {
         this.neighborChanged(pos.west(), blockType, pos);
      }

      if (skipSide != Direction.EAST) {
         this.neighborChanged(pos.east(), blockType, pos);
      }

      if (skipSide != Direction.DOWN) {
         this.neighborChanged(pos.down(), blockType, pos);
      }

      if (skipSide != Direction.UP) {
         this.neighborChanged(pos.up(), blockType, pos);
      }

      if (skipSide != Direction.NORTH) {
         this.neighborChanged(pos.north(), blockType, pos);
      }

      if (skipSide != Direction.SOUTH) {
         this.neighborChanged(pos.south(), blockType, pos);
      }

   }

   public void neighborChanged(BlockPos pos, Block blockIn, BlockPos fromPos) {
      if (!this.isRemote) {
         BlockState blockstate = this.getBlockState(pos);

         try {
            blockstate.neighborChanged(this, pos, blockIn, fromPos, false);
         } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while updating neighbours");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being updated");
            crashreportcategory.addDetail("Source block type", () -> {
               try {
                  return String.format("ID #%s (%s // %s)", blockIn.getRegistryName(), blockIn.getTranslationKey(), blockIn.getClass().getCanonicalName());
               } catch (Throwable var2) {
                  return "ID #" + blockIn.getRegistryName();
               }
            });
            CrashReportCategory.addBlockInfo(crashreportcategory, pos, blockstate);
            throw new ReportedException(crashreport);
         }
      }
   }

   public int getLightSubtracted(BlockPos pos, int amount) {
      if (pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000) {
         if (pos.getY() < 0) {
            return 0;
         } else {
            if (pos.getY() >= 256) {
               pos = new BlockPos(pos.getX(), 255, pos.getZ());
            }

            return this.getChunkAt(pos).getLightSubtracted(pos, amount);
         }
      } else {
         return 15;
      }
   }

   public int getHeight(Heightmap.Type heightmapType, int x, int z) {
      int i;
      if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
         if (this.chunkExists(x >> 4, z >> 4)) {
            i = this.func_212866_a_(x >> 4, z >> 4).getTopBlockY(heightmapType, x & 15, z & 15) + 1;
         } else {
            i = 0;
         }
      } else {
         i = this.getSeaLevel() + 1;
      }

      return i;
   }

   public int getLightFor(LightType type, BlockPos pos) {
      return this.func_72863_F().func_212863_j_().getLightEngine(type).getLightFor(pos);
   }

   public BlockState getBlockState(BlockPos pos) {
      if (isOutsideBuildHeight(pos)) {
         return Blocks.VOID_AIR.getDefaultState();
      } else {
         Chunk chunk = this.func_212866_a_(pos.getX() >> 4, pos.getZ() >> 4);
         return chunk.getBlockState(pos);
      }
   }

   public IFluidState getFluidState(BlockPos pos) {
      if (isOutsideBuildHeight(pos)) {
         return Fluids.EMPTY.getDefaultState();
      } else {
         // NoCubes Start
         return io.github.cadiboo.nocubes.hooks.Hooks.getFluidState(this, pos);
//         Chunk chunk = this.getChunk(pos);
//         return chunk.getFluidState(pos);
         // NoCubes End
      }
   }

   /**
    * Checks whether its daytime by seeing if the light subtracted from the skylight is less than 4. Always returns true
    * on the client because vanilla has no need for it on the client, therefore it is not synced to the client
    */
   public boolean isDaytime() {
      return this.dimension.isDaytime();
   }

   /**
    * Plays the specified sound for a player at the center of the given block position.
    */
   public void playSound(@Nullable PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
      this.playSound(player, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, soundIn, category, volume, pitch);
   }

   public abstract void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch);

   public abstract void playMovingSound(@Nullable PlayerEntity p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_, SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_);

   public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
   }

   public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
   }

   @OnlyIn(Dist.CLIENT)
   public void addParticle(IParticleData particleData, boolean forceAlwaysRender, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
   }

   public void addOptionalParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
   }

   public void func_217404_b(IParticleData p_217404_1_, boolean p_217404_2_, double p_217404_3_, double p_217404_5_, double p_217404_7_, double p_217404_9_, double p_217404_11_, double p_217404_13_) {
   }

   /**
    * Returns the sun brightness - checks time of day, rain and thunder
    */
   public float getSunBrightness(float partialTicks) {
      return this.dimension.getSunBrightness(partialTicks);
   }

   public float getSunBrightnessBody(float partialTicks) {
      float f = this.getCelestialAngle(partialTicks);
      float f1 = 1.0F - (MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
      f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
      f1 = 1.0F - f1;
      f1 = (float)((double)f1 * (1.0D - (double)(this.getRainStrength(partialTicks) * 5.0F) / 16.0D));
      f1 = (float)((double)f1 * (1.0D - (double)(this.getThunderStrength(partialTicks) * 5.0F) / 16.0D));
      return f1 * 0.8F + 0.2F;
   }

   @OnlyIn(Dist.CLIENT)
   public Vec3d func_217382_a(BlockPos p_217382_1_, float p_217382_2_) {
      return this.dimension.getSkyColor(p_217382_1_, p_217382_2_);
   }

   @OnlyIn(Dist.CLIENT)
   public Vec3d getSkyColorBody(BlockPos p_217382_1_, float p_217382_2_) {
      float f = this.getCelestialAngle(p_217382_2_);
      float f1 = MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
      f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
      int i = net.minecraftforge.client.ForgeHooksClient.getSkyBlendColour(this, p_217382_1_);
      float f3 = (float)(i >> 16 & 255) / 255.0F;
      float f4 = (float)(i >> 8 & 255) / 255.0F;
      float f5 = (float)(i & 255) / 255.0F;
      f3 = f3 * f1;
      f4 = f4 * f1;
      f5 = f5 * f1;
      float f6 = this.getRainStrength(p_217382_2_);
      if (f6 > 0.0F) {
         float f7 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.6F;
         float f8 = 1.0F - f6 * 0.75F;
         f3 = f3 * f8 + f7 * (1.0F - f8);
         f4 = f4 * f8 + f7 * (1.0F - f8);
         f5 = f5 * f8 + f7 * (1.0F - f8);
      }

      float f10 = this.getThunderStrength(p_217382_2_);
      if (f10 > 0.0F) {
         float f11 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.2F;
         float f9 = 1.0F - f10 * 0.75F;
         f3 = f3 * f9 + f11 * (1.0F - f9);
         f4 = f4 * f9 + f11 * (1.0F - f9);
         f5 = f5 * f9 + f11 * (1.0F - f9);
      }

      if (this.lastLightningBolt > 0) {
         float f12 = (float)this.lastLightningBolt - p_217382_2_;
         if (f12 > 1.0F) {
            f12 = 1.0F;
         }

         f12 = f12 * 0.45F;
         f3 = f3 * (1.0F - f12) + 0.8F * f12;
         f4 = f4 * (1.0F - f12) + 0.8F * f12;
         f5 = f5 * (1.0F - f12) + 1.0F * f12;
      }

      return new Vec3d((double)f3, (double)f4, (double)f5);
   }

   /**
    * Return getCelestialAngle()*2*PI
    */
   public float getCelestialAngleRadians(float partialTicks) {
      float f = this.getCelestialAngle(partialTicks);
      return f * ((float)Math.PI * 2F);
   }

   @OnlyIn(Dist.CLIENT)
   public Vec3d getCloudColour(float partialTicks) {
      return this.dimension.getCloudColor(partialTicks);
   }

   @OnlyIn(Dist.CLIENT)
   public Vec3d getCloudColorBody(float partialTicks) {
      float f = this.getCelestialAngle(partialTicks);
      float f1 = MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
      f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
      float f2 = 1.0F;
      float f3 = 1.0F;
      float f4 = 1.0F;
      float f5 = this.getRainStrength(partialTicks);
      if (f5 > 0.0F) {
         float f6 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.6F;
         float f7 = 1.0F - f5 * 0.95F;
         f2 = f2 * f7 + f6 * (1.0F - f7);
         f3 = f3 * f7 + f6 * (1.0F - f7);
         f4 = f4 * f7 + f6 * (1.0F - f7);
      }

      f2 = f2 * (f1 * 0.9F + 0.1F);
      f3 = f3 * (f1 * 0.9F + 0.1F);
      f4 = f4 * (f1 * 0.85F + 0.15F);
      float f9 = this.getThunderStrength(partialTicks);
      if (f9 > 0.0F) {
         float f10 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.2F;
         float f8 = 1.0F - f9 * 0.95F;
         f2 = f2 * f8 + f10 * (1.0F - f8);
         f3 = f3 * f8 + f10 * (1.0F - f8);
         f4 = f4 * f8 + f10 * (1.0F - f8);
      }

      return new Vec3d((double)f2, (double)f3, (double)f4);
   }

   /**
    * Returns vector(ish) with R/G/B for fog
    */
   @OnlyIn(Dist.CLIENT)
   public Vec3d getFogColor(float partialTicks) {
      float f = this.getCelestialAngle(partialTicks);
      return this.dimension.getFogColor(f, partialTicks);
   }

   /**
    * How bright are stars in the sky
    */
   @OnlyIn(Dist.CLIENT)
   public float getStarBrightness(float partialTicks) {
      return this.dimension.getStarBrightness(partialTicks);
   }

   @OnlyIn(Dist.CLIENT)
   public float getStarBrightnessBody(float partialTicks) {
      float f = this.getCelestialAngle(partialTicks);
      float f1 = 1.0F - (MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.25F);
      f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
      return f1 * f1 * 0.5F;
   }

   public boolean addTileEntity(TileEntity tile) {
      if (tile.getWorld() != this) tile.setWorld(this); // Forge - set the world early as vanilla doesn't set it until next tick
      if (this.processingLoadedTiles) {
         LOGGER.error("Adding block entity while ticking: {} @ {}", () -> {
            return Registry.BLOCK_ENTITY_TYPE.getKey(tile.getType());
         }, tile::getPos);
         return addedTileEntityList.add(tile); // Forge: wait to add new TE if we're currently processing existing ones
      }

      boolean flag = this.loadedTileEntityList.add(tile);
      if (flag && tile instanceof ITickableTileEntity) {
         this.tickableTileEntities.add(tile);
      }

      tile.onLoad();

      if (this.isRemote) {
         BlockPos blockpos = tile.getPos();
         BlockState blockstate = this.getBlockState(blockpos);
         this.notifyBlockUpdate(blockpos, blockstate, blockstate, 2);
      }

      return flag;
   }

   public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
      if (this.processingLoadedTiles) {
         tileEntityCollection.stream().filter(te -> te.getWorld() != this).forEach(te -> te.setWorld(this)); // Forge - set the world early as vanilla doesn't set it until next tick
         this.addedTileEntityList.addAll(tileEntityCollection);
      } else {
         for(TileEntity tileentity : tileEntityCollection) {
            this.addTileEntity(tileentity);
         }
      }

   }

   public void func_217391_K() {
      IProfiler iprofiler = this.getProfiler();
      iprofiler.startSection("blockEntities");
      this.processingLoadedTiles = true;// Forge: Move above remove to prevent CMEs

      if (!this.tileEntitiesToBeRemoved.isEmpty()) {
         this.tileEntitiesToBeRemoved.forEach(e -> e.onChunkUnloaded());
         this.tickableTileEntities.removeAll(this.tileEntitiesToBeRemoved);
         this.loadedTileEntityList.removeAll(this.tileEntitiesToBeRemoved);
         this.tileEntitiesToBeRemoved.clear();
      }

      Iterator<TileEntity> iterator = this.tickableTileEntities.iterator();

      while(iterator.hasNext()) {
         TileEntity tileentity = iterator.next();
         if (!tileentity.isRemoved() && tileentity.hasWorld()) {
            BlockPos blockpos = tileentity.getPos();
            if (this.chunkProvider.canTick(blockpos) && this.getWorldBorder().contains(blockpos)) {
               try {
                  net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileentity);
                  iprofiler.startSection(() -> {
                     return String.valueOf(tileentity.getType().getRegistryName());
                  });
                  if (tileentity.getType().isValidBlock(this.getBlockState(blockpos).getBlock())) {
                     ((ITickableTileEntity)tileentity).tick();
                  } else {
                     tileentity.warnInvalidBlock();
                  }

                  iprofiler.endSection();
               } catch (Throwable throwable) {
                  CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking block entity");
                  CrashReportCategory crashreportcategory = crashreport.makeCategory("Block entity being ticked");
                  tileentity.addInfoToCrashReport(crashreportcategory);
                  if (net.minecraftforge.common.ForgeConfig.SERVER.removeErroringTileEntities.get()) {
                     LogManager.getLogger().fatal("{}", crashreport.getCompleteReport());
                     tileentity.remove();
                     this.removeTileEntity(tileentity.getPos());
                  } else
                     throw new ReportedException(crashreport);
               }
               finally {
                  net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileentity);
               }
            }
         }

         if (tileentity.isRemoved()) {
            iterator.remove();
            this.loadedTileEntityList.remove(tileentity);
            if (this.isBlockLoaded(tileentity.getPos())) {
               //Forge: Bugfix: If we set the tile entity it immediately sets it in the chunk, so we could be desyned
               Chunk chunk = this.getChunkAt(tileentity.getPos());
               if (chunk.getTileEntity(tileentity.getPos(), Chunk.CreateEntityType.CHECK) == tileentity)
                  chunk.removeTileEntity(tileentity.getPos());
            }
         }
      }

      this.processingLoadedTiles = false;
      iprofiler.endStartSection("pendingBlockEntities");
      if (!this.addedTileEntityList.isEmpty()) {
         for(int i = 0; i < this.addedTileEntityList.size(); ++i) {
            TileEntity tileentity1 = this.addedTileEntityList.get(i);
            if (!tileentity1.isRemoved()) {
               if (!this.loadedTileEntityList.contains(tileentity1)) {
                  this.addTileEntity(tileentity1);
               }

               if (this.isBlockLoaded(tileentity1.getPos())) {
                  Chunk chunk = this.getChunkAt(tileentity1.getPos());
                  BlockState blockstate = chunk.getBlockState(tileentity1.getPos());
                  chunk.addTileEntity(tileentity1.getPos(), tileentity1);
                  this.notifyBlockUpdate(tileentity1.getPos(), blockstate, blockstate, 3);
               }
            }
         }

         this.addedTileEntityList.clear();
      }

      iprofiler.endSection();
   }

   public void func_217390_a(Consumer<Entity> p_217390_1_, Entity p_217390_2_) {
      try {
         net.minecraftforge.server.timings.TimeTracker.ENTITY_UPDATE.trackStart(p_217390_2_);
         p_217390_1_.accept(p_217390_2_);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking entity");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being ticked");
         p_217390_2_.fillCrashReport(crashreportcategory);
         throw new ReportedException(crashreport);
      } finally {
         net.minecraftforge.server.timings.TimeTracker.ENTITY_UPDATE.trackEnd(p_217390_2_);
      }
   }

   /**
    * Returns true if there are any blocks in the region constrained by an AxisAlignedBB
    */
   public boolean checkBlockCollision(AxisAlignedBB bb) {
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
                  BlockState blockstate = this.getBlockState(blockpos$pooledmutableblockpos.func_181079_c(k1, l1, i2));
                  if (!blockstate.isAir(this, blockpos$pooledmutableblockpos)) {
                     boolean flag = true;
                     return flag;
                  }
               }
            }
         }

         return false;
      }
   }

   public boolean isFlammableWithin(AxisAlignedBB bb) {
      int i = MathHelper.floor(bb.minX);
      int j = MathHelper.ceil(bb.maxX);
      int k = MathHelper.floor(bb.minY);
      int l = MathHelper.ceil(bb.maxY);
      int i1 = MathHelper.floor(bb.minZ);
      int j1 = MathHelper.ceil(bb.maxZ);
      if (this.isAreaLoaded(i, k, i1, j, l, j1)) {
         try (BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain()) {
            for(int k1 = i; k1 < j; ++k1) {
               for(int l1 = k; l1 < l; ++l1) {
                  for(int i2 = i1; i2 < j1; ++i2) {
                     BlockState state = this.getBlockState(blockpos$pooledmutableblockpos.func_181079_c(k1, l1, i2));
                     if (state.isBurning(this, blockpos$pooledmutableblockpos)) {
                        boolean flag = true;
                        return flag;
                     }
                  }
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public BlockState findBlockstateInArea(AxisAlignedBB area, Block blockIn) {
      int i = MathHelper.floor(area.minX);
      int j = MathHelper.ceil(area.maxX);
      int k = MathHelper.floor(area.minY);
      int l = MathHelper.ceil(area.maxY);
      int i1 = MathHelper.floor(area.minZ);
      int j1 = MathHelper.ceil(area.maxZ);
      if (this.isAreaLoaded(i, k, i1, j, l, j1)) {
         try (BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain()) {
            for(int k1 = i; k1 < j; ++k1) {
               for(int l1 = k; l1 < l; ++l1) {
                  for(int i2 = i1; i2 < j1; ++i2) {
                     BlockState blockstate = this.getBlockState(blockpos$pooledmutableblockpos.func_181079_c(k1, l1, i2));
                     if (blockstate.getBlock() == blockIn) {
                        BlockState blockstate1 = blockstate;
                        return blockstate1;
                     }
                  }
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   /**
    * Returns true if the given bounding box contains the given material
    */
   public boolean isMaterialInBB(AxisAlignedBB bb, Material materialIn) {
      int i = MathHelper.floor(bb.minX);
      int j = MathHelper.ceil(bb.maxX);
      int k = MathHelper.floor(bb.minY);
      int l = MathHelper.ceil(bb.maxY);
      int i1 = MathHelper.floor(bb.minZ);
      int j1 = MathHelper.ceil(bb.maxZ);
      BlockMaterialMatcher blockmaterialmatcher = BlockMaterialMatcher.forMaterial(materialIn);
      return BlockPos.getAllInBox(i, k, i1, j - 1, l - 1, j1 - 1).anyMatch((p_217397_2_) -> {
         return blockmaterialmatcher.test(this.getBlockState(p_217397_2_));
      });
   }

   public Explosion createExplosion(@Nullable Entity p_217385_1_, double p_217385_2_, double p_217385_4_, double p_217385_6_, float p_217385_8_, Explosion.Mode p_217385_9_) {
      return this.createExplosion(p_217385_1_, (DamageSource)null, p_217385_2_, p_217385_4_, p_217385_6_, p_217385_8_, false, p_217385_9_);
   }

   public Explosion createExplosion(@Nullable Entity p_217398_1_, double p_217398_2_, double p_217398_4_, double p_217398_6_, float p_217398_8_, boolean p_217398_9_, Explosion.Mode p_217398_10_) {
      return this.createExplosion(p_217398_1_, (DamageSource)null, p_217398_2_, p_217398_4_, p_217398_6_, p_217398_8_, p_217398_9_, p_217398_10_);
   }

   public Explosion createExplosion(@Nullable Entity p_217401_1_, @Nullable DamageSource p_217401_2_, double p_217401_3_, double p_217401_5_, double p_217401_7_, float p_217401_9_, boolean p_217401_10_, Explosion.Mode p_217401_11_) {
      Explosion explosion = new Explosion(this, p_217401_1_, p_217401_3_, p_217401_5_, p_217401_7_, p_217401_9_, p_217401_10_, p_217401_11_);
      if (p_217401_2_ != null) {
         explosion.setDamageSource(p_217401_2_);
      }
      if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(this, explosion)) return explosion;

      explosion.doExplosionA();
      explosion.doExplosionB(true);
      return explosion;
   }

   /**
    * Attempts to extinguish a fire
    */
   public boolean extinguishFire(@Nullable PlayerEntity player, BlockPos pos, Direction side) {
      pos = pos.offset(side);
      if (this.getBlockState(pos).getBlock() == Blocks.FIRE) {
         this.playEvent(player, 1009, pos, 0);
         this.removeBlock(pos, false);
         return true;
      } else {
         return false;
      }
   }

   /**
    * Returns the name of the current chunk provider, by calling chunkprovider.makeString()
    */
   @OnlyIn(Dist.CLIENT)
   public String getProviderName() {
      return this.chunkProvider.makeString();
   }

   @Nullable
   public TileEntity getTileEntity(BlockPos pos) {
      if (isOutsideBuildHeight(pos)) {
         return null;
      } else if (!this.isRemote && Thread.currentThread() != this.mainThread) {
         return null;
      } else {
         TileEntity tileentity = null;
         if (this.processingLoadedTiles) {
            tileentity = this.getPendingTileEntityAt(pos);
         }

         if (tileentity == null) {
            tileentity = this.getChunkAt(pos).getTileEntity(pos, Chunk.CreateEntityType.IMMEDIATE);
         }

         if (tileentity == null) {
            tileentity = this.getPendingTileEntityAt(pos);
         }

         return tileentity;
      }
   }

   @Nullable
   private TileEntity getPendingTileEntityAt(BlockPos pos) {
      for(int i = 0; i < this.addedTileEntityList.size(); ++i) {
         TileEntity tileentity = this.addedTileEntityList.get(i);
         if (!tileentity.isRemoved() && tileentity.getPos().equals(pos)) {
            return tileentity;
         }
      }

      return null;
   }

   public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
      if (!isOutsideBuildHeight(pos)) {
         pos = pos.toImmutable(); // Forge - prevent mutable BlockPos leaks
         if (tileEntityIn != null && !tileEntityIn.isRemoved()) {
            if (this.processingLoadedTiles) {
               tileEntityIn.setPos(pos);
               if (tileEntityIn.getWorld() != this)
                  tileEntityIn.setWorld(this); // Forge - set the world early as vanilla doesn't set it until next tick
               Iterator<TileEntity> iterator = this.addedTileEntityList.iterator();

               while(iterator.hasNext()) {
                  TileEntity tileentity = iterator.next();
                  if (tileentity.getPos().equals(pos)) {
                     tileentity.remove();
                     iterator.remove();
                  }
               }

               this.addedTileEntityList.add(tileEntityIn);
            } else {
               Chunk chunk = this.getChunkAt(pos);
               if (chunk != null) chunk.addTileEntity(pos, tileEntityIn);
               this.addTileEntity(tileEntityIn);
            }
         }

      }
   }

   public void removeTileEntity(BlockPos pos) {
      TileEntity tileentity = this.getTileEntity(pos);
      if (tileentity != null && this.processingLoadedTiles) {
         tileentity.remove();
         this.addedTileEntityList.remove(tileentity);
         if (!(tileentity instanceof ITickableTileEntity)) //Forge: If they are not tickable they wont be removed in the update loop.
            this.loadedTileEntityList.remove(tileentity);
      } else {
         if (tileentity != null) {
            this.addedTileEntityList.remove(tileentity);
            this.loadedTileEntityList.remove(tileentity);
            this.tickableTileEntities.remove(tileentity);
         }

         this.getChunkAt(pos).removeTileEntity(pos);
      }
      this.updateComparatorOutputLevel(pos, getBlockState(pos).getBlock()); //Notify neighbors of changes
   }

   public boolean isBlockPresent(BlockPos pos) {
      return isOutsideBuildHeight(pos) ? false : this.chunkProvider.chunkExists(pos.getX() >> 4, pos.getZ() >> 4);
   }

   public boolean func_217400_a(BlockPos p_217400_1_, Entity p_217400_2_) {
      if (isOutsideBuildHeight(p_217400_1_)) {
         return false;
      } else {
         IChunk ichunk = this.getChunk(p_217400_1_.getX() >> 4, p_217400_1_.getZ() >> 4, ChunkStatus.FULL, false);
         return ichunk == null ? false : ichunk.getBlockState(p_217400_1_).func_215682_a(this, p_217400_1_, p_217400_2_);
      }
   }

   /**
    * Called on construction of the World class to setup the initial skylight values
    */
   public void calculateInitialSkylight() {
      double d0 = 1.0D - (double)(this.getRainStrength(1.0F) * 5.0F) / 16.0D;
      double d1 = 1.0D - (double)(this.getThunderStrength(1.0F) * 5.0F) / 16.0D;
      double d2 = 0.5D + 2.0D * MathHelper.clamp((double)MathHelper.cos(this.getCelestialAngle(1.0F) * ((float)Math.PI * 2F)), -0.25D, 0.25D);
      this.skylightSubtracted = (int)((1.0D - d2 * d0 * d1) * 11.0D);
   }

   /**
    * first boolean for hostile mobs and second for peaceful mobs
    */
   public void setAllowedSpawnTypes(boolean hostile, boolean peaceful) {
      this.func_72863_F().setAllowedSpawnTypes(hostile, peaceful);
      this.getDimension().setAllowedSpawnTypes(hostile, peaceful);
   }

   /**
    * Called from World constructor to set rainingStrength and thunderingStrength
    */
   protected void calculateInitialWeather() {
      this.dimension.calculateInitialWeather();
   }

   public void calculateInitialWeatherBody() {
      if (this.worldInfo.isRaining()) {
         this.rainingStrength = 1.0F;
         if (this.worldInfo.isThundering()) {
            this.thunderingStrength = 1.0F;
         }
      }

   }

   public void close() throws IOException {
      this.chunkProvider.close();
   }

   public ChunkStatus getChunkStatus() {
      return ChunkStatus.FULL;
   }

   /**
    * Gets all entities within the specified AABB excluding the one passed into it.
    */
   public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
      List<Entity> list = Lists.newArrayList();
      int i = MathHelper.floor((boundingBox.minX - getMaxEntityRadius()) / 16.0D);
      int j = MathHelper.floor((boundingBox.maxX + getMaxEntityRadius()) / 16.0D);
      int k = MathHelper.floor((boundingBox.minZ - getMaxEntityRadius()) / 16.0D);
      int l = MathHelper.floor((boundingBox.maxZ + getMaxEntityRadius()) / 16.0D);

      for(int i1 = i; i1 <= j; ++i1) {
         for(int j1 = k; j1 <= l; ++j1) {
            Chunk chunk = this.func_72863_F().getChunk(i1, j1, false);
            if (chunk != null) {
               chunk.getEntitiesWithinAABBForEntity(entityIn, boundingBox, list, predicate);
            }
         }
      }

      return list;
   }

   public List<Entity> getEntitiesWithinAABB(@Nullable EntityType<?> p_217394_1_, AxisAlignedBB p_217394_2_, Predicate<? super Entity> p_217394_3_) {
      int i = MathHelper.floor((p_217394_2_.minX - getMaxEntityRadius()) / 16.0D);
      int j = MathHelper.ceil((p_217394_2_.maxX + getMaxEntityRadius()) / 16.0D);
      int k = MathHelper.floor((p_217394_2_.minZ - getMaxEntityRadius()) / 16.0D);
      int l = MathHelper.ceil((p_217394_2_.maxZ + getMaxEntityRadius()) / 16.0D);
      List<Entity> list = Lists.newArrayList();

      for(int i1 = i; i1 < j; ++i1) {
         for(int j1 = k; j1 < l; ++j1) {
            Chunk chunk = this.func_72863_F().getChunk(i1, j1, false);
            if (chunk != null) {
               chunk.func_217313_a(p_217394_1_, p_217394_2_, list, p_217394_3_);
            }
         }
      }

      return list;
   }

   public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
      int i = MathHelper.floor((aabb.minX - getMaxEntityRadius()) / 16.0D);
      int j = MathHelper.ceil((aabb.maxX + getMaxEntityRadius()) / 16.0D);
      int k = MathHelper.floor((aabb.minZ - getMaxEntityRadius()) / 16.0D);
      int l = MathHelper.ceil((aabb.maxZ + getMaxEntityRadius()) / 16.0D);
      List<T> list = Lists.newArrayList();

      for(int i1 = i; i1 < j; ++i1) {
         for(int j1 = k; j1 < l; ++j1) {
            Chunk chunk = this.func_72863_F().getChunk(i1, j1, false);
            if (chunk != null) {
               chunk.getEntitiesOfTypeWithinAABB(clazz, aabb, list, filter);
            }
         }
      }

      return list;
   }

   /**
    * Returns the Entity with the given ID, or null if it doesn't exist in this World.
    */
   @Nullable
   public abstract Entity getEntityByID(int id);

   public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity) {
      if (this.isBlockLoaded(pos)) {
         this.getChunkAt(pos).markDirty();
      }

   }

   public int getSeaLevel() {
      return 63;
   }

   public World getWorld() {
      return this;
   }

   public WorldType getWorldType() {
      return this.worldInfo.getGenerator();
   }

   /**
    * Returns the single highest strong power out of all directions using getStrongPower(BlockPos, EnumFacing)
    */
   public int getStrongPower(BlockPos pos) {
      int i = 0;
      i = Math.max(i, this.getStrongPower(pos.down(), Direction.DOWN));
      if (i >= 15) {
         return i;
      } else {
         i = Math.max(i, this.getStrongPower(pos.up(), Direction.UP));
         if (i >= 15) {
            return i;
         } else {
            i = Math.max(i, this.getStrongPower(pos.north(), Direction.NORTH));
            if (i >= 15) {
               return i;
            } else {
               i = Math.max(i, this.getStrongPower(pos.south(), Direction.SOUTH));
               if (i >= 15) {
                  return i;
               } else {
                  i = Math.max(i, this.getStrongPower(pos.west(), Direction.WEST));
                  if (i >= 15) {
                     return i;
                  } else {
                     i = Math.max(i, this.getStrongPower(pos.east(), Direction.EAST));
                     return i >= 15 ? i : i;
                  }
               }
            }
         }
      }
   }

   public boolean isSidePowered(BlockPos pos, Direction side) {
      return this.getRedstonePower(pos, side) > 0;
   }

   public int getRedstonePower(BlockPos pos, Direction facing) {
      BlockState blockstate = this.getBlockState(pos);
      return blockstate.shouldCheckWeakPower(this, pos, facing) ? this.getStrongPower(pos) : blockstate.getWeakPower(this, pos, facing);
   }

   public boolean isBlockPowered(BlockPos pos) {
      if (this.getRedstonePower(pos.down(), Direction.DOWN) > 0) {
         return true;
      } else if (this.getRedstonePower(pos.up(), Direction.UP) > 0) {
         return true;
      } else if (this.getRedstonePower(pos.north(), Direction.NORTH) > 0) {
         return true;
      } else if (this.getRedstonePower(pos.south(), Direction.SOUTH) > 0) {
         return true;
      } else if (this.getRedstonePower(pos.west(), Direction.WEST) > 0) {
         return true;
      } else {
         return this.getRedstonePower(pos.east(), Direction.EAST) > 0;
      }
   }

   /**
    * Checks if the specified block or its neighbors are powered by a neighboring block. Used by blocks like TNT and
    * Doors.
    */
   public int getRedstonePowerFromNeighbors(BlockPos pos) {
      int i = 0;

      for(Direction direction : FACING_VALUES) {
         int j = this.getRedstonePower(pos.offset(direction), direction);
         if (j >= 15) {
            return 15;
         }

         if (j > i) {
            i = j;
         }
      }

      return i;
   }

   /**
    * If on MP, sends a quitting packet.
    */
   @OnlyIn(Dist.CLIENT)
   public void sendQuittingDisconnectingPacket() {
   }

   public void setGameTime(long worldTime) {
      this.worldInfo.setGameTime(worldTime);
   }

   /**
    * gets the random world seed
    */
   public long getSeed() {
      return this.dimension.getSeed();
   }

   public long getGameTime() {
      return this.worldInfo.getGameTime();
   }

   public long getDayTime() {
      return this.dimension.getWorldTime();
   }

   /**
    * Sets the world time.
    */
   public void setDayTime(long time) {
      this.dimension.setWorldTime(time);
   }

   protected void advanceTime() {
      this.setGameTime(this.worldInfo.getGameTime() + 1L);
      if (this.worldInfo.getGameRulesInstance().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
         this.setDayTime(this.worldInfo.getDayTime() + 1L);
      }

   }

   /**
    * Gets the spawn point in the world
    */
   public BlockPos getSpawnPoint() {
      BlockPos blockpos = this.dimension.getSpawnPoint();
      if (!this.getWorldBorder().contains(blockpos)) {
         blockpos = this.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(this.getWorldBorder().getCenterX(), 0.0D, this.getWorldBorder().getCenterZ()));
      }

      return blockpos;
   }

   public void setSpawnPoint(BlockPos pos) {
      this.dimension.setSpawnPoint(pos);
   }

   public boolean isBlockModifiable(PlayerEntity player, BlockPos pos) {
      return dimension.canMineBlock(player, pos);
   }

   public boolean canMineBlockBody(PlayerEntity player, BlockPos pos) {
      return true;
   }

   /**
    * sends a Packet 38 (Entity Status) to all tracked players of that entity
    */
   public void setEntityState(Entity entityIn, byte state) {
   }

   public AbstractChunkProvider func_72863_F() {
      return this.chunkProvider;
   }

   public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
      this.getBlockState(pos).onBlockEventReceived(this, pos, eventID, eventParam);
   }

   /**
    * Returns the world's WorldInfo object
    */
   public WorldInfo getWorldInfo() {
      return this.worldInfo;
   }

   /**
    * Gets the GameRules instance.
    */
   public GameRules getGameRules() {
      return this.worldInfo.getGameRulesInstance();
   }

   public float getThunderStrength(float delta) {
      return MathHelper.lerp(delta, this.prevThunderingStrength, this.thunderingStrength) * this.getRainStrength(delta);
   }

   /**
    * Sets the strength of the thunder.
    */
   @OnlyIn(Dist.CLIENT)
   public void setThunderStrength(float strength) {
      this.prevThunderingStrength = strength;
      this.thunderingStrength = strength;
   }

   /**
    * Returns rain strength.
    */
   public float getRainStrength(float delta) {
      return MathHelper.lerp(delta, this.prevRainingStrength, this.rainingStrength);
   }

   /**
    * Sets the strength of the rain.
    */
   @OnlyIn(Dist.CLIENT)
   public void setRainStrength(float strength) {
      this.prevRainingStrength = strength;
      this.rainingStrength = strength;
   }

   /**
    * Returns true if the current thunder strength (weighted with the rain strength) is greater than 0.9
    */
   public boolean isThundering() {
      if (this.dimension.hasSkyLight() && !this.dimension.isNether()) {
         return (double)this.getThunderStrength(1.0F) > 0.9D;
      } else {
         return false;
      }
   }

   /**
    * Returns true if the current rain strength is greater than 0.2
    */
   public boolean isRaining() {
      return (double)this.getRainStrength(1.0F) > 0.2D;
   }

   /**
    * Check if precipitation is currently happening at a position
    */
   public boolean isRainingAt(BlockPos position) {
      if (!this.isRaining()) {
         return false;
      } else if (!this.func_217337_f(position)) {
         return false;
      } else if (this.getHeight(Heightmap.Type.MOTION_BLOCKING, position).getY() > position.getY()) {
         return false;
      } else {
         return this.getBiome(position).getPrecipitation() == Biome.RainType.RAIN;
      }
   }

   public boolean isBlockinHighHumidity(BlockPos pos) {
      return this.dimension.isHighHumidity(pos);
   }

   @Nullable
   public abstract MapData func_217406_a(String p_217406_1_);

   public abstract void func_217399_a(MapData p_217399_1_);

   public abstract int getNextMapId();

   public void playBroadcastSound(int id, BlockPos pos, int data) {
   }

   /**
    * Returns current world height.
    */
   public int getActualHeight() {
      return this.dimension.getActualHeight();
   }

   /**
    * Returns horizon height for use in rendering the sky.
    */
   public double getHorizon() {
      return this.dimension.getHorizon();
   }

   /**
    * Adds some basic stats of the world to the given crash report.
    */
   public CrashReportCategory fillCrashReport(CrashReport report) {
      CrashReportCategory crashreportcategory = report.makeCategoryDepth("Affected level", 1);
      crashreportcategory.addDetail("All players", () -> {
         return this.getPlayers().size() + " total; " + this.getPlayers();
      });
      crashreportcategory.addDetail("Chunk stats", this.chunkProvider::makeString);
      crashreportcategory.addDetail("Level dimension", () -> {
         return this.dimension.getType().toString();
      });

      try {
         this.worldInfo.addToCrashReport(crashreportcategory);
      } catch (Throwable throwable) {
         crashreportcategory.addCrashSectionThrowable("Level Data Unobtainable", throwable);
      }

      return crashreportcategory;
   }

   public abstract void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress);

   @OnlyIn(Dist.CLIENT)
   public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, @Nullable CompoundNBT compound) {
   }

   public abstract Scoreboard func_96441_U();

   public void updateComparatorOutputLevel(BlockPos pos, Block blockIn) {
      for(Direction direction : Direction.values()) { //Forge: TODO: change to VALUES once ATed
         BlockPos blockpos = pos.offset(direction);
         if (this.isBlockLoaded(blockpos)) {
            BlockState blockstate = this.getBlockState(blockpos);
            blockstate.onNeighborChange(this, blockpos, pos);
            if (blockstate.isNormalCube(this, blockpos)) {
               blockpos = blockpos.offset(direction);
               blockstate = this.getBlockState(blockpos);
               if (blockstate.getWeakChanges(this, blockpos)) {
                  blockstate.neighborChanged(this, blockpos, blockIn, pos, false);
               }
            }
         }
      }

   }

   public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
      long i = 0L;
      float f = 0.0F;
      if (this.isBlockLoaded(pos)) {
         f = this.getCurrentMoonPhaseFactor();
         i = this.getChunkAt(pos).getInhabitedTime();
      }

      return new DifficultyInstance(this.getDifficulty(), this.getDayTime(), i, f);
   }

   public int getSkylightSubtracted() {
      return this.skylightSubtracted;
   }

   @OnlyIn(Dist.CLIENT)
   public int getLastLightningBolt() {
      return this.lastLightningBolt;
   }

   public void setLastLightningBolt(int lastLightningBoltIn) {
      this.lastLightningBolt = lastLightningBoltIn;
   }

   public WorldBorder getWorldBorder() {
      return this.worldBorder;
   }

   public void sendPacketToServer(IPacket<?> packetIn) {
      throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
   }

   @Nullable
   public BlockPos findNearestStructure(String name, BlockPos pos, int radius, boolean p_211157_4_) {
      return null;
   }

   public Dimension getDimension() {
      return this.dimension;
   }

   public Random getRandom() {
      return this.rand;
   }

   public boolean hasBlockState(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
      return p_217375_2_.test(this.getBlockState(p_217375_1_));
   }

   public abstract RecipeManager getRecipeManager();

   public abstract NetworkTagManager getTags();

   public BlockPos func_217383_a(int p_217383_1_, int p_217383_2_, int p_217383_3_, int p_217383_4_) {
      this.updateLCG = this.updateLCG * 3 + 1013904223;
      int i = this.updateLCG >> 2;
      return new BlockPos(p_217383_1_ + (i & 15), p_217383_2_ + (i >> 16 & p_217383_4_), p_217383_3_ + (i >> 8 & 15));
   }

   public boolean isSaveDisabled() {
      return false;
   }

   public IProfiler getProfiler() {
      return this.profiler;
   }

   public BlockPos getHeight(Heightmap.Type heightmapType, BlockPos pos) {
      return new BlockPos(pos.getX(), this.getHeight(heightmapType, pos.getX(), pos.getZ()), pos.getZ());
   }

   private double maxEntityRadius = 2.0D;
   @Override
   public double getMaxEntityRadius() {
      return maxEntityRadius;
   }
   @Override
   public double increaseMaxEntityRadius(double value) {
      if (value > maxEntityRadius)
         maxEntityRadius = value;
      return maxEntityRadius;
   }
}
