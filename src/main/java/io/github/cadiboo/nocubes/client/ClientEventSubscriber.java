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
import net.minecraft.network.Connection;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class ClientEventSubscriber {

	private static long selfCheckInfoPrintedAt = Long.MIN_VALUE;

	@SubscribeEvent
	public static void printDebugInfo(TickEvent.ClientTickEvent event) {
		if (!NoCubesConfig.Client.debugEnabled || !Screen.hasAltDown())
			return;

		var world = Minecraft.getInstance().level;
		if (world == null)
			return;

		long time = world.getGameTime();
		if (time - 10 * 20 <= selfCheckInfoPrintedAt)
			return; // Only print once every 10 seconds, don't spam the log

		selfCheckInfoPrintedAt = time;
		LogManager.getLogger("NoCubes Hooks SelfCheck").debug(String.join("\n", SelfCheck.info()));
	}

	@SubscribeEvent
	public static void disableCollisionsIfServerDoesNotHaveNoCubes(ClientPlayerNetworkEvent.LoggedInEvent event) {
		if (NoCubesNetwork.currentServerHasNoCubes || !NoCubesConfig.Server.collisionsEnabled)
			return;
		NoCubesConfig.Server.collisionsEnabled = false;

		var msg_key = NoCubes.MOD_ID + ".notification.nocubesNotInstalledOnServerCollisionsUnavailable";
		var player = event.getPlayer();
		if (player != null)
			player.sendMessage(new TranslatableComponent(msg_key).withStyle(ChatFormatting.RED), Util.NIL_UUID);
		else
			LogManager.getLogger("NoCubes notification fallback").warn(I18n.get(msg_key));
	}

	/**
	 * This lets NoCubes load properly on modded servers that don't have it installed
	 */
	@SubscribeEvent
	public static void loadDefaultServerConfigIfWeAreOnAVanillaServer(ClientPlayerNetworkEvent.LoggedInEvent event) {
		if (NoCubesNetwork.currentServerHasNoCubes)
			return; // Forge has synced the server config to us, no need to load the default (see ConfigSync.syncConfigs)

		var connection = event.getConnection();
		if (connection != null && NetworkHooks.isVanillaConnection(connection))
			return; // Forge has already loaded the default server configs for us (see NetworkHooks#handleClientLoginSuccess(Connection))

		var logger = LogManager.getLogger("NoCubes Client-only features");
		if (connection == null)
			logger.warn("Connection was null, assuming we're connected to a modded server without NoCubes!");
		logger.info("Connected to a modded server that doesn't have NoCubes installed, loading default server config");
		NoCubesConfig.Hacks.loadDefaultServerConfig();
		logger.debug("Done loading default server config");
	}

}
