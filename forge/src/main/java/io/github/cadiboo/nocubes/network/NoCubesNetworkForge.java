package io.github.cadiboo.nocubes.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * Stores the mod's channel and registers the messages.
 */
public final class NoCubesNetworkForge {

	private static final Logger LOG = LogManager.getLogger();

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(MOD_ID, "main"),
		() -> NoCubesNetwork.NETWORK_PROTOCOL_VERSION,
		// Clients can run on servers with the same NoCubes version and servers without NoCubes (including vanilla)
		serverVersion -> {
			if (NetworkRegistry.ABSENT.version().equals(serverVersion) || NetworkRegistry.ACCEPTVANILLA.equals(serverVersion)) {
				NoCubesNetworkClient.currentServerHasNoCubes = false;
				return true;
			}
			NoCubesNetworkClient.currentServerHasNoCubes = true;
			return NoCubesNetwork.NETWORK_PROTOCOL_VERSION.equals(serverVersion);
		},
		// Clients must have the same version as the server
		NoCubesNetwork.NETWORK_PROTOCOL_VERSION::equals
	);

	/**
	 * Called from inside the mod constructor.
	 */
	public static void register() {
		int networkId = 0;
		// Client -> Server
		register(
			networkId,
			C2SRequestUpdateSmoothable.class,
			(msg, buffer) -> NoCubesNetwork.Serializer.encodeUpdateSmoothable(buffer, msg.newValue(), msg.states()),
			(buffer) -> NoCubesNetwork.Serializer.decodeUpdateSmoothable(buffer, C2SRequestUpdateSmoothable::new),
			C2SRequestUpdateSmoothable::handle
		);

		// Server -> Client
		register(
			++networkId,
			S2CUpdateSmoothable.class,
			(msg, buffer) -> NoCubesNetwork.Serializer.encodeUpdateSmoothable(buffer, msg.newValue(), msg.states()),
			(buffer) -> NoCubesNetwork.Serializer.decodeUpdateSmoothable(buffer, S2CUpdateSmoothable::new),
			S2CUpdateSmoothable::handle
		);
		register(
			++networkId,
			S2CUpdateServerConfig.class,
			(msg, buffer) -> NoCubesNetwork.Serializer.encodeS2CUpdateServerConfig(buffer, msg.data()),
			(buffer) -> NoCubesNetwork.Serializer.decodeS2CUpdateServerConfig(buffer, S2CUpdateServerConfig::new),
			S2CUpdateServerConfig::handle
		);
	}

	static <MSG> void register(int index, Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
		CHANNEL.registerMessage(
			index,
			messageType,
			(msg, buffer) -> {
				LOG.debug("Encoding {}", messageType.getSimpleName());
				encoder.accept(msg, buffer);
			},
			buffer -> {
				LOG.debug("Decoding {}", messageType.getSimpleName());
				return decoder.apply(buffer);
			},
			(msg, ctx) -> {
				LOG.debug("Handling {}", messageType.getSimpleName());
				handler.accept(msg, ctx);
			}
		);
	}

}
