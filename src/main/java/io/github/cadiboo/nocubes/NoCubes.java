package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.client.optifine.OptiFineLocator;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.future.ConfigTracker;
import io.github.cadiboo.nocubes.future.FileUtils;
import io.github.cadiboo.nocubes.future.ModConfig;
import io.github.cadiboo.nocubes.network.*;
import io.github.cadiboo.nocubes.tempcore.NoCubesLoadingPlugin;
import io.github.cadiboo.nocubes.future.DistExecutor;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.nio.file.Path;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static org.apache.logging.log4j.LogManager.getLogger;

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

	public NoCubes() {
		NoCubesConfig.register(ConfigTracker.INSTANCE.createModLoadingContext());
		NoCubesNetwork.register();
	}

	@Mod.EventHandler
	public void onPreInit(final FMLPreInitializationEvent event) {

		if (NoCubesLoadingPlugin.RCRCH_INSTALLED) {
			DistExecutor.runWhenOn(Side.CLIENT, () -> ClientUtil::crashIfRCRCHInstalled);
			DistExecutor.runWhenOn(Side.SERVER, () -> () -> {
				final String msg = "NoCubes Dependency Error! RenderChunk rebuildChunk Hooks CANNOT be installed! Remove RenderChunk rebuildChunk Hooks from the mods folder and then restart the game.";
				FMLCommonHandler.instance().raiseException(new IllegalStateException(msg), msg, true);
			});
		}
		DistExecutor.runWhenOn(Side.CLIENT, () -> () -> {
			if (OptiFineLocator.isOptiFineInstalled() && !OptiFineCompatibility.ENABLED)
				ClientUtil.crashIfIncompatibleOptiFine();
		});

		DistExecutor.runWhenOn(Side.CLIENT, () -> () -> ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, Loader.instance().getConfigDir().toPath()));
		ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.COMMON, Loader.instance().getConfigDir().toPath());

		getLogger().debug("Preloading patched classes...");
		ModUtil.preloadClass("net.minecraft.block.state.IBlockProperties", "IBlockProperties");
		ModUtil.preloadClass("net.minecraft.block.state.BlockStateContainer$StateImplementation", "StateImplementation");
		DistExecutor.runWhenOn(Side.CLIENT, () -> () -> {
			ModUtil.preloadClass("net.minecraft.client.renderer.chunk.RenderChunk", "RenderChunk");
			ModUtil.preloadClass("net.minecraft.client.renderer.BlockFluidRenderer", "BlockFluidRenderer");
		});
		getLogger().debug("Finished preloading patched classes");

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

}
