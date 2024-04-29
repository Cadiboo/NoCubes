package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.hooks.MixinAsm;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Allows NoCubes to
 * - {@link #shouldApply} - conditionally enable/disable its Mixins, depending on what mods are installed
 * - {@link #transformClass} - transform classes in ways that Mixins can't
 */
public final class NoCubesMixinPlugin implements IMixinConfigPlugin {

	private final boolean sodiumInstalled;
	private final boolean optiFineInstalled;
	private final HashSet<String> runTransformers = new HashSet<>();

	public NoCubesMixinPlugin() {
		var loadedModIds = LoadingModList.get().getMods().stream().map(ModInfo::getModId).collect(Collectors.toSet());
		sodiumInstalled = loadedModIds.contains("sodium") || loadedModIds.contains("rubidium") || loadedModIds.contains("embeddium");
		optiFineInstalled = ClassInfo.forName("net.optifine.Config") != null;
	}

	boolean shouldApply(String mixinClassName) {
		if (mixinClassName.equals("io.github.cadiboo.nocubes.mixin.client.LevelRendererMixin"))
			return !sodiumInstalled;
		if (mixinClassName.startsWith("io.github.cadiboo.nocubes.mixin.client.optifine"))
			return optiFineInstalled;
		if (mixinClassName.startsWith("io.github.cadiboo.nocubes.mixin.client.sodium"))
			return sodiumInstalled;
		return true;
	}

	void transformClass(String mixinClassName, ClassNode classNode) {
		switch (mixinClassName) {
			case "io.github.cadiboo.nocubes.mixin.client.RenderChunkRebuildTaskMixin" -> transformOnce(mixinClassName, classNode, MixinAsm::transformChunkRenderer);
			case "io.github.cadiboo.nocubes.mixin.client.LiquidBlockRendererMixin" -> transformOnce(mixinClassName, classNode, MixinAsm::transformFluidRenderer);
			case "io.github.cadiboo.nocubes.mixin.client.sodium.ChunkBuilderMeshingTaskMixin" -> transformOnce(mixinClassName, classNode, MixinAsm::transformSodiumChunkRenderer);
			case "io.github.cadiboo.nocubes.mixin.client.sodium.FluidRendererMixin" -> transformOnce(mixinClassName, classNode, MixinAsm::transformSodiumFluidRenderer);
			case "io.github.cadiboo.nocubes.mixin.client.sodium.WorldRendererMixin" -> transformOnce(mixinClassName, classNode, MixinAsm::transformSodiumWorldRenderer);
			case "io.github.cadiboo.nocubes.mixin.client.sodium.LevelRendererMixin" -> transformOnce(mixinClassName, classNode, MixinAsm::transformSodiumLevelRenderer);
		}
	}

	void transformOnce(String mixinClassName, ClassNode classNode, Consumer<ClassNode> transformer) {
		if (runTransformers.add(mixinClassName)) {
			transformer.accept(classNode);
		}
	}

	// region IMixinConfigPlugin boilerplate
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return shouldApply(mixinClassName);
	}

	@Override
	public void onLoad(String mixinPackage) {
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
		transformClass(mixinClassName, targetClass);
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
	// endregion

}
