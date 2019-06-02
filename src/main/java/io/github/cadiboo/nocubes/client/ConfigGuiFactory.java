package io.github.cadiboo.nocubes.client;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

@SuppressWarnings("unused")
public final class ConfigGuiFactory implements IModGuiFactory {

	private static final Set<RuntimeOptionCategoryElement> categories = ImmutableSet.of(new RuntimeOptionCategoryElement("HELP", "NOCUBES"));

	@Override
	public void initialize(Minecraft minecraftInstance) {
	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return new GuiErrorScreen(
				"NoCubes Config Unavailable",
				"The NoCubes Config GUI is currently unavailable. Change your config through the"
		) {
			@Override
			public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
				super.drawScreen(mouseX, mouseY, partialTicks);
				this.drawCenteredString(this.fontRenderer, "Keybinds or by editing \"nocubes-client.toml\" in your \"minecraft/config\" directory", this.width / 2, 120, 0xFFFFFF);
				this.drawCenteredString(this.fontRenderer, "and \"nocubes-server.toml\" in your \"minecraft/saves/world/serverconfig\" directory.", this.width / 2, 130, 0xFFFFFF);
			}

			@Override
			protected void actionPerformed(final GuiButton button) {
				this.mc.displayGuiScreen(parentScreen);
			}
		};
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return categories;
	}

}
