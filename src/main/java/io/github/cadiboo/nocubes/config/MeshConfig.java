package io.github.cadiboo.nocubes.config;

import net.minecraftforge.common.ForgeConfigSpec;

import static net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

/**
 * @author Cadiboo
 */
public final class MeshConfig {

	public static BooleanValue OFFSET_VERTICES;

	public static void build(final ForgeConfigSpec.Builder clientBuilder, final ForgeConfigSpec.Builder commonBuilder, final ForgeConfigSpec.Builder serverBuilder) {
		OFFSET_VERTICES = commonBuilder
				.comment("Number of ticks for one smelting operation")
				.define("offsetVertices", false);
	}

}
