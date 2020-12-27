package io.github.cadiboo.nocubes.future;

import net.minecraftforge.fml.common.ModContainer;

public class ModLoadingContext {

	private final FutureModContainer container;

	public ModLoadingContext(ModContainer container) {
		this.container = new FutureModContainer(container);
	}

	private FutureModContainer getActiveContainer() {
		return container;
	}

	public void registerConfig(ModConfig.Type type, ForgeConfigSpec spec) {
		getActiveContainer().addConfig(new ModConfig(type, spec, getActiveContainer()));
	}


	public void registerConfig(ModConfig.Type type, ForgeConfigSpec spec, String fileName) {
		getActiveContainer().addConfig(new ModConfig(type, spec, getActiveContainer(), fileName));
	}

}
