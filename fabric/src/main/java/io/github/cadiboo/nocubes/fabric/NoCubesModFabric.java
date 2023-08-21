package net.examplemod.fabric;

import io.github.cadiboo.nocubes.common.NoCubesMod;
import net.fabricmc.api.ModInitializer;

public class NoCubesModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NoCubesMod.init();
    }
}
