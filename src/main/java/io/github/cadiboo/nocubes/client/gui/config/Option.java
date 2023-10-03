package io.github.cadiboo.nocubes.client.gui.config;

import net.minecraft.client.gui.Gui;

//@OnlyIn(Dist.CLIENT)
public interface Option {

	Gui createWidget(int width);

}
