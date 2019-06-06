package net.minecraftforge.fml.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.config.IConfigEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.minecraftforge.fml.client.config.GuiUtils.RESET_CHAR;
import static net.minecraftforge.fml.client.config.GuiUtils.UNDO_CHAR;

/**
 * ListEntryBase
 * <p>
 * Provides a base entry for others to extend. Handles drawing the prop label (if drawLabel == true) and the Undo/Default buttons.
 */
public abstract class ListEntryBase extends GuiListExtended implements IConfigEntry {

	protected final GuiConfig owningScreen;
	protected final GuiConfigEntries owningEntryList;
	protected final IConfigElement configElement;
	protected final Minecraft mc;
	protected final String name;
	protected final GuiButtonExt btnUndoChanges;
	protected final GuiButtonExt btnDefault;
	protected List<String> toolTip;
	protected List<String> undoToolTip;
	protected List<String> defaultToolTip;
	protected boolean isValidValue = true;
	protected HoverChecker tooltipHoverChecker;
	protected HoverChecker undoHoverChecker;
	protected HoverChecker defaultHoverChecker;
	protected boolean drawLabel;

	public ListEntryBase(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
		this.owningScreen = owningScreen;
		this.owningEntryList = owningEntryList;
		this.configElement = configElement;
		this.mc = Minecraft.getInstance();
		String trans = I18n.format(configElement.getLanguageKey());
		if (!trans.equals(configElement.getLanguageKey()))
			this.name = trans;
		else
			this.name = configElement.getName();
		this.btnUndoChanges = new GuiButtonExt(0, 0, 0, 18, 18, UNDO_CHAR);
		this.btnDefault = new GuiButtonExt(0, 0, 0, 18, 18, RESET_CHAR);

		this.undoHoverChecker = new HoverChecker(this.btnUndoChanges, 800);
		this.defaultHoverChecker = new HoverChecker(this.btnDefault, 800);
		this.undoToolTip = Arrays.asList(I18n.format("fml.configgui.tooltip.undoChanges"));
		this.defaultToolTip = Arrays.asList(I18n.format("fml.configgui.tooltip.resetToDefault"));
		this.toolTip = new ArrayList<>();

		this.drawLabel = true;

		String comment;

		comment = I18n.format(configElement.getLanguageKey() + ".tooltip").replace("\\n", "\n");

		if (!comment.equals(configElement.getLanguageKey() + ".tooltip"))
			Collections.addAll(toolTip, (TextFormatting.GREEN + name + "\n" + TextFormatting.YELLOW + removeTag(comment, "[default:", "]")).split("\n"));
		else if (configElement.getComment() != null && !configElement.getComment().trim().isEmpty())
			Collections.addAll(toolTip, (TextFormatting.GREEN + name + "\n" + TextFormatting.YELLOW + removeTag(configElement.getComment(), "[default:", "]")).split("\n"));
		else
			Collections.addAll(toolTip, (TextFormatting.GREEN + name + "\n" + TextFormatting.RED + "No tooltip defined.").split("\n"));

		if ((configElement.getType() == ConfigGuiType.INTEGER
				&& (Integer.valueOf(configElement.getMinValue().toString()) != Integer.MIN_VALUE || Integer.valueOf(configElement.getMaxValue().toString()) != Integer.MAX_VALUE))
				|| (configElement.getType() == ConfigGuiType.DOUBLE
				&& (Double.valueOf(configElement.getMinValue().toString()) != -Double.MAX_VALUE || Double.valueOf(configElement.getMaxValue().toString()) != Double.MAX_VALUE)))
			Collections.addAll(toolTip, (TextFormatting.AQUA + I18n.format("fml.configgui.tooltip.defaultNumeric", configElement.getMinValue(), configElement.getMaxValue(), configElement.getDefault())).split("\n"));
		else if (configElement.getType() != ConfigGuiType.CONFIG_CATEGORY)
			Collections.addAll(toolTip, (TextFormatting.AQUA + I18n.format("fml.configgui.tooltip.default", configElement.getDefault())).split("\n"));

	}

	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
		boolean isChanged = isChanged();

		if (drawLabel) {
			String label = (!isValidValue ? TextFormatting.RED.toString() :
					(isChanged ? TextFormatting.WHITE.toString() : TextFormatting.GRAY.toString()))
					+ (isChanged ? TextFormatting.ITALIC.toString() : "") + this.name;
			this.mc.fontRenderer.drawString(
					label,
					this.owningScreen.entryList.labelX,
					y + slotHeight / 2 - this.mc.fontRenderer.FONT_HEIGHT / 2,
					16777215);
		}

		this.btnUndoChanges.x = this.owningEntryList.scrollBarX - 44;
		this.btnUndoChanges.y = y;
		this.btnUndoChanges.enabled = enabled() && isChanged;
		this.btnUndoChanges.render(mouseX, mouseY, partial);

		this.btnDefault.x = this.owningEntryList.scrollBarX - 22;
		this.btnDefault.y = y;
		this.btnDefault.enabled = enabled() && !isDefault();
		this.btnDefault.render(mouseX, mouseY, partial);

		if (this.tooltipHoverChecker == null)
			this.tooltipHoverChecker = new HoverChecker(y, y + slotHeight, x, this.owningScreen.entryList.controlX - 8, 800);
		else
			this.tooltipHoverChecker.updateBounds(y, y + slotHeight, x, this.owningScreen.entryList.controlX - 8);
	}

	/**
	 * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
	 * clicked and the list should not be dragged.
	 */
	@Override
	public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		if (this.btnDefault.mouseClicked(x, y)) {
			btnDefault.playPressSound(mc.getSoundHandler());
			setToDefault();
			return true;
		} else if (this.btnUndoChanges.mousePressed(this.mc, x, y)) {
			btnUndoChanges.playPressSound(mc.getSoundHandler());
			undoChanges();
			return true;
		}
		return false;
	}

	/**
	 * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
	 */
	@Override
	public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		this.btnDefault.mouseReleased(x, y);
	}

	@Override
	public void updatePosition(int p_178011_1_, int p_178011_2_, int p_178011_3_, float partial) {
	}

	@Override
	public IConfigElement getConfigElement() {
		return configElement;
	}

	@Override
	public String getName() {
		return configElement.getName();
	}

	@Override
	public abstract Object getCurrentValue();

	@Override
	public abstract Object[] getCurrentValues();

	@Override
	public boolean enabled() {
		return owningScreen.isWorldRunning ? !owningScreen.allRequireWorldRestart && !configElement.requiresWorldRestart() : true;
	}

	@Override
	public abstract void keyTyped(char eventChar, int eventKey);

	@Override
	public abstract void updateCursorCounter();

	@Override
	public abstract void mouseClicked(int x, int y, int mouseEvent);

	@Override
	public abstract boolean isDefault();

	@Override
	public abstract void setToDefault();

	@Override
	public abstract void undoChanges();

	@Override
	public abstract boolean isChanged();

	@Override
	public abstract boolean saveConfigElement();

	@Override
	public void drawToolTip(int mouseX, int mouseY) {
		boolean canHover = mouseY < this.owningScreen.entryList.bottom && mouseY > this.owningScreen.entryList.top;
		if (toolTip != null && this.tooltipHoverChecker != null) {
			if (this.tooltipHoverChecker.checkHover(mouseX, mouseY, canHover))
				this.owningScreen.drawToolTip(toolTip, mouseX, mouseY);
		}

		if (this.undoHoverChecker.checkHover(mouseX, mouseY, canHover))
			this.owningScreen.drawToolTip(undoToolTip, mouseX, mouseY);

		if (this.defaultHoverChecker.checkHover(mouseX, mouseY, canHover))
			this.owningScreen.drawToolTip(defaultToolTip, mouseX, mouseY);
	}

	@Override
	public int getLabelWidth() {
		return this.mc.fontRenderer.getStringWidth(this.name);
	}

	@Override
	public int getEntryRightBound() {
		return this.owningEntryList.resetX + 40;
	}

	@Override
	public void onGuiClosed() {
	}

	/**
	 * Get string surrounding tagged area.
	 */
	private String removeTag(String target, String tagStart, String tagEnd) {
		int tagStartPosition = tagStartPosition = target.indexOf(tagStart);
		int tagEndPosition = tagEndPosition = target.indexOf(tagEnd, tagStartPosition + tagStart.length());

		if (-1 == tagStartPosition || -1 == tagEndPosition)
			return target;

		String taglessResult = target.substring(0, tagStartPosition);
		taglessResult += target.substring(tagEndPosition + 1, target.length());

		return taglessResult;
	}

}
