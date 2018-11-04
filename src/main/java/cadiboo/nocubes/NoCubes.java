package cadiboo.nocubes;

import cadiboo.nocubes.config.ModConfig;
import cadiboo.nocubes.util.ModReference;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;

/**
 * NoCubes<br>
 * Original by Click_Me<br>
 * Reverse-engineered and re-implemented (with permission) by CosmicDan<br>
 * Refactored and updated to 1.12.x by Cadiboo<br>
 *
 * @author Click_Me (2016)
 * @author CosmicDan (2016)
 * @author Cadiboo (2018)
 */
/*@formatter:off*/
@Mod(modid = ModReference.MOD_ID,
	name = ModReference.MOD_NAME,
	version = ModReference.Version.VERSION,
	acceptedMinecraftVersions = ModReference.ACCEPTED_VERSIONS,
	dependencies = ModReference.DEPENDENCIES,
	canBeDeactivated = ModReference.CAN_BE_DEACTIVATED,
	clientSideOnly = true,
	serverSideOnly = false,
	modLanguage = "java")
/*@formatter:on*/
public class NoCubes {

	// TODO list:
	// "I found a hole in a mountain..."
	// cull facing - @Eddie "More bugs"
	// liquid smoothing OFF FAST FANCY
	// "I noticed that snow layers is rendered as a full block..." "I need to do some stuff with bounding boxes"
	// "btw the grass has some lightning issues" "Ill add an option to turn off aproximate lighting" "I'll do smooth(er) lighting soon" "Vanilla oil?"
	// "I'm standing in midair..." "Just stack like 10 blocks and you have an invisible border" "if (density < 10) or something"
	// "Another thing: add smooth lava in the config file"
	// "Sandstone villages are looking intresting..."
	// user changeable smoothable blocks
	// user changeable density function!
	// Marching Cubes
	// Marching Tetreheda (Might be too slow/computationally expensive of an algorithm)
	// Better algorithms
	// Use BakedQuads instead of BufferBuilder directly so that we get smooth lighting
	// Change config documentation to I18n (lang files)

	@Instance(ModReference.MOD_ID)
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
//			NoCubes.deactivate();
	}

}
