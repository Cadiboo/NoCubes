package io.github.cadiboo.nocubes.client.gui.config;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionButton extends Button {

	private final Option option;

	public OptionButton(int x, int y, int width, int height, Option option, String text, Button.IPressable onPressedHandler) {
		super(x, y, width, height, text, onPressedHandler);
		this.option = option;
	}

}
