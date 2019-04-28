package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.ClientProxy;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.server.ServerProxy;
import io.github.cadiboo.nocubes.util.IProxy;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.init.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
@Mod(MOD_ID)
public final class NoCubes {

	public static final String MOD_ID = "nocubes";
	//TODO: Do I still need this?
	public static final IProxy PROXY = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());
	private static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static boolean isEnabled = true;

	public NoCubes() {
		LOGGER.info("Loading RenderChunk...");
		final long startTime = System.nanoTime();
		RenderChunk.class.getName();
		LOGGER.info("Loaded RenderChunk in " + (System.nanoTime() - startTime) + " nano seconds");
		LOGGER.info("Initialising RenderChunk...");
		int ignored = RenderChunk.renderChunksUpdated;
		LOGGER.info("Initialised RenderChunk");

		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::setup);
		modEventBus.addListener(this::onLoadComplete);

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverConfig);

		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.ConfigHolder.CLIENT_SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.ConfigHolder.SERVER_SPEC);

	}

	public static boolean isEnabled() {
		return isEnabled;
	}

	public static void setEnabled(final boolean enabled) {
		isEnabled = enabled;
	}

	@SubscribeEvent
	public void setup(final FMLCommonSetupEvent event) {
		ModUtil.launchUpdateDaemon(ModList.get().getModContainerById(MOD_ID).get());
	}

	public void serverConfig(ModConfig.ModConfigEvent event) {
		if (event.getConfig().getSpec() == Config.ConfigHolder.CLIENT_SPEC) {
			Config.bakeClient();
		} else if (event.getConfig().getSpec() == Config.ConfigHolder.SERVER_SPEC) {
			Config.bakeServer();
		}
	}

	@SubscribeEvent
	public void onLoadComplete(final FMLLoadCompleteEvent event) {
		Blocks.GRASS_BLOCK.getDefaultState().nocubes_setTerrainSmoothable(true);
		Blocks.DIRT.getDefaultState().nocubes_setTerrainSmoothable(true);
		PROXY.replaceFluidRendererCauseImBored();

		{
			final GameSettings gameSettings = Minecraft.getInstance().gameSettings;
			boolean needsResave = false;
			if (gameSettings.ambientOcclusion < 1) {
				LOGGER.info("Smooth lighting was off. EW! Just set it to MINIMAL");
				gameSettings.ambientOcclusion = 1;
				needsResave = true;
			}
			if (!gameSettings.fancyGraphics) {
				LOGGER.info("Fancy graphics were off. Ew, who plays with black leaves??? Just turned it on");
				gameSettings.fancyGraphics = true;
				needsResave = true;
			}
			if (needsResave) {
				gameSettings.saveOptions();
			}
		}
	}

}
