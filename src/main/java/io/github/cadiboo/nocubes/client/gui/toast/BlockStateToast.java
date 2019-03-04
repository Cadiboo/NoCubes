package io.github.cadiboo.nocubes.client.gui.toast;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;

/**
 * @author Cadiboo
 */
public abstract class BlockStateToast implements IToast {

	@Nonnull
	private final ItemStack stateStack;
	@Nonnull
	private final IBlockState state;

	BlockStateToast(@Nonnull IBlockState state) {
		this.stateStack = new ItemStack(state.getBlock());
		this.state = state;
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
		minecraft.getRenderItem().renderItemAndEffectIntoGUI(null, stateStack, 8, 8);
		RenderHelper.disableStandardItemLighting();

		return delta >= 1000L ? Visibility.HIDE : Visibility.SHOW;
	}

	public static class Add extends BlockStateToast {

		public Add(final IBlockState state) {
			super(state);
		}

		@Override
		public String getUpdateType() {
			return MOD_ID + ".addedSmoothableBlockstate";
		}

	}

	public static class Remove extends BlockStateToast {

		public Remove(final IBlockState state) {
			super(state);
		}

		@Override
		public String getUpdateType() {
			return MOD_ID + ".removedSmoothableBlockstate";
		}

	}

}
