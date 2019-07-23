package net.minecraft.client.world;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.EntityTickableSound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.particle.FireworkParticle;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.EmptyTickList;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.ITickList;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientWorld extends World {
   private final List<Entity> globalEntities = Lists.newArrayList();
   private final Int2ObjectMap<Entity> entitiesById = new Int2ObjectOpenHashMap<>();
   private final ClientPlayNetHandler connection;
   private final WorldRenderer worldRenderer;
   private final Minecraft mc = Minecraft.getInstance();
   private final List<AbstractClientPlayerEntity> field_217431_w = Lists.newArrayList();
   private int ambienceTicks = this.rand.nextInt(12000);
   private Scoreboard scoreboard = new Scoreboard();
   private final Map<String, MapData> field_217432_z = Maps.newHashMap();

   public ClientWorld(ClientPlayNetHandler p_i51056_1_, WorldSettings p_i51056_2_, DimensionType dimType, int p_i51056_4_, IProfiler p_i51056_5_, WorldRenderer p_i51056_6_) {
      super(new WorldInfo(p_i51056_2_, "MpServer"), dimType, (p_217422_1_, p_217422_2_) -> {
         return new ClientChunkProvider((ClientWorld)p_217422_1_, p_i51056_4_);
      }, p_i51056_5_, true);
      this.connection = p_i51056_1_;
      this.worldRenderer = p_i51056_6_;
      this.setSpawnPoint(new BlockPos(8, 64, 8));
      this.calculateInitialSkylight();
      this.calculateInitialWeather();
      this.gatherCapabilities(dimension.initCapabilities());
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.WorldEvent.Load(this));
   }

   /**
    * Runs a single tick for the world
    */
   public void tick(BooleanSupplier hasTimeLeft) {
      this.getWorldBorder().tick();
      this.advanceTime();
      this.getProfiler().startSection("blocks");
      this.chunkProvider.tick(hasTimeLeft);
      this.func_217426_j();
      this.getProfiler().endSection();
   }

   public Iterable<Entity> getAllEntities() {
      return Iterables.concat(this.entitiesById.values(), this.globalEntities);
   }

   public void tickEntities() {
      IProfiler iprofiler = this.getProfiler();
      iprofiler.startSection("entities");
      iprofiler.startSection("global");

      for(int i = 0; i < this.globalEntities.size(); ++i) {
         Entity entity = this.globalEntities.get(i);
         this.func_217390_a((p_217415_0_) -> {
            ++p_217415_0_.ticksExisted;
            if (p_217415_0_.canUpdate())
            p_217415_0_.tick();
         }, entity);
         if (entity.removed) {
            this.globalEntities.remove(i--);
         }
      }

      iprofiler.endStartSection("regular");
      ObjectIterator<Entry<Entity>> objectiterator = this.entitiesById.int2ObjectEntrySet().iterator();

      while(objectiterator.hasNext()) {
         Entry<Entity> entry = objectiterator.next();
         Entity entity1 = entry.getValue();
         if (!entity1.isPassenger()) {
            iprofiler.startSection("tick");
            if (!entity1.removed) {
               this.func_217390_a(this::func_217418_a, entity1);
            }

            iprofiler.endSection();
            iprofiler.startSection("remove");
            if (entity1.removed) {
               objectiterator.remove();
               this.removeEntity(entity1);
            }

            iprofiler.endSection();
         }
      }

      iprofiler.endSection();
      this.func_217391_K();
      iprofiler.endSection();
   }

   public void func_217418_a(Entity p_217418_1_) {
      if (p_217418_1_ instanceof PlayerEntity || this.getChunkProvider().isChunkLoaded(p_217418_1_)) {
         p_217418_1_.lastTickPosX = p_217418_1_.posX;
         p_217418_1_.lastTickPosY = p_217418_1_.posY;
         p_217418_1_.lastTickPosZ = p_217418_1_.posZ;
         p_217418_1_.prevRotationYaw = p_217418_1_.rotationYaw;
         p_217418_1_.prevRotationPitch = p_217418_1_.rotationPitch;
         if (p_217418_1_.addedToChunk || p_217418_1_.isSpectator()) {
            ++p_217418_1_.ticksExisted;
            this.getProfiler().startSection(() -> {
               return Registry.ENTITY_TYPE.getKey(p_217418_1_.getType()).toString();
            });
            if (p_217418_1_.canUpdate())
            p_217418_1_.tick();
            this.getProfiler().endSection();
         }

         this.func_217423_b(p_217418_1_);
         if (p_217418_1_.addedToChunk) {
            for(Entity entity : p_217418_1_.getPassengers()) {
               this.func_217420_a(p_217418_1_, entity);
            }
         }

      }
   }

   public void func_217420_a(Entity p_217420_1_, Entity p_217420_2_) {
      if (!p_217420_2_.removed && p_217420_2_.getRidingEntity() == p_217420_1_) {
         if (p_217420_2_ instanceof PlayerEntity || this.getChunkProvider().isChunkLoaded(p_217420_2_)) {
            p_217420_2_.lastTickPosX = p_217420_2_.posX;
            p_217420_2_.lastTickPosY = p_217420_2_.posY;
            p_217420_2_.lastTickPosZ = p_217420_2_.posZ;
            p_217420_2_.prevRotationYaw = p_217420_2_.rotationYaw;
            p_217420_2_.prevRotationPitch = p_217420_2_.rotationPitch;
            if (p_217420_2_.addedToChunk) {
               ++p_217420_2_.ticksExisted;
               p_217420_2_.updateRidden();
            }

            this.func_217423_b(p_217420_2_);
            if (p_217420_2_.addedToChunk) {
               for(Entity entity : p_217420_2_.getPassengers()) {
                  this.func_217420_a(p_217420_2_, entity);
               }
            }

         }
      } else {
         p_217420_2_.stopRiding();
      }
   }

   public void func_217423_b(Entity p_217423_1_) {
      this.getProfiler().startSection("chunkCheck");
      int i = MathHelper.floor(p_217423_1_.posX / 16.0D);
      int j = MathHelper.floor(p_217423_1_.posY / 16.0D);
      int k = MathHelper.floor(p_217423_1_.posZ / 16.0D);
      if (!p_217423_1_.addedToChunk || p_217423_1_.chunkCoordX != i || p_217423_1_.chunkCoordY != j || p_217423_1_.chunkCoordZ != k) {
         if (p_217423_1_.addedToChunk && this.chunkExists(p_217423_1_.chunkCoordX, p_217423_1_.chunkCoordZ)) {
            this.getChunk(p_217423_1_.chunkCoordX, p_217423_1_.chunkCoordZ).removeEntityAtIndex(p_217423_1_, p_217423_1_.chunkCoordY);
         }

         if (!p_217423_1_.setPositionNonDirty() && !this.chunkExists(i, k)) {
            p_217423_1_.addedToChunk = false;
         } else {
            this.getChunk(i, k).addEntity(p_217423_1_);
         }
      }

      this.getProfiler().endSection();
   }

   public void onChunkUnloaded(Chunk p_217409_1_) {
      this.tileEntitiesToBeRemoved.addAll(p_217409_1_.getTileEntityMap().values());
      this.chunkProvider.getLightManager().func_215571_a(p_217409_1_.getPos(), false);
   }

   public boolean chunkExists(int chunkX, int chunkZ) {
      return true;
   }

   private void func_217426_j() {
      if (this.mc.player != null) {
         if (this.ambienceTicks > 0) {
            --this.ambienceTicks;
         } else {
            BlockPos blockpos = new BlockPos(this.mc.player);
            BlockPos blockpos1 = blockpos.add(4 * (this.rand.nextInt(3) - 1), 4 * (this.rand.nextInt(3) - 1), 4 * (this.rand.nextInt(3) - 1));
            double d0 = blockpos.distanceSq(blockpos1);
            if (d0 >= 4.0D && d0 <= 256.0D) {
               BlockState blockstate = this.getBlockState(blockpos1);
               if (blockstate.isAir() && this.getLightSubtracted(blockpos1, 0) <= this.rand.nextInt(8) && this.getLightFor(LightType.SKY, blockpos1) <= 0) {
                  this.playSound((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.5D, (double)blockpos1.getZ() + 0.5D, SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT, 0.7F, 0.8F + this.rand.nextFloat() * 0.2F, false);
                  this.ambienceTicks = this.rand.nextInt(12000) + 6000;
               }
            }

         }
      }
   }

   public int func_217425_f() {
      return this.entitiesById.size();
   }

   public void addLightning(LightningBoltEntity p_217410_1_) {
      this.globalEntities.add(p_217410_1_);
   }

   public void addPlayer(int p_217408_1_, AbstractClientPlayerEntity p_217408_2_) {
      this.addEntityImpl(p_217408_1_, p_217408_2_);
      this.field_217431_w.add(p_217408_2_);
   }

   public void addEntity(int p_217411_1_, Entity p_217411_2_) {
      this.addEntityImpl(p_217411_1_, p_217411_2_);
   }

   private void addEntityImpl(int p_217424_1_, Entity p_217424_2_) {
      if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(p_217424_2_, this))) return;
      this.removeEntityFromWorld(p_217424_1_);
      this.entitiesById.put(p_217424_1_, p_217424_2_);
      this.getChunkProvider().getChunk(MathHelper.floor(p_217424_2_.posX / 16.0D), MathHelper.floor(p_217424_2_.posZ / 16.0D), ChunkStatus.FULL, true).addEntity(p_217424_2_);
      p_217424_2_.onAddedToWorld();
   }

   public void removeEntityFromWorld(int eid) {
      Entity entity = this.entitiesById.remove(eid);
      if (entity != null) {
         entity.remove();
         this.removeEntity(entity);
      }

   }

   private void removeEntity(Entity p_217414_1_) {
      p_217414_1_.detach();
      if (p_217414_1_.addedToChunk) {
         this.getChunk(p_217414_1_.chunkCoordX, p_217414_1_.chunkCoordZ).removeEntity(p_217414_1_);
      }

      this.field_217431_w.remove(p_217414_1_);
      p_217414_1_.onRemovedFromWorld();
   }

   public void addEntitiesToChunk(Chunk p_217417_1_) {
      for(Entry<Entity> entry : this.entitiesById.int2ObjectEntrySet()) {
         Entity entity = entry.getValue();
         int i = MathHelper.floor(entity.posX / 16.0D);
         int j = MathHelper.floor(entity.posZ / 16.0D);
         if (i == p_217417_1_.getPos().x && j == p_217417_1_.getPos().z) {
            p_217417_1_.addEntity(entity);
         }
      }

   }

   /**
    * Returns the Entity with the given ID, or null if it doesn't exist in this World.
    */
   @Nullable
   public Entity getEntityByID(int id) {
      return this.entitiesById.get(id);
   }

   public void invalidateRegionAndSetBlock(BlockPos pos, BlockState state) {
      this.setBlockState(pos, state, 19);
   }

   /**
    * If on MP, sends a quitting packet.
    */
   public void sendQuittingDisconnectingPacket() {
      this.connection.getNetworkManager().closeChannel(new TranslationTextComponent("multiplayer.status.quitting"));
   }

   public void animateTick(int posX, int posY, int posZ) {
      int i = 32;
      Random random = new Random();
      ItemStack itemstack = this.mc.player.getHeldItemMainhand();
      boolean flag = this.mc.playerController.getCurrentGameType() == GameType.CREATIVE && !itemstack.isEmpty() && itemstack.getItem() == Blocks.BARRIER.asItem();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j = 0; j < 667; ++j) {
         this.animateTick(posX, posY, posZ, 16, random, flag, blockpos$mutableblockpos);
         this.animateTick(posX, posY, posZ, 32, random, flag, blockpos$mutableblockpos);
      }

   }

   public void animateTick(int x, int y, int z, int offset, Random random, boolean holdingBarrier, BlockPos.MutableBlockPos pos) {
      int i = x + this.rand.nextInt(offset) - this.rand.nextInt(offset);
      int j = y + this.rand.nextInt(offset) - this.rand.nextInt(offset);
      int k = z + this.rand.nextInt(offset) - this.rand.nextInt(offset);
      pos.setPos(i, j, k);
      BlockState blockstate = this.getBlockState(pos);
      blockstate.getBlock().animateTick(blockstate, this, pos, random);
      IFluidState ifluidstate = this.getFluidState(pos);
      if (!ifluidstate.isEmpty()) {
         ifluidstate.animateTick(this, pos, random);
         IParticleData iparticledata = ifluidstate.getDripParticleData();
         if (iparticledata != null && this.rand.nextInt(10) == 0) {
            boolean flag = blockstate.func_224755_d(this, pos, Direction.DOWN);
            BlockPos blockpos = pos.down();
            this.spawnFluidParticle(blockpos, this.getBlockState(blockpos), iparticledata, flag);
         }
      }

      if (holdingBarrier && blockstate.getBlock() == Blocks.BARRIER) {
         this.addParticle(ParticleTypes.BARRIER, (double)((float)i + 0.5F), (double)((float)j + 0.5F), (double)((float)k + 0.5F), 0.0D, 0.0D, 0.0D);
      }

   }

   private void spawnFluidParticle(BlockPos blockPosIn, BlockState blockStateIn, IParticleData particleDataIn, boolean shapeDownSolid) {
      if (blockStateIn.getFluidState().isEmpty()) {
         VoxelShape voxelshape = blockStateIn.getCollisionShape(this, blockPosIn);
         double d0 = voxelshape.getEnd(Direction.Axis.Y);
         if (d0 < 1.0D) {
            if (shapeDownSolid) {
               this.spawnParticle((double)blockPosIn.getX(), (double)(blockPosIn.getX() + 1), (double)blockPosIn.getZ(), (double)(blockPosIn.getZ() + 1), (double)(blockPosIn.getY() + 1) - 0.05D, particleDataIn);
            }
         } else if (!blockStateIn.isIn(BlockTags.IMPERMEABLE)) {
            double d1 = voxelshape.getStart(Direction.Axis.Y);
            if (d1 > 0.0D) {
               this.spawnParticle(blockPosIn, particleDataIn, voxelshape, (double)blockPosIn.getY() + d1 - 0.05D);
            } else {
               BlockPos blockpos = blockPosIn.down();
               BlockState blockstate = this.getBlockState(blockpos);
               VoxelShape voxelshape1 = blockstate.getCollisionShape(this, blockpos);
               double d2 = voxelshape1.getEnd(Direction.Axis.Y);
               if (d2 < 1.0D && blockstate.getFluidState().isEmpty()) {
                  this.spawnParticle(blockPosIn, particleDataIn, voxelshape, (double)blockPosIn.getY() - 0.05D);
               }
            }
         }

      }
   }

   private void spawnParticle(BlockPos posIn, IParticleData particleDataIn, VoxelShape voxelShapeIn, double p_211835_4_) {
      this.spawnParticle((double)posIn.getX() + voxelShapeIn.getStart(Direction.Axis.X), (double)posIn.getX() + voxelShapeIn.getEnd(Direction.Axis.X), (double)posIn.getZ() + voxelShapeIn.getStart(Direction.Axis.Z), (double)posIn.getZ() + voxelShapeIn.getEnd(Direction.Axis.Z), p_211835_4_, particleDataIn);
   }

   private void spawnParticle(double p_211834_1_, double p_211834_3_, double p_211834_5_, double p_211834_7_, double p_211834_9_, IParticleData p_211834_11_) {
      this.addParticle(p_211834_11_, MathHelper.lerp(this.rand.nextDouble(), p_211834_1_, p_211834_3_), p_211834_9_, MathHelper.lerp(this.rand.nextDouble(), p_211834_5_, p_211834_7_), 0.0D, 0.0D, 0.0D);
   }

   /**
    * also releases skins.
    */
   public void removeAllEntities() {
      ObjectIterator<Entry<Entity>> objectiterator = this.entitiesById.int2ObjectEntrySet().iterator();

      while(objectiterator.hasNext()) {
         Entry<Entity> entry = objectiterator.next();
         Entity entity = entry.getValue();
         if (entity.removed) {
            objectiterator.remove();
            this.removeEntity(entity);
         }
      }

   }

   /**
    * Adds some basic stats of the world to the given crash report.
    */
   public CrashReportCategory fillCrashReport(CrashReport report) {
      CrashReportCategory crashreportcategory = super.fillCrashReport(report);
      crashreportcategory.addDetail("Server brand", () -> {
         return this.mc.player.getServerBrand();
      });
      crashreportcategory.addDetail("Server type", () -> {
         return this.mc.getIntegratedServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server";
      });
      return crashreportcategory;
   }

   public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
      net.minecraftforge.event.entity.PlaySoundAtEntityEvent event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtEntity(player, soundIn, category, volume, pitch);
      if (event.isCanceled() || event.getSound() == null) return;
      soundIn = event.getSound();
      category = event.getCategory();
      volume = event.getVolume();
      if (player == this.mc.player) {
         this.playSound(x, y, z, soundIn, category, volume, pitch, false);
      }

   }

   public void playMovingSound(@Nullable PlayerEntity p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_, SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_) {
      net.minecraftforge.event.entity.PlaySoundAtEntityEvent event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtEntity(p_217384_1_, p_217384_3_, p_217384_4_, p_217384_5_, p_217384_6_);
      if (event.isCanceled() || event.getSound() == null) return;
      p_217384_3_ = event.getSound();
      p_217384_4_ = event.getCategory();
      p_217384_5_ = event.getVolume();
      if (p_217384_1_ == this.mc.player) {
         this.mc.getSoundHandler().play(new EntityTickableSound(p_217384_3_, p_217384_4_, p_217384_2_));
      }

   }

   public void playSound(BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
      this.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, soundIn, category, volume, pitch, distanceDelay);
   }

   public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
      double d0 = this.mc.gameRenderer.getActiveRenderInfo().getProjectedView().squareDistanceTo(x, y, z);
      SimpleSound simplesound = new SimpleSound(soundIn, category, volume, pitch, (float)x, (float)y, (float)z);
      if (distanceDelay && d0 > 100.0D) {
         double d1 = Math.sqrt(d0) / 40.0D;
         this.mc.getSoundHandler().playDelayed(simplesound, (int)(d1 * 20.0D));
      } else {
         this.mc.getSoundHandler().play(simplesound);
      }

   }

   public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, @Nullable CompoundNBT compound) {
      this.mc.particles.addEffect(new FireworkParticle.Starter(this, x, y, z, motionX, motionY, motionZ, this.mc.particles, compound));
   }

   public void sendPacketToServer(IPacket<?> packetIn) {
      this.connection.sendPacket(packetIn);
   }

   public RecipeManager getRecipeManager() {
      return this.connection.getRecipeManager();
   }

   public void setScoreboard(Scoreboard scoreboardIn) {
      this.scoreboard = scoreboardIn;
   }

   /**
    * Sets the world time.
    */
   public void setDayTime(long time) {
      if (time < 0L) {
         time = -time;
         this.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, (MinecraftServer)null);
      } else {
         this.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, (MinecraftServer)null);
      }

      super.setDayTime(time);
   }

   public ITickList<Block> getPendingBlockTicks() {
      return EmptyTickList.get();
   }

   public ITickList<Fluid> getPendingFluidTicks() {
      return EmptyTickList.get();
   }

   public ClientChunkProvider getChunkProvider() {
      return (ClientChunkProvider)super.getChunkProvider();
   }

   @Nullable
   public MapData func_217406_a(String p_217406_1_) {
      return this.field_217432_z.get(p_217406_1_);
   }

   public void func_217399_a(MapData p_217399_1_) {
      this.field_217432_z.put(p_217399_1_.getName(), p_217399_1_);
   }

   public int getNextMapId() {
      return 0;
   }

   public Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   public NetworkTagManager getTags() {
      return this.connection.getTags();
   }

   /**
    * Flags are as in setBlockState
    */
   public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
      this.worldRenderer.notifyBlockUpdate(this, pos, oldState, newState, flags);
   }

   public void func_225319_b(BlockPos pos, BlockState newState, BlockState oldState) {
      // NoCubes Start
      io.github.cadiboo.nocubes.hooks.Hooks.markForRerender(this.mc, this.worldRenderer, pos, newState, oldState);
      // NoCubes End
//      this.worldRenderer.func_224746_a(pos, newState, oldState);
   }

   public void markSurroundingsForRerender(int sectionX, int sectionY, int sectionZ) {
      this.worldRenderer.markSurroundingsForRerender(sectionX, sectionY, sectionZ);
   }

   public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
      this.worldRenderer.sendBlockBreakProgress(breakerId, pos, progress);
   }

   public void playBroadcastSound(int id, BlockPos pos, int data) {
      this.worldRenderer.broadcastSound(id, pos, data);
   }

   public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {
      try {
         this.worldRenderer.playEvent(player, type, pos, data);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Playing level event");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Level event being played");
         crashreportcategory.addDetail("Block coordinates", CrashReportCategory.getCoordinateInfo(pos));
         crashreportcategory.addDetail("Event source", player);
         crashreportcategory.addDetail("Event type", type);
         crashreportcategory.addDetail("Event data", data);
         throw new ReportedException(crashreport);
      }
   }

   public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      this.worldRenderer.addParticle(particleData, particleData.getType().getAlwaysShow(), x, y, z, xSpeed, ySpeed, zSpeed);
   }

   public void addParticle(IParticleData particleData, boolean forceAlwaysRender, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      this.worldRenderer.addParticle(particleData, particleData.getType().getAlwaysShow() || forceAlwaysRender, x, y, z, xSpeed, ySpeed, zSpeed);
   }

   public void addOptionalParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      this.worldRenderer.addParticle(particleData, false, true, x, y, z, xSpeed, ySpeed, zSpeed);
   }

   public void func_217404_b(IParticleData p_217404_1_, boolean p_217404_2_, double p_217404_3_, double p_217404_5_, double p_217404_7_, double p_217404_9_, double p_217404_11_, double p_217404_13_) {
      this.worldRenderer.addParticle(p_217404_1_, p_217404_1_.getType().getAlwaysShow() || p_217404_2_, true, p_217404_3_, p_217404_5_, p_217404_7_, p_217404_9_, p_217404_11_, p_217404_13_);
   }

   public List<AbstractClientPlayerEntity> getPlayers() {
      return this.field_217431_w;
   }
}
