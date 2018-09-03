package com.cosmicdan.nocubes;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventsForge {
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        ModConfig.buildSmoothBlockIds();
        //Minecraft.getMinecraft().refreshResources();
    }
}
