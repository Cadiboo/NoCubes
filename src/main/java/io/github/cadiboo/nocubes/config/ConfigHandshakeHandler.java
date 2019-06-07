package io.github.cadiboo.nocubes.config;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;

/**
 * @author Cadiboo
 */
public final class ConfigHandshakeHandler {

	/**
	 * Copied from net.minecraftforge.fml.network.FMLHandshakeHandler#handleConfigSync
	 */
	public static void handleConfigSync(final FMLHandshakeMessage.S2CConfigData msg/*, final Supplier<NetworkEvent.Context> contextSupplier*/) {
		NoCubes.LOGGER.debug("Received config sync from server");
		ConfigTracker.INSTANCE.receiveSyncedConfig(msg);
//		contextSupplier.get().setPacketHandled(true);
//		FMLNetworkConstants.handshakeChannel.reply(new FMLHandshakeMessages.C2SAcknowledge(), contextSupplier.get());
	}

}
