package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.config.NoCubesConfigImpl;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;
import java.util.function.Function;

public class NoCubesNetworkFabric {

	/**
	 * @see #handleS2CUpdateServerConfigDuringLogin
	 */
	public static <T> T createS2CUpdateServerConfigDuringLogin(Function<byte[], T> constructor) {
		return constructor.apply(NoCubesConfigImpl.readConfigFileBytes());
	}

	/**
	 * NoCubes needs to know if the server it is connecting to has NoCubes installed.
	 * This is because some features (collisions) require the mod to be installed on the server as well as the client.
	 * This packet lets us know that the mod is installed on the server - if we don't receive it, the mod isn't installed.
	 */
	public static void handleS2CUpdateServerConfigDuringLogin(Consumer<Runnable> enqueueWork, FriendlyByteBuf buf) {
		NoCubesNetworkClient.currentServerHasNoCubes = true;
		NoCubesNetworkClient.handleS2CUpdateServerConfig(enqueueWork, NoCubesNetwork.Serializer.decodeS2CUpdateServerConfig(buf, Function.identity()));
	}
}
