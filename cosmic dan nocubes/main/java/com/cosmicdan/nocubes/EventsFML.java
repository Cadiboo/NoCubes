package com.cosmicdan.nocubes;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;

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
