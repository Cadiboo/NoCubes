package io.github.cadiboo.nocubes.forge;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import io.github.cadiboo.nocubes.platform.IMixinPlatform;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.util.Set;
import java.util.stream.Collectors;

public class MixinPlatform implements IMixinPlatform {
	@Override
	public Set<String> getLoadedModIds() {
		return LoadingModList.get().getMods().stream().map(ModInfo::getModId).collect(Collectors.toSet());
	}

	@Override
	public void onLoad() {
		MixinExtrasBootstrap.init();
	}
}
