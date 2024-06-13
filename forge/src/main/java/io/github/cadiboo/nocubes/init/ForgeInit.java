package io.github.cadiboo.nocubes.init;

import io.github.cadiboo.nocubes.Constants;
import io.github.cadiboo.nocubes.NoCubes;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ForgeInit {

	public ForgeInit() {
		// Use Forge to bootstrap the Common mod.
		Constants.LOG.info("Hello Forge world!");
		NoCubes.init();
	}
}
