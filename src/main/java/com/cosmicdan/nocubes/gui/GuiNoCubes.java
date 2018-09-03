package com.cosmicdan.nocubes.gui;

import com.cosmicdan.nocubes.Main;
import com.cosmicdan.nocubes.ModConfig;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiNoCubes extends GuiScreen {
    boolean shouldSaveAndRefresh = false; 
    boolean shouldClose = false;
    
    boolean MOD_ENABLED_NEW = ModConfig.MOD_ENABLED;
    boolean AUTOSTEPUP_ENABLED_NEW = ModConfig.AUTOSTEPUP_ENABLED;
    
    @Override
    public void initGui() {
        this.buttonList.clear();
        GuiButton buttonModEnabled = new GuiButton(0, this.width / 2 - 100, this.height / 2 - 40, 200, 20, "Block Smoothing " + (MOD_ENABLED_NEW ? "enabled" : "disabled"));
        buttonList.add(buttonModEnabled);
        GuiButton buttonAutostep = new GuiButton(1, this.width / 2 - 100, this.height / 2 - 20, 200, 20, "Auto-Stepup " + (AUTOSTEPUP_ENABLED_NEW ? "enabled" : "disabled")); 
        buttonList.add(buttonAutostep);
        buttonAutostep.enabled = false; // TODO
        GuiButton buttonCancel = new GuiButton(2, this.width / 2 - 100, this.height / 2 + 20, 98, 20, "Cancel");
        buttonList.add(buttonCancel);
        GuiButton buttonSave = new GuiButton(3, this.width / 2 + 2, this.height / 2 + 20, 98, 20, "Save");
        buttonList.add(buttonSave);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 0:
                    MOD_ENABLED_NEW = !MOD_ENABLED_NEW;
                    button.displayString = "Block Smoothing " + (MOD_ENABLED_NEW ? "enabled" : "disabled");
                    break;
                case 1:
                    AUTOSTEPUP_ENABLED_NEW = !AUTOSTEPUP_ENABLED_NEW;
                    button.displayString = "Auto-Stepup " + (AUTOSTEPUP_ENABLED_NEW ? "enabled" : "disabled");
                    break;
                case 3:
                    shouldSaveAndRefresh = true;
                case 2:
                    shouldClose = true;
                    break;
            }
        }
    }
    
    @Override
    public void onGuiClosed() {
        if (shouldSaveAndRefresh) {
            Main.LOGGER.info("Saving config and reloading resources...");
            ModConfig.setEnabled(MOD_ENABLED_NEW);
            ModConfig.setStepup(AUTOSTEPUP_ENABLED_NEW);
            ModConfig.saveIfChanged();
        }
    }
    
    @Override
    public void drawScreen(int x, int y, float unused) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, "No Cubes Settings", this.width / 2, this.height / 2 - 70, 0xffffff);
        super.drawScreen(x, y, unused);
        if (shouldClose) {
            if (shouldSaveAndRefresh)
                mc.refreshResources();
            mc.displayGuiScreen(null);
        }
    }
}
