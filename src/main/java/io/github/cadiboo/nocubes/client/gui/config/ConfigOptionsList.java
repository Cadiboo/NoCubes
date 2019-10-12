package io.github.cadiboo.nocubes.client.gui.config;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.config.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.LazyLoadBase;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

/**
 * @author Cadiboo
 */
final class ConfigOptionsList extends AbstractOptionList<ConfigOptionsList.Entry> {

	private static final Joiner DOT_JOINER = Joiner.on(".");

	private final NoCubesConfigGui configGui;
	private final int maxListLabelWidth;

	public ConfigOptionsList(NoCubesConfigGui configGui, Minecraft mcIn) {
		super(mcIn, configGui.width + 45, configGui.height, 43, configGui.height - 32, 20);
		this.configGui = configGui;

		final List<ConfigOptionsList.Entry> entries = new ArrayList<>();

		// Client
		{
			final CategoryEntry clientCategoryEntry = new CategoryEntry(MOD_ID + ".config.client");
			entries.add(clientCategoryEntry);
			this.addEntry(clientCategoryEntry);
			getConfigValues(ConfigHolder.CLIENT).forEach((configValue, name) -> {
				final ValueEntry<?> e = createValueEntry(configValue, name, () -> ConfigHelper.clientConfig);
				entries.add(e);
				this.addEntry(e);
			});
		}

		// Server
		if (this.minecraft.world != null) {
			if (this.minecraft.getIntegratedServer() != null) {
				final CategoryEntry serverCategoryEntry = new CategoryEntry(MOD_ID + ".config.server");
				entries.add(serverCategoryEntry);
				this.addEntry(serverCategoryEntry);
				getConfigValues(ConfigHolder.SERVER).forEach((configValue, name) -> {
					final ValueEntry<?> e = createValueEntry(configValue, name, () -> ConfigHelper.serverConfig);
					entries.add(e);
					this.addEntry(e);
				});
			}
		}

		FontRenderer fontRenderer = mcIn.fontRenderer;
		int maxListLabelWidth = 0;
		for (final Entry entry : entries) {
			int i = fontRenderer.getStringWidth(entry.getTranslatedText());
			if (i > maxListLabelWidth) {
				maxListLabelWidth = i;
			}
		}
		this.maxListLabelWidth = maxListLabelWidth;

	}

	private static void saveValue(final ConfigValue<?> configValue, final Supplier<ModConfig> configSupplier, final Object newValue) {
		ConfigHelper.setValueAndSave(configSupplier.get(), DOT_JOINER.join(configValue.getPath()), newValue);
	}

	@Nonnull
	private ValueEntry<?> createValueEntry(final ConfigValue<?> configValue, final String name, Supplier<ModConfig> configSupplier) {
		if (configValue instanceof BooleanValue) {
			return new BooleanValueEntry((BooleanValue) configValue, name, configSupplier);
		} else if (configValue instanceof EnumValue<?>) {
			return new EnumValueEntry<>((EnumValue<?>) configValue, name, configSupplier);
		} else {
			try {
				return new ListValueEntry<>((ConfigValue<List<? extends String>>) configValue, name, configSupplier);
			} catch (Exception e) {
				return new NotImplementedValueEntry<>(configValue, name, configSupplier);
			}
		}
	}

	private Map<ConfigValue<?>, String> getConfigValues(final Object config) {
		final Map<ConfigValue<?>, String> configValuesAndNames = new HashMap<>();
		for (final Field declaredField : config.getClass().getDeclaredFields()) {
			declaredField.setAccessible(true);
			try {
				final Object o = declaredField.get(config);
				if (o instanceof ConfigValue) {
					configValuesAndNames.put((ConfigValue) o, declaredField.getName());
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return configValuesAndNames;
	}

	public int getRowWidth() {
		return super.getRowWidth() + 150;
	}

	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 15 + 20;
	}

	boolean saveChanged() {
		boolean wasAnythingSaved = false;
		for (final Entry entry : children()) {
			if (entry instanceof ValueEntry) {
				final ValueEntry<?> valueEntry = (ValueEntry) entry;

				if (valueEntry.isChanged()) {
					saveValue(valueEntry.configValue, valueEntry.configSupplier, valueEntry.currentValue);
					wasAnythingSaved = true;
				}

			} else if (entry instanceof CategoryEntry) {
				final CategoryEntry categoryEntry = (CategoryEntry) entry;
			}
		}
		return wasAnythingSaved;
	}

	abstract static class Entry extends AbstractOptionList.Entry<ConfigOptionsList.Entry> {

		abstract String getTranslatedText();

		void tick() {
		}

	}

	@OnlyIn(Dist.CLIENT)
	final class CategoryEntry extends ConfigOptionsList.Entry {

		private final String labelText;
		private final int labelWidth;

		CategoryEntry(final String name) {
			this.labelText = I18n.format(name);
			this.labelWidth = ConfigOptionsList.this.minecraft.fontRenderer.getStringWidth(this.labelText);
		}

		@Override
		public void render(int p_render_1_, int y, int x, int p_render_4_, int p_render_5_, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
			ConfigOptionsList.this.minecraft.fontRenderer.drawString("Category: " + this.labelText, (float) (configGui.width / 2 - this.labelWidth), (float) (y + p_render_5_ - 9 - 1), 0xFFFFFF);
		}

		@Nonnull
		public List<? extends IGuiEventListener> children() {
			return Collections.emptyList();
		}

		public boolean changeFocus(boolean p_changeFocus_1_) {
			return false;
		}

		@Override
		String getTranslatedText() {
			return labelText;
		}

	}

	@OnlyIn(Dist.CLIENT)
	abstract class ValueEntry<T> extends ConfigOptionsList.Entry {

		final ConfigValue<T> configValue;
		final String text;
		final int initialHash;
		final T initialValue;
		final Supplier<ModConfig> configSupplier;
		final LazyLoadBase<Widget> widgetSupplier;
		T currentValue;

		private ValueEntry(final ConfigValue<T> configValue, final String name, final Supplier<ModConfig> configSupplier) {
			this.configValue = configValue;
			this.text = I18n.format(MOD_ID + ".config." + name);
			this.configSupplier = configSupplier;
			final T value = configValue.get();
			this.currentValue = this.initialValue = value;
			this.initialHash = Objects.hashCode(value);
			this.widgetSupplier = new LazyLoadBase<>(this::makeWidget);
		}

		protected abstract Widget makeWidget();

		@Override
		public void render(int p_render_1_, int y, int x, int p_render_4_, int p_render_5_, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
			final Widget widget = this.widgetSupplier.getValue();
			if (widget instanceof TextFieldWidget) {
				widget.x = x + 2;
				widget.y = y + 2;
			} else {
				widget.x = x;
				widget.y = y;
			}
			widget.render(mouseX, mouseY, partialTicks);
		}

		@Nonnull
		public List<? extends IGuiEventListener> children() {
			return ImmutableList.of(this.widgetSupplier.getValue());
		}

		@Override
		String getTranslatedText() {
			return text;
		}

		void handleChanged(final T newValue) {
			this.currentValue = newValue;
		}

		public boolean isChanged() {
			return initialHash != Objects.hashCode(currentValue);
		}

	}

	@OnlyIn(Dist.CLIENT)
	final class BooleanValueEntry extends ValueEntry<Boolean> {

		BooleanValueEntry(final BooleanValue booleanValue, final String name, final Supplier<ModConfig> configSupplier) {
			super(booleanValue, name, configSupplier);
		}

		@Override
		protected Widget makeWidget() {
			return new BooleanOption(this.text, () -> this.currentValue, this::handleChanged)
					.createWidget();
		}

	}

	@OnlyIn(Dist.CLIENT)
	final class EnumValueEntry<T extends Enum<T>> extends ValueEntry<T> {

		final T[] values;

		EnumValueEntry(final EnumValue<T> enumValue, final String name, final Supplier<ModConfig> configSupplier) {
			super(enumValue, name, configSupplier);
			// ew
			this.values = this.initialValue.getDeclaringClass().getEnumConstants();
		}

		private int cycleAndSave(final int ordinal) {
			final int i = (ordinal + 1) % this.values.length;
			this.handleChanged(this.values[i]);
			return i;
		}

		private String getTranslatedText(final EnumOption enumOption) {
			return enumOption.getDisplayString() + this.currentValue.name();
		}

		@Override
		protected Widget makeWidget() {
			return new EnumOption(this.text, this::cycleAndSave, this::getTranslatedText, this.currentValue.ordinal())
					.createWidget();
		}

	}

	@OnlyIn(Dist.CLIENT)
	final class ListValueEntry<T> extends ValueEntry<List<? extends T>> {

		ListValueEntry(final ConfigValue<List<? extends T>> listValue, final String name, final Supplier<ModConfig> configSupplier) {
			super(listValue, name, configSupplier);
		}

		@Override
		protected Widget makeWidget() {
			return new ListOption(this.text, () -> this.currentValue.stream().map(Object::toString).toArray(String[]::new), this::handleChanged)
					.createWidget();
		}

		private void handleChanged(final String[] newValue) {
			final ArrayList<T> list = new ArrayList<>();
			for (String str : newValue) {
				list.add((T) str);
			}
			this.handleChanged(list);
		}

		@Override
		void tick() {
			super.tick();
			((TextFieldWidget) this.widgetSupplier.getValue()).tick();
		}

	}

	private final class NotImplementedValueEntry<T> extends ValueEntry<T> {

		NotImplementedValueEntry(final ConfigValue<T> configValue, final String name, final Supplier<ModConfig> configSupplier) {
			super(configValue, name, configSupplier);
		}

		@Override
		protected Widget makeWidget() {
			final Widget widget = new BooleanOption(this.text, () -> false, this::handleChanged) {
				@Override
				public String getTranslatedName() {
					return this.getDisplayString() + I18n.format("argument.criteria.invalid", getTranslatedText());
				}
			}
					.createWidget();
			widget.setFGColor(0xFF0000);
			return widget;
		}

		private void handleChanged(final Boolean ignored) {
			// NOOP
		}

	}

}
