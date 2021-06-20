package io.github.cadiboo.nocubes.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.mesh.*;
import io.github.cadiboo.nocubes.network.C2SRequestUpdateSmoothable;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.network.S2CUpdateServerConfig;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

	private static long lastSavedClientConfigAt = -1;
	private static long lastSavedServerConfigAt = -1;

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
		// TODO: Check if file modification time is smaller than 'lastSavedConfigAt' and reject if TRUE and file is not null
		ModConfig config = configEvent.getConfig();
		ForgeConfigSpec spec = config.getSpec();
//		if (spec == Common.SPEC && didNotSaveConfigRecently(lastSavedCommonConfigAt)) {
//			Common.bake();
//			lastSavedCommonConfigAt = -1;
//		} else
		if (spec == Client.SPEC && didNotSaveConfigRecently(lastSavedClientConfigAt)) {
			Client.bake(config);
			lastSavedClientConfigAt = -1;
		} else if (spec == Server.SPEC && didNotSaveConfigRecently(lastSavedServerConfigAt)) {
			Server.bake(config);
			lastSavedServerConfigAt = -1;
		}
	}

	static boolean didNotSaveConfigRecently(long lastSavedConfigAt) {
		final long ten_seconds = 10L * 1000_000_000;
		long now = System.nanoTime();
		return now - ten_seconds > lastSavedConfigAt;
	}

	/**
	 * Settings that effect the client state i.e. rendering options.
	 */
	public static class Client {

		public static final Impl INSTANCE;
		public static final ForgeConfigSpec SPEC;
		public static boolean render;
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
			boolean oldRender = render;
			int oldChunkRenderSettingsHash = hashChunkRenderSettings();

			// Directly querying the baked 'forceVisuals' field - won't cause a NPE on the client when there is no server
			render = Server.forceVisuals || INSTANCE.render.get();
			selectionBoxColor = ColorParser.parse(INSTANCE.selectionBoxColor.get());
			betterGrassSides = INSTANCE.betterGrassSides.get();
			moreSnow = INSTANCE.moreSnow.get();
			fixPlantHeight = INSTANCE.fixPlantHeight.get();
			grassTufts = INSTANCE.grassTufts.get();

			if (oldRender != render || (render && oldChunkRenderSettingsHash != hashChunkRenderSettings()))
				ClientUtil.reloadAllChunks();

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
			// Allow our bake method to run with the new values
			lastSavedClientConfigAt = -1;
			ReloadHacks.saveAndLoad(ModConfig.Type.CLIENT);
			lastSavedClientConfigAt = System.nanoTime();
		}

		/**
		 * Responsible for interfacing with Forge's config API and creating a Config with all our options.
		 */
		static class Impl {

			final BooleanValue render;
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
		public static MeshGenerator meshGenerator;
		public static boolean collisionsEnabled;
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
			// TODO: How are these values changing handled on the client?
			//  Does forge auto sync the config when it changes, if not I need to
			int oldChunkRenderSettingsHash = hashChunkRenderSettings();

			Smoothables.recomputeInMemoryLookup(INSTANCE.smoothableWhitelist.get(), INSTANCE.smoothableBlacklist.get());
			meshGenerator = INSTANCE.meshGenerator.get().generator;
			collisionsEnabled = INSTANCE.collisionsEnabled.get();
			forceVisuals = INSTANCE.forceVisuals.get();
			if (forceVisuals)
				// Directly setting the baked field - won't cause a NPE on the dedicated server
				Client.render = true;
			extendFluidsRange = INSTANCE.extendFluidsRange.get();
			if (extendFluidsRange < 0 || extendFluidsRange > 2)
				throw new IllegalStateException("Config was not validated! 'extendFluidsRange' must be between 0 and 2 but was " + extendFluidsRange);
			oldNoCubesRoughness = INSTANCE.oldNoCubesRoughness.get().floatValue();
			if (oldNoCubesRoughness < 0 || oldNoCubesRoughness > 1)
				throw new IllegalStateException("Config was not validated! 'oldNoCubesRoughness' must be between 0 and 1 but was " + oldNoCubesRoughness);
			if (oldChunkRenderSettingsHash != hashChunkRenderSettings())
				DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientUtil::reloadAllChunks);
			if (FMLEnvironment.dist.isDedicatedServer())
				NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), S2CUpdateServerConfig.create(config));
		}

		private static int hashChunkRenderSettings() {
			return Objects.hash(meshGenerator, forceVisuals);
		}

		public static void updateSmoothable(boolean newValue, BlockState... states) {
			Smoothables.updateSmoothables(newValue, states, (List) INSTANCE.smoothableWhitelist.get(), (List) INSTANCE.smoothableBlacklist.get());
			saveAndLoad();
		}

		private static void saveAndLoad() {
			// Allow our bake method to run with the new values
			lastSavedServerConfigAt = -1;
			ReloadHacks.saveAndLoad(ModConfig.Type.SERVER);
			lastSavedServerConfigAt = System.nanoTime();
		}

		public enum MeshGeneratorEnum {
			SurfaceNets(new SurfaceNets()),
			OldNoCubes(new OldNoCubes()),
			Debug_MarchingCubes(new MarchingCubes()),
			Debug_CullingCubic(new CullingCubic()),
			Debug_StupidCubic(new StupidCubic()),
			;

			public final MeshGenerator generator;

			MeshGeneratorEnum(MeshGenerator generator) {
				this.generator = generator;
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
			final EnumValue<MeshGeneratorEnum> meshGenerator;
			final BooleanValue collisionsEnabled;
			final BooleanValue forceVisuals;
			final IntValue extendFluidsRange;
			final DoubleValue oldNoCubesRoughness;

			private Impl(ForgeConfigSpec.Builder builder) {
				smoothableWhitelist = builder
					.translation(NoCubes.MOD_ID + ".config.smoothableWhitelist")
					.comment("What blocks should be smoothed by NoCubes")
					.defineListAllowEmpty(Collections.singletonList("smoothableWhitelist"), Lists::newArrayList, String.class::isInstance);

				smoothableBlacklist = builder
					.translation(NoCubes.MOD_ID + ".config.smoothableBlacklist")
					.comment("What blocks should not be smoothed by NoCubes")
					.defineListAllowEmpty(Collections.singletonList("smoothableBlacklist"), Lists::newArrayList, String.class::isInstance);

				collisionsEnabled = builder
					.translation(NoCubes.MOD_ID + ".config.collisionsEnabled")
					.comment("If players should be able to walk up the smooth slopes generated by NoCubes")
					.define("collisionsEnabled", true);

				meshGenerator = builder
					.translation(NoCubes.MOD_ID + ".config.meshGenerator")
					.comment("meshGenerator")
					.defineEnum("meshGenerator", MeshGeneratorEnum.SurfaceNets);

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
	public static class ReloadHacks {

		// Only call with correct type.
		static void saveAndLoad(ModConfig.Type type) {
			ConfigTracker_getConfig(NoCubes.MOD_ID, type).ifPresent(modConfig -> {
				modConfig.save();
				((CommentedFileConfig) modConfig.getConfigData()).load();
				modConfig.getSpec().afterReload();
//				modConfig.fireEvent(new ModConfig.Reloading(modConfig));
				fireReloadEvent(modConfig);
			});
		}

		private static Optional<ModConfig> ConfigTracker_getConfig(String modId, ModConfig.Type type) {
			Map<String, Map<ModConfig.Type, ModConfig>> configsByMod = ObfuscationReflectionHelper.getPrivateValue(ConfigTracker.class, ConfigTracker.INSTANCE, "configsByMod");
			return Optional.ofNullable(configsByMod.getOrDefault(modId, Collections.emptyMap()).getOrDefault(type, null));
		}


		private static void ModConfig_setConfigData(ModConfig modConfig, CommentedConfig data) {
			Method setConfigData = ObfuscationReflectionHelper.findMethod(ModConfig.class, "setConfigData", CommentedConfig.class);
			try {
				setConfigData.invoke(modConfig, data);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException("Could not set config data for config " + modConfig, e);
			}
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

		public static void receiveSyncedServerConfig(S2CUpdateServerConfig s2CConfigData) {
			assert FMLEnvironment.dist.isClient();
			ModConfig modConfig = ConfigTracker_getConfig(NoCubes.MOD_ID, ModConfig.Type.SERVER).get();
			ConfigParser<CommentedConfig> parser = (ConfigParser<CommentedConfig>) modConfig.getConfigData().configFormat().createParser();
			ModConfig_setConfigData(modConfig, parser.parse(new ByteArrayInputStream(s2CConfigData.getBytes())));
			fireReloadEvent(modConfig);
		}
	}

	public static class Smoothables {

		/**
		 * Stores the list of blocks that 'just are' smoothable by default.
		 * This includes stuff like Stone and any blocks that other mods register as smoothable.
		 */
		private static final Set<BlockState> DEFAULT_SMOOTHABLES = Sets.newIdentityHashSet();

		static {
			List<BlockState> vanilla = Lists.newArrayList(
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
				NETHER_GOLD_ORE.defaultBlockState(),

				INFESTED_STONE.defaultBlockState(),
				BONE_BLOCK.defaultBlockState().setValue(AXIS, Direction.Axis.X),
				BONE_BLOCK.defaultBlockState().setValue(AXIS, Direction.Axis.Y),
				BONE_BLOCK.defaultBlockState().setValue(AXIS, Direction.Axis.Z),

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

				ICE.defaultBlockState(),
				PACKED_ICE.defaultBlockState(),
				FROSTED_ICE.defaultBlockState().setValue(AGE_3, 0),
				FROSTED_ICE.defaultBlockState().setValue(AGE_3, 1),
				FROSTED_ICE.defaultBlockState().setValue(AGE_3, 2),
				FROSTED_ICE.defaultBlockState().setValue(AGE_3, 3),

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
				SOUL_SOIL.defaultBlockState(),
				BASALT.defaultBlockState(),
				MAGMA_BLOCK.defaultBlockState(),
				GLOWSTONE.defaultBlockState(),
				NETHER_WART_BLOCK.defaultBlockState(),
				CRIMSON_STEM.defaultBlockState(),
				WARPED_NYLIUM.defaultBlockState(),
				WARPED_WART_BLOCK.defaultBlockState(),
				WARPED_STEM.defaultBlockState(),

				END_STONE.defaultBlockState(),

				MYCELIUM.defaultBlockState().setValue(SNOWY, false),
				MYCELIUM.defaultBlockState().setValue(SNOWY, true)
			);

			for (Block log : new Block[]{OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG}) {
				for (Direction.Axis axis : Direction.Axis.values())
					vanilla.add(log.defaultBlockState().setValue(AXIS, axis));
			}

			for (Block leaves : new Block[]{OAK_LEAVES, SPRUCE_LEAVES, BIRCH_LEAVES, JUNGLE_LEAVES, ACACIA_LEAVES, DARK_OAK_LEAVES}) {
				for (int distance = 1; distance <= 7; ++distance)
					vanilla.add(leaves.defaultBlockState().setValue(DISTANCE, distance));
			}

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
			DEFAULT_SMOOTHABLES.addAll(vanilla);
			DEFAULT_SMOOTHABLES.addAll(parseBlockStates(Arrays.asList(modded)));
		}

		static void updateSmoothables(boolean newValue, BlockState[] states, List<String> whitelist, List<String> blacklist) {
			List<String> toAddTo = newValue ? whitelist : blacklist;
			List<String> toRemoveFrom = newValue ? blacklist : whitelist;
			for (BlockState state : states) {
				String string = BlockStateConverter.toString(state);
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
			Set<BlockState> whitelisted = parseBlockStates(whitelist);
			Set<BlockState> blacklisted = parseBlockStates(blacklist);
			ForgeRegistries.BLOCKS.getValues().parallelStream()
				.flatMap(block -> ModUtil.getStates(block).parallelStream())
				.forEach(state -> {
					if (blacklisted.contains(state))
						NoCubes.smoothableHandler.removeSmoothable(state);
					else if (whitelisted.contains(state) || Smoothables.DEFAULT_SMOOTHABLES.contains(state))
						NoCubes.smoothableHandler.addSmoothable(state);
				});
		}

		static Set<BlockState> parseBlockStates(List<? extends String> list) {
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
