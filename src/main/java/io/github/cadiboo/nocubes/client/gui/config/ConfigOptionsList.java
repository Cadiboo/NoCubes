package io.github.cadiboo.nocubes.client.gui.config;

import com.google.common.base.Joiner;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.config.ConfigHolder;
import io.github.cadiboo.nocubes.config.ForgeConfigSpec.BooleanValue;
import io.github.cadiboo.nocubes.config.ForgeConfigSpec.ConfigValue;
import io.github.cadiboo.nocubes.config.ForgeConfigSpec.EnumValue;
import io.github.cadiboo.nocubes.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
class ConfigOptionsList extends GuiListExtended {

	private static final Joiner DOT_JOINER = Joiner.on(".");
	final List<Entry> entries;
	private final NoCubesConfigGui configGui;
	private final int maxListLabelWidth;

	public ConfigOptionsList(NoCubesConfigGui configGui, Minecraft mcIn) {
		super(mcIn, configGui.width + 45, configGui.height, 43, configGui.height - 32, 20);
		this.configGui = configGui;

		entries = new ArrayList<>();

		// Client
		{
			entries.add(new CategoryEntry(MOD_ID + ".config.client"));
			getConfigValues(ConfigHolder.CLIENT).forEach((configValue, name) -> {
				final ValueEntry<?> e = createValueEntry(configValue, name, () -> ConfigHelper.clientConfig);
				entries.add(e);
			});
		}

		// Server
		if (this.mc.world != null) {
			if (this.mc.getIntegratedServer() != null) {
				entries.add(new CategoryEntry(MOD_ID + ".config.server"));
				getConfigValues(ConfigHolder.SERVER).forEach((configValue, name) -> {
					final ValueEntry<?> e = createValueEntry(configValue, name, () -> ConfigHelper.serverConfig);
					entries.add(e);
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
			return new NotImplementedValueEntry<>(configValue, name, configSupplier);
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

	@Override
	public IGuiListEntry getListEntry(final int index) {
		return entries.get(index);
	}

	@Override
	protected int getSize() {
		return entries.size();
	}

	public int getListWidth() {
		return super.getListWidth() + 150;
	}

	public int getScrollBarX() {
		return super.getScrollBarX() + 15 + 20;
	}

	boolean saveChanged() {
		boolean wasAnythingSaved = false;
		for (final Entry entry : entries) {
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

	abstract static class Entry implements IGuiListEntry {

		abstract String getTranslatedText();

		@Override
		public void updatePosition(final int slotIndex, final int x, final int y, final float partialTicks) {

		}

		@Override
		public void mouseReleased(final int slotIndex, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {

		}

	}

	//	@OnlyIn(Dist.CLIENT)
	class CategoryEntry extends ConfigOptionsList.Entry {

		private final String labelText;
		private final int labelWidth;

		CategoryEntry(final String name) {
			this.labelText = I18n.format(name);
			this.labelWidth = ConfigOptionsList.this.mc.fontRenderer.getStringWidth(this.labelText);
		}

		@Override
		public void drawEntry(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks) {
			ConfigOptionsList.this.mc.fontRenderer.drawString("Category: " + this.labelText, configGui.width / 2 - this.labelWidth / 2, y + slotHeight - 9 - 1, 0xFFFFFF);
		}

		@Override
		public boolean mousePressed(final int slotIndex, final int mouseX, final int mouseY, final int mouseEvent, final int relativeX, final int relativeY) {
			return false;
		}

		@Override
		String getTranslatedText() {
			return labelText;
		}

	}

	//	@OnlyIn(Dist.CLIENT)
	abstract class ValueEntry<T> extends ConfigOptionsList.Entry {

		final ConfigValue<T> configValue;
		final String text;
		final int initialHash;
		final T initialValue;
		final Supplier<ModConfig> configSupplier;
		final LazyLoadBase<OptionButton> widgetSupplier;
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

		protected abstract OptionButton makeWidget();

		@Override
		public void drawEntry(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks) {
			final OptionButton widget = this.widgetSupplier.getValue();
			widget.x = x;
			widget.y = y;
			widget.drawButton(mc, mouseX, mouseY, partialTicks);
		}

//		@Nonnull
//		public List<? extends IGuiEventListener> children() {
//			return ImmutableList.of(this.widgetSupplier.getValue());
//		}

		@Override
		public boolean mousePressed(final int slotIndex, final int mouseX, final int mouseY, final int mouseEvent, final int relativeX, final int relativeY) {
			return widgetSupplier.getValue().mousePressed(mc, mouseX, mouseY);
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

	//	@OnlyIn(Dist.CLIENT)
	class BooleanValueEntry extends ValueEntry<Boolean> {

		BooleanValueEntry(final BooleanValue booleanValue, final String name, final Supplier<ModConfig> configSupplier) {
			super(booleanValue, name, configSupplier);
		}

		@Override
		protected OptionButton makeWidget() {
			return new BooleanOption(this.text, () -> this.currentValue, this::handleChanged)
					.createWidget((configGui.width / 4) * 3);
		}

	}

	//	@OnlyIn(Dist.CLIENT)
	class EnumValueEntry<T extends Enum<T>> extends ValueEntry<T> {

		final T[] values;

		EnumValueEntry(final EnumValue<T> enumValue, final String name, final Supplier<ModConfig> configSupplier) {
			super(enumValue, name, configSupplier);
			// ew
			this.values = this.initialValue.getDeclaringClass().getEnumConstants();
		}

		private int cycle(final int ordinal) {
			final int i = (ordinal + 1) % this.values.length;
			this.handleChanged(this.values[i]);
			return i;
		}

		private String getTranslatedText(final EnumOption enumOption) {
			return enumOption.getDisplayString() + this.currentValue.name();
		}

		@Override
		protected OptionButton makeWidget() {
			return new EnumOption(this.text, this::cycle, this::getTranslatedText, this.currentValue.ordinal())
					.createWidget((configGui.width / 4) * 3);
		}

	}

	private class NotImplementedValueEntry<T> extends ValueEntry<T> {

		NotImplementedValueEntry(final ConfigValue<T> configValue, final String name, final Supplier<ModConfig> configSupplier) {
			super(configValue, name, configSupplier);
		}

		@Override
		protected OptionButton makeWidget() {
			final OptionButton widget = new BooleanOption(this.text, () -> false, this::handleChanged) {
				@Override
				public String getTranslatedName() {
					return this.getDisplayString() + I18n.format("commands.scoreboard.objectives.add.wrongType", getTranslatedText());
				}
			}
					.createWidget((configGui.width / 4) * 3);
			widget.packedFGColour = 0xFF0000;
			return widget;
		}

		private void handleChanged(final Boolean ignored) {
			// NOOP
		}

	}

}
