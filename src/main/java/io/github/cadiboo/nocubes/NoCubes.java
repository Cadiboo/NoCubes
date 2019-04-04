package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.ClientProxy;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.hooks.AddCollisionBoxToListHook;
import io.github.cadiboo.nocubes.hooks.GetCollisionBoundingBoxHook;
import io.github.cadiboo.nocubes.hooks.IsEntityInsideOpaqueBlockHook;
import io.github.cadiboo.nocubes.hooks.IsOpaqueCubeHook;
import io.github.cadiboo.nocubes.mesh.MeshDispatcher;
import io.github.cadiboo.nocubes.server.ServerProxy;
import io.github.cadiboo.nocubes.util.IProxy;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.init.Blocks;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.DistExecutor;
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
@Mod(MOD_ID)
public final class NoCubes {

	public static final Logger NO_CUBES_LOG = LogManager.getLogger(MOD_ID);

	private static final Logger LOGGER = LogManager.getLogger();

	public static final ArrayList<ModProfiler> PROFILERS = new ArrayList<>();

	public static boolean profilingEnabled = false;
	private static boolean pastPreInit = false;

	public static void enableProfiling() {
		profilingEnabled = true;
		for (final ModProfiler profiler : PROFILERS) {
			if (profiler == null) { //hmmm....
				LogManager.getLogger(MOD_NAME + " Profiling").warn("Tried to enable null profiler!");
				continue;
			}
			profiler.startProfiling(0);
		}
	}

	public static void disableProfiling() {
		profilingEnabled = false;
		for (final ModProfiler profiler : PROFILERS) {
			if (profiler == null) { //hmmm....
				LogManager.getLogger(MOD_NAME + " Profiling").warn("Tried to disable null profiler!");
				continue;
			}
			profiler.stopProfiling();
		}
	}

	private static final ThreadLocal<ModProfiler> PROFILER = ThreadLocal.withInitial(() -> {
		final ModProfiler profiler = new ModProfiler();
		PROFILERS.add(profiler);
		return profiler;
	});

	public static final MeshDispatcher MESH_DISPATCHER = new MeshDispatcher();

	public static final IProxy PROXY = DistExecutor.runForDist(()->()->new ClientProxy(), ()->()->new ServerProxy());

	public NoCubes() {
	}

	@SuppressWarnings("unused")
	public static boolean areHooksEnabled() {
		if (!pastPreInit) {
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

	public void onPreInit(final FMLPreInitializationEvent event) {
		pastPreInit = true;

		testHooks();

		ModUtil.launchUpdateDaemon(Loader.instance().activeModContainer());
	}

	public void onPostInit(final FMLPostInitializationEvent event) {
		PROXY.replaceFluidRendererCauseImBored();
	}

	public void testHooks() {
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
			try {
				IsEntityInsideOpaqueBlockHook.isEntityInsideOpaqueBlock(null);
			} catch (NullPointerException e) {
			}
		}
		{
			try {
				Blocks.DIRT.getDefaultState().isOpaqueCube(null, null);
			} catch (NullPointerException e) {
			}
			try {
				Blocks.DIRT.getDefaultState().getCollisionBoundingBox(null, null);
			} catch (NullPointerException e) {
			}
			try {
				Blocks.DIRT.getDefaultState().addCollisionBoxToList(null, null, null, null, null, false);
			} catch (NullPointerException e) {
			}
			try {
				new EntityRabbit(null).isEntityInsideOpaqueBlock();
			} catch (NullPointerException e) {
			}
		}
	}

}
