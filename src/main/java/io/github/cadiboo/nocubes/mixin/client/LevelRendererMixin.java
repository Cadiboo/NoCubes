package io.github.cadiboo.nocubes.mixin.client;

import io.github.cadiboo.nocubes.hooks.ClientHooks;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

	/**
	 * @see ClientHooks#expandDirtyRenderAreaExtension
	 */
	@ModifyConstant(
		method = {
			"setBlockDirty(Lnet/minecraft/core/BlockPos;Z)V",
			"setBlocksDirty(IIIIII)V",
		},
		constant = @Constant(intValue = 1),
		require = 6 * 2 // 6 replacements for each method, targets 2 methods
	)
	public int noCubes$setBlocksDirty(int originalValue) {
		return ClientHooks.expandDirtyRenderAreaExtension(originalValue);
	}

}
