package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@OnlyIn(Dist.CLIENT)
public class RenderChunk implements net.minecraftforge.client.extensions.IForgeRenderChunk {
   public volatile World world;
   public final WorldRenderer renderGlobal;
   public static int renderChunksUpdated;
   public CompiledChunk compiledChunk = CompiledChunk.DUMMY;
   public final ReentrantLock lockCompileTask = new ReentrantLock();
   public final ReentrantLock lockCompiledChunk = new ReentrantLock();
   public ChunkRenderTask compileTask;
   public final Set<TileEntity> setTileEntities = Sets.newHashSet();
   public final FloatBuffer modelviewMatrix = GLAllocation.createDirectFloatBuffer(16);
   public final VertexBuffer[] vertexBuffers = new VertexBuffer[BlockRenderLayer.values().length];
   public AxisAlignedBB boundingBox;
   public int frameIndex = -1;
   public boolean needsUpdate = true;
   public final BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos(-1, -1, -1);
   public final BlockPos.MutableBlockPos[] mapEnumFacing = Util.make(new BlockPos.MutableBlockPos[6], (p_205125_0_) -> {
      for(int j = 0; j < p_205125_0_.length; ++j) {
         p_205125_0_[j] = new BlockPos.MutableBlockPos();
      }

   });
   public boolean needsImmediateUpdate;

   public RenderChunk(World p_i49841_1_, WorldRenderer p_i49841_2_) {
      this.world = p_i49841_1_;
      this.renderGlobal = p_i49841_2_;
      if (OpenGlHelper.useVbo()) {
         for(int i = 0; i < BlockRenderLayer.values().length; ++i) {
            this.vertexBuffers[i] = new VertexBuffer(DefaultVertexFormats.BLOCK);
         }
      }

   }

   public boolean setFrameIndex(int frameIndexIn) {
      if (this.frameIndex == frameIndexIn) {
         return false;
      } else {
         this.frameIndex = frameIndexIn;
         return true;
      }
   }

   public VertexBuffer getVertexBufferByLayer(int layer) {
      return this.vertexBuffers[layer];
   }

   /**
    * Sets the RenderChunk base position
    */
   public void setPosition(int x, int y, int z) {
      if (x != this.position.getX() || y != this.position.getY() || z != this.position.getZ()) {
         this.stopCompileTask();
         this.position.setPos(x, y, z);
         this.boundingBox = new AxisAlignedBB((double)x, (double)y, (double)z, (double)(x + 16), (double)(y + 16), (double)(z + 16));

         for(EnumFacing enumfacing : EnumFacing.values()) {
            this.mapEnumFacing[enumfacing.ordinal()].setPos(this.position).move(enumfacing, 16);
         }

         this.initModelviewMatrix();
      }
   }

   public void resortTransparency(float x, float y, float z, ChunkRenderTask generator) {
      CompiledChunk compiledchunk = generator.getCompiledChunk();
      if (compiledchunk.getState() != null && !compiledchunk.isLayerEmpty(BlockRenderLayer.TRANSLUCENT)) {
         this.preRenderBlocks(generator.getRegionRenderCacheBuilder().getBuilder(BlockRenderLayer.TRANSLUCENT), this.position);
         generator.getRegionRenderCacheBuilder().getBuilder(BlockRenderLayer.TRANSLUCENT).setVertexState(compiledchunk.getState());
         this.postRenderBlocks(BlockRenderLayer.TRANSLUCENT, x, y, z, generator.getRegionRenderCacheBuilder().getBuilder(BlockRenderLayer.TRANSLUCENT), compiledchunk);
      }
   }

   public void rebuildChunk(float x, float y, float z, ChunkRenderTask generator) {
      CompiledChunk compiledchunk = new CompiledChunk();
      int i = 1;
      BlockPos blockpos = this.position.toImmutable();
      BlockPos blockpos1 = blockpos.add(15, 15, 15);
      World world = this.world;
      if (world != null) {
         generator.getLock().lock();

         try {
            if (generator.getStatus() != ChunkRenderTask.Status.COMPILING) {
               return;
            }

            generator.setCompiledChunk(compiledchunk);
         } finally {
            generator.getLock().unlock();
         }

         RenderChunkCache lvt_10_1_ = createRegionRenderCache(world, blockpos.add(-1, -1, -1), blockpos.add(16, 16, 16), 1);
         net.minecraftforge.client.MinecraftForgeClient.onRebuildChunk(this.world, this.position, lvt_10_1_);
         VisGraph lvt_11_1_ = new VisGraph();
         HashSet lvt_12_1_ = Sets.newHashSet();
         if (lvt_10_1_ != null) {
            ++renderChunksUpdated;
            boolean[] aboolean = new boolean[BlockRenderLayer.values().length];
            BlockModelRenderer.enableCache();
            Random random = new Random();
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

            // NoCubes Start
            io.github.cadiboo.nocubes.hooks.Hooks.preIteration(this, x, y, z, generator, compiledchunk, blockpos, blockpos1, world, lvt_10_1_, lvt_11_1_, lvt_12_1_, aboolean, random, blockrendererdispatcher);
            // NoCubes End
            for(BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(blockpos, blockpos1)) {
               IBlockState iblockstate = lvt_10_1_.getBlockState(blockpos$mutableblockpos);
               Block block = iblockstate.getBlock();
               if (iblockstate.isOpaqueCube(lvt_10_1_, blockpos$mutableblockpos)) {
                  lvt_11_1_.setOpaqueCube(blockpos$mutableblockpos);
               }

               if (iblockstate.hasTileEntity()) {
                  TileEntity tileentity = lvt_10_1_.getTileEntity(blockpos$mutableblockpos, Chunk.EnumCreateEntityType.CHECK);
                  if (tileentity != null) {
                     TileEntityRenderer<TileEntity> tileentityrenderer = TileEntityRendererDispatcher.instance.getRenderer(tileentity);
                     if (tileentityrenderer != null) {
                        if (tileentityrenderer.isGlobalRenderer(tileentity)) {
                           lvt_12_1_.add(tileentity);
                        }
                        else compiledchunk.addTileEntity(tileentity); // FORGE: Fix MC-112730
                     }
                  }
               }

               IFluidState ifluidstate = lvt_10_1_.getFluidState(blockpos$mutableblockpos);
               net.minecraftforge.client.model.data.IModelData modelData = generator.getModelData(blockpos$mutableblockpos);
               for(BlockRenderLayer blockrenderlayer1 : BlockRenderLayer.values()) {
                   net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockrenderlayer1);
               if (!ifluidstate.isEmpty() && ifluidstate.canRenderInLayer(blockrenderlayer1)) {
                  int j = blockrenderlayer1.ordinal();
                  BufferBuilder bufferbuilder = generator.getRegionRenderCacheBuilder().getBuilder(j);
                  if (!compiledchunk.isLayerStarted(blockrenderlayer1)) {
                     compiledchunk.setLayerStarted(blockrenderlayer1);
                     this.preRenderBlocks(bufferbuilder, blockpos);
                  }

                  aboolean[j] |= blockrendererdispatcher.renderFluid(blockpos$mutableblockpos, lvt_10_1_, bufferbuilder, ifluidstate);
               }

               // NoCubes Start
               if (!io.github.cadiboo.nocubes.config.Config.renderSmoothTerrain || !iblockstate.nocubes_isTerrainSmoothable())
               if (!io.github.cadiboo.nocubes.config.Config.renderSmoothLeaves || !iblockstate.nocubes_isLeavesSmoothable())
               // NoCubes End
               if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && iblockstate.canRenderInLayer(blockrenderlayer1)) {
                  int k = blockrenderlayer1.ordinal();
                  BufferBuilder bufferbuilder1 = generator.getRegionRenderCacheBuilder().getBuilder(k);
                  if (!compiledchunk.isLayerStarted(blockrenderlayer1)) {
                     compiledchunk.setLayerStarted(blockrenderlayer1);
                     this.preRenderBlocks(bufferbuilder1, blockpos);
                  }

                  aboolean[k] |= blockrendererdispatcher.renderBlock(iblockstate, blockpos$mutableblockpos, lvt_10_1_, bufferbuilder1, random, modelData);
               }
               }
               net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
            }

            for(BlockRenderLayer blockrenderlayer : BlockRenderLayer.values()) {
               if (aboolean[blockrenderlayer.ordinal()]) {
                  compiledchunk.setLayerUsed(blockrenderlayer);
               }

               if (compiledchunk.isLayerStarted(blockrenderlayer)) {
                  this.postRenderBlocks(blockrenderlayer, x, y, z, generator.getRegionRenderCacheBuilder().getBuilder(blockrenderlayer), compiledchunk);
               }
            }

            BlockModelRenderer.disableCache();
         }

         compiledchunk.setVisibility(lvt_11_1_.computeVisibility());
         this.lockCompileTask.lock();

         try {
            Set<TileEntity> set = Sets.newHashSet(lvt_12_1_);
            Set<TileEntity> set1 = Sets.newHashSet(this.setTileEntities);
            set.removeAll(this.setTileEntities);
            set1.removeAll(lvt_12_1_);
            this.setTileEntities.clear();
            this.setTileEntities.addAll(lvt_12_1_);
            this.renderGlobal.updateTileEntities(set1, set);
         } finally {
            this.lockCompileTask.unlock();
         }

      }
   }

   protected void finishCompileTask() {
      this.lockCompileTask.lock();

      try {
         if (this.compileTask != null && this.compileTask.getStatus() != ChunkRenderTask.Status.DONE) {
            this.compileTask.finish();
            this.compileTask = null;
         }
      } finally {
         this.lockCompileTask.unlock();
      }

   }

   public ReentrantLock getLockCompileTask() {
      return this.lockCompileTask;
   }

   public ChunkRenderTask makeCompileTaskChunk() {
      this.lockCompileTask.lock();

      ChunkRenderTask chunkrendertask;
      try {
         this.finishCompileTask();
         this.compileTask = new ChunkRenderTask(this, ChunkRenderTask.Type.REBUILD_CHUNK, this.getDistanceSq());
         chunkrendertask = this.compileTask;
      } finally {
         this.lockCompileTask.unlock();
      }

      return chunkrendertask;
   }

   @Nullable
   public ChunkRenderTask makeCompileTaskTransparency() {
      this.lockCompileTask.lock();

      ChunkRenderTask chunkrendertask;
      try {
         if (this.compileTask == null || this.compileTask.getStatus() != ChunkRenderTask.Status.PENDING) {
            if (this.compileTask != null && this.compileTask.getStatus() != ChunkRenderTask.Status.DONE) {
               this.compileTask.finish();
               this.compileTask = null;
            }

            this.compileTask = new ChunkRenderTask(this, ChunkRenderTask.Type.RESORT_TRANSPARENCY, this.getDistanceSq());
            this.compileTask.setCompiledChunk(this.compiledChunk);
            chunkrendertask = this.compileTask;
            return chunkrendertask;
         }

         chunkrendertask = null;
      } finally {
         this.lockCompileTask.unlock();
      }

      return chunkrendertask;
   }

   protected double getDistanceSq() {
      EntityPlayerSP entityplayersp = Minecraft.getInstance().player;
      double d0 = this.boundingBox.minX + 8.0D - entityplayersp.posX;
      double d1 = this.boundingBox.minY + 8.0D - entityplayersp.posY;
      double d2 = this.boundingBox.minZ + 8.0D - entityplayersp.posZ;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public void preRenderBlocks(BufferBuilder bufferBuilderIn, BlockPos pos) {
      bufferBuilderIn.begin(7, DefaultVertexFormats.BLOCK);
      bufferBuilderIn.setTranslation((double)(-pos.getX()), (double)(-pos.getY()), (double)(-pos.getZ()));
   }

   public void postRenderBlocks(BlockRenderLayer layer, float x, float y, float z, BufferBuilder bufferBuilderIn, CompiledChunk compiledChunkIn) {
      if (layer == BlockRenderLayer.TRANSLUCENT && !compiledChunkIn.isLayerEmpty(layer)) {
         bufferBuilderIn.sortVertexData(x, y, z);
         compiledChunkIn.setState(bufferBuilderIn.getVertexState());
      }

      bufferBuilderIn.finishDrawing();
   }

   private void initModelviewMatrix() {
      GlStateManager.pushMatrix();
      GlStateManager.loadIdentity();
      float f = 1.000001F;
      GlStateManager.translatef(-8.0F, -8.0F, -8.0F);
      GlStateManager.scalef(1.000001F, 1.000001F, 1.000001F);
      GlStateManager.translatef(8.0F, 8.0F, 8.0F);
      GlStateManager.getFloatv(2982, this.modelviewMatrix);
      GlStateManager.popMatrix();
   }

   public void multModelviewMatrix() {
      GlStateManager.multMatrixf(this.modelviewMatrix);
   }

   public CompiledChunk getCompiledChunk() {
      return this.compiledChunk;
   }

   public void setCompiledChunk(CompiledChunk compiledChunkIn) {
      this.lockCompiledChunk.lock();

      try {
         this.compiledChunk = compiledChunkIn;
      } finally {
         this.lockCompiledChunk.unlock();
      }

   }

   public void stopCompileTask() {
      this.finishCompileTask();
      this.compiledChunk = CompiledChunk.DUMMY;
   }

   public void deleteGlResources() {
      this.stopCompileTask();
      this.world = null;

      for(int i = 0; i < BlockRenderLayer.values().length; ++i) {
         if (this.vertexBuffers[i] != null) {
            this.vertexBuffers[i].deleteGlBuffers();
         }
      }

   }

   public BlockPos getPosition() {
      return this.position;
   }

   public void setNeedsUpdate(boolean immediate) {
      if (this.needsUpdate) {
         immediate |= this.needsImmediateUpdate;
      }

      this.needsUpdate = true;
      this.needsImmediateUpdate = immediate;
   }

   public void clearNeedsUpdate() {
      this.needsUpdate = false;
      this.needsImmediateUpdate = false;
   }

   public boolean needsUpdate() {
      return this.needsUpdate;
   }

   public boolean needsImmediateUpdate() {
      return this.needsUpdate && this.needsImmediateUpdate;
   }

   public BlockPos getBlockPosOffset16(EnumFacing facing) {
      return this.mapEnumFacing[facing.ordinal()];
   }

   public World getWorld() {
      return this.world;
   }
}
