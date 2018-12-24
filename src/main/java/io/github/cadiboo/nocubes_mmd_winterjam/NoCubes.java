package io.github.cadiboo.nocubes_mmd_winterjam;

import io.github.cadiboo.nocubes_mmd_winterjam.config.ModConfig;
import io.github.cadiboo.nocubes_mmd_winterjam.util.IProxy;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.github.cadiboo.nocubes_mmd_winterjam.util.ModReference.ACCEPTED_VERSIONS;
import static io.github.cadiboo.nocubes_mmd_winterjam.util.ModReference.CLIENT_PROXY_CLASS;
import static io.github.cadiboo.nocubes_mmd_winterjam.util.ModReference.DEPENDENCIES;
import static io.github.cadiboo.nocubes_mmd_winterjam.util.ModReference.MOD_ID;
import static io.github.cadiboo.nocubes_mmd_winterjam.util.ModReference.MOD_NAME;
import static io.github.cadiboo.nocubes_mmd_winterjam.util.ModReference.SERVER_PROXY_CLASS;
import static io.github.cadiboo.nocubes_mmd_winterjam.util.ModReference.VERSION;

/**
 * Our main mod class
 *
 * @author Cadiboo
 */
@Mod(
		modid = MOD_ID,
		name = MOD_NAME,
		version = VERSION,
		acceptedMinecraftVersions = ACCEPTED_VERSIONS,
		dependencies = DEPENDENCIES,
		clientSideOnly = true
)
public final class NoCubes {

	public static final Logger NO_CUBES_LOG = LogManager.getLogger(MOD_ID);
	private static final Logger LOGGER = LogManager.getLogger();

	@Instance(MOD_ID)
	public static NoCubes instance;

	@SidedProxy(serverSide = SERVER_PROXY_CLASS, clientSide = CLIENT_PROXY_CLASS)
	public static IProxy proxy;

	public static boolean isEnabled() {
		//		return isEnabled;
		return ModConfig.isEnabled;
	}

	// not implemented unfortunately, we use our config system instead
	@EventHandler
	public static void onDisableEvent(final FMLModDisabledEvent event) {
		LOGGER.fatal("DEBUG: " + MOD_NAME + " was disabled :o this is... impossible???");
		//			NoCubes.deactivate();
	}

	/**
	 * Run before anything else. <s>Read your config, create blocks, items, etc, and register them with the GameRegistry</s>
	 *
	 * @param event the event
	 * @see ForgeModContainer#preInit(FMLPreInitializationEvent)
	 */
	@EventHandler
	public void preInit(final FMLPreInitializationEvent event) {
		LOGGER.debug("preInit");
		proxy.logPhysicalSide(NO_CUBES_LOG);

	}

	//	private static boolean isEnabled;
	//
	//	public static void deactivate() {
	//		isEnabled = false;
	//	}
	//
	//	public static void reactivate() {
	//		isEnabled = true;
	//	}

	/**
	 * Do your mod setup. Build whatever data structures you care about. Register recipes, send FMLInterModComms messages to other mods.
	 *
	 * @param event the event
	 */
	@EventHandler
	public void init(final FMLInitializationEvent event) {
		LOGGER.debug("init");
	}

	/**
	 * Mod compatibility, or anything which depends on other mods’ init phases being finished.
	 *
	 * @param event the event
	 * @see ForgeModContainer#postInit(FMLPostInitializationEvent)
	 */
	@EventHandler
	public void postInit(final FMLPostInitializationEvent event) {
		LOGGER.debug("postInit");
//		SmoothLightingFluid.changeFluidRenderer();
	}

}
