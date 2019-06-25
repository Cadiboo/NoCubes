package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.config.ConfigHolder;
import io.github.cadiboo.nocubes.config.ConfigTracker;
import io.github.cadiboo.nocubes.config.ForgeConfigSpec;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.tempcore.NoCubesLoadingPlugin;
import io.github.cadiboo.nocubes.tempnetwork.ModNetworkManager;
import io.github.cadiboo.nocubes.util.DistExecutor;
import io.github.cadiboo.nocubes.util.FileUtils;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Proxy;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Optional;
import java.util.function.Consumer;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
@Mod(
		modid = MOD_ID,
		guiFactory = "io.github.cadiboo.nocubes.client.ConfigGuiFactory",
		updateJSON = "https://Cadiboo.github.io/projects/nocubes/update.json",
		dependencies = "required-after:forge@[14.23.5.2779,);" // ObfuscationReflectionHelper reimplemented
)
public final class NoCubes {

	public static final String MOD_ID = "nocubes";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final ModNetworkManager NETWORK_MANAGER = new ModNetworkManager();
	@SidedProxy(serverSide = "io.github.cadiboo.nocubes.server.ServerProxy", clientSide = "io.github.cadiboo.nocubes.client.ClientProxy")
	public static Proxy PROXY = null;
	@Instance
	public static NoCubes INSTANCE = null;
	public final EnumMap<ModConfig.Type, ModConfig> configs = new EnumMap<>(ModConfig.Type.class);
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	protected Optional<Consumer<ModConfig.ModConfigEvent>> configHandler = Optional.empty();

	public NoCubes() {

//		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
//		modEventBus.addListener((ModConfig.ModConfigEvent event) -> {
//			final ModConfig config = event.getConfig();
//			if (config.getSpec() == ConfigHolder.CLIENT_SPEC) {
//				ConfigHelper.bakeClient(config);
//			} else if (config.getSpec() == ConfigHolder.SERVER_SPEC) {
//				ConfigHelper.bakeServer(config);
//			}
//		});
//
//		final ModLoadingContext modLoadingContext = ModLoadingContext.get();
//		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);
//		modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigHolder.SERVER_SPEC);

		this.configHandler = Optional.of((ModConfig.ModConfigEvent event) -> {
			final ModConfig config = event.getConfig();
			if (config.getSpec() == ConfigHolder.CLIENT_SPEC) {
				ConfigHelper.bakeClient(config);
			} else if (config.getSpec() == ConfigHolder.SERVER_SPEC) {
				ConfigHelper.bakeServer(config);
			}
		});

	}

	@Mod.EventHandler
	public void onPreInit(final FMLPreInitializationEvent event) {

		if (NoCubesLoadingPlugin.RCRCH_INSTALLED) {
			PROXY.crashIfRCRCHInstalled();
		}

		this.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);
		this.registerConfig(ModConfig.Type.SERVER, ConfigHolder.SERVER_SPEC);

		DistExecutor.runWhenOn(Side.CLIENT, () -> () -> ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, Loader.instance().getConfigDir().toPath()));
		ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.COMMON, Loader.instance().getConfigDir().toPath());

		PROXY.preloadClasses();
		ModUtil.launchUpdateDaemon(Loader.instance().getIndexedModList().get(MOD_ID));
	}

	@Mod.EventHandler
	public void onPostInit(final FMLPostInitializationEvent event) {
		PROXY.replaceFluidRendererCauseImBored();
	}

	@Mod.EventHandler
	public void onServerAboutToStart(final FMLServerAboutToStartEvent event) {
		final MinecraftServer server = event.getServer();
		final Path serverConfig = server.getActiveAnvilConverter().getFile(server.getFolderName(), "serverconfig").toPath();
		FileUtils.getOrCreateDirectory(serverConfig, "serverconfig");
		ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.SERVER, serverConfig);
	}

	public void registerConfig(ModConfig.Type type, ForgeConfigSpec spec) {
		INSTANCE.addConfig(new ModConfig(type, spec, Loader.instance().activeModContainer()));
	}

	public void registerConfig(ModConfig.Type type, ForgeConfigSpec spec, String fileName) {
		INSTANCE.addConfig(new ModConfig(type, spec, Loader.instance().activeModContainer(), fileName));
	}

	public void addConfig(final ModConfig modConfig) {
		configs.put(modConfig.getType(), modConfig);
	}

	public void dispatchConfigEvent(final ModConfig.ModConfigEvent event) {
		configHandler.ifPresent(configHandler -> configHandler.accept(event));
	}

}
