package io.github.cadiboo.nocubes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * There is more to this mixin, see the documentation in {@link io.github.cadiboo.nocubes.hooks.MixinAsm}
 * and {@link io.github.cadiboo.nocubes.hooks.MixinAsm#transformSodiumLevelRenderer}.
 */
@Pseudo // Sodium may not be installed
@Mixin(targets = "net.minecraft.client.renderer.LevelRenderer")
public class SodiumLevelRendererMixin {
}
