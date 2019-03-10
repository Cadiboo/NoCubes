package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.hooks.AddCollisionBoxToListHook;
import io.github.cadiboo.nocubes.hooks.GetCollisionBoundingBoxHook;
import io.github.cadiboo.nocubes.hooks.IsOpaqueCubeHook;
import io.github.cadiboo.nocubes.mesh.MeshDispatcher;
import io.github.cadiboo.nocubes.util.IProxy;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Blocks;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
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

/**
 * @author Cadiboo
 */
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

	public static final ArrayList<ModProfiler> PROFILERS = new ArrayList<>();

	public static boolean profilingEnabled = false;
	private static boolean pastInit = false;

	public static void enableProfiling() {
		profilingEnabled = true;
		for (final ModProfiler profiler : PROFILERS) {
			if (profiler == null) { //hmmm....
				LogManager.getLogger(MOD_NAME + " Profiling").warn("Tried to enable null profiler!");
				continue;
			}
			profiler.profilingEnabled = true;
		}
	}

	public static void disableProfiling() {
		profilingEnabled = false;
		for (final ModProfiler profiler : PROFILERS) {
			if (profiler == null) { //hmmm....
				LogManager.getLogger(MOD_NAME + " Profiling").warn("Tried to disable null profiler!");
				continue;
			}
			profiler.profilingEnabled = false;
		}
	}

	private static final ThreadLocal<ModProfiler> PROFILER = ThreadLocal.withInitial(() -> {
		final ModProfiler profiler = new ModProfiler();
		PROFILERS.add(profiler);
		return profiler;
	});

	public static final MeshDispatcher MESH_DISPATCHER = new MeshDispatcher();

	@Mod.Instance(MOD_ID)
	public static NoCubes INSTANCE;

	@SidedProxy(serverSide = SERVER_PROXY_CLASS, clientSide = CLIENT_PROXY_CLASS)
	public static IProxy PROXY;

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

	@SuppressWarnings("unused")
	public static boolean areHooksEnabled() {
		if (!pastInit) {
			return false;
		}
		return isEnabled();
	}

	public static boolean isEnabled() {
		return ModConfig.isEnabled;
	}

	public static ModProfiler getProfiler() {
		return PROFILER.get();
	}

	@Mod.EventHandler
	public void onPreInit(final FMLPreInitializationEvent event) {
		pastInit = true;

		{
			try {
				IsOpaqueCubeHook.isOpaqueCube(null, null);
			} catch (NullPointerException e) {
			}
			try {
				GetCollisionBoundingBoxHook.getCollisionBoundingBox(null, null, null, null);
			} catch (NullPointerException e) {
			}
			try {
				AddCollisionBoxToListHook.addCollisionBoxToList(null, null, null, null, null, null, null, false);
			} catch (NullPointerException e) {
			}
		}
		{
			try {
				Blocks.BED.getDefaultState().isOpaqueCube();
			} catch (NullPointerException e) {
			}
			try {
				Blocks.BED.getDefaultState().getCollisionBoundingBox(null, null);
			} catch (NullPointerException e) {
			}
			try {
				Blocks.BED.getDefaultState().addCollisionBoxToList(null, null, null, null, null, false);
			} catch (NullPointerException e) {
			}
		}
		ModUtil.fixConfig(event.getSuggestedConfigurationFile());
		ModUtil.launchUpdateDaemon(Loader.instance().activeModContainer());
	}

	@Mod.EventHandler
	public void onPostInit(final FMLPostInitializationEvent event) {
		PROXY.replaceFluidRendererCauseImBored();
	}

}
