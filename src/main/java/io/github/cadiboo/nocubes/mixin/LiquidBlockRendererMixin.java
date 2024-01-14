package io.github.cadiboo.nocubes.mixin;

import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Yes, this is empty.
 * This is because our transformations are applied in {@link NoCubesMixinPlugin#transformClass}.
 * DO NOT DELETE IT.
 * It needs to exist so that {@link NoCubesMixinPlugin#transformClass} runs properly.
 */
@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {
}
