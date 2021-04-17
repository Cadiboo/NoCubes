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
	 * Stores the list of blocks that 'just are' smoothable by default.
	 * This includes stuff like Stone and any blocks that other mods register as smoothable.
	 */
	public static Set<BlockState> DEFAULT_SMOOTHABLES = Sets.newIdentityHashSet();

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

	/**
	 * Called from inside the mod constructor.
	 *
	 * @param context The ModLoadingContext to register the configs to
	 */
	public static void register(final ModLoadingContext context) {
//		context.registerConfig(ModConfig.Type.COMMON, Common.SPEC);
		context.registerConfig(ModConfig.Type.CLIENT, Client.SPEC);
		context.registerConfig(ModConfig.Type.SERVER, Server.SPEC);
	}

	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
		final ForgeConfigSpec spec = configEvent.getConfig().getSpec();
		/*if (spec == Common.SPEC)
			Common.bake();
		else */
		if (spec == Client.SPEC) {
			Client.bake();
			recomputeSmoothables(Client.INSTANCE.smoothableWhitelistPreference.get(), Client.INSTANCE.smoothableBlacklistPreference.get());
		} else if (spec == Server.SPEC) {
			Server.bake();
			recomputeSmoothables(Server.INSTANCE.smoothableWhitelist.get(), Server.INSTANCE.smoothableBlacklist.get());
		}
	}

	private static void recomputeSmoothables(List<? extends String> whitelist, List<? extends String> blacklist) {
		Set<BlockState> whitelisted = parseBlockstates(whitelist);
		Set<BlockState> blacklisted = parseBlockstates(blacklist);
		ForgeRegistries.BLOCKS.getValues().parallelStream()
			.flatMap(block -> ModUtil.getStates(block).parallelStream())
			.forEach(state -> {
				if (blacklisted.contains(state))
					NoCubes.smoothableHandler.removeSmoothable(state);
				else if (whitelisted.contains(state) || DEFAULT_SMOOTHABLES.contains(state))
					NoCubes.smoothableHandler.addSmoothable(state);
			});
	}

	private static Set<BlockState> parseBlockstates(List<? extends String> list) {
		Set<BlockState> set = Sets.newIdentityHashSet();
		list.parallelStream()
			.map(BlockStateConverter::fromStringOrNull)
			.filter(Objects::nonNull)
			.forEach(set::add);
		return set;
	}

	// Only call with correct type.
	public static void saveAndLoad(final ModConfig.Type type) {
		ConfigTracker_getConfig(NoCubes.MOD_ID, type).ifPresent(modConfig -> {
			modConfig.save();
			((CommentedFileConfig) modConfig.getConfigData()).load();
//			modConfig.fireEvent(new ModConfig.Reloading(modConfig));
			fireReloadEvent(modConfig);
		});
	}

	private static Optional<ModConfig> ConfigTracker_getConfig(final String modId, final ModConfig.Type type) {
		Map<String, Map<ModConfig.Type, ModConfig>> configsByMod = ObfuscationReflectionHelper.getPrivateValue(ConfigTracker.class, ConfigTracker.INSTANCE, "configsByMod");
		return Optional.ofNullable(configsByMod.getOrDefault(modId, Collections.emptyMap()).getOrDefault(type, null));
	}

	private static void fireReloadEvent(final ModConfig modConfig) {
		final ModContainer modContainer = ModList.get().getModContainerById(modConfig.getModId()).get();
		final ModConfig.Reloading event;
		try {
			event = ObfuscationReflectionHelper.findConstructor(ModConfig.Reloading.class, ModConfig.class).newInstance(modConfig);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		modContainer.dispatchConfigEvent(event);
	}

	public static class Client {

		public static final Impl INSTANCE;
		public static final ForgeConfigSpec SPEC;
		public static boolean render;
		public static ColorParser.Color selectionBoxColor;

		static {
			final Pair<Impl, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Impl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		public static void bake() {
			render = INSTANCE.render.get();
			selectionBoxColor = ColorParser.parse(INSTANCE.selectionBoxColor.get());
		}

		public static void updateSmoothablePreference(final boolean newValue, final BlockState... states) {
			final NoCubesConfig.Client.Impl cfg = NoCubesConfig.Client.INSTANCE;
			NoCubesConfig.updateSmoothable(newValue, states, (List) cfg.smoothableWhitelistPreference.get(), (List) cfg.smoothableBlacklistPreference.get());
			saveAndLoad(ModConfig.Type.CLIENT);
		}

		public static void updateRender(final boolean newValue) {
			Client.INSTANCE.render.set(newValue);
			saveAndLoad(ModConfig.Type.CLIENT);
		}

		static class Impl {

			final BooleanValue render;
			final ConfigValue<String> selectionBoxColor;
			final ConfigValue<List<? extends String>> smoothableWhitelistPreference;
			final ConfigValue<List<? extends String>> smoothableBlacklistPreference;

			private Impl(final ForgeConfigSpec.Builder builder) {
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
						"With HSLA (hue, saturation, lightness, alpha): \"hsl(270, 100%, 100%, 50%)\" (a partially transparent dark purple)"
					)
					.define("selectionBoxColor", "#0006");

				smoothableWhitelistPreference = builder
					.translation(NoCubes.MOD_ID + ".config.smoothableWhitelistPreference")
					.defineList("smoothableWhitelistPreference", Lists::newArrayList, String.class::isInstance);

				smoothableBlacklistPreference = builder
					.translation(NoCubes.MOD_ID + ".config.smoothableBlacklistPreference")
					.defineList("smoothableBlacklistPreference", Lists::newArrayList, String.class::isInstance);
			}

		}

	}

	public static class Server {

		public static final Impl INSTANCE;
		public static final ForgeConfigSpec SPEC;
		public static MeshGenerator meshGenerator = new SurfaceNets();

		static {
			final Pair<Impl, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Impl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		public static void bake() {
		}

		static class Impl {

			/**
			 * These lists can contain whatever valid/invalid strings the user wants.
			 * We do not clear them as we want them to be able to add a state, remove the mod,
			 * add the mod and still have the state be smoothable.
			 */
			final ConfigValue<List<? extends String>> smoothableWhitelist;
			final ConfigValue<List<? extends String>> smoothableBlacklist;

			private Impl(final ForgeConfigSpec.Builder builder) {
				smoothableWhitelist = builder
					.translation(NoCubes.MOD_ID + ".config.smoothableWhitelist")
					.defineList("smoothableWhitelist", Lists::newArrayList, String.class::isInstance);

				smoothableBlacklist = builder
					.translation(NoCubes.MOD_ID + ".config.smoothableBlacklist")
					.defineList("smoothableBlacklist", Lists::newArrayList, String.class::isInstance);
			}

		}

		public static void updateSmoothable(final boolean newValue, final BlockState... states) {
			System.out.println("Server.updateSmoothable");
			final NoCubesConfig.Server.Impl cfg = NoCubesConfig.Server.INSTANCE;
			NoCubesConfig.updateSmoothable(newValue, states, (List) cfg.smoothableWhitelist.get(), (List) cfg.smoothableBlacklist.get());
			saveAndLoad(ModConfig.Type.SERVER);
		}

	}

	private static void updateSmoothable(final boolean newValue, final BlockState[] states, final List<String> whitelist, final List<String> blacklist) {
		for (final BlockState state : states) {
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

}
