package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.ClientProxy;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.server.ServerProxy;
import io.github.cadiboo.nocubes.util.IProxy;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.renderchunkrebuildchunkhooks.RenderChunkRebuildChunkHooks;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;

@Mod(MOD_ID)
public final class NoCubes {

	public static final Logger NO_CUBES_LOG = LogManager.getLogger(MOD_ID);

	private static final Logger LOGGER = LogManager.getLogger();

	private static final ArrayList<ModProfiler> PROFILERS = new ArrayList<>();

	private static final ThreadLocal<ModProfiler> PROFILER = ThreadLocal.withInitial(() -> {
		final ModProfiler profiler = new ModProfiler();
		PROFILERS.add(profiler);
		return profiler;
	});

//	@Mod.Instance(MOD_ID)
//	public static NoCubes instance;

	public static IProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());

	public NoCubes() {

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		RenderChunkRebuildChunkHooks.HookConfig.enableRebuildChunkPreRenderEvent();
		RenderChunkRebuildChunkHooks.HookConfig.enableRebuildChunkBlockEvent();

	}

	public static boolean isEnabled() {
		return ModConfig.isEnabled;
	}

	public static ModProfiler getProfiler() {
		return PROFILER.get();
	}

	public void setup(final FMLCommonSetupEvent event) {
		//ModLoadingContext.get().getActiveContainer()
		ModUtil.launchUpdateDaemon(ModList.get().getModContainerById(MOD_ID).get());
	}

}
