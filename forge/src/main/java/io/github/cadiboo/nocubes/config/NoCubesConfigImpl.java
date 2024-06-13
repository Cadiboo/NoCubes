package io.github.cadiboo.nocubes.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.google.common.collect.Lists;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.NoCubesConfig.Server.MesherType;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.network.S2CUpdateServerConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
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
import java.util.function.Consumer;

import static io.github.cadiboo.nocubes.client.RenderHelper.reloadAllChunks;

/**
 * Handles registering and baking the configs.
 *
 * @author Cadiboo
 * @see NoCubesConfig
 */
public final class NoCubesConfigImpl {

	private static final Logger LOG = LogManager.getLogger();

	/**
	 * Called from inside the mod constructor.
	 *
	 * @param context The {@link ModLoadingContext} to register the configs to
	 */
	public static void register(ModLoadingContext context, IEventBus modBus) {
		var specs = new HashMap<ForgeConfigSpec, Pair<ModConfig.Type, Consumer<ModConfig>>>();
		specs.put(Common.SPEC, Pair.of(ModConfig.Type.COMMON, Common::bake));
		specs.put(Client.SPEC, Pair.of(ModConfig.Type.CLIENT, Client::bake));
		specs.put(Server.SPEC, Pair.of(ModConfig.Type.SERVER, Server::bake));
		specs.forEach((spec, typeAndBaker) -> context.registerConfig(typeAndBaker.getKey(), spec));
		modBus.addListener((ModConfigEvent event) -> {
			var config = event.getConfig();
			var typeAndBaker = specs.get(config.getSpec());
			if (typeAndBaker == null)
				LOG.debug("Received config event for unknown config {}", config.getFileName());
			else
				bakeConfig(config, typeAndBaker.getValue());
		});
	}

	/**
	 * Each time our config changes we get the values from it and store them in our own fields ('baking' them)
	 * instead of looking up the values on the config (which is pretty slow) each time we need them.
	 */
	private static void bakeConfig(ModConfig config, Consumer<ModConfig> baker) {
		if (!((ForgeConfigSpec) config.getSpec()).isLoaded()) {
			LOG.debug("Not baking unloaded config {}", config.getFileName());
			return;
		}
		LOG.debug("Baking config {}", config.getFileName());
		baker.accept(config);
	}

	/**
	 * Settings that effect the app state regardless of if it is a client or server.
	 */
	public static class Common {

		public static final Impl INSTANCE;
		public static final ForgeConfigSpec SPEC;

		static {
			var specPair = new Builder().configure(Impl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		/**
		 * See {@link NoCubesConfigImpl#bakeConfig}
		 */
		public static void bake(ModConfig config) {
			NoCubesConfig.Common.debugEnabled = INSTANCE.debugEnabled.get();
		}

		/**
		 * Responsible for interfacing with Forge's config API and creating a Config with all our options.
		 */
		static class Impl {

			final BooleanValue debugEnabled;

			private Impl(Builder builder) {
				debugEnabled = builder
					.translation(NoCubes.MOD_ID + ".config.debugEnabled")
					.comment("If debugging features should be enabled")
					.define("debugEnabled", false);
			}
		}
	}

	/**
	 * Settings that effect the client state i.e. rendering options.
	 */
	public static class Client {

		public static final String INFO_MESSAGE = "infoMessage";
		public static String RENDER = "render";

		public static final Impl INSTANCE;
		public static final ForgeConfigSpec SPEC;

		static {
			var specPair = new Builder().configure(Impl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		/**
		 * See {@link NoCubesConfigImpl#bakeConfig}
		 */
		public static void bake(ModConfig config) {
			boolean oldRender = NoCubesConfig.Client.render;
			int oldChunkRenderSettingsHash = hashChunkRenderSettings();

			NoCubesConfig.Client.infoMessage = INSTANCE.infoMessage.get();
			// Directly querying the baked 'forceVisuals' field - won't cause a NPE on the client when there is no server
			NoCubesConfig.Client.render = NoCubesConfig.Server.forceVisuals || INSTANCE.render.get();
			NoCubesConfig.Client.renderSelectionBox = INSTANCE.renderSelectionBox.get();
			NoCubesConfig.Client.selectionBoxColor = ColorParser.parse(INSTANCE.selectionBoxColor.get()).toRenderableColor();
			NoCubesConfig.Client.betterGrassSides = INSTANCE.betterGrassSides.get();
			NoCubesConfig.Client.moreSnow = INSTANCE.moreSnow.get();
			NoCubesConfig.Client.fixPlantHeight = INSTANCE.fixPlantHeight.get();
			NoCubesConfig.Client.grassTufts = INSTANCE.grassTufts.get();

			if (oldRender != NoCubesConfig.Client.render)
				reloadAllChunks("custom rendering was toggled to %b in the client config", NoCubesConfig.Client.render);
			else if (NoCubesConfig.Client.render && oldChunkRenderSettingsHash != hashChunkRenderSettings())
				reloadAllChunks("options affecting chunk rendering in the client config were changed");

			NoCubesConfig.Client.debugOutlineSmoothables = INSTANCE.debugOutlineSmoothables.get();
			NoCubesConfig.Client.debugVisualiseDensitiesGrid = INSTANCE.debugVisualiseDensitiesGrid.get();
			NoCubesConfig.Client.debugRenderCollisions = INSTANCE.debugRenderCollisions.get();
			NoCubesConfig.Client.debugRenderMeshCollisions = INSTANCE.debugRenderMeshCollisions.get();
			NoCubesConfig.Client.debugRecordMeshPerformance = INSTANCE.debugRecordMeshPerformance.get();
			NoCubesConfig.Client.debugOutlineNearbyMesh = INSTANCE.debugOutlineNearbyMesh.get();
			NoCubesConfig.Client.debugSkipNoCubesRendering = INSTANCE.debugSkipNoCubesRendering.get();
		}

		private static int hashChunkRenderSettings() {
			return Objects.hash(NoCubesConfig.Client.betterGrassSides, NoCubesConfig.Client.moreSnow, NoCubesConfig.Client.fixPlantHeight, NoCubesConfig.Client.grassTufts);
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

			final BooleanValue infoMessage;
			final BooleanValue render;
			final BooleanValue renderSelectionBox;
			final ConfigValue<String> selectionBoxColor;
			final BooleanValue betterGrassSides;
			final BooleanValue moreSnow;
			final BooleanValue fixPlantHeight;
			final BooleanValue grassTufts;

			final BooleanValue debugOutlineSmoothables;
			final BooleanValue debugVisualiseDensitiesGrid;
			final BooleanValue debugRenderCollisions;
			final BooleanValue debugRenderMeshCollisions;
			final BooleanValue debugRecordMeshPerformance;
			final BooleanValue debugOutlineNearbyMesh;
			final BooleanValue debugSkipNoCubesRendering;

			private Impl(Builder builder) {
				infoMessage = builder
					.translation(NoCubes.MOD_ID + ".config.infoMessage")
					.comment("If NoCubes should display a helpful message when you join a world")
					.define(INFO_MESSAGE, true);

				render = builder
					.translation(NoCubes.MOD_ID + ".config.render")
					.comment("If NoCubes' custom rendering is enabled")
					.define(RENDER, true);

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
					final var debugComment = "Enable debug mode in the common config";
					debugOutlineSmoothables = builder.comment(debugComment).define("debugOutlineSmoothables", false);
					debugVisualiseDensitiesGrid = builder.comment(debugComment).define("debugVisualiseDensitiesGrid", false);
					debugRenderCollisions = builder.comment(debugComment).define("debugRenderCollisions", false);
					debugRenderMeshCollisions = builder.comment(debugComment).define("debugRenderMeshCollisions", false);
					debugRecordMeshPerformance = builder.comment(debugComment).define("debugRecordMeshPerformance", false);
					debugOutlineNearbyMesh = builder.comment(debugComment).define("debugOutlineNearbyMesh", false);
					debugSkipNoCubesRendering = builder.comment(debugComment).define("debugSkipNoCubesRendering", false);
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

		static {
			var specPair = new Builder().configure(Impl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		/**
		 * See {@link NoCubesConfigImpl#bakeConfig}
		 */
		public static void bake(ModConfig config) {
			int oldChunkRenderSettingsHash = hashChunkRenderSettings();

			NoCubesConfig.Smoothables.recomputeInMemoryLookup(NoCubes.platform.blockStateSerializer(), ForgeRegistries.BLOCKS.getValues(), INSTANCE.smoothableWhitelist.get(), INSTANCE.smoothableBlacklist.get(), INSTANCE.useDefaultSmoothableList.get());
			NoCubesConfig.Server.mesher = INSTANCE.mesher.get().instance;
			NoCubesConfig.Server.collisionsEnabled = INSTANCE.collisionsEnabled.get();
			NoCubesConfig.Server.tempMobCollisionsDisabled = INSTANCE.tempMobCollisionsDisabled.get();
			NoCubesConfig.Server.oldStyleCollisionsEnhancementLevel = INSTANCE.oldStyleCollisionsEnhancementLevel.get();
			NoCubesConfig.Server.onlyOldStyleCollisions = INSTANCE.onlyOldStyleCollisions.get();
			NoCubesConfig.Server.forceVisuals = INSTANCE.forceVisuals.get();
			if (NoCubesConfig.Server.forceVisuals)
				// Directly setting the baked field - won't cause a NPE on the dedicated server
				NoCubesConfig.Client.render = true;
			NoCubesConfig.Server.extendFluidsRange = validateRange(0, 2, INSTANCE.extendFluidsRange.get(), "extendFluidsRange");
			NoCubesConfig.Server.oldNoCubesSlopes = INSTANCE.oldNoCubesSlopes.get();
			NoCubesConfig.Server.oldNoCubesInFluids = INSTANCE.oldNoCubesInFluids.get();
			NoCubesConfig.Server.oldNoCubesRoughness = validateRange(0d, 1d, INSTANCE.oldNoCubesRoughness.get(), "oldNoCubesRoughness").floatValue();

			if (NoCubesConfig.Client.render && oldChunkRenderSettingsHash != hashChunkRenderSettings())
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
			var smoothables = ForgeRegistries.BLOCKS.getValues().stream()
				.flatMap(block -> ModUtil.getStates(block).stream())
				.map(NoCubes.smoothableHandler::isSmoothable)
				.toArray(Boolean[]::new);
			return Objects.hash(NoCubesConfig.Server.mesher, NoCubesConfig.Server.forceVisuals, Arrays.hashCode(smoothables));
		}

		public static void updateSmoothable(boolean newValue, BlockState... states) {
			NoCubesConfig.Smoothables.updateUserDefinedSmoothableStringLists(newValue, states, (List) INSTANCE.smoothableWhitelist.get(), (List) INSTANCE.smoothableBlacklist.get());
			saveAndLoad();
		}

		private static void saveAndLoad() {
			Hacks.saveAndLoad(ModConfig.Type.SERVER);
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
			final BooleanValue useDefaultSmoothableList;
			final EnumValue<MesherType> mesher;
			final BooleanValue collisionsEnabled;
			final BooleanValue tempMobCollisionsDisabled;
			final IntValue oldStyleCollisionsEnhancementLevel;
			final BooleanValue onlyOldStyleCollisions;
			final BooleanValue forceVisuals;
			final IntValue extendFluidsRange;
			final BooleanValue oldNoCubesSlopes;
			final BooleanValue oldNoCubesInFluids;
			final DoubleValue oldNoCubesRoughness;

			private Impl(Builder builder) {
				final var smoothableListCommentExtra = "Instead of manually editing this list, you can smoothen or un-smoothen blocks by looking at them in-game and pressing the 'N' key, or whatever it may have been rebound to.";
				smoothableWhitelist = builder
					.translation(NoCubes.MOD_ID + ".config.smoothableWhitelist")
					.comment(
						"What blocks should be smoothed by NoCubes (same syntax as the /setblock command)",
						smoothableListCommentExtra
					)
					.defineListAllowEmpty(Collections.singletonList("smoothableWhitelist"), Lists::newArrayList, String.class::isInstance);

				smoothableBlacklist = builder
					.translation(NoCubes.MOD_ID + ".config.smoothableBlacklist")
					.comment(
						"What blocks should not be smoothed by NoCubes (same syntax as the /setblock command)",
						smoothableListCommentExtra
					)
					.defineListAllowEmpty(Collections.singletonList("smoothableBlacklist"), Lists::newArrayList, String.class::isInstance);

				useDefaultSmoothableList = builder
					.translation(NoCubes.MOD_ID + ".config.useDefaultSmoothableList")
					.comment("If NoCubes should smooth common natural blocks (e.g. dirt, stone, ore) even if they are not included in the above whitelist")
					.define("useDefaultSmoothableList", true);

				collisionsEnabled = builder
					.translation(NoCubes.MOD_ID + ".config.collisionsEnabled")
					.comment("If players should be able to walk up the smooth slopes generated by NoCubes")
					.define("collisionsEnabled", true);

				tempMobCollisionsDisabled = builder
					.translation(NoCubes.MOD_ID + ".config.tempMobCollisionsDisabled")
					.comment("If ONLY players should be able to walk up the smooth slopes generated by NoCubes")
					.define("tempMobCollisionsDisabled", false);

				oldStyleCollisionsEnhancementLevel = builder
					.translation(NoCubes.MOD_ID + ".config.oldStyleCollisionsEnhancementLevel")
					.comment(
						"Set to a value higher than 0 if the old collisions system from 1.13.2-0.2.9-pre11 should be enabled",
						"Higher value means more enhancement and worse performance"
					)
					.defineInRange("oldStyleCollisionsEnhancementLevel", 0, 0, CollisionHandler.OLD_COLLISIONS_ENHANCEMENT_LEVEL_MAX);

				onlyOldStyleCollisions = builder
					.translation(NoCubes.MOD_ID + ".config.onlyOldStyleCollisions")
					.comment("If ONLY the old-style collision algorithm should be used (only use this in conjunction with 'oldStyleCollisionsEnhancementLevel')")
					.define("onlyOldStyleCollisions", false);

				mesher = builder
					.translation(NoCubes.MOD_ID + ".config.meshGenerator")
					.comment("The algorithm that should be used to smooth terrain")
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

				oldNoCubesSlopes = builder
					.translation(NoCubes.MOD_ID + ".config.oldNoCubesSlopes")
					.comment(
						"If slopes should be featured in the mesh generated by OldNoCubes",
						"Disable this if you simply want roughness applied to the ground as demonstrated in https://youtu.be/46uok05EKbY"
					)
					.define("oldNoCubesSlopes", true);

				oldNoCubesInFluids = builder
					.translation(NoCubes.MOD_ID + ".config.oldNoCubesInFluids")
					.comment("If slopes should be generated inside fluids by OldNoCubes")
					.define("oldNoCubesInFluids", true);

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
				loadConfig(modConfig);
			});
		}

		// Separate function so I can easily call it while debugging
		static void loadConfig(ModConfig modConfig) {
			((CommentedFileConfig) modConfig.getConfigData()).load();
			modConfig.getSpec().afterReload();
//			modConfig.fireEvent(new IConfigEvent.reloading(modConfig));
			ModConfig_fireEvent(modConfig, IConfigEvent.reloading(modConfig));
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

}
