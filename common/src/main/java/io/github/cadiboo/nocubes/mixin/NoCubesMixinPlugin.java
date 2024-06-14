package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.platform.IMixinPlatform;
import io.github.cadiboo.nocubes.platform.PlatformLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

import java.util.List;
import java.util.Set;

/**
 * Allows NoCubes to {@link #shouldApply conditionally enable/disable} its Mixins, depending on what mods are installed.
 */
public final class NoCubesMixinPlugin implements IMixinConfigPlugin {

	private final IMixinPlatform platform;
	private final boolean sodiumInstalled;
	private final boolean optiFineInstalled;
	private final boolean apoliInstalled;

	public NoCubesMixinPlugin() {
		platform = PlatformLoader.load(IMixinPlatform.class);
		var loadedModIds = platform.getLoadedModIds();
		sodiumInstalled = loadedModIds.contains("sodium") || loadedModIds.contains("rubidium") || loadedModIds.contains("embeddium");
		optiFineInstalled = ClassInfo.forName("net.optifine.Config") != null;
		apoliInstalled = loadedModIds.contains("apoli");
	}

	void onLoad() {
		platform.onLoad();
	}

	boolean shouldApply(String mixinClassName) {
		if (mixinClassName.equals("io.github.cadiboo.nocubes.mixin.client.NonSodiumLevelRendererMixin"))
			return !sodiumInstalled;
		if (mixinClassName.equals("io.github.cadiboo.nocubes.mixin.EntityMixin"))
			return !apoliInstalled;
		if (mixinClassName.startsWith("io.github.cadiboo.nocubes.mixin.client.optifine"))
			return optiFineInstalled;
		if (mixinClassName.startsWith("io.github.cadiboo.nocubes.mixin.client.sodium"))
			return sodiumInstalled;
		return true;
	}

	// region IMixinConfigPlugin boilerplate
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return shouldApply(mixinClassName);
	}

	@Override
	public void onLoad(String mixinPackage) {
		onLoad();
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
	// endregion

}
