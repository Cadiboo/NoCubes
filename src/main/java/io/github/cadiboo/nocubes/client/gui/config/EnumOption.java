package io.github.cadiboo.nocubes.client.gui.config;

import java.util.function.Function;

//@OnlyIn(Dist.CLIENT)
public final class EnumOption extends Option {

	private final Function<Integer, Integer> cycler;
	private final Function<EnumOption, String> translatedNameGetter;
	private int currentOrdinal;

	public EnumOption(String translationKey, Function<Integer, Integer> cycler, Function<EnumOption, String> translatedNameGetter, int initialOrdinal) {
		super(translationKey);
		this.cycler = cycler;
		this.translatedNameGetter = translatedNameGetter;
		this.currentOrdinal = initialOrdinal;
	}

	@Override
	public OptionButton createWidget(int width) {
		return new OptionButton(0, 0, width, 20, this, this.getTranslatedName(), widget -> {
			this.currentOrdinal = this.cycler.apply(this.currentOrdinal);
			widget.setMessage(this.getTranslatedName());
		});
	}

	public String getTranslatedName() {
		return this.translatedNameGetter.apply(this);
	}

}
