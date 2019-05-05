package io.github.cadiboo.nocubes.config;

import com.google.common.collect.Lists;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
final class ServerConfig {

	ForgeConfigSpec.ConfigValue<List<? extends String>> terrainSmoothable;
	ForgeConfigSpec.ConfigValue<ExtendFluidsRange> extendFluidsRange;
	ForgeConfigSpec.ConfigValue<MeshGenerator> terrainMeshGenerator;
	ForgeConfigSpec.BooleanValue terrainCollisions;

	ServerConfig(ForgeConfigSpec.Builder builder) {
		builder.push("general");
		terrainSmoothable = builder
				.comment("terrainSmoothable")
				.translation(MOD_ID + ".config.terrainSmoothable")
				.defineList("terrainSmoothable", Lists.newArrayList(), o -> o instanceof String);
		extendFluidsRange = builder
				.comment("extendFluidsRange")
				.translation(MOD_ID + ".config.extendFluidsRange")
				.defineEnum("extendFluidsRange", ExtendFluidsRange.OneBlock);
		terrainMeshGenerator = builder
				.comment("terrainMeshGenerator")
				.translation(MOD_ID + ".config.terrainMeshGenerator")
				.defineEnum("terrainMeshGenerator", MeshGenerator.SurfaceNets);
		terrainCollisions = builder
				.comment("terrainCollisions")
				.translation(MOD_ID + ".config.terrainCollisions")
				.define("terrainCollisions", false);
		builder.pop();
	}

}
