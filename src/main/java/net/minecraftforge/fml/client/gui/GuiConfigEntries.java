package net.minecraftforge.fml.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.config.IConfigEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cadiboo
 */
public class GuiConfigEntries extends GuiListExtended {

	public final GuiConfig owningScreen;
	public final Minecraft mc;
	public List<IConfigEntry> listEntries;
	/**
	 * The max width of the label of all IConfigEntry objects.
	 */
	public int maxLabelTextWidth = 0;
	/**
	 * The max x boundary of all IConfigEntry objects.
	 */
	public int maxEntryRightBound = 0;
	/**
	 * The x position where the label should be drawn.
	 */
	public int labelX;
	/**
	 * The x position where the control should be drawn.
	 */
	public int controlX;
	/**
	 * The width of the control.
	 */
	public int controlWidth;
	/**
	 * The minimum x position where the Undo/Default buttons will start
	 */
	public int resetX;
	/**
	 * The x position of the scroll bar.
	 */
	public int scrollBarX;

	public GuiConfigEntries(final Minecraft mc, final GuiConfig parent) {
		super(mc, parent.width, parent.height, parent.subTitle != null ? 33 : 23, parent.height - 32, 20);
		this.owningScreen = parent;
		this.setShowSelectionBox(false);
		this.mc = mc;
		this.listEntries = new ArrayList<>();

		for (IConfigElement configElement : parent.configElements) {
			if (configElement != null) {
				if (configElement.isProperty() && configElement.showInGui()) // as opposed to being a child category entry
				{
					int length;

					// protects against language keys that are not defined in the .lang file
					final String languageKey = configElement.getLanguageKey();
					final String formatted = I18n.format(languageKey);
					if (!formatted.equals(languageKey))
						length = mc.fontRenderer.getStringWidth(formatted);
					else
						length = mc.fontRenderer.getStringWidth(configElement.getName());

					if (length > this.maxLabelTextWidth)
						this.maxLabelTextWidth = length;
				}
			}
		}

		int viewWidth = this.maxLabelTextWidth + 8 + (width / 2);
		labelX = (this.width / 2) - (viewWidth / 2);
		controlX = labelX + maxLabelTextWidth + 8;
		resetX = (this.width / 2) + (viewWidth / 2) - 45;
		controlWidth = resetX - controlX - 5;
		scrollBarX = this.width;

		for (ConfigValue configElement : parent.configElements) {
			if(configElement instanceof BooleanValue) {
				this.listEntries.add(new GuiConfigEntries.BooleanEntry(this.owningScreen, this, configElement));
			}
//			if (configElement != null && configElement.showInGui()) {
//				if (configElement.isProperty()) {
//					if (configElement.isList())
//						this.listEntries.add(new GuiConfigEntries.ArrayEntry(this.owningScreen, this, configElement));
//					else if (configElement.getType() == ConfigGuiType.BOOLEAN)
//						this.listEntries.add(new GuiConfigEntries.BooleanEntry(this.owningScreen, this, configElement));
//					else if (configElement.getType() == ConfigGuiType.INTEGER)
//						this.listEntries.add(new GuiConfigEntries.IntegerEntry(this.owningScreen, this, configElement));
//					else if (configElement.getType() == ConfigGuiType.DOUBLE)
//						this.listEntries.add(new GuiConfigEntries.DoubleEntry(this.owningScreen, this, configElement));
//					else if (configElement.getType() == ConfigGuiType.COLOR) {
//						if (configElement.getValidValues() != null && configElement.getValidValues().length > 0)
//							this.listEntries.add(new GuiConfigEntries.ChatColorEntry(this.owningScreen, this, configElement));
//						else
//							this.listEntries.add(new GuiConfigEntries.StringEntry(this.owningScreen, this, configElement));
//					} else if (configElement.getType() == ConfigGuiType.MOD_ID) {
//						Map<Object, String> values = new TreeMap<Object, String>();
//						for (ModContainer mod : Loader.instance().getActiveModList())
//							values.put(mod.getModId(), mod.getName());
//						values.put("minecraft", "Minecraft");
//						this.listEntries.add(new SelectValueEntry(this.owningScreen, this, configElement, values));
//					} else if (configElement.getType() == ConfigGuiType.STRING) {
//						if (configElement.getValidValues() != null && configElement.getValidValues().length > 0)
//							this.listEntries.add(new GuiConfigEntries.CycleValueEntry(this.owningScreen, this, configElement));
//						else
//							this.listEntries.add(new GuiConfigEntries.StringEntry(this.owningScreen, this, configElement));
//					}
//				} else if (configElement.getType() == ConfigGuiType.CONFIG_CATEGORY)
//					this.listEntries.add(new CategoryEntry(this.owningScreen, this, configElement));
//			}
		}
	}

	/**
	 * Calls the drawToolTip() method for all IConfigEntry objects on this screen. This is called from the parent GuiConfig screen
	 * after drawing all other elements.
	 */
	public void drawScreenPost(final int mouseX, final int mouseY, final float partialTicks) {
		for (IConfigEntry entry : this.listEntries)
			entry.drawToolTip(mouseX, mouseY);
	}

	/**
	 * Returns true if all IConfigEntry objects on this screen are set to default. If includeChildren is true sub-category
	 * objects are checked as well.
	 */
	public boolean areAllEntriesDefault(boolean includeChildren) {
		for (IConfigEntry entry : this.listEntries)
//			if (includeChildren || !(entry instanceof CategoryEntry))
			if (!entry.isDefault())
				return false;
		return true;
	}

	/**
	 * Sets all IConfigEntry objects on this screen to default. If includeChildren is true sub-category objects are set as
	 * well.
	 */
	public void resetToDefault(boolean includeChildren) {
		for (IConfigEntry entry : this.listEntries)
//			if ((includeChildren || !(entry instanceof CategoryEntry)))
			entry.setToDefault();
	}

	/**
	 * Returns true if any IConfigEntry objects on this screen are changed. If includeChildren is true sub-category objects
	 * are checked as well.
	 */
	public boolean hasChangedEntry(boolean includeChildren) {
		for (IConfigEntry entry : this.listEntries)
//			if (includeChildren || !(entry instanceof CategoryEntry))
			if (entry.isChanged())
				return true;
		return false;
	}

	/**
	 * Returns true if any IConfigEntry objects on this screen are enabled. If includeChildren is true sub-category objects
	 * are checked as well.
	 */
	public boolean areAnyEntriesEnabled(boolean includeChildren) {
		for (IConfigEntry entry : this.listEntries)
//			if (includeChildren || !(entry instanceof CategoryEntry))
			if (entry.enabled())
				return true;
		return false;
	}

	/**
	 * Reverts changes to all IConfigEntry objects on this screen. If includeChildren is true sub-category objects are
	 * reverted as well.
	 */
	public void undoChanges(boolean includeChildren) {
		for (IConfigEntry entry : this.listEntries)
//			if ((includeChildren || !(entry instanceof CategoryEntry)))
			entry.undoChanges();
	}

	public void onGuiClosed() {
	}

	public void initGui() {
		this.width = this.owningScreen.width;
		this.height = this.owningScreen.height;

		this.maxLabelTextWidth = 0;
		for (IConfigEntry entry : this.listEntries)
			if (entry.getLabelWidth() > this.maxLabelTextWidth)
				this.maxLabelTextWidth = entry.getLabelWidth();

		this.top = this.owningScreen.subTitle != null ? 33 : 23;
		this.bottom = this.owningScreen.height - 32;
		this.left = 0;
		this.right = this.width;
		int viewWidth = this.maxLabelTextWidth + 8 + (this.width / 2);
		labelX = (this.width / 2) - (viewWidth / 2);
		controlX = this.labelX + this.maxLabelTextWidth + 8;
		resetX = (this.width / 2) + (viewWidth / 2) - 45;

		this.maxEntryRightBound = 0;
		for (IConfigEntry entry : this.listEntries)
			if (entry.getEntryRightBound() > this.maxEntryRightBound)
				this.maxEntryRightBound = entry.getEntryRightBound();

		scrollBarX = this.maxEntryRightBound + 5;
		controlWidth = this.maxEntryRightBound - this.controlX - 45;
	}

}
