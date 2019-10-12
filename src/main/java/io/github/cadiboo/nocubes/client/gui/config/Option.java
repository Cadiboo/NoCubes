package io.github.cadiboo.nocubes.client.gui.config;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Option {

	private final String translationKey;

	public Option(String translationKeyIn) {
		this.translationKey = translationKeyIn;
	}

	public abstract Widget createWidget();

	public String getDisplayString() {
		return I18n.format(this.translationKey) + ": ";
	}

	public String getTranslationKey() {
		return translationKey;
	}

}
