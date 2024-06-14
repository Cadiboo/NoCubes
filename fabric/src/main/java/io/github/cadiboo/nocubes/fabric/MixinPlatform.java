package io.github.cadiboo.nocubes.fabric;

import io.github.cadiboo.nocubes.platform.IMixinPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Set;
import java.util.stream.Collectors;

public class MixinPlatform implements IMixinPlatform {
	@Override
	public Set<String> getLoadedModIds() {
		return FabricLoader.getInstance().getAllMods().stream().map(mc -> mc.getMetadata().getId()).collect(Collectors.toSet());
	}

	@Override
	public void onLoad() {
		// No op
	}
}
