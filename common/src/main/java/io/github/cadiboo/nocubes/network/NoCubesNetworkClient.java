package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.KeyMappings;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

import static io.github.cadiboo.nocubes.client.RenderHelper.reloadAllChunks;

public class NoCubesNetworkClient {

	private static final Logger LOG = LogManager.getLogger();
	/**
	 * Only valid when connected to a server on the client.
	 * Contains random values from the most recently pinged server otherwise.
	 * Also valid for singleplayer integrated servers (always true).
	 */
	public static boolean currentServerHasNoCubes = false;

	public static void handleS2CUpdateServerConfig(Consumer<Runnable> enqueueWork, byte[] configData) {
		ClientUtil.platform.receiveSyncedServerConfig(configData);
	}

	public static void handleS2CUpdateSmoothable(Consumer<Runnable> enqueueWork, boolean newValue, BlockState[] states) {
		enqueueWork.accept(() -> {
			NoCubes.smoothableHandler.setSmoothable(newValue, states);
			reloadAllChunks("the server told us that the smoothness of some states changed");
		});
	}

	public static void onJoinedServer(boolean forgeAlreadyLoadedDefaultConfig) {
		LOG.debug("Client joined server");
		loadDefaultServerConfigIfWeAreOnAModdedServerWithoutNoCubes(forgeAlreadyLoadedDefaultConfig);
		ClientUtil.sendPlayerInfoMessage();
		ClientUtil.warnPlayerIfVisualsDisabled();
		if (!currentServerHasNoCubes) {
			// This lets players not phase through the ground on servers that don't have NoCubes installed
			NoCubesConfig.Server.collisionsEnabled = false;
			ClientUtil.warnPlayer(NoCubes.MOD_ID + ".notification.notInstalledOnServer", KeyMappings.translate(KeyMappings.TOGGLE_SMOOTHABLE_BLOCK_TYPE));
		}
	}

	/**
	 * This lets NoCubes load properly on modded servers that don't have it installed
	 */
	private static void loadDefaultServerConfigIfWeAreOnAModdedServerWithoutNoCubes(boolean forgeAlreadyLoadedDefaultConfig) {
		if (currentServerHasNoCubes) {
			// Forge has synced the server config to us, no need to load the default (see ConfigSync.syncConfigs)
			LOG.debug("Not loading default server config - current server has NoCubes installed");
			return;
		}

		if (forgeAlreadyLoadedDefaultConfig) {
			// Forge has already loaded the default server configs for us (see NetworkHooks#handleClientLoginSuccess(Connection))
			LOG.debug("Not loading default server config - Forge has already loaded it for us");
			return;
		}

		LOG.debug("Connected to a modded server that doesn't have NoCubes installed, loading default server config");
		ClientUtil.platform.loadDefaultServerConfig();
	}
}
