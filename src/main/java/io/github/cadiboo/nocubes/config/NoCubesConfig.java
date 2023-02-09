package io.github.cadiboo.nocubes.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.mesh.*;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.network.S2CUpdateServerConfig;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.cadiboo.nocubes.client.RenderHelper.reloadAllChunks;
import static net.minecraft.world.level.block.Blocks.*;

/**
 * The Config for NoCubes.
 * Contains the Common, Client and Sever configs as inner classes.
 * Handles registering and baking the configs.
 *
 * @author Cadiboo
 */
public final class NoCubesConfig {

	private static final Logger LOG = LogManager.getLogger();

	/**
	 * Called from inside the mod constructor.
	 *
	 * @param context The ModLoadingContext to register the configs to
	 */
	public static void register(ModLoadingContext context, IEventBus modBus) {
		Lists.newArrayList(
//			Pair.of(ModConfig.Type.COMMON, Common.SPEC),
			Pair.of(ModConfig.Type.CLIENT, Client.SPEC),
			Pair.of(ModConfig.Type.SERVER, Server.SPEC)
		).forEach(pair -> context.registerConfig(pair.getKey(), pair.getValue()));
		modBus.addListener((ModConfigEvent event) -> {
			var config = event.getConfig();
			var spec = config.getSpec();
			if (!((ForgeConfigSpec)spec).isLoaded())
				return;
			LOG.debug("Received config event for {}", config.getFileName());
			if (spec == Client.SPEC)
				Client.bake(config);
			else if (spec == Server.SPEC)
				Server.bake(config);
		});
	}

	/**
	 * Settings that effect the client state i.e. rendering options.
	 */
	public static class Client {

		public static final Impl INSTANCE;
		public static final ForgeConfigSpec SPEC;
		public static boolean render;
		public static boolean renderSelectionBox;
		public static ColorParser.Color selectionBoxColor;
		public static boolean betterGrassSides;
		public static boolean moreSnow;
		public static boolean fixPlantHeight;
		public static boolean grassTufts;

		public static boolean debugEnabled;
		public static boolean debugOutlineSmoothables;
		public static boolean debugVisualiseDensitiesGrid;
		public static boolean debugRenderCollisions;
		public static boolean debugRenderMeshCollisions;
		public static boolean debugRecordMeshPerformance;
		public static boolean debugOutlineNearbyMesh;

		static {
			Pair<Impl, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Impl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		/**
		 * Each time our config changes we get the values from it and store them in our own fields ('baking' them)
		 * instead of looking up the values on the config (which is pretty slow) each time we need them.
		 */
		public static void bake(ModConfig config) {
			LOG.debug("Baking config {}", config.getFileName());
			boolean oldRender = render;
			int oldChunkRenderSettingsHash = hashChunkRenderSettings();

			// Directly querying the baked 'forceVisuals' field - won't cause a NPE on the client when there is no server
			render = Server.forceVisuals || INSTANCE.render.get();
			renderSelectionBox = INSTANCE.renderSelectionBox.get();
			selectionBoxColor = ColorParser.parse(INSTANCE.selectionBoxColor.get());
			betterGrassSides = INSTANCE.betterGrassSides.get();
			moreSnow = INSTANCE.moreSnow.get();
			fixPlantHeight = INSTANCE.fixPlantHeight.get();
			grassTufts = INSTANCE.grassTufts.get();

			if (oldRender != render)
				reloadAllChunks("custom rendering was toggled to %b in the client config", render);
			else if (render && oldChunkRenderSettingsHash != hashChunkRenderSettings())
				reloadAllChunks("options affecting chunk rendering in the client config were changed");

			debugEnabled = INSTANCE.debugEnabled.get();
			debugOutlineSmoothables = INSTANCE.debugOutlineSmoothables.get();
			debugVisualiseDensitiesGrid = INSTANCE.debugVisualiseDensitiesGrid.get();
			debugRenderCollisions = INSTANCE.debugRenderCollisions.get();
			debugRenderMeshCollisions = INSTANCE.debugRenderMeshCollisions.get();
			debugRecordMeshPerformance = INSTANCE.debugRecordMeshPerformance.get();
			debugOutlineNearbyMesh = INSTANCE.debugOutlineNearbyMesh.get();
		}

		private static int hashChunkRenderSettings() {
			return Objects.hash(betterGrassSides, moreSnow, fixPlantHeight, grassTufts);
		}

		public static void updateRender(boolean newValue) {
			Client.INSTANCE.render.set(newValue);
			saveAndLoad();
		}

		private static void saveAndLoad() {
			Hacks.saveAndLoad(ModConfig.Type.CLIENT);
		}

		/**
		 * Responsible for interfacing with Forge's config API and creating a Config with all our options.
		 */
		static class Impl {

			final BooleanValue render;
			final BooleanValue renderSelectionBox;
			final ConfigValue<String> selectionBoxColor;
			final BooleanValue betterGrassSides;
			final BooleanValue moreSnow;
			final BooleanValue fixPlantHeight;
			final BooleanValue grassTufts;

			final BooleanValue debugEnabled;
			final BooleanValue debugOutlineSmoothables;
			final BooleanValue debugVisualiseDensitiesGrid;
			final BooleanValue debugRenderCollisions;
			final BooleanValue debugRenderMeshCollisions;
			final BooleanValue debugRecordMeshPerformance;
			final BooleanValue debugOutlineNearbyMesh;

			private Impl(ForgeConfigSpec.Builder builder) {
				render = builder
					.translation(NoCubes.MOD_ID + ".config.render")
					.comment("If NoCubes' custom rendering is enabled")
					.define("render", true);

				renderSelectionBox = builder
					.translation(NoCubes.MOD_ID + ".config.renderSelectionBox")
					.comment("If NoCubes' should render a custom outline (selection box) for smoothed blocks (set to false to use Vanilla's cubic rendering).")
					.define("renderSelectionBox", true);
				selectionBoxColor = builder
					.translation(NoCubes.MOD_ID + ".config.selectionBoxColor")
					.comment(
						"The color of the outline (selection box) over a smoothed block.",
						"Supports pretty much any format you can imagine.",
						"Some examples of ways to define colors:",
						"By name: \"red\"",
						"By name: \"firebrick\" (a red-orangeish color)",
						"By name: \"gainsboro\" (a light gray color)",
						"With RGB (red, green, blue) integers (0-255): \"rgb(255, 0, 0)\" (pure red)",
						"With RGB (red, green, blue) floats (0.0-1.0): \"rgb(1.0, 0, 0)\" (also pure red)",
						"With RGBA (red, green, blue, alpha) integers (0-255): \"rgba(255, 0, 0, 0.5)\" (partially transparent pure red)",
						"With RGBA (red, green, blue, alpha) integers (0-255): \"rgba(1.0, 0, 0, 1.0)\" (also partially transparent pure red)",
						"With hexadecimal (case insensitive) RGB (red, green, blue) integers (00-FF): \"0x0ff\" (aqua)",
						"With hexadecimal (case insensitive) RGBA (red, green, blue, alpha) integers (00-FF): \"#0FF6\" (partially transparent aqua)",
						"With HSL (hue, saturation, lightness): \"hsl(270, 100%, 100%)\" (a dark purple)",
						"With HSLA (hue, saturation, lightness, alpha): \"hsla(270, 100%, 100%, 0.5)\" (a partially transparent dark purple)"
					)
					.define("selectionBoxColor", "#0006");

				betterGrassSides = builder
					.translation(NoCubes.MOD_ID + ".config.betterGrassSides")
					.comment(
						"Similar to OptiFine's 'Better Grass' feature",
						"OFF - The sides of grass blocks have the default texture",
						"ON - The sides of grass blocks have the texture of the top of the block"
					)
					.define("betterGrassSides", false);

				moreSnow = builder
					.translation(NoCubes.MOD_ID + ".config.moreSnow")
					.comment(
						"Similar to OptiFine's 'Better Snow' feature",
						"OFF - The sides of blocks nearby snow have their own texture",
						"ON - The sides of blocks nearby snow have the snow texture"
					)
					.define("moreSnow", false);

				fixPlantHeight = builder
					.translation(NoCubes.MOD_ID + ".config.fixPlantHeight")
					.comment("If small plants like flowers and grass should be moved onto NoCubes' terrain")
					.define("fixPlantHeight", false);

				grassTufts = builder
					.translation(NoCubes.MOD_ID + ".config.grassTufts")
					.comment("If small tufts of grass should be rendered on top of grass blocks, similar to BetterFoliage's 'Short Grass' feature")
					.define("grassTufts", false);

				builder
					.push("debug");
				{
					debugEnabled = builder
						.translation(NoCubes.MOD_ID + ".config.debugEnabled")
						.comment("If debugging features should be enabled")
						.define("debugEnabled", false);

					debugOutlineSmoothables = builder.define("debugOutlineSmoothables", false);
					debugVisualiseDensitiesGrid = builder.define("debugVisualiseDensitiesGrid", false);
					debugRenderCollisions = builder.define("debugRenderCollisions", false);
					debugRenderMeshCollisions = builder.define("debugRenderMeshCollisions", false);
					debugRecordMeshPerformance = builder.define("debugRecordMeshPerformance", false);
					debugOutlineNearbyMesh = builder.define("debugOutlineNearbyMesh", false);
				}
				builder.pop();
			}

		}

	}

	/**
	 * Settings that effect the server state i.e. gameplay options.
	 */
	public static class Server {

		public static final Impl INSTANCE;
		public static final ForgeConfigSpec SPEC;
		public static Mesher mesher;
		public static boolean collisionsEnabled;
		public static boolean tempMobCollisionsDisabled;
		public static boolean forceVisuals;
		public static int extendFluidsRange;
		public static float oldNoCubesRoughness;

		static {
			Pair<Impl, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Impl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		/**
		 * Each time our config changes we get the values from it and store them in our own fields ('baking' them)
		 * instead of looking up the values on the config (which is pretty slow) each time we need them.
		 */
		public static void bake(ModConfig config) {
			LOG.debug("Baking config {}", config.getFileName());
			int oldChunkRenderSettingsHash = hashChunkRenderSettings();

			Smoothables.recomputeInMemoryLookup(INSTANCE.smoothableWhitelist.get(), INSTANCE.smoothableBlacklist.get());
			mesher = INSTANCE.mesher.get().instance;
			collisionsEnabled = INSTANCE.collisionsEnabled.get();
			tempMobCollisionsDisabled = INSTANCE.tempMobCollisionsDisabled.get();
			forceVisuals = INSTANCE.forceVisuals.get();
			if (forceVisuals)
				// Directly setting the baked field - won't cause a NPE on the dedicated server
				Client.render = true;
			extendFluidsRange = validateRange(0, 2, INSTANCE.extendFluidsRange.get(), "extendFluidsRange");
			oldNoCubesRoughness = validateRange(0d, 1d, INSTANCE.oldNoCubesRoughness.get(), "oldNoCubesRoughness").floatValue();

			if (oldChunkRenderSettingsHash != hashChunkRenderSettings())
				// TODO: This seems to be broken and never gets called in singleplayer (should it be?)
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> reloadAllChunks("options affecting chunk rendering in the server config were changed"));
			if (FMLEnvironment.dist.isDedicatedServer() && ServerLifecycleHooks.getCurrentServer() != null)
				NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), S2CUpdateServerConfig.create(config));
		}

		private static <T extends Number & Comparable<T>> T validateRange(T min, T max, T value, String name) {
			if (value.compareTo(min) < 0 || value.compareTo(max) > 0)
				throw new IllegalStateException("Config was not validated! '" + name + "' must be between " + min + " and " + max + " but was " + value);
			return value;
		}

		private static int hashChunkRenderSettings() {
			return Objects.hash(mesher, forceVisuals);
		}

		public static void updateSmoothable(boolean newValue, BlockState... states) {
			Smoothables.updateSmoothables(newValue, states, (List) INSTANCE.smoothableWhitelist.get(), (List) INSTANCE.smoothableBlacklist.get());
			saveAndLoad();
		}

		private static void saveAndLoad() {
			Hacks.saveAndLoad(ModConfig.Type.SERVER);
		}

		public enum MesherType {
			SurfaceNets(new SurfaceNets(false)),
			OldNoCubes(new OldNoCubes()),
			Debug_SurfaceNets2xSmoothness(new SurfaceNets(true)),
			Debug_MarchingCubes(new MarchingCubes(false)),
			Debug_MarchingCubes2xSmoothness(new MarchingCubes(true)),
			Debug_CullingCubic(new CullingCubic()),
			Debug_StupidCubic(new StupidCubic()),
			Debug_CullingChamfer(new CullingChamfer()),
			;

			public final Mesher instance;

			MesherType(Mesher instance) {
				this.instance = instance;
			}
		}

		/**
		 * Responsible for interfacing with Forge's config API and creating a Config with all our options.
		 */
		static class Impl {

			/**
			 * These lists can contain whatever valid/invalid strings the user wants.
			 * We do not clear them as we want them to be able to add a state, remove the mod,
			 * add the mod and still have the state be smoothable.
			 */
			final ConfigValue<List<? extends String>> smoothableWhitelist;
			final ConfigValue<List<? extends String>> smoothableBlacklist;
			final EnumValue<MesherType> mesher;
			final BooleanValue collisionsEnabled;
			final BooleanValue tempMobCollisionsDisabled;
			final BooleanValue forceVisuals;
			final IntValue extendFluidsRange;
			final DoubleValue oldNoCubesRoughness;

			private Impl(ForgeConfigSpec.Builder builder) {
				smoothableWhitelist = builder
					.translation(NoCubes.MOD_ID + ".config.smoothableWhitelist")
					.comment("What blocks should be smoothed by NoCubes (same syntax as the /setblock command)")
					.defineListAllowEmpty(Collections.singletonList("smoothableWhitelist"), Lists::newArrayList, String.class::isInstance);

				smoothableBlacklist = builder
					.translation(NoCubes.MOD_ID + ".config.smoothableBlacklist")
					.comment("What blocks should not be smoothed by NoCubes (same syntax as the /setblock command)")
					.defineListAllowEmpty(Collections.singletonList("smoothableBlacklist"), Lists::newArrayList, String.class::isInstance);

				collisionsEnabled = builder
					.translation(NoCubes.MOD_ID + ".config.collisionsEnabled")
					.comment("If players should be able to walk up the smooth slopes generated by NoCubes")
					.define("collisionsEnabled", true);

				tempMobCollisionsDisabled = builder
					.translation(NoCubes.MOD_ID + ".config.tempMobCollisionsDisabled")
					.comment("If ONLY players should be able to walk up the smooth slopes generated by NoCubes")
					.define("tempMobCollisionsDisabled", false);

				mesher = builder
					.translation(NoCubes.MOD_ID + ".config.meshGenerator")
					.comment("meshGenerator")
					.defineEnum("meshGenerator", MesherType.SurfaceNets);

				forceVisuals = builder
					.translation(NoCubes.MOD_ID + ".config.forceVisuals")
					.comment(
						"For MMO servers that require NoCubes to be enabled for a proper player experience.",
						"If you enable this make sure that you've manually checked that every chunk is navigable!"
					)
					.define("forceVisuals", false);

				extendFluidsRange = builder
					.translation(NoCubes.MOD_ID + ".config.extendFluidsRange")
					.comment("The range at which to extend fluids (water & lava) into smoothable blocks")
					.defineInRange("extendFluidsRange", 1, 0, 2);

				oldNoCubesRoughness = builder
					.translation(NoCubes.MOD_ID + ".config.oldNoCubesRoughness")
					.comment("How much pseudo-random roughness should be applied to mesh generated by OldNoCubes")
					.defineInRange("oldNoCubesRoughness", 0.5F, 0F, 1F);
			}

		}

	}

	/**
	 * Utils to allow us to save and load our config when we programmatically change its values (i.e. from keybinds and packets)
	 */
	public static class Hacks {

		/**
		 * Only call with correct type!
		 * Similar to {@link ConfigFileTypeHandler.ConfigWatcher#run()}
		 */
		static void saveAndLoad(ModConfig.Type type) {
			LOG.debug("Saving and loading {} config", type.name());
			ConfigTracker_getConfig(type).ifPresent(modConfig -> {
				LOG.debug("Found {} ModConfig to save and load", type.name());
				modConfig.save();
				((CommentedFileConfig) modConfig.getConfigData()).load();
				modConfig.getSpec().afterReload();
//				modConfig.fireEvent(new IConfigEvent.reloading(modConfig));
				ModConfig_fireEvent(modConfig, IConfigEvent.reloading(modConfig));
			});
		}

		/**
		 * Similar to {@link ConfigTracker#loadDefaultServerConfigs}
		 */
		public static void loadDefaultServerConfig() {
			LOG.debug("Loading default server config");
			ConfigTracker_getConfig(ModConfig.Type.SERVER).ifPresent(modConfig -> {
				LOG.debug("Found ModConfig to load as default");
				var config = CommentedConfig.inMemory();
				modConfig.getSpec().correct(config);
//				modConfig.setConfigData(config);
				ModConfig_setConfigData(modConfig, config);
//				modConfig.fireEvent(IConfigEvent.loading(modConfig));
				ModConfig_fireEvent(modConfig, IConfigEvent.loading(modConfig));
			});
		}

		/**
		 * Similar to {@link ConfigTracker#getConfigFileName}
		 */
		private static Optional<ModConfig> ConfigTracker_getConfig(ModConfig.Type type) {
			LOG.debug("Getting {} ModConfig from ConfigTracker", type.name());
			return ConfigTracker.INSTANCE.configSets().get(type).stream()
				.filter(modConfig -> modConfig.getModId().equals(NoCubes.MOD_ID))
				.findFirst();
		}

		private static void ModConfig_setConfigData(ModConfig modConfig, CommentedConfig data) {
			LOG.debug("Setting ModConfig config data");
			var setConfigData = ObfuscationReflectionHelper.findMethod(ModConfig.class, "setConfigData", CommentedConfig.class);
			try {
				setConfigData.invoke(modConfig, data);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException("Could not set config data for config " + modConfig, e);
			}
		}

		private static void ModConfig_fireEvent(ModConfig modConfig, IConfigEvent event) {
			LOG.debug("Firing ModConfig event");
//			modConfig.fireEvent(event);
			ModList.get().getModContainerById(modConfig.getModId()).get().dispatchConfigEvent(event);
		}

		public static void receiveSyncedServerConfig(S2CUpdateServerConfig s2CConfigData) {
			LOG.debug("Setting logical server config (on the client) from server sync packet");
			assert FMLEnvironment.dist.isClient() : "This packet should have only be sent server->client";
			var modConfig = ConfigTracker_getConfig(ModConfig.Type.SERVER).get();
			var parser = (ConfigParser<CommentedConfig>) modConfig.getConfigData().configFormat().createParser();
			ModConfig_setConfigData(modConfig, parser.parse(new ByteArrayInputStream(s2CConfigData.getBytes())));
			ModConfig_fireEvent(modConfig, IConfigEvent.reloading(modConfig));
		}
	}

	public static class Smoothables {

		/**
		 * Stores the list of blocks that 'just are' smoothable by default.
		 * This includes stuff like Stone and any blocks that other mods register as smoothable.
		 */
		// TODO: Convert this to a tag
		private static final Set<BlockState> DEFAULT_SMOOTHABLES = Sets.newIdentityHashSet();

		static {
			// Add all possible BlockStates for these blocks
			DEFAULT_SMOOTHABLES.addAll(Arrays.stream(new Block[]{
				STONE, GRANITE, DIORITE, ANDESITE,
				GRASS_BLOCK, DIRT, COARSE_DIRT, PODZOL, MYCELIUM,
				DEEPSLATE, ROOTED_DIRT, TUFF, CALCITE, SMOOTH_BASALT, AMETHYST_BLOCK, BUDDING_AMETHYST,
				BEDROCK,
				SAND, RED_SAND,
				SANDSTONE, RED_SANDSTONE,
				GRAVEL,
				COAL_ORE, IRON_ORE, COPPER_ORE, GOLD_ORE, REDSTONE_ORE, DIAMOND_ORE, LAPIS_ORE, EMERALD_ORE, NETHER_QUARTZ_ORE, NETHER_GOLD_ORE,
				DEEPSLATE_COAL_ORE, DEEPSLATE_IRON_ORE, DEEPSLATE_COPPER_ORE, DEEPSLATE_GOLD_ORE, DEEPSLATE_REDSTONE_ORE, DEEPSLATE_DIAMOND_ORE, DEEPSLATE_LAPIS_ORE, DEEPSLATE_EMERALD_ORE,
				INFESTED_STONE, INFESTED_DEEPSLATE,
				BONE_BLOCK,
				DIRT_PATH,
				CLAY, TERRACOTTA, WHITE_TERRACOTTA, ORANGE_TERRACOTTA, MAGENTA_TERRACOTTA, LIGHT_BLUE_TERRACOTTA, YELLOW_TERRACOTTA, LIME_TERRACOTTA, PINK_TERRACOTTA, GRAY_TERRACOTTA, LIGHT_GRAY_TERRACOTTA, CYAN_TERRACOTTA, PURPLE_TERRACOTTA, BLUE_TERRACOTTA, BROWN_TERRACOTTA, GREEN_TERRACOTTA, RED_TERRACOTTA, BLACK_TERRACOTTA,
				SNOW, SNOW_BLOCK, ICE, PACKED_ICE, FROSTED_ICE,
				NETHERRACK, SOUL_SAND, SOUL_SOIL, BASALT, MAGMA_BLOCK, GLOWSTONE, NETHER_WART_BLOCK, CRIMSON_STEM, WARPED_NYLIUM, WARPED_WART_BLOCK, WARPED_STEM,
				END_STONE,
				OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG,
				OAK_LEAVES, SPRUCE_LEAVES, BIRCH_LEAVES, JUNGLE_LEAVES, ACACIA_LEAVES, DARK_OAK_LEAVES,
			}).flatMap(block -> ModUtil.getStates(block).stream()).collect(Collectors.toList()));

			// Add each of these individual BlockStates
			DEFAULT_SMOOTHABLES.addAll(Arrays.stream(new BlockState[]{

			}).collect(Collectors.toList()));

			// Add these modded BlockStates
			DEFAULT_SMOOTHABLES.addAll(parseBlockStates(Arrays.asList(
				"biomesoplenty:grass[snowy=false,variant=sandy]",
				"biomesoplenty:dirt[coarse=false,variant=sandy]",
				"biomesoplenty:white_sand",
				"biomesoplenty:grass[snowy=false,variant=silty]",
				"biomesoplenty:dirt[coarse=false,variant=loamy]",
				"biomesoplenty:grass[snowy=false,variant=loamy]",
				"biomesoplenty:dried_sand",
				"biomesoplenty:hard_ice",
				"biomesoplenty:mud[variant=mud]",
				"biomesoplenty:dirt[coarse=false,variant=silty]",
				"chisel:marble2[variation=7]",
				"chisel:limestone2[variation=7]",
				"dynamictrees:rootydirtspecies[life=0]",
				"dynamictrees:rootysand[life=0]",
				"iceandfire:ash",
				"iceandfire:sapphire_ore",
				"iceandfire:chared_grass",
				"iceandfire:chared_stone",
				"iceandfire:frozen_grass_path",
				"notenoughroofs:copper_ore",
				"rustic:slate"
			)));
		}

		static void updateSmoothables(boolean newValue, BlockState[] states, List<String> whitelist, List<String> blacklist) {
			LOG.debug("Updating user-defined smoothable string lists");
			var toAddTo = newValue ? whitelist : blacklist;
			var toRemoveFrom = newValue ? blacklist : whitelist;
			for (var state : states) {
				var string = BlockStateConverter.toString(state);
				NoCubes.smoothableHandler.setSmoothable(newValue, state);
				if (!toAddTo.contains(string))
					toAddTo.add(string);
				//noinspection StatementWithEmptyBody
				while (toRemoveFrom.remove(string))
					// The loop runs until there are no more occurrences of 'string' in the list
					;
			}
		}

		static void recomputeInMemoryLookup(List<? extends String> whitelist, List<? extends String> blacklist) {
			LOG.debug("Recomputing in-memory smoothable lookups from user-defined smoothable string lists");
			var whitelisted = parseBlockStates(whitelist);
			var blacklisted = parseBlockStates(blacklist);
			ForgeRegistries.BLOCKS.getValues().parallelStream()
				.flatMap(block -> ModUtil.getStates(block).parallelStream())
				.forEach(state -> {
					var smoothable = (whitelisted.contains(state) || Smoothables.DEFAULT_SMOOTHABLES.contains(state)) && !blacklisted.contains(state);
					NoCubes.smoothableHandler.setSmoothable(smoothable, state);
				});
		}

		static Set<BlockState> parseBlockStates(List<? extends String> list) {
			var set = Sets.<BlockState>newIdentityHashSet();
			list.parallelStream()
				.map(BlockStateConverter::fromStringOrNull)
				.filter(Objects::nonNull)
				.forEach(set::add);
			return set;
		}

		public static void addDefault(BlockState... states) {
			DEFAULT_SMOOTHABLES.addAll(Arrays.asList(states));
		}
	}

}
