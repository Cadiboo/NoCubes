package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.optifine.Dummy;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.client.optifine.OptiFineLocator;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.repackage.net.minecraftforge.common.ForgeConfigSpec;
import io.github.cadiboo.nocubes.repackage.net.minecraftforge.fml.config.ConfigTracker;
import io.github.cadiboo.nocubes.repackage.net.minecraftforge.fml.config.ModConfig;
import io.github.cadiboo.nocubes.smoothable.SmoothableHandler;
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
	public static final SmoothableHandler smoothableHandler = SmoothableHandler.create();
	@Instance
	public static NoCubes INSTANCE = null;
	public final EnumMap<ModConfig.Type, ModConfig> configs = new EnumMap<>(ModConfig.Type.class);
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public Optional<Consumer<ModConfig.ModConfigEvent>> configHandler = Optional.empty();

	public NoCubes() {
		NoCubesNetwork.register();
	}

	@Mod.EventHandler
	public void onPreInit(final FMLPreInitializationEvent event) {
		if (NoCubesLoadingPlugin.RCRCH_INSTALLED) {
			DistExecutor.runWhenOn(Side.CLIENT, () -> ClientUtil::crashIfRCRCHInstalled);
			DistExecutor.runWhenOn(Side.SERVER, () -> () -> FMLCommonHandler.instance().raiseException(new IllegalStateException("NoCubes Dependency Error! RenderChunk rebuildChunk Hooks CANNOT be installed! Remove RenderChunk rebuildChunk Hooks from the mods folder and then restart the game."), "NoCubes Dependency Error! RenderChunk rebuildChunk Hooks CANNOT be installed! Remove RenderChunk rebuildChunk Hooks from the mods folder and then restart the game.", true));
		}
		DistExecutor.runWhenOn(Side.CLIENT, () -> () -> {
			if (OptiFineLocator.isOptiFineInstalled() && OptiFineCompatibility.proxy() instanceof Dummy)
				ClientUtil.crashIfIncompatibleOptiFine();
		});

		NoCubesConfig.register(this);

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
