package io.github.cadiboo.nocubes.client.gui.config;

import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public final class ListOption extends Option {

	private final Supplier<String[]> getter;
	private final Consumer<String[]> setter;

	public ListOption(String translationKey, Supplier<String[]> getter, Consumer<String[]> setter) {
		super(translationKey);
		this.getter = getter;
		this.setter = setter;
	}

	private void set(String[] newValue) {
		this.setter.accept(newValue);
	}

	public String[] get() {
		return this.getter.get();
	}

	@Override
	public Widget createWidget(int width) {
		final TextFieldWidget textFieldWidget = new TextFieldWidget(Minecraft.getInstance().fontRenderer, 0, 0, width - 4, 16, I18n.format(this.getTranslationKey())) {
			@Override
			public boolean keyPressed(final int keyCode, final int p_keyPressed_2_, final int p_keyPressed_3_) {
				if (super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_)) {
					ListOption.this.set(this.getText().split(", "));
					return true;
				} else {
					return false;
				}
			}

			@Override
			public boolean keyReleased(final int p_223281_1_, final int p_223281_2_, final int p_223281_3_) {
				if (super.keyReleased(p_223281_1_, p_223281_2_, p_223281_3_)) {
					ListOption.this.set(this.getText().split(", "));
					return true;
				} else {
					return false;
				}
			}

			@Override
			public boolean charTyped(char character, int p_charTyped_2_) {
				if (super.charTyped(character, p_charTyped_2_)) {
					ListOption.this.set(this.getText().split(", "));
					return true;
				} else {
					return false;
				}
			}
		};
		textFieldWidget.setMaxStringLength(Integer.MAX_VALUE);
		textFieldWidget.setText(Strings.join(this.get(), ", "));
		return textFieldWidget;
	}

}
