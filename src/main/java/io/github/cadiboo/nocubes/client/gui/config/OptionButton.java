package io.github.cadiboo.nocubes.client.gui.config;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiButtonExt;

//@OnlyIn(Dist.CLIENT)
public class OptionButton extends GuiButtonExt {

	protected final IPressable onPressedHandler;

	private final Option option;

	public OptionButton(int x, int y, int width, int height, Option option, String text, IPressable onPressedHandler) {
		super(0, x, y, width, height, text);
		this.option = option;
		this.onPressedHandler = onPressedHandler;
	}

	public void onPress() {
		this.onPressedHandler.onPress(this);
	}

	public void setMessage(String message) {
		this.displayString = message;
	}

	@Override
	public boolean mousePressed(final Minecraft mc, final int mouseX, final int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			this.onPressedHandler.onPress(this);
			return true;
		} else {
			return false;
		}
	}

	public interface IPressable {

		void onPress(OptionButton p_onPress_1_);

	}

}
