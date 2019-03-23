package io.github.cadiboo.nocubes.config;

import net.minecraftforge.common.config.Config;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;

/**
 * @author Cadiboo
 */
@Config.LangKey(MOD_ID + ".config.fluid")  // Doesn't work for some reason
@Config(modid = MOD_ID)
public final class FluidConfig {

	@Config.LangKey(MOD_ID + ".config.fluid.smoothFluidBiomeColorTransitions") // Doesn't work for some reason
	public boolean smoothFluidBiomeColorTransitions = true;

	@Config.LangKey(MOD_ID + ".config.fluid.smoothFluidLighting") // Doesn't work for some reason
	public boolean smoothFluidLighting = true;

	@Config.LangKey(MOD_ID + ".config.fluid.naturalFluidTextures") // Doesn't work for some reason
	public boolean naturalFluidTextures = false;

	public static boolean areSmoothFluidBiomeColorTransitionsEnabled() {
		return ModConfig.fluidConfig.smoothFluidBiomeColorTransitions;
	}

	public static boolean isSmoothFluidLightingEnabled() {
		return ModConfig.fluidConfig.smoothFluidLighting;
	}

	public static boolean areNaturalFluidTexturesEnabled() {
		return ModConfig.fluidConfig.naturalFluidTextures;
	}

}
