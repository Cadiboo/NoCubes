package io.github.cadiboo.nocubes.client.gui.config;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.config.GuiButtonExt;

@OnlyIn(Dist.CLIENT)
public final class OptionButton extends GuiButtonExt {

	public OptionButton(int x, int y, int width, int height, String text, Button.IPressable onPressedHandler) {
		super(x, y, width, height, text, onPressedHandler);
	}

}
