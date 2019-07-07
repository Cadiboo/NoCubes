package io.github.cadiboo.nocubes.client;

import com.google.common.collect.ImmutableSet;
import io.github.cadiboo.nocubes.client.gui.config.NoCubesConfigGui;
import net.minecraft.client.Minecraft;
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
		return new NoCubesConfigGui(parentScreen.mc, parentScreen);
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return categories;
	}

}
