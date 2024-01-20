package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.hooks.MixinAsm;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Allows NoCubes to
 * - conditionally enable/disable its Mixins, depending on what mods are installed
 * - transform classes in ways that Mixins can't
 */
public final class NoCubesMixinPlugin implements IMixinConfigPlugin {

	// region Conditional mixins

	private static boolean shouldApplyMixin(String mixinClassName) {
		return switch (mixinClassName) {
			case "io.github.cadiboo.nocubes.mixin.LevelRendererMixin" -> !isSodiumLoaded();
			case "io.github.cadiboo.nocubes.mixin.SodiumLevelRendererMixin" -> isSodiumLoaded();
			case "io.github.cadiboo.nocubes.mixin.SodiumWorldRendererMixin" -> isSodiumLoaded();
			case "io.github.cadiboo.nocubes.mixin.SodiumChunkBuilderMeshingTaskMixin" -> isSodiumLoaded();
			default -> true;
		};
	}

	private static boolean isSodiumLoaded() {
		var mods = LoadingModList.get();
		if (mods.getModFileById("rubidium") != null)
			return true;
		if (mods.getModFileById("embeddium") != null)
			return true;
		return false;
	}

	// endregion

	private static void transformClass(String mixinClassName, ClassNode classNode) {
		switch (mixinClassName) {
			case "io.github.cadiboo.nocubes.mixin.RenderChunkRebuildTaskMixin" -> MixinAsm.transformChunkRenderer(classNode);
			case "io.github.cadiboo.nocubes.mixin.LiquidBlockRendererMixin" -> MixinAsm.transformFluidRenderer(classNode);
			case "io.github.cadiboo.nocubes.mixin.SodiumChunkBuilderMeshingTaskMixin" -> MixinAsm.transformSodiumChunkRenderer(classNode);
			case "io.github.cadiboo.nocubes.mixin.SodiumFluidRendererMixin" -> MixinAsm.transformSodiumFluidRenderer(classNode);
			case "io.github.cadiboo.nocubes.mixin.SodiumWorldRendererMixin" -> MixinAsm.transformSodiumWorldRenderer(classNode);
			case "io.github.cadiboo.nocubes.mixin.SodiumLevelRendererMixin" -> MixinAsm.transformSodiumLevelRenderer(classNode);
		}
	}

	// region IMixinConfigPlugin boilerplate
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return shouldApplyMixin(mixinClassName);
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
