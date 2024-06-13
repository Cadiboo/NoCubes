package io.github.cadiboo.nocubes.init;

import io.github.cadiboo.nocubes.Constants;
import io.github.cadiboo.nocubes.NoCubes;
import net.fabricmc.api.ModInitializer;

public class FabricInit implements ModInitializer {

	@Override
	public void onInitialize() {
		Constants.LOG.info("Hello Fabric world!");
		NoCubes.init();
	}
}
