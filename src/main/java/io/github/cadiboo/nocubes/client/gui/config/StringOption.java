package io.github.cadiboo.nocubes.client.gui.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

import java.util.function.Consumer;
import java.util.function.Supplier;

//@OnlyIn(Dist.CLIENT)
public final class StringOption implements Option {

	private final Supplier<String> getter;
	private final Consumer<String> setter;

	public StringOption(Supplier<String> getter, Consumer<String> setter) {
		this.getter = getter;
		this.setter = setter;
	}

	public void set(String newValue) {
		this.setter.accept(newValue);
	}

	public String get() {
		return this.getter.get();
	}

	@Override
	public GuiTextField createWidget(int width) {
		return new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, width - 4, 16) {
			{
				this.setMaxStringLength(9999999);
				this.setText(StringOption.this.get());
				this.setGuiResponder(null);
			}

			@Override
			public boolean textboxKeyTyped(char typedChar, int keyCode) {
				if (super.textboxKeyTyped(typedChar, keyCode)) {
					StringOption.this.set(this.getText());
					return true;
				} else {
					return false;
				}
			}
		};
	}

}
