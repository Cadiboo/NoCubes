package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

	/**
	 * The method 'setBlocksDirty' gets called when a block is updated and marked for re-render.
	 * Extending the size of the area that gets updated fixes seams that appear when meshes along chunk borders change.
	 */
	private static int nocubes_updateDirtyRenderAreaExtension(int originalValue) {
		// Math.max so if someone else also modifies the value (e.g. to 3) we don't overwrite their extension
		return NoCubesConfig.Client.render ? Math.max(2, originalValue) : originalValue;
	}

	/**
	 * @see #nocubes_updateDirtyRenderAreaExtension
	 */
	@ModifyConstant(
		method = "setBlocksDirty(IIIIII)V",
		constant = @Constant(intValue = 1)
		// No require here to allow compatibility with Rubidium
		// This is needed since Rubidium @Overwrites the setBlockDirty method
		// See https://github.com/Asek3/Rubidium/blob/f93e979e6a4caeb8c620370a5911d825098b172f/src/main/java/me/jellysquid/mods/sodium/mixin/features/chunk_rendering/MixinWorldRenderer.java#L138-L145
	)
	public int nocubes_setBlocksDirty(int originalValue) {
		return nocubes_updateDirtyRenderAreaExtension(originalValue);
	}

	/**
	 * @see #nocubes_updateDirtyRenderAreaExtension
	 */
	@ModifyConstant(
		method = "setBlockDirty(Lnet/minecraft/core/BlockPos;Z)V",
		constant = @Constant(intValue = 1),
		require = 6
	)
	public int nocubes_setBlockDirty(int originalValue) {
		return nocubes_updateDirtyRenderAreaExtension(originalValue);
	}

}
