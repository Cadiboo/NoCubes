package com.cosmicdan.nocubes;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class EventsFML {
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Main.KEYBIND_SETTINGS.isPressed()) {
            Main.openSettingsGui();
        }
        //else if ((Main.KEYBIND_DEBUG != null) && (Main.KEYBIND_DEBUG.isPressed())) {
        //    Minecraft.getMinecraft().refreshResources();
        //}
    }
}
