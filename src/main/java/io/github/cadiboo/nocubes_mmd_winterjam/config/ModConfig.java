package io.github.cadiboo.nocubes_mmd_winterjam.config;

import io.github.cadiboo.nocubes_mmd_winterjam.util.ModReference;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Our Mod's configuration
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
@Config(modid = ModReference.MOD_ID)
@LangKey(ModReference.MOD_ID + ".config.title")
public final class ModConfig {

	@LangKey(ModReference.MOD_ID + ".config.isEnabled")
	public static boolean isEnabled = true;

	@Mod.EventBusSubscriber(modid = ModReference.MOD_ID)
	private static class EventSubscriber {

		/**
		 * Inject the new values and save to the config file when the config has been changed from the GUI.
		 *
		 * @param event The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {

			if (event.getModID().equals(ModReference.MOD_ID)) {
				ConfigManager.sync(ModReference.MOD_ID, Config.Type.INSTANCE);

				if (Minecraft.getMinecraft().renderGlobal != null) {
					Minecraft.getMinecraft().renderGlobal.loadRenderers();
				}

			}

		}

	}

}
