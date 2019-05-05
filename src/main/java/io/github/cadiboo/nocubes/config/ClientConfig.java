package io.github.cadiboo.nocubes.config;

import com.google.common.collect.Lists;
import io.github.cadiboo.nocubes.client.LazyBlockColorCache;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.SmoothLeavesType;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
final class ClientConfig {

	ForgeConfigSpec.BooleanValue renderSmoothTerrain;

	ForgeConfigSpec.BooleanValue renderSmoothLeaves;
	ForgeConfigSpec.ConfigValue<List<? extends String>> leavesSmoothable;
	ForgeConfigSpec.ConfigValue<MeshGenerator> leavesMeshGenerator;
	ForgeConfigSpec.ConfigValue<SmoothLeavesType> smoothLeavesType;

	ForgeConfigSpec.BooleanValue renderExtendedFluids;

	ForgeConfigSpec.BooleanValue smoothFluidLighting;
	ForgeConfigSpec.BooleanValue smoothFluidColors;
	ForgeConfigSpec.BooleanValue naturalFluidTextures;

	ClientConfig(final ForgeConfigSpec.Builder builder) {
		builder.push("general");
		renderSmoothTerrain = builder
				.comment("renderSmoothTerrain")
				.translation(MOD_ID + ".config.renderSmoothTerrain")
				.define("renderSmoothTerrain", true);

		renderSmoothLeaves = builder
				.comment("renderSmoothLeaves")
				.translation(MOD_ID + ".config.renderSmoothLeaves")
				.define("renderSmoothLeaves", true);
		leavesSmoothable = builder
				.comment("leavesSmoothable")
				.translation(MOD_ID + ".config.leavesSmoothable")
				.defineList("leavesSmoothable", Lists.newArrayList(), o -> o instanceof String);
		leavesMeshGenerator = builder
				.comment("leavesMeshGenerator")
				.translation(MOD_ID + ".config.leavesMeshGenerator")
				.defineEnum("leavesMeshGenerator", MeshGenerator.SurfaceNets);
		smoothLeavesType = builder
				.comment("smoothLeavesType")
				.translation(MOD_ID + ".config.smoothLeavesType")
				.defineEnum("smoothLeavesType", SmoothLeavesType.TOGETHER);

		renderExtendedFluids = builder
				.comment("renderExtendedFluids")
				.translation(MOD_ID + ".config.renderExtendedFluids")
				.define("renderExtendedFluids", true);

		smoothFluidLighting = builder
				.comment("smoothFluidLighting")
				.translation(MOD_ID + ".config.smoothFluidLighting")
				.define("smoothFluidLighting", true);
		smoothFluidColors = builder
				.comment("smoothFluidColors")
				.translation(MOD_ID + ".config.smoothFluidColors")
				.define("smoothFluidColors", true);
		naturalFluidTextures = builder
				.comment("naturalFluidTextures")
				.translation(MOD_ID + ".config.naturalFluidTextures")
				.define("naturalFluidTextures", true);

		builder.pop();
	}

}
