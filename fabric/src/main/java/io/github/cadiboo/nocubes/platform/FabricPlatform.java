package io.github.cadiboo.nocubes.platform;

import io.github.cadiboo.nocubes.platform.services.IPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.*;

public class FabricPlatform implements IPlatform {

	@Override
	public String getPlatformName() {
		return "Fabric";
	}

	@Override
	public boolean isModLoaded(String modId) {

		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {

		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	@Override
	public Color parseColor(String color) {
		throw new RuntimeException("Not Implemented");
	}
}
