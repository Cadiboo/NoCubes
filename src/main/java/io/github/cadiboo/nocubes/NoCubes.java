package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModProfiler;
import net.minecraftforge.fml.common.Mod;
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
//
//	@SidedProxy(serverSide = SERVER_PROXY_CLASS, clientSide = CLIENT_PROXY_CLASS)
//	public static IProxy proxy;

	public NoCubes() {
//		try {
//			//ew hacks
//			ModUtil.fixConfig(new File(Loader.instance().getConfigDir(), MOD_ID + ".cfg"));
//		} catch (Exception e) {
//			final CrashReport crashReport = new CrashReport("Something went terribly wrong trying to hack our config", e);
//			crashReport.makeCategory("Initialising NoCubes");
//			throw new ReportedException(crashReport);
//		}
	}

	public static boolean isEnabled() {
		return ModConfig.isEnabled;
	}

	public static ModProfiler getProfiler() {
		return PROFILER.get();
	}

//	@Mod.EventHandler
//	public void onPreInit(final FMLPreInitializationEvent event) {
//		ModUtil.fixConfig(event.getSuggestedConfigurationFile());
//		ModUtil.launchUpdateDaemon(Loader.instance().activeModContainer());
//	}

}
