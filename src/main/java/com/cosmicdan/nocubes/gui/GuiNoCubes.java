package com.cosmicdan.nocubes.gui;

import com.cosmicdan.nocubes.Main;
import com.cosmicdan.nocubes.ModConfig;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiNoCubes extends GuiScreen {
	boolean	shouldSaveAndRefresh	= false;
	boolean	shouldClose				= false;

	boolean	MOD_ENABLED_NEW			= ModConfig.MOD_ENABLED;
	boolean	AUTOSTEPUP_ENABLED_NEW	= ModConfig.AUTOSTEPUP_ENABLED;

	@Override
	public void initGui() {
		this.buttonList.clear();
		final GuiButton buttonModEnabled = new GuiButton(0, (this.width / 2) - 100, (this.height / 2) - 40, 200, 20, "Block Smoothing " + (this.MOD_ENABLED_NEW ? "enabled" : "disabled"));
		this.buttonList.add(buttonModEnabled);
		final GuiButton buttonAutostep = new GuiButton(1, (this.width / 2) - 100, (this.height / 2) - 20, 200, 20, "Auto-Stepup " + (this.AUTOSTEPUP_ENABLED_NEW ? "enabled" : "disabled"));
		this.buttonList.add(buttonAutostep);
		buttonAutostep.enabled = false; // TODO
		final GuiButton buttonCancel = new GuiButton(2, (this.width / 2) - 100, (this.height / 2) + 20, 98, 20, "Cancel");
		this.buttonList.add(buttonCancel);
		final GuiButton buttonSave = new GuiButton(3, (this.width / 2) + 2, (this.height / 2) + 20, 98, 20, "Save");
		this.buttonList.add(buttonSave);
	}

	@Override
	protected void actionPerformed(final GuiButton button) {
		if (button.enabled) {
			switch (button.id) {
				case 0:
					this.MOD_ENABLED_NEW = !this.MOD_ENABLED_NEW;
					button.displayString = "Block Smoothing " + (this.MOD_ENABLED_NEW ? "enabled" : "disabled");
					break;
				case 1:
					this.AUTOSTEPUP_ENABLED_NEW = !this.AUTOSTEPUP_ENABLED_NEW;
					button.displayString = "Auto-Stepup " + (this.AUTOSTEPUP_ENABLED_NEW ? "enabled" : "disabled");
					break;
				case 3:
					this.shouldSaveAndRefresh = true;
				case 2:
					this.shouldClose = true;
					break;
			}
		}
	}

	@Override
	public void onGuiClosed() {
		if (this.shouldSaveAndRefresh) {
			Main.LOGGER.info("Saving config and reloading resources...");
			ModConfig.setEnabled(this.MOD_ENABLED_NEW);
			ModConfig.setStepup(this.AUTOSTEPUP_ENABLED_NEW);
			ModConfig.saveIfChanged();
		}
	}

	@Override
	public void drawScreen(final int x, final int y, final float unused) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, "No Cubes Settings", this.width / 2, (this.height / 2) - 70, 0xffffff);
		super.drawScreen(x, y, unused);
		if (this.shouldClose) {
			if (this.shouldSaveAndRefresh) {
				this.mc.refreshResources();
			}
			this.mc.displayGuiScreen(null);
		}
	}
}
