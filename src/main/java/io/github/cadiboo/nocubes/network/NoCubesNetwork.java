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

	private static final String NETWORK_PROTOCOL_VERSION = "2";
	/**
	 * Only valid when connected to a server on the client.
	 * Contains random values from the most recently pinged server otherwise.
	 * Also valid for singleplayer integrated servers (always true).
	 */
	public static boolean currentServerHasNoCubes = false;
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(MOD_ID, "main"),
		() -> NETWORK_PROTOCOL_VERSION,
		// Clients can run on servers with the same NoCubes version and servers without NoCubes (including vanilla)
		serverVersion -> {
			if (NetworkRegistry.ABSENT.equals(serverVersion) || NetworkRegistry.ACCEPTVANILLA.equals(serverVersion)) {
				currentServerHasNoCubes = false;
				return true;
			}
			currentServerHasNoCubes = true;
			return NETWORK_PROTOCOL_VERSION.equals(serverVersion);
		},
		// Clients must have the same version as the server
		NETWORK_PROTOCOL_VERSION::equals
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
			C2SRequestUpdateSmoothable::encode,
			C2SRequestUpdateSmoothable::decode,
			C2SRequestUpdateSmoothable::handle
		);

		// Server -> Client
		register(
			++networkId,
			S2CUpdateSmoothable.class,
			S2CUpdateSmoothable::encode,
			S2CUpdateSmoothable::decode,
			S2CUpdateSmoothable::handle
		);
		register(
			++networkId,
			S2CUpdateServerConfig.class,
			S2CUpdateServerConfig::encode,
			S2CUpdateServerConfig::decode,
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
