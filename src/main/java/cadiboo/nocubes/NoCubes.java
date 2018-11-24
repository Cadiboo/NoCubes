package cadiboo.nocubes;

import cadiboo.nocubes.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cadiboo.nocubes.util.ModReference.*;
import static cadiboo.nocubes.util.ModReference.Version.VERSION;

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

	serverSideOnly = false,

	modLanguage = "java"

)
public class NoCubes {

	// TODO list:
	// Marching Cubes BLOCK method
	// Surface Nets BLOCK method
	// add an option to force reload chunks on algorithm change
	// a shortcut key to toggle the mod on and off for building
	// "I found a hole in a mountain..."
	// cull facing - @Eddie "More bugs" "sometimes the bottom-most log on trees doesn't appear"
	// liquid smoothing OFF FAST FANCY
	// "I noticed that snow layers is rendered as a full block..." "I need to do some stuff with bounding boxes"
	// "btw the grass has some lightning issues" "Ill add an option to turn off aproximate lighting" "I'll do smooth(er) lighting soon" "Vanilla oil?"
	// "I'm standing in midair..." "Just stack like 10 blocks and you have an invisible border" "if (density < 10) or something"
	// "Sandstone villages are looking intresting..."
	// user changeable smoothable blocks
	// user changeable density function!?
	// Marching Tetreheda (Might be too slow/computationally expensive of an algorithm)
	// Better algorithms
	// Use BakedQuads instead of BufferBuilder directly so that we get smooth lighting OR implement smooth lighting
	// Change config documentation to I18n (lang files)

	// vrackfall, why did you disapear? you got SO FAR and all your work is just down the drain

	// https://www.reddit.com/r/VoxelGameDev/comments/5amgtz/is_marching_surface_nets_possible/
	// https://wordsandbuttons.online/interactive_explanation_of_marching_cubes_and_dual_contouring.html

	public static final Logger LOGGER = LogManager.getLogger();

	@Instance(MOD_ID)
	public static NoCubes instance;

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
