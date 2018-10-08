package cadiboo.nocubes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cadiboo.nocubes.util.ModReference;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

//@SideOnly(Side.CLIENT)
@Mod(modid = ModReference.MOD_ID, name = ModReference.MOD_NAME, version = "0.0", clientSideOnly = true)
public class NoCubes {

	public static KeyBinding	KEYBIND_SETTINGS;
	public static KeyBinding	KEYBIND_DEBUG;

	@Instance(ModReference.MOD_ID)
	public static NoCubes		INSTANCE;

	public static Logger		LOGGER	= LogManager.getLogger(ModReference.MOD_ID);


	@EventHandler
	public void postInit(final FMLPostInitializationEvent event) {
		try {

			LOGGER.info("Successfully added our hook into RebuildChunk");
		} catch (final Throwable throwable) {
			LOGGER.error("Failed to add our hook into RebuildChunk");
			// This should only happen rarely (never honestly) - so printing the Stack Trace shoudn't spam any logs
			throwable.printStackTrace();
			// TODO: throw the throwable? Maybe, keep it commented out for now
			// throw throwable;
		}
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
