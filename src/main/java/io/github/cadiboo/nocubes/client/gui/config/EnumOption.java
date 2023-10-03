package io.github.cadiboo.nocubes.client.gui.config;

import java.util.function.Function;

//@OnlyIn(Dist.CLIENT)
public final class EnumOption implements Option {

	private final Function<Integer, Integer> cycler;
	private final Function<Integer, String> translatedNameGetter;
	private int currentOrdinal;

	public EnumOption(Function<Integer, Integer> cycler, Function<Integer, String> translatedNameGetter, int initialOrdinal) {
		this.cycler = cycler;
		this.translatedNameGetter = translatedNameGetter;
		this.currentOrdinal = initialOrdinal;
	}

	@Override
	public OptionButton createWidget(int width) {
		return new OptionButton(0, 0, width, 20, this, this.getDisplayString(), widget -> {
			this.currentOrdinal = this.cycler.apply(this.currentOrdinal);
			widget.setMessage(this.getDisplayString());
		});
	}

	public String getDisplayString() {
		return this.translatedNameGetter.apply(this.currentOrdinal);
	}

}
