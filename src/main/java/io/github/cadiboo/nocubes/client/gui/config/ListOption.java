package io.github.cadiboo.nocubes.client.gui.config;

import joptsimple.internal.Strings;
import net.minecraft.client.resources.I18n;

import java.util.function.Consumer;
import java.util.function.Supplier;

//@OnlyIn(Dist.CLIENT)
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
	public OptionButton createWidget(int width) {
		final TextFieldOption textFieldWidget = new TextFieldOption(0, 0, width - 4, 16, this, I18n.format(this.getTranslationKey()), p_onPress_1_ -> {
		}) {
			@Override
			public boolean textboxKeyTyped(char p_charTyped_1_, int p_charTyped_2_) {
				if (super.textboxKeyTyped(p_charTyped_1_, p_charTyped_2_)) {
					ListOption.this.set(this.getText().split(", "));
					return true;
				} else {
					return false;
				}
			}
		};
		textFieldWidget.setMaxStringLength(9999999);
		textFieldWidget.setText(Strings.join(this.get(), ", "));
		return textFieldWidget;
	}

}
