package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.ClientProxy;
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
import net.minecraftforge.fml.common.Mod;
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
	public static final ArrayList<ModProfiler> PROFILERS = new ArrayList<>();
	private static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static final ThreadLocal<ModProfiler> PROFILER = ThreadLocal.withInitial(() -> {
		final ModProfiler profiler = new ModProfiler();
		PROFILERS.add(profiler);
		return profiler;
	});

	public static boolean profilingEnabled = false;
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

	}

	public static void enableProfiling() {
		profilingEnabled = true;
		for (final ModProfiler profiler : PROFILERS) {
			if (profiler == null) { //hmmm....
				LogManager.getLogger("NoCubes Profiling").warn("Tried to enable null profiler!");
				continue;
			}
			profiler.startProfiling(0);
		}
	}

	public static void disableProfiling() {
		profilingEnabled = false;
		for (final ModProfiler profiler : PROFILERS) {
			if (profiler == null) { //hmmm....
				LogManager.getLogger("NoCubes Profiling").warn("Tried to disable null profiler!");
				continue;
			}
			profiler.stopProfiling();
		}
	}

	public static ModProfiler getProfiler() {
		return PROFILER.get();
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

	@SubscribeEvent
	public void onLoadComplete(final FMLLoadCompleteEvent event) {
		Blocks.GRASS_BLOCK.getDefaultState().nocubes_setTerrainSmoothable(true);
		Blocks.DIRT.getDefaultState().nocubes_setTerrainSmoothable(true);
		PROXY.replaceFluidRendererCauseImBored();

		{
			final GameSettings gameSettings = Minecraft.getInstance().gameSettings;
			boolean needsResave = false;
			if (gameSettings.ambientOcclusion < 1) {
//			LOGGER.info("Smooth lighting was off. EW! Just set it to MINIMAL");
				gameSettings.ambientOcclusion = 1;
				needsResave = true;
			}
			if (!gameSettings.fancyGraphics) {
//			LOGGER.info("Fancy graphics were off. Ew, who plays with black leaves??? Just turned it on");
				gameSettings.fancyGraphics = true;
				needsResave = true;
			}
			if (needsResave) {
				gameSettings.saveOptions();
			}
		}
	}

}
