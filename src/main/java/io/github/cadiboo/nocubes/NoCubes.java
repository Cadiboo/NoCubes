package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.github.cadiboo.nocubes.util.ModReference.ACCEPTED_VERSIONS;
import static io.github.cadiboo.nocubes.util.ModReference.CAN_BE_DEACTIVATED;
import static io.github.cadiboo.nocubes.util.ModReference.DEPENDENCIES;
import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModReference.MOD_NAME;
import static io.github.cadiboo.nocubes.util.ModReference.UPDATE_JSON;
import static io.github.cadiboo.nocubes.util.ModReference.Version.VERSION;

/**
 * NoCubes<br>
 * Original by Click_Me<br>
 * Reverse-engineered and re-implemented (with permission) by CosmicDan<br>
 * Refactored and updated to 1.12.2 by Cadiboo<br>
 *
 * @author Click_Me (2016)
 * @author CosmicDan (2016)
 * @author Cadiboo (2018)
 */
@Mod(

		modid = MOD_ID,

		name = MOD_NAME,

		version = VERSION,

		acceptedMinecraftVersions = ACCEPTED_VERSIONS,

		dependencies = DEPENDENCIES,

		canBeDeactivated = CAN_BE_DEACTIVATED,

		clientSideOnly = true,

		updateJSON = UPDATE_JSON,

		serverSideOnly = false,

		modLanguage = "java"

)
public class NoCubes {

	// vrackfall, why did you disapear? you got SO FAR and all your work is just down the drain

	// https://www.reddit.com/r/VoxelGameDev/comments/5amgtz/is_marching_surface_nets_possible/
	// https://wordsandbuttons.online/interactive_explanation_of_marching_cubes_and_dual_contouring.html

	public static final Logger LOGGER = LogManager.getLogger();

	@Instance(MOD_ID)
	public static NoCubes instance;

	@EventHandler
	public static void onPreInit(final FMLPreInitializationEvent event) {
		event.getModMetadata().url = "https://cadiboo.github.io/projects/" + MOD_ID;
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

	public static boolean isEnabled() {
		//		return isEnabled;
		return ModConfig.isEnabled;
	}

	// not implemented, we use our config system instead
	@EventHandler
	public static void onDisableEvent(final FMLModDisabledEvent event) {
		LOGGER.fatal("DEBUG: " + MOD_NAME + " was disabled");
		//			NoCubes.deactivate();
	}

}
