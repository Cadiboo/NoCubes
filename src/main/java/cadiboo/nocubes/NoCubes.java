package cadiboo.nocubes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod(modid = NoCubes.MOD_ID, name = NoCubes.MOD_NAME, version = "${version}", clientSideOnly = true)
public class NoCubes {

	public static final String	MOD_ID		= "nocubes";
	public static final String	MOD_NAME	= "NoCubes";

	public static KeyBinding	KEYBIND_SETTINGS;
	public static KeyBinding	KEYBIND_DEBUG;

	@Instance(MOD_ID)
	public static NoCubes		INSTANCE;

	public static Logger		LOGGER		= LogManager.getLogger(MOD_ID);

	@EventHandler
	public void preInit(final FMLPreInitializationEvent event) {
		// TODO: remove this when I'm done - its only needed cause the old no-cubes also has an event subscriber & forge can't determine the owning mod for my event subscriber
		MinecraftForge.EVENT_BUS.register(new EventSubscriber());
	}

//	protected static void openSettingsGui() {
//		Minecraft.getMinecraft().displayGuiScreen(new GuiNoCubes());
//	}
//
//	public static boolean shouldSmooth(final Block block) {
//		if (!ModConfig.MOD_ENABLED) {
//			return false;
//		}
//		if (ModConfig.SMOOTHBLOCKS_IDS.contains(Block.getIdFromBlock(block))) {
//			return true;
//		}
//		return false;
//	}

}
