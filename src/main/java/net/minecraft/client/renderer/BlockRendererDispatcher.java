package net.minecraft.client.renderer;

import java.util.Random;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.fluid.IFluidState;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockRendererDispatcher implements IResourceManagerReloadListener {
   private final BlockModelShapes blockModelShapes;
   private final BlockModelRenderer blockModelRenderer;
   private final ChestRenderer chestRenderer = new ChestRenderer();
   public FluidBlockRenderer field_175025_e;
   private final Random random = new Random();

   public BlockRendererDispatcher(BlockModelShapes p_i46577_1_, BlockColors p_i46577_2_) {
      this.blockModelShapes = p_i46577_1_;
      this.blockModelRenderer = new net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer(p_i46577_2_);
      this.field_175025_e = new FluidBlockRenderer();
   }

   public BlockModelShapes getBlockModelShapes() {
      return this.blockModelShapes;
   }

   public void func_215329_a(BlockState p_215329_1_, BlockPos p_215329_2_, TextureAtlasSprite p_215329_3_, IEnviromentBlockReader p_215329_4_) {
      if(io.github.cadiboo.nocubes.hooks.Hooks.renderBlockDamage(this, p_215329_1_, p_215329_2_, p_215329_3_, p_215329_4_)){
         return;
      }
      if (p_215329_1_.getRenderType() == BlockRenderType.MODEL) {
         IBakedModel ibakedmodel = this.blockModelShapes.getModel(p_215329_1_);
         long i = p_215329_1_.getPositionRandom(p_215329_2_);
         IBakedModel ibakedmodel1 = net.minecraftforge.client.ForgeHooksClient.getDamageModel(ibakedmodel, p_215329_3_, p_215329_1_, p_215329_4_, p_215329_2_, i);
         this.blockModelRenderer.func_217631_a(p_215329_4_, ibakedmodel1, p_215329_1_, p_215329_2_, Tessellator.getInstance().getBuffer(), true, this.random, i);
      }
   }

   @Deprecated //Forge: Model parameter
   public boolean func_215330_a(BlockState p_215330_1_, BlockPos p_215330_2_, IEnviromentBlockReader p_215330_3_, BufferBuilder p_215330_4_, Random p_215330_5_) {
      return renderBlock(p_215330_1_, p_215330_2_, p_215330_3_, p_215330_4_, p_215330_5_, net.minecraftforge.client.model.data.EmptyModelData.INSTANCE);
   }
   public boolean renderBlock(BlockState p_215330_1_, BlockPos p_215330_2_, IEnviromentBlockReader p_215330_3_, BufferBuilder p_215330_4_, Random p_215330_5_, net.minecraftforge.client.model.data.IModelData modelData) {
      try {
         BlockRenderType blockrendertype = p_215330_1_.getRenderType();
         if (blockrendertype == BlockRenderType.INVISIBLE) {
            return false;
         } else {
            switch(blockrendertype) {
            case MODEL:
               return this.blockModelRenderer.renderModel(p_215330_3_, this.getModelForState(p_215330_1_), p_215330_1_, p_215330_2_, p_215330_4_, true, p_215330_5_, p_215330_1_.getPositionRandom(p_215330_2_), modelData);
            case ENTITYBLOCK_ANIMATED:
               return false;
            default:
               return false;
            }
         }
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block in world");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
         CrashReportCategory.addBlockInfo(crashreportcategory, p_215330_2_, p_215330_1_);
         throw new ReportedException(crashreport);
      }
   }

   public boolean func_215331_a(BlockPos p_215331_1_, IEnviromentBlockReader p_215331_2_, BufferBuilder p_215331_3_, IFluidState p_215331_4_) {
      try {
         return this.field_175025_e.func_217638_a(p_215331_2_, p_215331_1_, p_215331_3_, p_215331_4_);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating liquid in world");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
         CrashReportCategory.addBlockInfo(crashreportcategory, p_215331_1_, (BlockState)null);
         throw new ReportedException(crashreport);
      }
   }

   public BlockModelRenderer getBlockModelRenderer() {
      return this.blockModelRenderer;
   }

   public IBakedModel getModelForState(BlockState state) {
      return this.blockModelShapes.getModel(state);
   }

   public void renderBlockBrightness(BlockState state, float brightness) {
      BlockRenderType blockrendertype = state.getRenderType();
      if (blockrendertype != BlockRenderType.INVISIBLE) {
         switch(blockrendertype) {
         case MODEL:
            IBakedModel ibakedmodel = this.getModelForState(state);
            this.blockModelRenderer.renderModelBrightness(ibakedmodel, state, brightness, true);
            break;
         case ENTITYBLOCK_ANIMATED:
            this.chestRenderer.renderChestBrightness(state.getBlock(), brightness);
         }

      }
   }

   public void onResourceManagerReload(IResourceManager resourceManager) {
      this.field_175025_e.initAtlasSprites();
   }

   @Override
   public net.minecraftforge.resource.IResourceType getResourceType() {
      return net.minecraftforge.resource.VanillaResourceType.MODELS;
   }
}
