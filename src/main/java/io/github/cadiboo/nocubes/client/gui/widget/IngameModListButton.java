package io.github.cadiboo.nocubes.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.gui.GuiModList;

/**
 * @author Cadiboo
 */
public class IngameModListButton extends Button {

	public IngameModListButton(final Screen gui, final int y) {
		super(gui.width / 2 - 100, y, 98, 20, I18n.format("fml.menu.mods"), button -> Minecraft.getInstance().displayGuiScreen(new GuiModList(gui)));
	}

}
