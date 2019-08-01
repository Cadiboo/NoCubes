package io.github.cadiboo.nocubes.client.gui.config;

import io.github.cadiboo.nocubes.client.ClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
public final class NoCubesConfigGui extends GuiScreen {

	private final Minecraft minecraft;
	private final GuiScreen parentScreen;
	private final TextComponentTranslation title;

	private ConfigOptionsList configOptionsList;

	public NoCubesConfigGui(final Minecraft minecraft, final GuiScreen parentScreen) {
		super();
		title = new TextComponentTranslation(MOD_ID + ".config.title");
		this.minecraft = minecraft;
		this.parentScreen = parentScreen;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();

		this.configOptionsList.drawScreen(mouseX, mouseY, partialTicks);

		this.drawCenteredString(this.minecraft.fontRenderer, this.title.getFormattedText(), this.width / 2, 8, 0xFFFFFF);

//		boolean flag = false;
//		for (KeyBinding keybinding : this.options.keyBindings) {
//			if (!keybinding.isDefault()) {
//				flag = true;
//				break;
//			}
//		}
//		this.field_146493_s.active = flag;
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

//
//	@Override
//	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
////		if (this.buttonId != null) {
////			if (p_keyPressed_1_ == 256) {
////				this.buttonId.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.getActiveModifier(), InputMappings.INPUT_INVALID);
////				this.options.setKeyBindingCode(this.buttonId, InputMappings.INPUT_INVALID);
////			} else {
////				this.buttonId.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.getActiveModifier(), InputMappings.getInputByCode(p_keyPressed_1_, p_keyPressed_2_));
////				this.options.setKeyBindingCode(this.buttonId, InputMappings.getInputByCode(p_keyPressed_1_, p_keyPressed_2_));
////			}
////
////			if (!net.minecraftforge.client.settings.KeyModifier.isKeyCodeModifier(this.buttonId.getKey()))
////				this.buttonId = null;
////			this.time = Util.milliTime();
////			KeyBinding.resetKeyBindingArrayAndHash();
////			return true;
////		} else {
////			return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
////		}
//		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
//	}
//
//	@Override
//	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
////		if (this.buttonId != null) {
////			this.options.setKeyBindingCode(this.buttonId, InputMappings.Type.MOUSE.getOrMakeInput(p_mouseClicked_5_));
////			this.buttonId = null;
////			KeyBinding.resetKeyBindingArrayAndHash();
////			return true;
////		} else {
//		return super.mouseClicked(mouseX, mouseY, mouseButton);
////		}
//	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	@Override
	protected void keyTyped(char eventChar, int eventKey) {
		if (eventKey == Keyboard.KEY_ESCAPE)
			this.mc.displayGuiScreen(this.parentScreen);
		else
			this.configOptionsList.keyTyped(eventChar, eventKey);
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	@Override
	protected void mouseClicked(int x, int y, int mouseEvent) throws IOException {
		if (mouseEvent != 0 || !this.configOptionsList.mouseClicked(x, y, mouseEvent)) {
			this.configOptionsList.mouseClickedPassThru(x, y, mouseEvent);
			super.mouseClicked(x, y, mouseEvent);
		}
	}

	/**
	 * Called when a mouse button is released.
	 */
	@Override
	protected void mouseReleased(int x, int y, int mouseEvent) {
		if (mouseEvent != 0 || !this.configOptionsList.mouseReleased(x, y, mouseEvent)) {
			super.mouseReleased(x, y, mouseEvent);
		}
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 2000) {
			if (this.configOptionsList.saveChanged())
				ClientUtil.tryReloadRenderers();
			this.minecraft.displayGuiScreen(parentScreen);
		}
//		super.actionPerformed(button);
	}

	@Override
	public void initGui() {
		super.initGui();

		FontRenderer fontRenderer = this.minecraft.fontRenderer;

		final String doneText = I18n.format("gui.done");
//		final String cancelText = I18n.format("gui.cancel");

		final int doneWidth = Math.max(fontRenderer.getStringWidth(doneText) + 20, 100);
//		final int cancelWidth = Math.max(fontRenderer.getStringWidth(cancelText) + 20, 100);

		final int buttonWidthHalf = (doneWidth + 5) / 2;

		final int halfWidth = this.width / 2;
		final int buttonsHeight = this.height - 29;

		this.addButton(new GuiButtonExt(2000, halfWidth - buttonWidthHalf, buttonsHeight, doneWidth, 20, doneText));

		this.configOptionsList = new ConfigOptionsList(this, this.minecraft);

//		this.children.add(this.configOptionsList);
//		this.field_146493_s = this.addButton(new Button(this.width / 2 - 155, this.height - 29, 150, 20, I18n.format("controls.resetAll"), (p_213125_1_) -> {
//			for (KeyBinding keybinding : this.minecraft.gameSettings.keyBindings) {
//				keybinding.setToDefault();
//			}
//
//			KeyBinding.resetKeyBindingArrayAndHash();
//		}));
//		this.addButton(new Button(this.width / 2 - 155 + 160, this.height - 29, 150, 20, I18n.format("gui.done"), (p_213124_1_) -> {
//			this.minecraft.displayGuiScreen(this.parentScreen);
//		}));
	}

	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.configOptionsList.handleMouseInput();
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen() {
		super.updateScreen();
		this.configOptionsList.updateScreen();
	}

}
