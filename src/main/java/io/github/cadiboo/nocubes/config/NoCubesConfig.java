package io.github.cadiboo.nocubes.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.Lists;
import io.github.cadiboo.nocubes.future.*;
import io.github.cadiboo.nocubes.future.ForgeConfigSpec.BooleanValue;
import io.github.cadiboo.nocubes.future.ForgeConfigSpec.ConfigValue;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import io.github.cadiboo.nocubes.util.ColorParser;
import io.github.cadiboo.nocubes.util.IsSmoothable;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * The Config for NoCubes.
 * Contains the Common, Client and Sever configs as inner classes.
 * Handles registering and baking the configs.
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = MOD_ID)
public final class NoCubesConfig {

	public static final Set<IBlockState> MODDED_TERRAIN_STATES = Collections.emptySet();

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
		/*if (spec == Common.SPEC)
			Common.bake();
		else */
		if (spec == Client.SPEC)
			Client.bake();
		else if (spec == Server.SPEC)
			Server.bake();
	}

	private static void setAllSmoothables(IsSmoothable isSmoothable, Collection<? extends String> whitelist, Collection<? extends String> blacklist, Set<IBlockState> modded) {
		Set<IBlockState> whitelisted = BlockStateConverter.fromStrings(whitelist);
		Set<IBlockState> blacklisted = BlockStateConverter.fromStrings(blacklist);
		ForgeRegistries.BLOCKS.getValues().parallelStream()
//				.flatMap(block -> block.getStateContainer().getValidStates().parallelStream())
				.flatMap(block -> block.getBlockState().getValidStates().parallelStream())
				.forEach(state -> {
					boolean smoothable = !blacklisted.contains(state) && (whitelisted.contains(state) || modded.contains(state));
					isSmoothable.set(state, smoothable);
				});
	}

	// Only call with correct type.
	public static void saveAndLoad(ModConfig.Type type) {
		Side side = FMLCommonHandler.instance().getSide();
		if (side == Side.CLIENT && type == ModConfig.Type.SERVER || side == Side.SERVER && type == ModConfig.Type.CLIENT)
			throw new RuntimeException("Config.saveAndLoad called for the wrong side: " + type.name());
		ConfigTracker_getConfig(MOD_ID, type).ifPresent(modConfig -> {
			modConfig.save();
			((CommentedFileConfig) modConfig.getConfigData()).load();
//			modConfig.fireEvent(new ModConfig.Reloading(modConfig));
			fireReloadEvent(modConfig);
		});
	}

	public static Optional<ModConfig> ConfigTracker_getConfig(final String modId, final ModConfig.Type type) {
//		Map<String, Map<ModConfig.Type, ModConfig>> configsByMod = ObfuscationReflectionHelper.getPrivateValue(ConfigTracker.class, ConfigTracker.INSTANCE, "configsByMod");
		Map<String, Map<ModConfig.Type, ModConfig>> configsByMod = ConfigTracker.INSTANCE.configsByMod;
		return Optional.ofNullable(configsByMod.getOrDefault(modId, Collections.emptyMap()).getOrDefault(type, null));
	}

	private static void fireReloadEvent(final ModConfig modConfig) {
//		ModContainer modContainer = ModList.get().getModContainerById(modConfig.getModId()).get();
//		ModConfig.Reloading event;
//		try {
//			event = ObfuscationReflectionHelper.findConstructor(ModConfig.Reloading.class, ModConfig.class).newInstance(modConfig);
//		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
//			throw new RuntimeException(e);
//		}
		FutureModContainer modContainer = new FutureModContainer(Loader.instance().getIndexedModList().get(modConfig.getModId()));
		ModConfig.Reloading event = new ModConfig.Reloading(modConfig);

		modContainer.dispatchConfigEvent(event);
	}

	public static class Client {

		public static final Impl INSTANCE;
		static final ForgeConfigSpec SPEC;
		public static boolean renderSmoothTerrain;
		public static boolean renderSmoothLeaves;
		public static boolean renderSmoothAndVanillaLeaves;
		public static ColorParser.Color selectionBoxColor;
		public static boolean smoothFluidLighting;
		public static boolean smoothFluidColors;
		public static boolean naturalFluidTextures;

		static {
			final Pair<Impl, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Impl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		static void bake() {
			renderSmoothTerrain = INSTANCE.renderSmoothTerrain.get();
			renderSmoothLeaves = INSTANCE.renderSmoothLeaves.get();
			renderSmoothAndVanillaLeaves = INSTANCE.renderSmoothAndVanillaLeaves.get();
			selectionBoxColor = ColorParser.parse(INSTANCE.selectionBoxColor.get());
			List<? extends String> terrainWhitelist = INSTANCE.terrainSmoothableWhitelistPreference.get();
			List<? extends String> terrainBlacklist = INSTANCE.terrainSmoothableBlacklistPreference.get();
			setAllSmoothables(IsSmoothable.TERRAIN, terrainWhitelist, terrainBlacklist, MODDED_TERRAIN_STATES);
			smoothFluidLighting = INSTANCE.smoothFluidLighting.get();
			smoothFluidColors = INSTANCE.smoothFluidColors.get();
			naturalFluidTextures = INSTANCE.naturalFluidTextures.get();
		}

		public static void updateTerrainSmoothable(boolean newValue, IBlockState... states) {
			getLogger().debug("Client.updateSmoothable");
			updateSmoothablesLists(newValue, states, (List) INSTANCE.terrainSmoothableWhitelistPreference.get(), (List) INSTANCE.terrainSmoothableBlacklistPreference.get());
			saveAndLoad(ModConfig.Type.CLIENT);
		}

		public static void updateRenderSmoothTerrain(boolean newValue) {
			System.out.println("Client.updateRenderSmoothTerrain");
			INSTANCE.renderSmoothTerrain.set(newValue);
			saveAndLoad(ModConfig.Type.CLIENT);
		}

		public static void updateRenderSmoothLeaves(boolean newValue) {
			System.out.println("Client.updateRenderSmoothLeaves");
			INSTANCE.renderSmoothLeaves.set(newValue);
			saveAndLoad(ModConfig.Type.CLIENT);
		}

		static class Impl {

			final BooleanValue renderSmoothTerrain;
			final BooleanValue renderSmoothLeaves;
			final BooleanValue renderSmoothAndVanillaLeaves;
			final ConfigValue<String> selectionBoxColor;
			final ConfigValue<List<? extends String>> terrainSmoothableWhitelistPreference;
			final ConfigValue<List<? extends String>> terrainSmoothableBlacklistPreference;
			final BooleanValue smoothFluidLighting;
			final BooleanValue smoothFluidColors;
			final BooleanValue naturalFluidTextures;

			private Impl(final ForgeConfigSpec.Builder builder) {
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

				// Stable, doesn't need refactoring
				selectionBoxColor = builder
					.translation(MOD_ID + ".config.selectionBoxColor")
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
						"With HSLA (hue, saturation, lightness, alpha): \"hsl(270, 100%, 100%, 50%)\" (a partially transparent dark purple)"
					)
					.define("selectionBoxColor", "#0006");

				terrainSmoothableWhitelistPreference = builder
					.translation(MOD_ID + ".config.terrainSmoothableWhitelistPreference")
					.defineList("terrainSmoothableWhitelistPreference", Lists::newArrayList, String.class::isInstance);

				terrainSmoothableBlacklistPreference = builder
					.translation(MOD_ID + ".config.terrainSmoothableBlacklistPreference")
					.defineList("terrainSmoothableBlacklistPreference", Lists::newArrayList, String.class::isInstance);


				builder
						.push("fluids");
				{
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
				}
			}

		}

	}

	public static class Server {

		public static final Impl INSTANCE;
		public static final ForgeConfigSpec SPEC;
		public static boolean terrainCollisionsEnabled;

		static {
			final Pair<Impl, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Impl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		public static void bake() {
			terrainCollisionsEnabled = INSTANCE.terrainCollisionsEnabled.get();
			List<? extends String> whitelist = INSTANCE.terrainSmoothableWhitelist.get();
			List<? extends String> blacklist = INSTANCE.terrainSmoothableBlacklist.get();
			setAllSmoothables(IsSmoothable.TERRAIN, whitelist, blacklist, MODDED_TERRAIN_STATES);
		}

		static class Impl {

			final BooleanValue terrainCollisionsEnabled;
			/**
			 * These lists can contain whatever valid/invalid strings the user wants.
			 * We do not clear them as we want them to be able to add a state, remove the mod,
			 * add the mod and still have the state be smoothable.
			 */
			final ConfigValue<List<? extends String>> terrainSmoothableWhitelist;
			final ConfigValue<List<? extends String>> terrainSmoothableBlacklist;

			private Impl(final ForgeConfigSpec.Builder builder) {
				terrainCollisionsEnabled = builder
						.translation(MOD_ID + ".config.terrainCollisionsEnabled")
						.define("terrainCollisionsEnabled", true);

				terrainSmoothableWhitelist = builder
					.translation(MOD_ID + ".config.terrainSmoothableWhitelist")
					.defineList("terrainSmoothableWhitelist", Lists::newArrayList, String.class::isInstance);

				terrainSmoothableBlacklist = builder
					.translation(MOD_ID + ".config.terrainSmoothableBlacklist")
					.defineList("terrainSmoothableBlacklist", Lists::newArrayList, String.class::isInstance);
			}

		}

		public static void setTerrainCollisions(boolean newValue) {
			INSTANCE.terrainCollisionsEnabled.set(newValue);
			saveAndLoad(ModConfig.Type.SERVER);
		}

		public static void updateTerrainSmoothable(boolean newValue, IBlockState... states) {
			getLogger().debug("Server.updateSmoothable");
			updateSmoothablesLists(newValue, states, (List) INSTANCE.terrainSmoothableWhitelist.get(), (List) INSTANCE.terrainSmoothableBlacklist.get());
			saveAndLoad(ModConfig.Type.SERVER);
		}

	}

	public static class Common {

		public static final Impl INSTANCE;
		public static final ForgeConfigSpec SPEC;
		public static boolean autoUpdaterEnabled;

		static {
			final Pair<Impl, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Impl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		public static void bake() {
			autoUpdaterEnabled = INSTANCE.autoUpdaterEnabled.get();
		}

		public static void updateAutoUpdaterEnabled(boolean newValue) {
			getLogger().debug("updateAutoUpdaterEnabled: " + newValue);
			INSTANCE.autoUpdaterEnabled.set(newValue);
			saveAndLoad(ModConfig.Type.CLIENT);
		}

		static class Impl {

			final BooleanValue autoUpdaterEnabled;

			private Impl(final ForgeConfigSpec.Builder builder) {
				autoUpdaterEnabled = builder
						.translation(MOD_ID + ".config.autoUpdaterEnabled")
						.define("autoUpdaterEnabled", true);
			}
		}
	}

	private static void updateSmoothablesLists(boolean newValue, final IBlockState[] states, final List<String> whitelist, final List<String> blacklist) {
		for (IBlockState state : states) {
			String string = BlockStateConverter.toString(state);
			if (newValue) {
				whitelist.add(string);
				blacklist.remove(string);
			} else {
				whitelist.remove(string);
				blacklist.add(string);
			}
		}
	}

}
