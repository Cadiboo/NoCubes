package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.hooks.SelfCheck;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class ClientEventSubscriber {

	private static long selfCheckInfoPrintedAt = Long.MIN_VALUE;

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (NoCubesConfig.Client.debugEnabled && Screen.hasAltDown())
			printDebugInfo();

		if (NoCubesConfig.Server.collisionsEnabled && !NoCubesNetwork.currentServerHasNoCubes) {
			NoCubesConfig.Server.collisionsEnabled = false;
			String msg_key = NoCubes.MOD_ID + ".notification.nocubesNotInstalledOnServerCollisionsUnavailable";
			var player = Minecraft.getInstance().player;
			if (player == null)
				LogManager.getLogger("NoCubes notification fallback").warn(I18n.get(msg_key));
			else
				player.sendMessage(new TranslatableComponent(msg_key).withStyle(ChatFormatting.RED), Util.NIL_UUID);
		}
	}

	private static void printDebugInfo() {
		var world = Minecraft.getInstance().level;
		if (world == null)
			return;
		long time = world.getGameTime();
		// Only print once every 10 seconds, don't spam the log
		if (time - 10 * 20 > selfCheckInfoPrintedAt) {
			selfCheckInfoPrintedAt = time;
			LogManager.getLogger("NoCubes Hooks SelfCheck").debug(String.join("\n", SelfCheck.info()));
		}
	}

}
