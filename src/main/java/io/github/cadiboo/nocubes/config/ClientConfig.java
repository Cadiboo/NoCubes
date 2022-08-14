package io.github.cadiboo.nocubes.config;

import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.util.SmoothLeavesType;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import java.util.List;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
final class ClientConfig {

	@Nonnull
	final ForgeConfigSpec.BooleanValue renderSmoothTerrain;

	@Nonnull
	final ForgeConfigSpec.BooleanValue renderSmoothLeaves;
	@Nonnull
	final ForgeConfigSpec.BooleanValue renderSmoothAndVanillaLeaves;
	@Nonnull
	final ForgeConfigSpec.ConfigValue<List<? extends String>> leavesSmoothable;
	@Nonnull
	final ForgeConfigSpec.ConfigValue<MeshGeneratorType> leavesMeshGenerator;
	@Nonnull
	final ForgeConfigSpec.ConfigValue<SmoothLeavesType> smoothLeavesType;

	@Nonnull
	final ForgeConfigSpec.BooleanValue applyDiffuseLighting;

	@Nonnull
	final ForgeConfigSpec.BooleanValue betterTextures;

	@Nonnull
	final ForgeConfigSpec.BooleanValue shortGrass;

	@Nonnull
	final ForgeConfigSpec.BooleanValue smoothFluidLighting;
	@Nonnull
	final ForgeConfigSpec.BooleanValue smoothFluidColors;
	@Nonnull
	final ForgeConfigSpec.BooleanValue naturalFluidTextures;

	ClientConfig(@Nonnull final ForgeConfigSpec.Builder builder) {
		builder.push("general");
		renderSmoothTerrain = builder
				.comment("If smooth terrain should be rendered")
				.translation(MOD_ID + ".config.renderSmoothTerrain")
				.define("renderSmoothTerrain", true);

		renderSmoothLeaves = builder
				.comment("If smooth leaves should be rendered")
				.translation(MOD_ID + ".config.renderSmoothLeaves")
				.define("renderSmoothLeaves", true);
		renderSmoothAndVanillaLeaves = builder
				.comment("If both smooth and vanilla leaves should be rendered")
				.translation(MOD_ID + ".config.renderSmoothAndVanillaLeaves")
				.define("renderSmoothAndVanillaLeaves", true);
		leavesSmoothable = builder
				.comment("The list of leaves smoothable blocks")
				.translation(MOD_ID + ".config.leavesSmoothable")
				.defineList("leavesSmoothable", ConfigHelper.getDefaultLeavesSmoothable(), o -> o instanceof String);
		leavesMeshGenerator = builder
				.comment("The mesh generator that generates leaves")
				.translation(MOD_ID + ".config.leavesMeshGenerator")
				.defineEnum("leavesMeshGenerator", MeshGeneratorType.SurfaceNets);
		smoothLeavesType = builder
				.comment("How leaves should be rendered")
				.translation(MOD_ID + ".config.smoothLeavesType")
				.defineEnum("smoothLeavesType", SmoothLeavesType.TOGETHER);

		applyDiffuseLighting = builder
				.comment("If diffuse lighting should be applied when rendering. Accentuates differences between heights")
				.translation(MOD_ID + ".config.applyDiffuseLighting")
				.define("applyDiffuseLighting", true);

		betterTextures = builder
				.comment("If better textures should be searched for when rendering")
				.translation(MOD_ID + ".config.betterTextures")
				.define("betterTextures", true);

		shortGrass = builder
				.comment("If short grass should be rendered")
				.translation(MOD_ID + ".config.shortGrass")
				.define("shortGrass", true);

		smoothFluidLighting = builder
				.comment("If fluids should be rendered with smooth lighting")
				.translation(MOD_ID + ".config.smoothFluidLighting")
				.define("smoothFluidLighting", true);
		smoothFluidColors = builder
				.comment("If fluids should be rendered with smooth biome blending")
				.translation(MOD_ID + ".config.smoothFluidColors")
				.define("smoothFluidColors", true);
		naturalFluidTextures = builder
				.comment("If fluids should be rendered with flipped and rotated variants of their textures")
				.translation(MOD_ID + ".config.naturalFluidTextures")
				.define("naturalFluidTextures", false);

		builder.pop();
	}

}
