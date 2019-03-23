package io.github.cadiboo.nocubes.client.gui.toast;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;

/**
 * @author Cadiboo
 */
public abstract class BlockStateToast implements IToast {

	@Nonnull
	private final IBlockState state;
	@Nonnull
	private final BlockPos pos;

	BlockStateToast(@Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
		this.state = state;
		this.pos = pos;
	}

	public abstract String getUpdateType();

	@Nonnull
	@Override
	public Visibility draw(@Nonnull final GuiToast toastGui, final long delta) {
		final Minecraft minecraft = toastGui.getMinecraft();
		minecraft.getTextureManager().bindTexture(TEXTURE_TOASTS);
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		toastGui.drawTexturedModalRect(0, 0, 0, 0, 160, 32);

		toastGui.getMinecraft().fontRenderer.drawString(I18n.format(getUpdateType()) + ":", 30, 7, 0xFFFFFFFF);
		toastGui.getMinecraft().fontRenderer.drawString(state.getBlock().getLocalizedName(), 30, 18, 0xFFFFFFFF);

		RenderHelper.enableGUIStandardItemLighting();

		GlStateManager.pushMatrix();
		{
			IBakedModel bakedmodel = minecraft.getBlockRendererDispatcher().getModelForState(state);

			GlStateManager.pushMatrix();
			minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			minecraft.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//			minecraft.getRenderItem().setupGuiTransform(x, y, bakedmodel.isGui3d());
			{
				GlStateManager.translate(2.75F, 2.75F, 100.0F + minecraft.getRenderItem().zLevel);
				GlStateManager.translate(8.0F, 8.0F, 0.0F);
				GlStateManager.scale(1.0F, -1.0F, 1.0F);
				GlStateManager.scale(16.0F, 16.0F, 16.0F);

				if (bakedmodel.isGui3d()) {
					GlStateManager.enableLighting();
				} else {
					GlStateManager.disableLighting();
				}
			}
			bakedmodel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(bakedmodel, ItemCameraTransforms.TransformType.GUI, false);
			GlStateManager.scale(1.45, 1.45, 0);
			final ItemStack stack = new ItemStack(state.getBlock());
			minecraft.getRenderItem().renderItem(stack, bakedmodel);
			GlStateManager.pushMatrix();
			GlStateManager.translate(-0.5F, -0.5F, -0.5F);

			if (bakedmodel.isBuiltInRenderer())
			{
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.enableRescaleNormal();
				stack.getItem().getTileEntityItemStackRenderer().renderByItem();
			}
			else
			{
				this.renderModel(model, stack);

				if (stack.hasEffect())
				{
					this.renderEffect(model);
				}
			}

			GlStateManager.popMatrix();
			GlStateManager.disableAlpha();
			GlStateManager.disableRescaleNormal();
			GlStateManager.disableLighting();
			GlStateManager.popMatrix();
			minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			minecraft.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

		}
		GlStateManager.popMatrix();

		RenderHelper.disableStandardItemLighting();

		return delta >= 100000L ? Visibility.HIDE : Visibility.SHOW;
	}

	public static class Add extends BlockStateToast {

		public Add(@Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
			super(state, pos);
		}

		@Override
		public String getUpdateType() {
			return MOD_ID + ".addedSmoothableBlockState";
		}

	}

	public static class Remove extends BlockStateToast {

		public Remove(@Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
			super(state, pos);
		}

		@Override
		public String getUpdateType() {
			return MOD_ID + ".removedSmoothableBlockState";
		}

	}

}
