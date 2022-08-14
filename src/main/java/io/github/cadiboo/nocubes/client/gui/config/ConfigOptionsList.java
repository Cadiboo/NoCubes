package io.github.cadiboo.nocubes.client.gui.config;

import com.google.common.base.Joiner;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.config.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import static net.minecraftforge.common.ForgeConfigSpec.EnumValue;

/**
 * @author Cadiboo
 */
final class ConfigOptionsList extends GuiListExtended {

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

	/**
	 * This method is a pass-through for IConfigEntry objects that require keystrokes. Called from the parent GuiConfig screen.
	 */
	public void keyTyped(char eventChar, int eventKey) {
		for (Entry entry : this.entries)
			entry.keyTyped(eventChar, eventKey);
	}

	/**
	 * This method is a pass-through for IConfigEntry objects that contain GuiTextField elements. Called from the parent GuiConfig
	 * screen.
	 */
	public void mouseClickedPassThru(int mouseX, int mouseY, int mouseEvent) {
		for (Entry entry : this.entries)
			entry.mouseClicked(mouseX, mouseY, mouseEvent);
	}

	/**
	 * This method is a pass-through for IConfigEntry objects that contain GuiTextField elements. Called from the parent GuiConfig
	 * screen.
	 */
	public void updateScreen() {
		for (Entry entry : this.entries)
			entry.updateCursorCounter();
	}

	abstract static class Entry implements IGuiListEntry {

		abstract String getTranslatedText();

		@Override
		public void updatePosition(final int slotIndex, final int x, final int y, final float partialTicks) {
		}

		@Override
		public void mouseReleased(final int slotIndex, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
		}

		public void keyTyped(final char eventChar, final int eventKey) {
		}

		public void mouseClicked(final int mouseX, final int mouseY, final int mouseEvent) {
		}

		public void updateCursorCounter() {
		}

	}

	//	@OnlyIn(Dist.CLIENT)
	final class CategoryEntry extends ConfigOptionsList.Entry {

		private final String labelText;
		private final int labelWidth;

		CategoryEntry(final String name) {
			this.labelText = I18n.format(name);
			this.labelWidth = ConfigOptionsList.this.mc.fontRenderer.getStringWidth(this.labelText);
		}

		@Override
		public void drawEntry(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks) {
			ConfigOptionsList.this.mc.fontRenderer.drawString("Category: " + this.labelText, configGui.width / 2 - this.labelWidth, y + slotHeight - 9 - 1, 0xFFFFFF);
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
		final LazyLoadBase<Gui> widgetSupplier;
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

		protected abstract Gui makeWidget();

		@Override
		public void drawEntry(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks) {
			final Gui widget = this.widgetSupplier.getValue();
			// I HATE 1.12.2 & OptiFine. WTF IS EVEN GOING ON WITH THIS
			if (widget instanceof GuiTextField) {
				final GuiTextField ihate1122 = (GuiTextField) widget;
				ihate1122.x = x + 2;
				ihate1122.y = y + 2;
				ihate1122.drawTextBox();
			} else {
				final OptionButton ihate1122 = (OptionButton) widget;
				ihate1122.x = x;
				ihate1122.y = y;
				ihate1122.drawButton(mc, mouseX, mouseY, partialTicks);
			}
		}

//		@Nonnull
//		public List<? extends IGuiEventListener> children() {
//			return ImmutableList.of(this.widgetSupplier.getValue());
//		}

		@Override
		public boolean mousePressed(final int slotIndex, final int mouseX, final int mouseY, final int mouseEvent, final int relativeX, final int relativeY) {
			final Gui value = widgetSupplier.getValue();
			if (value instanceof GuiTextField) {
				return ((GuiTextField) value).mouseClicked(mouseX, mouseY, 0);
			} else {
				return ((OptionButton) value).mousePressed(mc, mouseX, mouseY);
			}
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
	final class BooleanValueEntry extends ValueEntry<Boolean> {

		BooleanValueEntry(final BooleanValue booleanValue, final String name, final Supplier<ModConfig> configSupplier) {
			super(booleanValue, name, configSupplier);
		}

		@Override
		protected Gui makeWidget() {
			return new BooleanOption(this.text, () -> this.currentValue, this::handleChanged)
					.createWidget((configGui.width / 4) * 3);
		}

	}

	//	@OnlyIn(Dist.CLIENT)
	final class EnumValueEntry<T extends Enum<T>> extends ValueEntry<T> {

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

	final class ListValueEntry<T> extends ValueEntry<List<? extends T>> {

		ListValueEntry(final ConfigValue<List<? extends T>> listValue, final String name, final Supplier<ModConfig> configSupplier) {
			super(listValue, name, configSupplier);
		}

		@Override
		protected GuiTextField makeWidget() {
			return new ListOption(this.text, () -> this.currentValue.stream().map(Object::toString).toArray(String[]::new), this::handleChanged)
					.createWidget((configGui.width / 4) * 3);
		}

		private void handleChanged(final String[] newValue) {
			final ArrayList<T> list = new ArrayList<>();
			for (String str : newValue) {
				list.add((T) str);
			}
			this.handleChanged(list);
		}

		@Override
		public void keyTyped(final char eventChar, final int eventKey) {
			((GuiTextField) widgetSupplier.getValue()).textboxKeyTyped(eventChar, eventKey);
		}

		@Override
		public void mouseClicked(final int mouseX, final int mouseY, final int mouseEvent) {
			((GuiTextField) widgetSupplier.getValue()).mouseClicked(mouseX, mouseY, mouseEvent);
		}

		@Override
		public void updateCursorCounter() {
			((GuiTextField) widgetSupplier.getValue()).updateCursorCounter();
		}

	}

	private final class NotImplementedValueEntry<T> extends ValueEntry<T> {

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
