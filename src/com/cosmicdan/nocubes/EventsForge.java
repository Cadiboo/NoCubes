package com.cosmicdan.nocubes;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.WorldEvent;

public class EventsForge {
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        ModConfig.buildSmoothBlockIds();
        //Minecraft.getMinecraft().refreshResources();
    }
}
