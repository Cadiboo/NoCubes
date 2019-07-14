package net.optifine.model;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
//import net.optifine.BetterGrass;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.optifine.Config;
//import net.optifine.ConnectedTextures;
//import net.optifine.NaturalTextures;
//import net.optifine.SmartLeaves;
import net.optifine.render.RenderEnv;

@SuppressWarnings("all")
public class BlockModelCustomizer {
   public static final List NO_QUADS = ImmutableList.of();

   public static IBakedModel getRenderModel(IBakedModel modelIn, BlockState stateIn, RenderEnv renderEnv) {
//   public static dwb getRenderModel(dwb modelIn, bvo stateIn, RenderEnv renderEnv) {
//      if (renderEnv.isSmartLeaves()) {
//         modelIn = SmartLeaves.getLeavesModel(modelIn, stateIn);
//      }

      return modelIn;
   }

   public static List<BakedQuad> getRenderQuads(List quads, IEnviromentBlockReader worldIn, BlockState stateIn, BlockPos posIn, Direction enumfacing, BlockRenderLayer layer, long rand, RenderEnv renderEnv) {
//      if (enumfacing != null) {
//         if (renderEnv.isSmartLeaves() && SmartLeaves.isSameLeaves(worldIn.e_(posIn.a(enumfacing)), stateIn)) {
//            return NO_QUADS;
//         }
//
//         if (!renderEnv.isBreakingAnimation(quads) && Config.isBetterGrass()) {
//            quads = BetterGrass.getFaceQuads(worldIn, stateIn, posIn, enumfacing, quads);
//         }
//      }

      List quadsNew = new ArrayList<>();
//      List quadsNew = renderEnv.getListQuadsCustomizer();
//      quadsNew.clear();

//      for(int i = 0; i < quads.size(); ++i) {
//         dll quad = (dll)quads.get(i);
//         dll[] quadArr = getRenderQuads(quad, worldIn, stateIn, posIn, enumfacing, rand, renderEnv);
//         if (i == 0 && quads.size() == 1 && quadArr.length == 1 && quadArr[0] == quad && quad.getQuadEmissive() == null) {
//            return quads;
//         }
//
//         for(int q = 0; q < quadArr.length; ++q) {
//            dll quadSingle = quadArr[q];
//            quadsNew.add(quadSingle);
//            if (quadSingle.getQuadEmissive() != null) {
//               renderEnv.getListQuadsOverlay(getEmissiveLayer(layer)).addQuad(quadSingle.getQuadEmissive(), stateIn);
//               renderEnv.setOverlaysRendered(true);
//            }
//         }
//      }

      return quadsNew;
   }

//   public static bgx getEmissiveLayer(bgx layer) {
//      return layer != null && layer != bgx.a ? layer : bgx.b;
//   }
//
//   public static dll[] getRenderQuads(dll quad, bgu worldIn, bvo stateIn, ev posIn, fa enumfacing, long rand, RenderEnv renderEnv) {
//      if (renderEnv.isBreakingAnimation(quad)) {
//         return renderEnv.getArrayQuadsCtm(quad);
//      } else {
//         dll quadOriginal = quad;
//         if (Config.isConnectedTextures()) {
//            dll[] quads = ConnectedTextures.getConnectedTexture(worldIn, stateIn, posIn, quad, renderEnv);
//            if (quads.length != 1 || quads[0] != quad) {
//               return quads;
//            }
//         }
//
//         if (Config.isNaturalTextures()) {
//            quad = NaturalTextures.getNaturalTexture(posIn, quad);
//            if (quad != quadOriginal) {
//               return renderEnv.getArrayQuadsCtm(quad);
//            }
//         }
//
//         return renderEnv.getArrayQuadsCtm(quad);
//      }
//   }
}
