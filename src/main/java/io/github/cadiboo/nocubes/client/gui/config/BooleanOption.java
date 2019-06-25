package io.github.cadiboo.nocubes.client.gui.config;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class BooleanOption extends Option {

	private final Supplier<Boolean> getter;
	private final Consumer<Boolean> setter;

	public BooleanOption(String translationKey, Supplier<Boolean> getter, Consumer<Boolean> setter) {
		super(translationKey);
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
	public Widget createWidget(int width) {
		return new OptionButton(0, 0, width, 20, this, this.getTranslatedName(), (widget) -> {
			this.save();
			widget.setMessage(this.getTranslatedName());
		});
	}

	public String getTranslatedName() {
		return this.getDisplayString() + I18n.format(this.get() ? "options.on" : "options.off");
	}

}
