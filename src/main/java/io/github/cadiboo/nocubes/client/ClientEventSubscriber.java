package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.hooks.SelfCheck;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
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
	public static void onTick(TickEvent.ClientTickEvent event) {
		if (!NoCubesConfig.Client.debugEnabled)
			return;

		Minecraft minecraft = Minecraft.getInstance();
		Level world = minecraft.level;
		if (world == null)
			return;

		if (Screen.hasAltDown()) {
			long time = world.getGameTime();
			// Only print once every 10 seconds, don't spam the log
			if (time - 10 * 20 > selfCheckInfoPrintedAt) {
				selfCheckInfoPrintedAt = time;
				LogManager.getLogger("NoCubes Hooks SelfCheck").debug(String.join("\n", SelfCheck.info()));
			}
		}

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;

		if (NoCubesConfig.Server.collisionsEnabled && !NoCubesNetwork.currentServerHasNoCubes) {
			player.sendMessage(new TranslatableComponent(NoCubes.MOD_ID + ".notification.nocubesNotInstalledOnServerCollisionsUnavailable").withStyle(ChatFormatting.RED), Util.NIL_UUID);
			NoCubesConfig.Server.collisionsEnabled = false;
		}
	}

}
