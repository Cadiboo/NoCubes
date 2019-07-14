package net.optifine.render;

//import dlk.b;
import it.unimi.dsi.fastutil.longs.Long2ByteLinkedOpenHashMap;
//import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
//import net.optifine.BlockPosM;
//import net.optifine.Config;
//import net.optifine.model.ListQuadsOverlay;

@SuppressWarnings("all")
public class RenderEnv {
//   public bvo blockState;
//   public ev blockPos;
   public int blockId = -1;
   public int metadata = -1;
   public int breakingAnimation = -1;
   public int smartLeaves = -1;
   public float[] quadBounds;
   public BitSet boundsFlags;
//   public b aoFace;
//   public BlockPosM colorizerBlockPosM;
   public boolean[] borderFlags;
   public boolean[] borderFlags2;
   public boolean[] borderFlags3;
//   public fa[] borderDirections;
   public List listQuadsCustomizer;
   public List listQuadsCtmMultipass;
//   public dll[] arrayQuadsCtm1;
//   public dll[] arrayQuadsCtm2;
//   public dll[] arrayQuadsCtm3;
//   public dll[] arrayQuadsCtm4;
//   public dkh regionRenderCacheBuilder;
//   public ListQuadsOverlay[] listsQuadsOverlay;
   public boolean overlaysRendered;
   public Long2ByteLinkedOpenHashMap renderSideMap;
   public static final int UNKNOWN = -1;
   public static final int FALSE = 0;
   public static final int TRUE = 1;

   public RenderEnv(BlockState blockState, BlockPos blockPos) {
//   public RenderEnv(bvo blockState, ev blockPos) {
//      this.quadBounds = new float[fa.n.length * 2];
//      this.boundsFlags = new BitSet(3);
//      this.aoFace = new b();
//      this.colorizerBlockPosM = null;
//      this.borderFlags = null;
//      this.borderFlags2 = null;
//      this.borderFlags3 = null;
//      this.borderDirections = null;
//      this.listQuadsCustomizer = new ArrayList();
//      this.listQuadsCtmMultipass = new ArrayList();
//      this.arrayQuadsCtm1 = new dll[1];
//      this.arrayQuadsCtm2 = new dll[2];
//      this.arrayQuadsCtm3 = new dll[3];
//      this.arrayQuadsCtm4 = new dll[4];
//      this.regionRenderCacheBuilder = null;
//      this.listsQuadsOverlay = new ListQuadsOverlay[bgx.values().length];
//      this.overlaysRendered = false;
//      this.renderSideMap = new Long2ByteLinkedOpenHashMap();
//      this.blockState = blockState;
//      this.blockPos = blockPos;
   }

   public void reset(BlockState blockStateIn, BlockPos blockPosIn) {
//   public void reset(bvo blockStateIn, ev blockPosIn) {
//      if (this.blockState != blockStateIn || this.blockPos != blockPosIn) {
//         this.blockState = blockStateIn;
//         this.blockPos = blockPosIn;
//         this.blockId = -1;
//         this.metadata = -1;
//         this.breakingAnimation = -1;
//         this.smartLeaves = -1;
//         this.boundsFlags.clear();
//      }
   }
//
//   public int getBlockId() {
//      if (this.blockId < 0) {
//         this.blockId = this.blockState.getBlockId();
//      }
//
//      return this.blockId;
//   }
//
//   public int getMetadata() {
//      if (this.metadata < 0) {
//         this.metadata = this.blockState.getMetadata();
//      }
//
//      return this.metadata;
//   }
//
//   public float[] getQuadBounds() {
//      return this.quadBounds;
//   }
//
//   public BitSet getBoundsFlags() {
//      return this.boundsFlags;
//   }
//
//   public b getAoFace() {
//      return this.aoFace;
//   }
//
//   public boolean isBreakingAnimation(List listQuads) {
//      if (this.breakingAnimation == -1 && listQuads.size() > 0) {
//         if (listQuads.get(0) instanceof dls) {
//            this.breakingAnimation = 1;
//         } else {
//            this.breakingAnimation = 0;
//         }
//      }
//
//      return this.breakingAnimation == 1;
//   }
//
//   public boolean isBreakingAnimation(dll quad) {
//      if (this.breakingAnimation < 0) {
//         if (quad instanceof dls) {
//            this.breakingAnimation = 1;
//         } else {
//            this.breakingAnimation = 0;
//         }
//      }
//
//      return this.breakingAnimation == 1;
//   }
//
//   public boolean isBreakingAnimation() {
//      return this.breakingAnimation == 1;
//   }
//
//   public bvo getBlockState() {
//      return this.blockState;
//   }
//
//   public BlockPosM getColorizerBlockPosM() {
//      if (this.colorizerBlockPosM == null) {
//         this.colorizerBlockPosM = new BlockPosM(0, 0, 0);
//      }
//
//      return this.colorizerBlockPosM;
//   }
//
//   public boolean[] getBorderFlags() {
//      if (this.borderFlags == null) {
//         this.borderFlags = new boolean[4];
//      }
//
//      return this.borderFlags;
//   }
//
//   public boolean[] getBorderFlags2() {
//      if (this.borderFlags2 == null) {
//         this.borderFlags2 = new boolean[4];
//      }
//
//      return this.borderFlags2;
//   }
//
//   public boolean[] getBorderFlags3() {
//      if (this.borderFlags3 == null) {
//         this.borderFlags3 = new boolean[4];
//      }
//
//      return this.borderFlags3;
//   }
//
//   public fa[] getBorderDirections() {
//      if (this.borderDirections == null) {
//         this.borderDirections = new fa[4];
//      }
//
//      return this.borderDirections;
//   }
//
//   public fa[] getBorderDirections(fa dir0, fa dir1, fa dir2, fa dir3) {
//      fa[] dirs = this.getBorderDirections();
//      dirs[0] = dir0;
//      dirs[1] = dir1;
//      dirs[2] = dir2;
//      dirs[3] = dir3;
//      return dirs;
//   }
//
//   public boolean isSmartLeaves() {
//      if (this.smartLeaves == -1) {
//         if (Config.isTreesSmart() && this.blockState.d() instanceof bpr) {
//            this.smartLeaves = 1;
//         } else {
//            this.smartLeaves = 0;
//         }
//      }
//
//      return this.smartLeaves == 1;
//   }
//
//   public List getListQuadsCustomizer() {
//      return this.listQuadsCustomizer;
//   }
//
//   public dll[] getArrayQuadsCtm(dll quad) {
//      this.arrayQuadsCtm1[0] = quad;
//      return this.arrayQuadsCtm1;
//   }
//
//   public dll[] getArrayQuadsCtm(dll quad0, dll quad1) {
//      this.arrayQuadsCtm2[0] = quad0;
//      this.arrayQuadsCtm2[1] = quad1;
//      return this.arrayQuadsCtm2;
//   }
//
//   public dll[] getArrayQuadsCtm(dll quad0, dll quad1, dll quad2) {
//      this.arrayQuadsCtm3[0] = quad0;
//      this.arrayQuadsCtm3[1] = quad1;
//      this.arrayQuadsCtm3[2] = quad2;
//      return this.arrayQuadsCtm3;
//   }
//
//   public dll[] getArrayQuadsCtm(dll quad0, dll quad1, dll quad2, dll quad3) {
//      this.arrayQuadsCtm4[0] = quad0;
//      this.arrayQuadsCtm4[1] = quad1;
//      this.arrayQuadsCtm4[2] = quad2;
//      this.arrayQuadsCtm4[3] = quad3;
//      return this.arrayQuadsCtm4;
//   }
//
//   public List getListQuadsCtmMultipass(dll[] quads) {
//      this.listQuadsCtmMultipass.clear();
//      if (quads != null) {
//         for(int i = 0; i < quads.length; ++i) {
//            dll quad = quads[i];
//            this.listQuadsCtmMultipass.add(quad);
//         }
//      }
//
//      return this.listQuadsCtmMultipass;
//   }
//
//   public dkh getRegionRenderCacheBuilder() {
//      return this.regionRenderCacheBuilder;
//   }
//
//   public void setRegionRenderCacheBuilder(dkh regionRenderCacheBuilder) {
//      this.regionRenderCacheBuilder = regionRenderCacheBuilder;
//   }
//
//   public ListQuadsOverlay getListQuadsOverlay(bgx layer) {
//      ListQuadsOverlay list = this.listsQuadsOverlay[layer.ordinal()];
//      if (list == null) {
//         list = new ListQuadsOverlay();
//         this.listsQuadsOverlay[layer.ordinal()] = list;
//      }
//
//      return list;
//   }
//
//   public boolean isOverlaysRendered() {
//      return this.overlaysRendered;
//   }
//
//   public void setOverlaysRendered(boolean overlaysRendered) {
//      this.overlaysRendered = overlaysRendered;
//   }
//
//   public Long2ByteLinkedOpenHashMap getRenderSideMap() {
//      return this.renderSideMap;
//   }
}
