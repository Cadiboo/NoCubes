package net.optifine.override;

//import bxo.a;
//import java.util.Arrays;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
//import net.optifine.Config;
//import net.optifine.DynamicLights;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.optifine.render.RenderEnv;

import javax.annotation.Nullable;
//import net.optifine.util.ArrayCache;

@SuppressWarnings("all")
public class ChunkCacheOF implements IEnviromentBlockReader {
//public class ChunkCacheOF implements bgu {
   public final ChunkRenderCache chunkCache;
//   public final dnm chunkCache;
//   public final int posX;
//   public final int posY;
//   public final int posZ;
//   public final int sizeX;
//   public final int sizeY;
//   public final int sizeZ;
//   public final int sizeXY;
   public int[] combinedLights;
//   public bvo[] blockStates;
//   public final int arraySize;
//   public final boolean dynamicLights = Config.isDynamicLights();
   public RenderEnv renderEnv;
//   public static final ArrayCache cacheCombinedLights;
//   public static final ArrayCache cacheBlockStates;

   public ChunkCacheOF(ChunkRenderCache chunkCache, BlockPos posFromIn, BlockPos posToIn, int subIn) {
//   public ChunkCacheOF(dnm chunkCache, ev posFromIn, ev posToIn, int subIn) {
      this.chunkCache = chunkCache;
//      int minChunkX = posFromIn.o() - subIn >> 4;
//      int minChunkY = posFromIn.p() - subIn >> 4;
//      int minChunkZ = posFromIn.q() - subIn >> 4;
//      int maxChunkX = posToIn.o() + subIn >> 4;
//      int maxChunkY = posToIn.p() + subIn >> 4;
//      int maxChunkZ = posToIn.q() + subIn >> 4;
//      this.sizeX = maxChunkX - minChunkX + 1 << 4;
//      this.sizeY = maxChunkY - minChunkY + 1 << 4;
//      this.sizeZ = maxChunkZ - minChunkZ + 1 << 4;
//      this.sizeXY = this.sizeX * this.sizeY;
//      this.arraySize = this.sizeX * this.sizeY * this.sizeZ;
//      this.posX = minChunkX << 4;
//      this.posY = minChunkY << 4;
//      this.posZ = minChunkZ << 4;
   }

//   public int getPositionIndex(ev pos) {
//      int dx = pos.o() - this.posX;
//      if (dx >= 0 && dx < this.sizeX) {
//         int dy = pos.p() - this.posY;
//         if (dy >= 0 && dy < this.sizeY) {
//            int dz = pos.q() - this.posZ;
//            return dz >= 0 && dz < this.sizeZ ? dz * this.sizeXY + dy * this.sizeX + dx : -1;
//         } else {
//            return -1;
//         }
//      } else {
//         return -1;
//      }
//   }
//
//   public int b(ev pos, int lightValue) {
//      int index = this.getPositionIndex(pos);
//      if (index >= 0 && index < this.arraySize && this.combinedLights != null) {
//         int light = this.combinedLights[index];
//         if (light == -1) {
//            light = this.getCombinedLightRaw(pos, lightValue);
//            this.combinedLights[index] = light;
//         }
//
//         return light;
//      } else {
//         return this.getCombinedLightRaw(pos, lightValue);
//      }
//   }
//
//   public int getCombinedLightRaw(ev pos, int lightValue) {
//      int light = this.chunkCache.b(pos, lightValue);
//      if (this.dynamicLights && !this.e_(pos).g(this, pos)) {
//         light = DynamicLights.getCombinedLight(pos, light);
//      }
//
//      return light;
//   }
//
//   public bvo e_(ev pos) {
//      int index = this.getPositionIndex(pos);
//      if (index >= 0 && index < this.arraySize && this.blockStates != null) {
//         bvo iblockstate = this.blockStates[index];
//         if (iblockstate == null) {
//            iblockstate = this.chunkCache.e_(pos);
//            this.blockStates[index] = iblockstate;
//         }
//
//         return iblockstate;
//      } else {
//         return this.chunkCache.e_(pos);
//      }
//   }
//
//   public void renderStart() {
//      if (this.combinedLights == null) {
//         this.combinedLights = (int[])((int[])cacheCombinedLights.allocate(this.arraySize));
//      }
//
//      Arrays.fill(this.combinedLights, -1);
//      if (this.blockStates == null) {
//         this.blockStates = (bvo[])((bvo[])cacheBlockStates.allocate(this.arraySize));
//      }
//
//      Arrays.fill(this.blockStates, (Object)null);
//   }
//
//   public void renderFinish() {
//      cacheCombinedLights.free(this.combinedLights);
//      this.combinedLights = null;
//      cacheBlockStates.free(this.blockStates);
//      this.blockStates = null;
//   }
//
//   public bij c(ev pos) {
//      return this.chunkCache.c(pos);
//   }
//
//   public btr d(ev pos) {
//      return this.chunkCache.a(pos, a.c);
//   }
//
//   public btr getTileEntity(ev pos, a type) {
//      return this.chunkCache.a(pos, type);
//   }
//
//   public int a(bhv type, ev pos) {
//      return this.chunkCache.a(type, pos);
//   }
//
//   public boolean f(ev pos) {
//      return this.chunkCache.f(pos);
//   }
//
//   public clf b(ev pos) {
//      return this.e_(pos).p();
//   }
//
//   public RenderEnv getRenderEnv() {
//      return this.renderEnv;
//   }
//
//   public void setRenderEnv(RenderEnv renderEnv) {
//      this.renderEnv = renderEnv;
//   }
//
//   static {
//      cacheCombinedLights = new ArrayCache(Integer.TYPE, 16);
//      cacheBlockStates = new ArrayCache(bvo.class, 16);
//   }

   // Start stubbed out methods to get this to compile



   @Override
   public Biome getBiome(final BlockPos pos) {
      return null;
   }

   @Override
   public int getLightFor(final LightType type, final BlockPos pos) {
      return 0;
   }

   @Nullable
   @Override
   public TileEntity getTileEntity(final BlockPos pos) {
      return null;
   }

   @Override
   public BlockState getBlockState(final BlockPos pos) {
      return null;
   }

   @Override
   public IFluidState getFluidState(final BlockPos pos) {
      return null;
   }


}
