package io.github.cadiboo.nocubes.client.gui.config;

import net.minecraft.client.gui.widget.Widget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public final class EnumOption extends Option {

	private final Function<Integer, Integer> cyclerAndSaver;
	private final Function<EnumOption, String> translatedNameGetter;
	private int currentOrdinal;

	public EnumOption(String translationKey, Function<Integer, Integer> cyclerAndSaver, Function<EnumOption, String> translatedNameGetter, int initialOrdinal) {
		super(translationKey);
		this.cyclerAndSaver = cyclerAndSaver;
		this.translatedNameGetter = translatedNameGetter;
		this.currentOrdinal = initialOrdinal;
	}

	@Override
	public Widget createWidget(int width) {
		return new OptionButton(0, 0, width, 20, this, this.getTranslatedName(), widget -> {
			this.currentOrdinal = this.cyclerAndSaver.apply(this.currentOrdinal);
			widget.setMessage(this.getTranslatedName());
		});
	}

	public String getTranslatedName() {
		return this.translatedNameGetter.apply(this);
	}

}
