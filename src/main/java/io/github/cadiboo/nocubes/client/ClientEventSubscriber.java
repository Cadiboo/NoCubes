package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.hooks.SelfCheck;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class ClientEventSubscriber {

	private static final Logger LOG = LogManager.getLogger();

	private static long selfCheckInfoPrintedAt = Long.MIN_VALUE;

	@SubscribeEvent
	public static void printDebugInfo(TickEvent.ClientTickEvent event) {
		if (!NoCubesConfig.Common.debugEnabled || !Screen.hasAltDown())
			return;

		var world = Minecraft.getInstance().level;
		if (world == null)
			return;

		long time = world.getGameTime();
		if (time - 10 * 20 <= selfCheckInfoPrintedAt)
			return; // Only print once every 10 seconds, don't spam the log

		selfCheckInfoPrintedAt = time;
		LogManager.getLogger("NoCubes Hooks SelfCheck").info(String.join("\n", SelfCheck.info()));
	}

	@SubscribeEvent
	public static void onClientJoinServer(ClientPlayerNetworkEvent.LoggingIn event) {
		LOG.debug("Client joined server");
		loadDefaultServerConfigIfWeAreOnAModdedServerWithoutNoCubes(event);
		ClientUtil.sendPlayerInfoMessage();
		ClientUtil.warnPlayerIfVisualsDisabled();
		if (!NoCubesNetwork.currentServerHasNoCubes) {
			// This lets players not phase through the ground on servers that don't have NoCubes installed
			NoCubesConfig.Server.collisionsEnabled = false;
			ClientUtil.warnPlayer(NoCubes.MOD_ID + ".notification.notInstalledOnServer", KeyMappings.translate(KeyMappings.TOGGLE_SMOOTHABLE));
		}
	}

	/**
	 * This lets NoCubes load properly on modded servers that don't have it installed
	 */
	private static void loadDefaultServerConfigIfWeAreOnAModdedServerWithoutNoCubes(ClientPlayerNetworkEvent.LoggingIn event) {
		if (NoCubesNetwork.currentServerHasNoCubes) {
			// Forge has synced the server config to us, no need to load the default (see ConfigSync.syncConfigs)
			LOG.debug("Not loading default server config - current server has NoCubes installed");
			return;
		}

		var connection = event.getConnection();
		if (connection != null && NetworkHooks.isVanillaConnection(connection)) {
			// Forge has already loaded the default server configs for us (see NetworkHooks#handleClientLoginSuccess(Connection))
			LOG.debug("Not loading default server config - Forge has already loaded it for us");
			return;
		}

		if (connection == null)
			LOG.debug("Connection was null, assuming we're connected to a modded server without NoCubes!");
		LOG.debug("Connected to a modded server that doesn't have NoCubes installed, loading default server config");
		NoCubesConfig.Hacks.loadDefaultServerConfig();
	}

}
