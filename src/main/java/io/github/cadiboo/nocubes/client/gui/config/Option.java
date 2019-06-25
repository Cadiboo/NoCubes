package io.github.cadiboo.nocubes.client.gui.config;

import net.minecraft.client.resources.I18n;

//@OnlyIn(Dist.CLIENT)
public abstract class Option {

	private final String translationKey;

	public Option(String translationKeyIn) {
		this.translationKey = translationKeyIn;
	}

	public abstract OptionButton createWidget(int width);

	public String getDisplayString() {
		return I18n.format(this.translationKey) + ": ";
	}

	public String getTranslationKey() {
		return translationKey;
	}

}
