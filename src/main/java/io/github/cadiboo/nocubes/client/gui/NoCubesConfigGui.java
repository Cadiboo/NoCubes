package io.github.cadiboo.nocubes.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.MouseSettingsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.AbstractOption;
import net.minecraft.util.text.TranslationTextComponent;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
public class NoCubesConfigGui extends Screen {

	private final Minecraft minecraft;
	private final Screen parentScreen;

	private Button renderSmoothTerrain;
	private ConfigOptionsList configOptionsList;

	public NoCubesConfigGui(final Minecraft minecraft, final Screen parentScreen) {
		super(new TranslationTextComponent(MOD_ID + ".config.title"));
		this.minecraft = minecraft;
		this.parentScreen = parentScreen;
	}

	@Override
	public void render(final int mouseX, final int mouseY, final float partialTicks) {
		this.renderBackground();

		this.configOptionsList.render(mouseX, mouseY, partialTicks);

		this.drawCenteredString(this.minecraft.fontRenderer, this.title.getFormattedText(), this.width / 2, 8, 0xFFFFFF);

//		boolean flag = false;
//		for (KeyBinding keybinding : this.options.keyBindings) {
//			if (!keybinding.isDefault()) {
//				flag = true;
//				break;
//			}
//		}
//		this.field_146493_s.active = flag;
		super.render(mouseX, mouseX, partialTicks);
	}

	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
//		if (this.buttonId != null) {
//			if (p_keyPressed_1_ == 256) {
//				this.buttonId.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.getActiveModifier(), InputMappings.INPUT_INVALID);
//				this.options.setKeyBindingCode(this.buttonId, InputMappings.INPUT_INVALID);
//			} else {
//				this.buttonId.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.getActiveModifier(), InputMappings.getInputByCode(p_keyPressed_1_, p_keyPressed_2_));
//				this.options.setKeyBindingCode(this.buttonId, InputMappings.getInputByCode(p_keyPressed_1_, p_keyPressed_2_));
//			}
//
//			if (!net.minecraftforge.client.settings.KeyModifier.isKeyCodeModifier(this.buttonId.getKey()))
//				this.buttonId = null;
//			this.time = Util.milliTime();
//			KeyBinding.resetKeyBindingArrayAndHash();
//			return true;
//		} else {
//			return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
//		}
		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	protected void init() {
		super.init();

		FontRenderer fontRenderer = this.minecraft.fontRenderer;

		final String doneText = I18n.format("gui.done");

		final int doneWidth = Math.max(fontRenderer.getStringWidth(doneText) + 20, 100);

		final int buttonWidthHalf = (doneWidth + 5) / 2;

		final int halfWidth = this.width / 2;
		final int buttonsHeight = this.height - 29;

		this.addButton(new Button(halfWidth - buttonWidthHalf, buttonsHeight, doneWidth, 20, doneText,
				$ -> this.minecraft.displayGuiScreen(parentScreen)
		));

		this.addButton(new Button(this.width / 2 - 155, 18, 150, 20, I18n.format("options.mouse_settings"), (p_213126_1_) -> {
			this.minecraft.displayGuiScreen(new MouseSettingsScreen(this));
		}));
		this.addButton(AbstractOption.AUTO_JUMP.createWidget(this.minecraft.gameSettings, this.width / 2 - 155 + 160, 18, 150));
		this.configOptionsList = new ConfigOptionsList(this, this.minecraft);
		this.children.add(this.configOptionsList);
//		this.field_146493_s = this.addButton(new Button(this.width / 2 - 155, this.height - 29, 150, 20, I18n.format("controls.resetAll"), (p_213125_1_) -> {
//			for (KeyBinding keybinding : this.minecraft.gameSettings.keyBindings) {
//				keybinding.setToDefault();
//			}
//
//			KeyBinding.resetKeyBindingArrayAndHash();
//		}));
		this.addButton(new Button(this.width / 2 - 155 + 160, this.height - 29, 150, 20, I18n.format("gui.done"), (p_213124_1_) -> {
			this.minecraft.displayGuiScreen(this.parentScreen);
		}));
	}

	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
//		if (this.buttonId != null) {
//			this.options.setKeyBindingCode(this.buttonId, InputMappings.Type.MOUSE.getOrMakeInput(p_mouseClicked_5_));
//			this.buttonId = null;
//			KeyBinding.resetKeyBindingArrayAndHash();
//			return true;
//		} else {
		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
//		}
	}

}
