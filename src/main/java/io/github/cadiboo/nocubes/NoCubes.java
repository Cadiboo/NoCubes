package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.ClientProxy;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHolder;
import io.github.cadiboo.nocubes.server.ServerProxy;
import io.github.cadiboo.nocubes.util.IProxy;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.BlockDirtSnowy;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
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

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
@Mod(MOD_ID)
public final class NoCubes {

	public static final String MOD_ID = "nocubes";
	//TODO: Do I still need this?
	public static final IProxy PROXY = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static boolean isEnabled = true;

	public NoCubes() {

		//TODO FIXME this will die on the dedicated server
		try {
			loadRenderChunk();
		} catch (ClassNotFoundException e) {
			LOGGER.fatal("Failed to load RenderChunk. This should not be possible!", e);
			final CrashReport crashReport = new CrashReport("Failed to load RenderChunk. This should not be possible!", e);
			crashReport.makeCategory("Loading RenderChunk");
			throw new ReportedException(crashReport);
		}

		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::onFMLCommonSetupEvent);
		modEventBus.addListener(this::onLoadComplete);
		modEventBus.addListener(this::onModConfigEvent);

		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigHolder.SERVER_SPEC);

	}

	public static boolean isEnabled() {
		return isEnabled;
	}

	public static void setEnabled(final boolean enabled) {
		isEnabled = enabled;
	}

	private void loadRenderChunk() throws ClassNotFoundException {
		LOGGER.info("Loading RenderChunk...");
		final long startTime = System.nanoTime();
		Class.forName("net.minecraft.client.renderer.chunk.RenderChunk", false, this.getClass().getClassLoader());
		LOGGER.info("Loaded RenderChunk in " + (System.nanoTime() - startTime) + " nano seconds");
		LOGGER.info("Initialising RenderChunk...");
		Class.forName("net.minecraft.client.renderer.chunk.RenderChunk", true, this.getClass().getClassLoader());
		LOGGER.info("Initialised RenderChunk");
	}

	@SubscribeEvent
	public void onFMLCommonSetupEvent(final FMLCommonSetupEvent event) {
		ModUtil.launchUpdateDaemon(ModList.get().getModContainerById(MOD_ID).get());
	}

	public void onModConfigEvent(ModConfig.ModConfigEvent event) {
		if (event.getConfig().getSpec() == ConfigHolder.CLIENT_SPEC) {
			Config.bakeClient(event.getConfig());
		} else if (event.getConfig().getSpec() == ConfigHolder.SERVER_SPEC) {
			Config.bakeServer(event.getConfig());
		}
	}

	@SubscribeEvent
	public void onLoadComplete(final FMLLoadCompleteEvent event) {
		{
			Blocks.GRASS_BLOCK.getDefaultState().nocubes_setTerrainSmoothable(true);
			Blocks.GRASS_BLOCK.getDefaultState().with(BlockDirtSnowy.SNOWY, true).nocubes_setTerrainSmoothable(true);
			Blocks.DIRT.getDefaultState().nocubes_setTerrainSmoothable(true);
			Blocks.SAND.getDefaultState().nocubes_setTerrainSmoothable(true);
			Blocks.GRAVEL.getDefaultState().nocubes_setTerrainSmoothable(true);
			Blocks.CLAY.getDefaultState().nocubes_setTerrainSmoothable(true);
			Blocks.STONE.getDefaultState().nocubes_setTerrainSmoothable(true);
			Blocks.DIORITE.getDefaultState().nocubes_setTerrainSmoothable(true);
			Blocks.GRANITE.getDefaultState().nocubes_setTerrainSmoothable(true);
			Blocks.ANDESITE.getDefaultState().nocubes_setTerrainSmoothable(true);
			Blocks.COAL_ORE.getDefaultState().nocubes_setTerrainSmoothable(true);
			Blocks.IRON_ORE.getDefaultState().nocubes_setTerrainSmoothable(true);
			Blocks.SNOW.getDefaultState().nocubes_setTerrainSmoothable(true);
		}
		PROXY.replaceFluidRendererCauseImBored();
	}

}
