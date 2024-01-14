package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.hooks.Hooks;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

	/**
	 * @see Hooks#expandDirtyRenderAreaExtension
	 */
	@ModifyConstant(
		method = {
			"setBlockDirty(Lnet/minecraft/core/BlockPos;Z)V",
			"setBlocksDirty(IIIIII)V",
		},
		constant = @Constant(intValue = 1),
		require = 6 * 2 // 6 replacements for each method, targets 2 methods
	)
	public int nocubes_setBlocksDirty(int originalValue) {
		return Hooks.expandDirtyRenderAreaExtension(originalValue);
	}

}
