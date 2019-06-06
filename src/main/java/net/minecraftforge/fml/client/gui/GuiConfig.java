package net.minecraftforge.fml.client.gui;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.config.IConfigEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static net.minecraftforge.fml.client.config.GuiUtils.RESET_CHAR;
import static net.minecraftforge.fml.client.config.GuiUtils.UNDO_CHAR;

/**
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class GuiConfig extends GuiScreen {

	public final Minecraft minecraft;
	public final GuiScreen parent;
	public final String modID;
	/**
	 * When set to a non-null value the OnConfigChanged and PostConfigChanged events will be posted when the Done button is pressed
	 * if any configElements were changed (includes child screens). If not defined, the events will be posted if the parent gui is null
	 * or if the parent gui is not an instance of GuiConfig.
	 */
	@Nullable
	public final String configID;
	public final String title;
	@Nullable
	public final String subTitle;
	public final GuiConfigEntries entryList;
	public final boolean isWorldRunning;
	public final List<ConfigValue> configElements;

	public boolean needsRefresh = true;

	public GuiButtonExt buttonUndoChanges;
	public GuiButtonExt buttonResetToDefault;
	public GuiCheckBox checkBoxApplyToSubcategories;

	public HoverChecker resetToDefaultHoverChecker;
	public HoverChecker undoChangesHoverChecker;
	public HoverChecker applyToSubcategoriesHoverChecker;

	/**
	 * @param parent        the parent GuiScreen object
	 * @param modID         the mod ID for the mod whose config settings will be edited
	 * @param title         the desired title for this screen. For consistency it is recommended that you pass the path of the config file being
	 *                      edited.
	 * @param configClasses an array of classes annotated with {@code @Config} providing the configuration
	 */
	public GuiConfig(final Minecraft minecraft, final GuiScreen parent, final String modID, final String title, final Class<?>... configClasses) {
		this(minecraft, parent, modID, null, title, null, collectConfigElements(configClasses));
	}

	/**
	 * GuiConfig constructor that will use ConfigChangedEvent when editing is concluded. If a non-null value is passed for configID,
	 * the OnConfigChanged and PostConfigChanged events will be posted when the Done button is pressed if any configElements were changed
	 * (includes child screens). If configID is not defined, the events will be posted if the parent gui is null or if the parent gui
	 * is not an instance of GuiConfig.
	 *
	 * @param parent         the parent GuiScreen object
	 * @param modID          the mod ID for the mod whose config settings will be edited
	 * @param configID       an identifier that will be passed to the OnConfigChanged and PostConfigChanged events. Setting this value will force
	 *                       the save action to be called when the Done button is pressed on this screen if any configElements were changed.
	 * @param title          the desired title for this screen. For consistency it is recommended that you pass the path of the config file being
	 *                       edited.
	 * @param subTitle       the desired title second line for this screen. Typically this is used to send the category name of the category
	 *                       currently being edited.
	 * @param configElements a List of IConfigElement objects
	 */
	public GuiConfig(final Minecraft minecraft, final GuiScreen parent, final String modID, @Nullable final String configID, final String title, @Nullable final String subTitle, final List<ConfigValue> configElements) {
		this.minecraft = minecraft;
		this.parent = parent;
		this.modID = modID;
		this.configID = configID;
		if (title != null) {
			this.title = title;
		} else {
			this.title = "Config GUI";
		}
		this.subTitle = subTitle;

		this.configElements = configElements;
		this.entryList = new GuiConfigEntries(mc, this);

		this.isWorldRunning = minecraft.world != null;
	}

	public GuiConfig(final Minecraft minecraft, final GuiScreen parent) {
		this(minecraft, parent, NoCubes.MOD_ID, "NoCubes Config", GuiButtonClickConsumer.class, GuiButtonClickConsumer.class);
	}

	private static List<ConfigValue> collectConfigElements(Class<?>... configClasses) {
		List<ConfigValue> toReturn;
		if (configClasses.length == 1) {
			toReturn = ConfigElement.from(configClasses[0]).getChildElements();
		} else {
			toReturn = new ArrayList<>();
			for (Class<?> clazz : configClasses) {
				toReturn.add(ConfigElement.from(clazz));
			}
		}
		toReturn.sort(Comparator.comparing(e -> I18n.format(e.getLanguageKey())));
		return toReturn;
	}

	@Override
	public void render(final int mouseX, final int mouseY, final float partialTicks) {
		this.drawDefaultBackground();

		final FontRenderer fontRenderer = this.fontRenderer;

		this.drawCenteredString(fontRenderer, this.title, this.width / 2, 8, 0xFFFFFF);

		String subTitle = this.subTitle;
		if (subTitle != null) {
			int strWidth = fontRenderer.getStringWidth(subTitle);
			int ellipsisWidth = fontRenderer.getStringWidth("...");
			if (strWidth > width - 6 && strWidth > ellipsisWidth) {
				subTitle = fontRenderer.trimStringToWidth(subTitle, width - 6 - ellipsisWidth).trim() + "...";
			}
			this.drawCenteredString(fontRenderer, subTitle, this.width / 2, 18, 0xFFFFFF);
		}

		super.render(mouseX, mouseX, partialTicks);

		this.entryList.drawScreenPost(mouseX, mouseY, partialTicks);
		if (this.undoChangesHoverChecker.checkHover(mouseX, mouseY))
			this.drawToolTip(Arrays.asList(I18n.format("fml.configgui.tooltip.undoChanges").split("\n")), mouseX, mouseY);
		if (this.resetToDefaultHoverChecker.checkHover(mouseX, mouseY))
			this.drawToolTip(Arrays.asList(I18n.format("fml.configgui.tooltip.resetToDefault").split("\n")), mouseX, mouseY);
		if (this.applyToSubcategoriesHoverChecker.checkHover(mouseX, mouseY))
			this.drawToolTip(Arrays.asList(I18n.format("fml.configgui.tooltip.applyToSubcategories").split("\n")), mouseX, mouseY);

	}

	@Override
	protected void initGui() {
		super.initGui();

		FontRenderer fontRenderer = this.fontRenderer;

		final String doneText = I18n.format("gui.done");
		final String undoChangesText = " " + I18n.format("fml.configgui.tooltip.undoChanges");
		final String resetToDefaultText = " " + I18n.format("fml.configgui.tooltip.resetToDefault");
		final String applyToSubcategoriesText = I18n.format("fml.configgui.tooltip.applyToSubcategories");

		final int undoGlyphWidth = fontRenderer.getStringWidth(UNDO_CHAR) * 2;
		final int resetGlyphWidth = fontRenderer.getStringWidth(RESET_CHAR) * 2;

		final int doneWidth = Math.max(fontRenderer.getStringWidth(doneText) + 20, 100);
		final int undoChangesWidth = fontRenderer.getStringWidth(undoChangesText) + undoGlyphWidth + 20;
		final int resetToDefaultWidth = fontRenderer.getStringWidth(resetToDefaultText) + resetGlyphWidth + 20;
		final int applyToSubcategoriesWidth = fontRenderer.getStringWidth(applyToSubcategoriesText) + 13;

		final int buttonWidthHalf = (doneWidth + 5 + undoChangesWidth + 5 + resetToDefaultWidth + 5 + applyToSubcategoriesWidth) / 2;

		final int halfWidth = this.width / 2;
		final int buttonsHeight = this.height - 29;

		int buttonId = 2000;

		this.addButton(new GuiButtonClickConsumer(buttonId++, halfWidth - buttonWidthHalf, buttonsHeight, doneWidth, 20, doneText,
				(x, y) -> mc.displayGuiScreen(parent)
		));
		this.addButton(this.buttonUndoChanges = new GuiUnicodeGlyphButton(buttonId++, halfWidth - buttonWidthHalf + doneWidth + 5, buttonsHeight, undoChangesWidth, 20, undoChangesText, UNDO_CHAR, 2.0F,
				(x, y) -> this.entryList.undoChanges(this.checkBoxApplyToSubcategories.isChecked())
		));
		this.addButton(this.buttonResetToDefault = new GuiUnicodeGlyphButton(buttonId++, halfWidth - buttonWidthHalf + doneWidth + 5 + undoChangesWidth + 5, buttonsHeight, resetToDefaultWidth, 20, resetToDefaultText, RESET_CHAR, 2.0F,
				(x, y) -> this.entryList.resetToDefault(this.checkBoxApplyToSubcategories.isChecked())
		));
		this.addButton(this.checkBoxApplyToSubcategories = new GuiCheckBox(buttonId++, halfWidth - buttonWidthHalf + doneWidth + 5 + undoChangesWidth + 5 + resetToDefaultWidth + 5, this.height - 24, applyToSubcategoriesText, false));

		this.undoChangesHoverChecker = new HoverChecker(this.buttonUndoChanges, 800);
		this.resetToDefaultHoverChecker = new HoverChecker(this.buttonResetToDefault, 800);
		this.applyToSubcategoriesHoverChecker = new HoverChecker(checkBoxApplyToSubcategories, 800);

		this.entryList.initGui();
	}

	@Override
	public void tick() {
		super.tick();
		final boolean applyToSubcategories = this.checkBoxApplyToSubcategories.isChecked();
		this.buttonUndoChanges.enabled = this.entryList.areAnyEntriesEnabled(applyToSubcategories) && this.entryList.hasChangedEntry(applyToSubcategories);
		this.buttonResetToDefault.enabled = this.entryList.areAnyEntriesEnabled(applyToSubcategories) && !this.entryList.areAllEntriesDefault(applyToSubcategories);
	}

	@Override
	public void onGuiClosed() {
		this.entryList.onGuiClosed();

		if (this.configID != null && this.parent instanceof GuiConfig) {
			GuiConfig parentGuiConfig = (GuiConfig) this.parent;
			parentGuiConfig.needsRefresh = true;
			parentGuiConfig.initGui();
		}

//		if (!(this.parent instanceof GuiConfig))
//			Keyboard.enableRepeatEvents(false);
		super.onGuiClosed();
	}

	private void drawToolTip(List<String> stringList, int x, int y) {
		GuiUtils.drawHoveringText(stringList, x, y, width, height, 300, fontRenderer);
	}

}
