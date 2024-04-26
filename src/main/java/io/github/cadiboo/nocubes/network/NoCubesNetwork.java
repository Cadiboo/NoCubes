package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Network handler.
 * Stores the mod's channel and registers the messages.
 *
 * @author Cadiboo
 */
public final class NoCubesNetwork {

	private static final Logger LOG = LogManager.getLogger();

	/**
	 * From the minecraft wiki.
	 * 1. Ops can bypass spawn protection.
	 * 2. Ops can use /clear, /difficulty, /effect, /gamemode, /gamerule, /give, /summon, /setblock and /tp, and can edit command blocks.
	 * 3. Ops can use /ban, /deop, /whitelist, /kick, and /op.
	 * 4. Ops can use /stop.
	 */
	public static final int REQUIRED_PERMISSION_LEVEL = 2;

	/**
	 * Only valid when connected to a server on the client.
	 * Contains random values from the most recently pinged server otherwise.
	 * Also valid for singleplayer integrated servers (always true).
	 */
	public static boolean currentServerHasNoCubes = false;

	/**
	 * Called from inside the mod constructor.
	 */
	public static void register(RegisterPayloadHandlerEvent event) {
		event.registrar(NoCubes.MOD_ID)
			.optional()
			.configuration(S2CUpdateServerConfig.ID, S2CUpdateServerConfig::decode, S2CUpdateServerConfig::handle)
			.play(C2SRequestUpdateSmoothable.ID, C2SRequestUpdateSmoothable::decode, C2SRequestUpdateSmoothable::handle)
			.play(S2CUpdateSmoothable.ID, S2CUpdateSmoothable::decode, S2CUpdateSmoothable::handle);
	}

	/**
	 * Before we connect to a server, we load the default server config.
	 * This lets NoCubes load properly on vanilla servers or modded servers that don't have the mod installed.
	 * If the server DOES have NoCubes installed, when we finish connecting to it, it will send us a packet with its config values.
	 * We don't do this for integrated servers because there is only one instance of NoCubes' config, and overwriting the values here would mess up the server's values.
	 */
	public static void beforeConnectingToRemoteServer() {
		NoCubesConfig.Hacks.loadDefaultServerConfig();
	}

}
