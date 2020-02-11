package io.github.cadiboo.nocubes.config;

import com.google.common.collect.Lists;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.util.NoCubesAPIImpl;
import net.minecraft.block.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * The Config for NoCubes.
 * Contains the Common, Client and Sever configs as inner classes.
 * Handles registering and baking the configs.
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = NoCubes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class NoCubesConfig {

	/**
	 * Called from inside the mod constructor.
	 *
	 * @param context The ModLoadingContext to register the configs to
	 */
	public static void register(final ModLoadingContext context) {
		context.registerConfig(ModConfig.Type.COMMON, Common.SPEC);
		context.registerConfig(ModConfig.Type.CLIENT, Client.SPEC);
		context.registerConfig(ModConfig.Type.SERVER, Server.SPEC);
	}

	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
		final ForgeConfigSpec spec = configEvent.getConfig().getSpec();
		if (spec == Common.SPEC)
			Common.bake();
		else if (spec == Client.SPEC)
			Client.bake();
		else if (spec == Server.SPEC)
			Server.bake();

	}

	public static class Common {

		public static final ConfigImpl INSTANCE;
		public static final ForgeConfigSpec SPEC;
		public static boolean enableAutoUpdater;
		static {
			final Pair<ConfigImpl, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ConfigImpl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		public static void bake() {
			enableAutoUpdater = INSTANCE.enableAutoUpdater.get();
		}

		static class ConfigImpl {

			final BooleanValue enableAutoUpdater;

			private ConfigImpl(final ForgeConfigSpec.Builder builder) {
				enableAutoUpdater = builder
						.comment("If the mod auto updater should check for updates when the game starts and install any new updates")
						.translation(MOD_ID + ".config.enableAutoUpdater")
						.define("enableAutoUpdater", true);
			}

		}

	}

	public static class Client {

		public static final ConfigImpl INSTANCE;
		public static final ForgeConfigSpec SPEC;
		public static boolean renderSmoothTerrain;
		public static boolean renderShortGrass;
		public static Set<BlockState> terrainSmoothableWhitelistPreference;
		public static Set<BlockState> terrainSmoothableBlacklistPreference;
		static {
			final Pair<ConfigImpl, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ConfigImpl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		public static void bake() {
			renderSmoothTerrain = INSTANCE.renderSmoothTerrain.get();
			renderShortGrass = INSTANCE.renderShortGrass.get();
			terrainSmoothableWhitelistPreference = ConfigHelper.stringsToBlockStates(INSTANCE.terrainSmoothableWhitelistPreference.get());
			terrainSmoothableBlacklistPreference = ConfigHelper.stringsToBlockStates(INSTANCE.terrainSmoothableBlacklistPreference.get());
		}

		static class ConfigImpl {

			final BooleanValue renderSmoothTerrain;
			final BooleanValue renderShortGrass;
			final ConfigValue<List<String>> terrainSmoothableWhitelistPreference;
			final ConfigValue<List<String>> terrainSmoothableBlacklistPreference;

			private ConfigImpl(final ForgeConfigSpec.Builder builder) {
				renderSmoothTerrain = builder
						.comment("If smooth terrain should be rendered")
						.translation(MOD_ID + ".config.renderSmoothTerrain")
						.define("renderSmoothTerrain", true);

				renderShortGrass = builder
						.comment("If short grass should be rendered")
						.translation(MOD_ID + ".config.renderShortGrass")
						.define("renderShortGrass", true);

				terrainSmoothableWhitelistPreference = ((ConfigValue) builder
						.comment("The whitelist of preferred terrain smoothable blockstates")
						.translation(MOD_ID + ".config.terrainSmoothableWhitelistPreference")
						.defineList("terrainSmoothableWhitelistPreference", ConfigHelper::getDefaultTerrainSmoothable, String.class::isInstance));

				terrainSmoothableBlacklistPreference = ((ConfigValue) builder
						.comment("The blacklist of preferred terrain smoothable blockstates")
						.translation(MOD_ID + ".config.terrainSmoothableBlacklistPreference")
						.defineList("terrainSmoothableBlacklistPreference", Lists::newArrayList, String.class::isInstance));
			}

		}

	}

	public static class Server {

		public static final ConfigImpl INSTANCE;
		public static final ForgeConfigSpec SPEC;
		//		public static ExtendFluidsRange extendFluidsRange;
		public static MeshGeneratorType terrainMeshGenerator;
		public static boolean terrainCollisions;
		public static Set<BlockState> terrainSmoothableWhitelist;
		public static Set<BlockState> terrainSmoothableBlacklist;
		static {
			final Pair<ConfigImpl, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ConfigImpl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		public static void bake() {
//			extendFluidsRange = CONFIG.extendFluidsRange.get();
			terrainMeshGenerator = INSTANCE.terrainMeshGenerator.get();
			terrainCollisions = INSTANCE.terrainCollisions.get();
			terrainSmoothableWhitelist = ConfigHelper.stringsToBlockStates(INSTANCE.terrainSmoothableWhitelist.get());
			terrainSmoothableBlacklist = ConfigHelper.stringsToBlockStates(INSTANCE.terrainSmoothableBlacklist.get());
			ConfigHelper.addApiAddedBlockStates();
			ConfigHelper.refreshTerrainSmoothableBlockStateFields();
		}

		static class ConfigImpl {

			//			final EnumValue<ExtendFluidsRange> extendFluidsRange;
			final EnumValue<MeshGeneratorType> terrainMeshGenerator;
			final BooleanValue terrainCollisions;
			final ConfigValue<List<String>> terrainSmoothableWhitelist;
			final ConfigValue<List<String>> terrainSmoothableBlacklist;

			private ConfigImpl(final ForgeConfigSpec.Builder builder) {
//				extendFluidsRange = builder
//				        .comment("The range at which to extend fluids into smoothable blocks")
//						.translation(MOD_ID + ".config.extendFluidsRange")
//						.defineEnum("extendFluidsRange", ExtendFluidsRange.OneBlock);

				NoCubesAPIImpl.disableAddingMeshGenerators();
				terrainMeshGenerator = builder
						.comment("The mesh generator that generates the terrain")
						.translation(MOD_ID + ".config.terrainMeshGenerator")
						.defineEnum("terrainMeshGenerator", MeshGeneratorType.OLD_NO_CUBES);

				terrainCollisions = builder
						.comment("If realistic terrain collisions should be calculated")
						.translation(MOD_ID + ".config.terrainCollisions")
						.define("terrainCollisions", true);

				terrainSmoothableWhitelist = ((ConfigValue) builder
						.defineList("terrainSmoothableWhitelist", ConfigHelper::getDefaultTerrainSmoothable, String.class::isInstance));

				terrainSmoothableBlacklist = ((ConfigValue) builder
						.defineList("terrainSmoothableBlacklist", Lists::newArrayList, String.class::isInstance));
			}

		}

	}

}
