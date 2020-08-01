package io.github.cadiboo.nocubes.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.Lists;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
			Set<BlockState> whitelisted = Client.INSTANCE.smoothableWhitelistPreference.get().parallelStream()
				.map(BlockStateConverter::fromStringOrNull)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
			Set<BlockState> blacklisted = Client.INSTANCE.smoothableBlacklistPreference.get().parallelStream()
				.map(BlockStateConverter::fromStringOrNull)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
			ForgeRegistries.BLOCKS.getValues().parallelStream()
				.flatMap(block -> block.getStateContainer().getValidStates().parallelStream())
				.forEach(state -> {
					if (whitelisted.contains(state))
						NoCubes.smoothableHandler.addSmoothable(state);
					else if (blacklisted.contains(state))
						NoCubes.smoothableHandler.removeSmoothable(state);
				});
		} else if (spec == Server.SPEC) {
			Server.bake();
			Set<BlockState> whitelisted = Server.INSTANCE.smoothableWhitelist.get().parallelStream()
				.map(BlockStateConverter::fromStringOrNull)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
			Set<BlockState> blacklisted = Server.INSTANCE.smoothableBlacklist.get().parallelStream()
				.map(BlockStateConverter::fromStringOrNull)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
			ForgeRegistries.BLOCKS.getValues().parallelStream()
				.flatMap(block -> block.getStateContainer().getValidStates().parallelStream())
				.forEach(state -> {
					if (whitelisted.contains(state))
						NoCubes.smoothableHandler.addSmoothable(state);
					else if (blacklisted.contains(state))
						NoCubes.smoothableHandler.removeSmoothable(state);
				});
		}
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

		static {
			final Pair<Impl, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Impl::new);
			SPEC = specPair.getRight();
			INSTANCE = specPair.getLeft();
		}

		public static void bake() {
			render = INSTANCE.render.get();
		}

		public static void updateSmoothablePreference(final boolean newValue, final BlockState... states) {
			System.out.println("Client.updateSmoothablePreference");
			final NoCubesConfig.Client.Impl cfg = NoCubesConfig.Client.INSTANCE;
			NoCubesConfig.updateSmoothable(newValue, states, (List) cfg.smoothableWhitelistPreference.get(), (List) cfg.smoothableBlacklistPreference.get());
			saveAndLoad(ModConfig.Type.CLIENT);
		}

		public static void updateRender(final boolean newValue) {
			System.out.println("Client.updateRender");
			Client.INSTANCE.render.set(newValue);
			saveAndLoad(ModConfig.Type.CLIENT);
		}

		static class Impl {

			final BooleanValue render;
			final ConfigValue<List<? extends String>> smoothableWhitelistPreference;
			final ConfigValue<List<? extends String>> smoothableBlacklistPreference;

			private Impl(final ForgeConfigSpec.Builder builder) {
				render = builder
					.translation(NoCubes.MOD_ID + ".config.render")
					.define("render", true);

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
