package io.github.cadiboo.nocubes.client.gui.config;

import net.minecraft.client.resources.I18n;

import java.util.function.Consumer;
import java.util.function.Supplier;

//@OnlyIn(Dist.CLIENT)
public class BooleanOption implements Option {

	private final Supplier<Boolean> getter;
	private final Consumer<Boolean> setter;

	public BooleanOption(Supplier<Boolean> getter, Consumer<Boolean> setter) {
		this.getter = getter;
		this.setter = setter;
	}

	public void set(final String str) {
		this.set("true".equals(str));
	}

	public void save() {
		this.set(!this.get());
	}

	private void set(boolean newValue) {
		this.setter.accept(newValue);
	}

	public boolean get() {
		return this.getter.get();
	}

	@Override
	public OptionButton createWidget(int width) {
		return new OptionButton(0, 0, width, 20, this, this.getDisplayString(), (widget) -> {
			this.save();
			widget.setMessage(this.getDisplayString());
		});
	}

	public String getDisplayString() {
		return I18n.format(this.get() ? "options.on" : "options.off");
	}

}
