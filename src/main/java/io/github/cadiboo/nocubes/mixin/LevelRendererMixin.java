package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WorldRenderer.class)
public class LevelRendererMixin {

	/**
	 * The method 'setBlocksDirty' gets called when a block is updated and marked for re-render.
	 * Extending the size of the area that gets updated fixes seams that appear when meshes along chunk borders change.
	 */
	@ModifyConstant(
		method = {
			"setBlockDirty(Lnet/minecraft/util/math/BlockPos;Z)V",
			"setBlocksDirty(IIIIII)V",
		},
		constant = @Constant(intValue = 1),
		require = 6 * 2 // 6 replacements for each method, targets 2 methods
	)
	public int nocubes_setBlocksDirty(int originalValue) {
		// Math.max so if someone else also modifies the value (e.g. to 3) we don't overwrite their extension
		return NoCubesConfig.Client.render ? Math.max(2, originalValue) : originalValue;
	}

}
