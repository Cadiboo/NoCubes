package io.github.cadiboo.nocubes.config;

import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;

import javax.annotation.Nonnull;
import java.util.List;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
final class ServerConfig {

	@Nonnull
	final ForgeConfigSpec.ConfigValue<List<? extends String>> terrainSmoothable;
	@Nonnull
	final ForgeConfigSpec.ConfigValue<ExtendFluidsRange> extendFluidsRange;
	@Nonnull
	final ForgeConfigSpec.ConfigValue<MeshGeneratorType> terrainMeshGenerator;
	@Nonnull
	final ForgeConfigSpec.BooleanValue terrainCollisions;
	@Nonnull
	final ForgeConfigSpec.BooleanValue forceVisuals;

	ServerConfig(@Nonnull final ForgeConfigSpec.Builder builder) {
		builder.push("general");
		terrainSmoothable = builder
				.comment("The list of terrain smoothable blockstates")
				.translation(MOD_ID + ".config.terrainSmoothable")
				.defineList("terrainSmoothable", ConfigHelper.getDefaultTerrainSmoothable(), o -> o instanceof String);
		extendFluidsRange = builder
				.comment("The range at which to extend fluids into smoothable blocks")
				.translation(MOD_ID + ".config.extendFluidsRange")
				.defineEnum("extendFluidsRange", ExtendFluidsRange.OneBlock);
		terrainMeshGenerator = builder
				.comment("The mesh generator that generates the terrain")
				.translation(MOD_ID + ".config.terrainMeshGenerator")
				.defineEnum("terrainMeshGenerator", MeshGeneratorType.SurfaceNets);
		terrainCollisions = builder
				.comment("If realistic terrain collisions should be calculated")
				.translation(MOD_ID + ".config.terrainCollisions")
				.define("terrainCollisions", true);
		forceVisuals = builder
				.translation(MOD_ID + ".config.forceVisuals")
				.comment(
					"For MMO servers that require NoCubes to be enabled for a proper player experience.",
					"If you enable this make sure that you've manually checked that every chunk is navigable!"
				)
				.define("forceVisuals", false);
		builder.pop();
	}

}
