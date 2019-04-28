package io.github.cadiboo.nocubes.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class Config {

	public static boolean renderSmoothTerrain = true;
//	public static boolean renderSmoothLeaves = true;

	public static Set<String> terrainSmoothable = Sets.newHashSet();
	//	public static Set<String> leavesSmoothable = Sets.newHashSet();
	public static ExtendFluidsRange extendFluidsRange = ExtendFluidsRange.Off;
	public static MeshGenerator terrainMeshGenerator = MeshGenerator.SurfaceNets;

	public static void bakeClient() {
		renderSmoothTerrain = ConfigHolder.CLIENT.renderSmoothTerrain.get();
//		renderSmoothLeaves = ConfigHolder.CLIENT.renderSmoothLeaves.get();
	}

	public static void bakeServer() {
		terrainSmoothable = Sets.newHashSet(ConfigHolder.SERVER.terrainSmoothable.get());
//		leavesSmoothable = Sets.newHashSet(ConfigHolder.SERVER.leavesSmoothable.get());
		extendFluidsRange = ConfigHolder.SERVER.extendFluidsRange.get();
		terrainMeshGenerator = ConfigHolder.SERVER.terrainMeshGenerator.get();
	}

	public static class ConfigHolder {

		public static final ClientConfig CLIENT;
		public static final ForgeConfigSpec CLIENT_SPEC;
		public static final ServerConfig SERVER;
		public static final ForgeConfigSpec SERVER_SPEC;
		static {
			{
				final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
				CLIENT = specPair.getLeft();
				CLIENT_SPEC = specPair.getRight();
			}
			{
				final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
				SERVER = specPair.getLeft();
				SERVER_SPEC = specPair.getRight();
			}
		}
	}

	public static class ClientConfig {

		public ForgeConfigSpec.BooleanValue renderSmoothTerrain;
//		public ForgeConfigSpec.BooleanValue renderSmoothLeaves;

		ClientConfig(final ForgeConfigSpec.Builder builder) {
			builder.push("general");
			renderSmoothTerrain = builder
					.comment("renderSmoothTerrain")
					.translation(MOD_ID + ".config.renderSmoothTerrain")
					.define("renderSmoothTerrain", true);
//			renderSmoothLeaves = builder
//					.comment("renderSmoothLeaves")
//					.translation(MOD_ID + ".config.renderSmoothLeaves")
//					.define("renderSmoothLeaves", true);
			builder.pop();
		}

	}

	public static class ServerConfig {

		public ForgeConfigSpec.ConfigValue<List<? extends String>> terrainSmoothable;
		//		public ForgeConfigSpec.ConfigValue<List<? extends String>> leavesSmoothable;
		public ForgeConfigSpec.ConfigValue<ExtendFluidsRange> extendFluidsRange;
		public ForgeConfigSpec.ConfigValue<MeshGenerator> terrainMeshGenerator;

		ServerConfig(ForgeConfigSpec.Builder builder) {
			builder.push("general");
			terrainSmoothable = builder
					.comment("terrainSmoothable")
					.translation(MOD_ID + ".config.terrainSmoothable")
					.defineList("terrainSmoothable", Lists.newArrayList(), o -> o instanceof String);
//			leavesSmoothable = builder
//					.comment("leavesSmoothable")
//					.translation(MOD_ID + ".config.leavesSmoothable")
//					.defineList("leavesSmoothable", Lists.newArrayList(), o -> o instanceof String);
			extendFluidsRange = builder
					.comment("extendFluidsRange")
					.translation(MOD_ID + ".config.extendFluidsRange")
					.defineEnum("extendFluidsRange", ExtendFluidsRange.Off);
			terrainMeshGenerator = builder
					.comment("terrainMeshGenerator")
					.translation(MOD_ID + ".config.terrainMeshGenerator")
					.defineEnum("terrainMeshGenerator", MeshGenerator.SurfaceNets);
			builder.pop();
		}

	}

}
