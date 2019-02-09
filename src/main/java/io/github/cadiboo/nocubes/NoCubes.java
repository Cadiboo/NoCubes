package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.IProxy;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;

import static io.github.cadiboo.nocubes.util.ModReference.ACCEPTED_MINECRAFT_VERSIONS;
import static io.github.cadiboo.nocubes.util.ModReference.CERTIFICATE_FINGERPRINT;
import static io.github.cadiboo.nocubes.util.ModReference.CLIENT_PROXY_CLASS;
import static io.github.cadiboo.nocubes.util.ModReference.DEPENDENCIES;
import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModReference.MOD_NAME;
import static io.github.cadiboo.nocubes.util.ModReference.SERVER_PROXY_CLASS;
import static io.github.cadiboo.nocubes.util.ModReference.UPDATE_JSON;
import static io.github.cadiboo.nocubes.util.ModReference.VERSION;

@Mod(
		modid = MOD_ID,
		name = MOD_NAME,
		version = VERSION,
		acceptedMinecraftVersions = ACCEPTED_MINECRAFT_VERSIONS,
		updateJSON = UPDATE_JSON,
		dependencies = DEPENDENCIES,
		clientSideOnly = true,
		acceptableRemoteVersions = VERSION,
		certificateFingerprint = CERTIFICATE_FINGERPRINT
)
public final class NoCubes {

	public static final Logger NO_CUBES_LOG = LogManager.getLogger(MOD_ID);

	private static final Logger LOGGER = LogManager.getLogger();

	private static final ArrayList<ModProfiler> PROFILERS = new ArrayList<>();

	private static final ThreadLocal<ModProfiler> PROFILER = ThreadLocal.withInitial(() -> {
		final ModProfiler profiler = new ModProfiler();
		PROFILERS.add(profiler);
		return profiler;
	});

	@Mod.Instance(MOD_ID)
	public static NoCubes instance;

	@SidedProxy(serverSide = SERVER_PROXY_CLASS, clientSide = CLIENT_PROXY_CLASS)
	public static IProxy proxy;

	public NoCubes() {
		try {
			//ew hacks
			ModUtil.fixConfig(new File(Loader.instance().getConfigDir(), MOD_ID + ".cfg"));
		} catch (Exception e) {
			final CrashReport crashReport = new CrashReport("Something went terribly wrong trying to hack our config", e);
			crashReport.makeCategory("Initialising NoCubes");
			throw new ReportedException(crashReport);
		}
	}

	public static boolean isEnabled() {
		return ModConfig.isEnabled;
	}

	public static ModProfiler getProfiler() {
		return PROFILER.get();
	}

	@Mod.EventHandler
	public void onPreInit(final FMLPreInitializationEvent event) {
		ModUtil.fixConfig(event.getSuggestedConfigurationFile());
		ModUtil.launchUpdateDaemon(Loader.instance().activeModContainer());
	}

}
