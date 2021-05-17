package io.github.cadiboo.nocubes.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.mesh.SurfaceNets;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static net.minecraft.block.Blocks.*;
import static net.minecraft.state.properties.BlockStateProperties.*;

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
	public static void register(ModLoadingContext context) {
//		context.registerConfig(ModConfig.Type.COMMON, Common.SPEC);
		context.registerConfig(ModConfig.Type.CLIENT, Client.SPEC);
		context.registerConfig(ModConfig.Type.SERVER, Server.SPEC);
	}

	@SubscribeEvent
	public static void onModConfigEvent(ModConfig.ModConfigEvent configEvent) {
		ForgeConfigSpec spec = configEvent.getConfig().getSpec();
//		if (spec == Common.SPEC)
//			Common.bake();
//		else
		if (spec == Client.SPEC)
			Client.bake();
		else if (spec == Server.SPEC)
			Server.bake();
	}

	/**
	 * Settings that effect the client state i.e. rendering options.
	 */
	public static class Client {

		public static final Impl INSTANCE;
		public static final ForgeConfigSpec SPEC;
		public static boolean render;
		public static ColorParser.Color selectionBoxColor;
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
		public static void bake() {
			// Directly querying the baked field - won't cause a NPE on the client when there is no server
			render = Server.forceVisuals || INSTANCE.render.get();
			selectionBoxColor = ColorParser.parse(INSTANCE.selectionBoxColor.get());

			debugEnabled = INSTANCE.debugEnabled.get();
			debugOutlineSmoothables = INSTANCE.debugOutlineSmoothables.get();
			debugVisualiseDensitiesGrid = INSTANCE.debugVisualiseDensitiesGrid.get();
			debugRenderCollisions = INSTANCE.debugRenderCollisions.get();
			debugRenderMeshCollisions = INSTANCE.debugRenderMeshCollisions.get();
			debugRecordMeshPerformance = INSTANCE.debugRecordMeshPerformance.get();
			debugOutlineNearbyMesh = INSTANCE.debugOutlineNearbyMesh.get();
		}

		public static void updateRender(boolean newValue) {
			Client.INSTANCE.render.set(newValue);
			saveAndLoad();
		}

		private static void saveAndLoad() {
			ReloadHacks.saveAndLoad(ModConfig.Type.CLIENT);
		}

		/**
		 * Responsible for interfacing with Forge's config API and creating a Config with all our options.
		 */
		static class Impl {

			final BooleanValue render;
			final ConfigValue<String> selectionBoxColor;
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
					.define("render", true);

				// Stable, doesn't need refactoring
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

				builder
					.push("debug");
				{
					debugEnabled = builder
						.translation(NoCubes.MOD_ID + ".config.debugEnabled")
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
		public static MeshGenerator meshGenerator = new SurfaceNets();
		public static boolean collisionsEnabled;
		public static boolean forceVisuals;

		static {
			Pair<Impl, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Impl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		/**
		 * Each time our config changes we get the values from it and store them in our own fields ('baking' them)
		 * instead of looking up the values on the config (which is pretty slow) each time we need them.
		 */
		public static void bake() {
			collisionsEnabled = INSTANCE.collisionsEnabled.get();
			forceVisuals = INSTANCE.forceVisuals.get();
			if (forceVisuals)
				// Directly setting the baked field - won't cause a NPE on the dedicated server
				Client.render = true;
			Smoothables.recomputeInMemoryLookup(INSTANCE.smoothableWhitelist.get(), INSTANCE.smoothableBlacklist.get());
		}

		public static void updateSmoothable(boolean newValue, BlockState... states) {
			NoCubesConfig.Server.Impl cfg = NoCubesConfig.Server.INSTANCE;
			Smoothables.updateSmoothables(newValue, states, (List) cfg.smoothableWhitelist.get(), (List) cfg.smoothableBlacklist.get());
			saveAndLoad();
		}

		public static void updateCollisions(boolean newValue) {
			Server.INSTANCE.collisionsEnabled.set(newValue);
			saveAndLoad();
		}

		private static void saveAndLoad() {
			ReloadHacks.saveAndLoad(ModConfig.Type.SERVER);
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
			final BooleanValue collisionsEnabled;
			final BooleanValue forceVisuals;

			private Impl(ForgeConfigSpec.Builder builder) {
				smoothableWhitelist = builder
					.translation(NoCubes.MOD_ID + ".config.smoothableWhitelist")
					.defineListAllowEmpty(Collections.singletonList("smoothableWhitelist"), Lists::newArrayList, String.class::isInstance);

				smoothableBlacklist = builder
					.translation(NoCubes.MOD_ID + ".config.smoothableBlacklist")
					.defineListAllowEmpty(Collections.singletonList("smoothableBlacklist"), Lists::newArrayList, String.class::isInstance);

				collisionsEnabled = builder
					.translation(NoCubes.MOD_ID + ".config.collisionsEnabled")
					.define("collisionsEnabled", true);

				forceVisuals = builder
					.translation(NoCubes.MOD_ID + ".config.forceVisuals")
					.comment(
						"For MMO servers that require NoCubes to be enabled for a proper player experience.",
						"If you enable this make sure that you've manually checked that every chunk is navigable!"
					)
					.define("forceVisuals", false);
			}

		}

	}

	/**
	 * Utils to allow us to save & load our config when we programmatically change its values (i.e. from keybinds and packets)
	 */
	static class ReloadHacks {

		// Only call with correct type.
		public static void saveAndLoad(ModConfig.Type type) {
			ConfigTracker_getConfig(NoCubes.MOD_ID, type).ifPresent(modConfig -> {
				modConfig.save();
				((CommentedFileConfig) modConfig.getConfigData()).load();
//				modConfig.fireEvent(new ModConfig.Reloading(modConfig));
				fireReloadEvent(modConfig);
			});
		}

		private static Optional<ModConfig> ConfigTracker_getConfig(String modId, ModConfig.Type type) {
			Map<String, Map<ModConfig.Type, ModConfig>> configsByMod = ObfuscationReflectionHelper.getPrivateValue(ConfigTracker.class, ConfigTracker.INSTANCE, "configsByMod");
			return Optional.ofNullable(configsByMod.getOrDefault(modId, Collections.emptyMap()).getOrDefault(type, null));
		}

		private static void fireReloadEvent(ModConfig modConfig) {
			ModContainer modContainer = ModList.get().getModContainerById(modConfig.getModId()).get();
			ModConfig.Reloading event;
			try {
				event = ObfuscationReflectionHelper.findConstructor(ModConfig.Reloading.class, ModConfig.class).newInstance(modConfig);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
			modContainer.dispatchConfigEvent(event);
		}
	}

	public static class Smoothables {

		/**
		 * Stores the list of blocks that 'just are' smoothable by default.
		 * This includes stuff like Stone and any blocks that other mods register as smoothable.
		 */
		private static final Set<BlockState> DEFAULT_SMOOTHABLES = Sets.newIdentityHashSet();

		static {
			BlockState[] vanilla = {
				STONE.defaultBlockState(),
				GRASS_BLOCK.defaultBlockState().setValue(SNOWY, false),
				GRASS_BLOCK.defaultBlockState().setValue(SNOWY, true),

				STONE.defaultBlockState(),
				GRANITE.defaultBlockState(),
				DIORITE.defaultBlockState(),
				ANDESITE.defaultBlockState(),

				DIRT.defaultBlockState(),
				COARSE_DIRT.defaultBlockState(),

				PODZOL.defaultBlockState().setValue(SNOWY, false),
				PODZOL.defaultBlockState().setValue(SNOWY, true),

				SAND.defaultBlockState(),
				RED_SAND.defaultBlockState(),

				SANDSTONE.defaultBlockState(),

				RED_SANDSTONE.defaultBlockState(),

				GRAVEL.defaultBlockState(),

				COAL_ORE.defaultBlockState(),
				IRON_ORE.defaultBlockState(),
				GOLD_ORE.defaultBlockState(),
				REDSTONE_ORE.defaultBlockState().setValue(LIT, false),
				REDSTONE_ORE.defaultBlockState().setValue(LIT, true),
				DIAMOND_ORE.defaultBlockState(),
				LAPIS_ORE.defaultBlockState(),
				EMERALD_ORE.defaultBlockState(),
				NETHER_QUARTZ_ORE.defaultBlockState(),

				INFESTED_STONE.defaultBlockState(),
				BONE_BLOCK.defaultBlockState(),

				GRASS_PATH.defaultBlockState(),

				CLAY.defaultBlockState(),
				TERRACOTTA.defaultBlockState(),

				WHITE_TERRACOTTA.defaultBlockState(),
				ORANGE_TERRACOTTA.defaultBlockState(),
				MAGENTA_TERRACOTTA.defaultBlockState(),
				LIGHT_BLUE_TERRACOTTA.defaultBlockState(),
				YELLOW_TERRACOTTA.defaultBlockState(),
				LIME_TERRACOTTA.defaultBlockState(),
				PINK_TERRACOTTA.defaultBlockState(),
				GRAY_TERRACOTTA.defaultBlockState(),
				LIGHT_GRAY_TERRACOTTA.defaultBlockState(),
				CYAN_TERRACOTTA.defaultBlockState(),
				PURPLE_TERRACOTTA.defaultBlockState(),
				BLUE_TERRACOTTA.defaultBlockState(),
				BROWN_TERRACOTTA.defaultBlockState(),
				GREEN_TERRACOTTA.defaultBlockState(),
				RED_TERRACOTTA.defaultBlockState(),
				BLACK_TERRACOTTA.defaultBlockState(),

				PACKED_ICE.defaultBlockState(),

				SNOW.defaultBlockState().setValue(LAYERS, 1),
				SNOW.defaultBlockState().setValue(LAYERS, 2),
				SNOW.defaultBlockState().setValue(LAYERS, 3),
				SNOW.defaultBlockState().setValue(LAYERS, 4),
				SNOW.defaultBlockState().setValue(LAYERS, 5),
				SNOW.defaultBlockState().setValue(LAYERS, 6),
				SNOW.defaultBlockState().setValue(LAYERS, 7),
				SNOW.defaultBlockState().setValue(LAYERS, 8),
				SNOW_BLOCK.defaultBlockState(),

				BEDROCK.defaultBlockState(),

				NETHERRACK.defaultBlockState(),
				SOUL_SAND.defaultBlockState(),
				MAGMA_BLOCK.defaultBlockState(),
				GLOWSTONE.defaultBlockState(),

				END_STONE.defaultBlockState(),

				MYCELIUM.defaultBlockState().setValue(SNOWY, false),
				MYCELIUM.defaultBlockState().setValue(SNOWY, true),

			};
			String[] modded = {
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
				"rustic:slate",
			};
			DEFAULT_SMOOTHABLES.addAll(Arrays.asList(vanilla));
			DEFAULT_SMOOTHABLES.addAll(parseBlockstates(Arrays.asList(modded)));
		}

		static void updateSmoothables(boolean newValue, BlockState[] states, List<String> whitelist, List<String> blacklist) {
			for (BlockState state : states) {
				String string = BlockStateConverter.toString(state);
				if (newValue) {
					NoCubes.smoothableHandler.addSmoothable(state);
					whitelist.add(string);
					blacklist.remove(string);
				} else {
					NoCubes.smoothableHandler.removeSmoothable(state);
					whitelist.remove(string);
					blacklist.add(string);
				}
			}
		}

		static void recomputeInMemoryLookup(List<? extends String> whitelist, List<? extends String> blacklist) {
			Set<BlockState> whitelisted = parseBlockstates(whitelist);
			Set<BlockState> blacklisted = parseBlockstates(blacklist);
			ForgeRegistries.BLOCKS.getValues().parallelStream()
				.flatMap(block -> ModUtil.getStates(block).parallelStream())
				.forEach(state -> {
					if (blacklisted.contains(state))
						NoCubes.smoothableHandler.removeSmoothable(state);
					else if (whitelisted.contains(state) || Smoothables.DEFAULT_SMOOTHABLES.contains(state))
						NoCubes.smoothableHandler.addSmoothable(state);
				});
		}

		static Set<BlockState> parseBlockstates(List<? extends String> list) {
			Set<BlockState> set = Sets.newIdentityHashSet();
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
