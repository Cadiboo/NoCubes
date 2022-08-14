package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.client.optifine.OptiFineLocator;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.config.ConfigHolder;
import io.github.cadiboo.nocubes.network.*;
import io.github.cadiboo.nocubes.repackage.net.minecraftforge.common.ForgeConfigSpec;
import io.github.cadiboo.nocubes.repackage.net.minecraftforge.fml.config.ConfigTracker;
import io.github.cadiboo.nocubes.repackage.net.minecraftforge.fml.config.ModConfig;
import io.github.cadiboo.nocubes.tempcore.NoCubesLoadingPlugin;
import io.github.cadiboo.nocubes.util.DistExecutor;
import io.github.cadiboo.nocubes.util.FileUtils;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
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
	public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(NoCubes.MOD_ID);
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

		int networkId = 0;
		CHANNEL.registerMessage(
				C2SRequestAddTerrainSmoothable.class,
				C2SRequestAddTerrainSmoothable.class,
				networkId++,
				Side.SERVER
		);
		CHANNEL.registerMessage(
				C2SRequestDisableTerrainCollisions.class,
				C2SRequestDisableTerrainCollisions.class,
				networkId++,
				Side.SERVER
		);
		CHANNEL.registerMessage(
				C2SRequestEnableTerrainCollisions.class,
				C2SRequestEnableTerrainCollisions.class,
				networkId++,
				Side.SERVER
		);
		CHANNEL.registerMessage(
				C2SRequestRemoveTerrainSmoothable.class,
				C2SRequestRemoveTerrainSmoothable.class,
				networkId++,
				Side.SERVER
		);
		CHANNEL.registerMessage(
				C2SRequestSetExtendFluidsRange.class,
				C2SRequestSetExtendFluidsRange.class,
				networkId++,
				Side.SERVER
		);
		CHANNEL.registerMessage(
				C2SRequestSetTerrainMeshGenerator.class,
				C2SRequestSetTerrainMeshGenerator.class,
				networkId++,
				Side.SERVER
		);

		/* Server -> Client */
		CHANNEL.registerMessage(
				S2CSyncConfig.class,
				S2CSyncConfig.class,
				networkId++,
				Side.CLIENT
		);
		CHANNEL.registerMessage(
				S2CAddTerrainSmoothable.class,
				S2CAddTerrainSmoothable.class,
				networkId++,
				Side.CLIENT
		);
		CHANNEL.registerMessage(
				S2CDisableTerrainCollisions.class,
				S2CDisableTerrainCollisions.class,
				networkId++,
				Side.CLIENT
		);
		CHANNEL.registerMessage(
				S2CEnableTerrainCollisions.class,
				S2CEnableTerrainCollisions.class,
				networkId++,
				Side.CLIENT
		);
		CHANNEL.registerMessage(
				S2CRemoveTerrainSmoothable.class,
				S2CRemoveTerrainSmoothable.class,
				networkId++,
				Side.CLIENT
		);
		CHANNEL.registerMessage(
				S2CSetExtendFluidsRange.class,
				S2CSetExtendFluidsRange.class,
				networkId++,
				Side.CLIENT
		);
		CHANNEL.registerMessage(
				S2CSetTerrainMeshGenerator.class,
				S2CSetTerrainMeshGenerator.class,
				networkId++,
				Side.CLIENT
		);

	}

	@Mod.EventHandler
	public void onPreInit(final FMLPreInitializationEvent event) {

		if (NoCubesLoadingPlugin.RCRCH_INSTALLED) {
			DistExecutor.runWhenOn(Side.CLIENT, () -> ClientUtil::crashIfRCRCHInstalled);
			DistExecutor.runWhenOn(Side.SERVER, () -> () -> FMLCommonHandler.instance().raiseException(new IllegalStateException("NoCubes Dependency Error! RenderChunk rebuildChunk Hooks CANNOT be installed! Remove RenderChunk rebuildChunk Hooks from the mods folder and then restart the game."), "NoCubes Dependency Error! RenderChunk rebuildChunk Hooks CANNOT be installed! Remove RenderChunk rebuildChunk Hooks from the mods folder and then restart the game.", true));
		}
		DistExecutor.runWhenOn(Side.CLIENT, () -> () -> {
			if (OptiFineLocator.isOptiFineInstalled() && !OptiFineCompatibility.ENABLED)
				ClientUtil.crashIfIncompatibleOptiFine();
		});

		this.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);
		this.registerConfig(ModConfig.Type.SERVER, ConfigHolder.SERVER_SPEC);

		DistExecutor.runWhenOn(Side.CLIENT, () -> () -> ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, Loader.instance().getConfigDir().toPath()));
		ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.COMMON, Loader.instance().getConfigDir().toPath());

		LOGGER.debug("Preloading patched classes...");
		ModUtil.preloadClass("net.minecraft.block.state.IBlockProperties", "IBlockProperties");
		ModUtil.preloadClass("net.minecraft.block.state.BlockStateContainer$StateImplementation", "StateImplementation");
		DistExecutor.runWhenOn(Side.CLIENT, () -> () -> {
			ModUtil.preloadClass("net.minecraft.client.renderer.chunk.RenderChunk", "RenderChunk");
			ModUtil.preloadClass("net.minecraft.client.renderer.BlockFluidRenderer", "BlockFluidRenderer");
		});
		LOGGER.debug("Finished preloading patched classes");

		ModUtil.launchUpdateDaemon(Loader.instance().getIndexedModList().get(MOD_ID));
	}

	@Mod.EventHandler
	public void onPostInit(final FMLPostInitializationEvent event) {
		DistExecutor.runWhenOn(Side.CLIENT, () -> ClientUtil::replaceFluidRenderer);
	}

	@Mod.EventHandler
	public void onServerAboutToStart(final FMLServerAboutToStartEvent event) {
		final MinecraftServer server = event.getServer();
		final Path serverConfig = server.getActiveAnvilConverter().getFile(server.getFolderName(), "serverconfig").toPath();
		FileUtils.getOrCreateDirectory(serverConfig, "serverconfig");
		ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.SERVER, serverConfig);
	}

	public void registerConfig(ModConfig.Type type, ForgeConfigSpec spec) {
		this.addConfig(new ModConfig(type, spec, Loader.instance().activeModContainer()));
	}

	public void registerConfig(ModConfig.Type type, ForgeConfigSpec spec, String fileName) {
		this.addConfig(new ModConfig(type, spec, Loader.instance().activeModContainer(), fileName));
	}

	public void addConfig(final ModConfig modConfig) {
		configs.put(modConfig.getType(), modConfig);
	}

	public void dispatchConfigEvent(final ModConfig.ModConfigEvent event) {
		configHandler.ifPresent(configHandler -> configHandler.accept(event));
	}

}
