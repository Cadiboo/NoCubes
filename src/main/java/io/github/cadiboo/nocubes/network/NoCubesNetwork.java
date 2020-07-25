package io.github.cadiboo.nocubes.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * Network handler.
 * Stores the mod's channel and registers the messages.
 *
 * @author Cadiboo
 */
public final class NoCubesNetwork {

	private static final String NETWORK_PROTOCOL_VERSION = "1";
	/**
	 * Only valid when connected to a server on the client.
	 * Contains random values from the most recently pinged server otherwise.
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
		CHANNEL.registerMessage(networkId++,
			C2SRequestUpdateSmoothable.class,
			C2SRequestUpdateSmoothable::encode,
			C2SRequestUpdateSmoothable::decode,
			C2SRequestUpdateSmoothable::handle
		);
//		CHANNEL.registerMessage(networkId++,
//			C2SRequestSetTerrainCollisions.class,
//			C2SRequestSetTerrainCollisions::encode,
//			C2SRequestSetTerrainCollisions::decode,
//			C2SRequestSetTerrainCollisions::handle
//		);
//		CHANNEL.registerMessage(networkId++,
//			C2SRequestSetExtendFluidsRange.class,
//			C2SRequestSetExtendFluidsRange::encode,
//			C2SRequestSetExtendFluidsRange::decode,
//			C2SRequestSetExtendFluidsRange::handle
//		);
//		CHANNEL.registerMessage(networkId++,
//			C2SRequestSetTerrainMeshGenerator.class,
//			C2SRequestSetTerrainMeshGenerator::encode,
//			C2SRequestSetTerrainMeshGenerator::decode,
//			C2SRequestSetTerrainMeshGenerator::handle
//		);

		// Server -> Client
		CHANNEL.registerMessage(networkId++,
			S2CUpdateSmoothable.class,
			S2CUpdateSmoothable::encode,
			S2CUpdateSmoothable::decode,
			S2CUpdateSmoothable::handle
		);
//		CHANNEL.registerMessage(networkId++,
//			S2CSetTerrainCollisions.class,
//			S2CSetTerrainCollisions::encode,
//			S2CSetTerrainCollisions::decode,
//			S2CSetTerrainCollisions::handle
//		);
//		CHANNEL.registerMessage(networkId++,
//			S2CSetExtendFluidsRange.class,
//			S2CSetExtendFluidsRange::encode,
//			S2CSetExtendFluidsRange::decode,
//			S2CSetExtendFluidsRange::handle
//		);
//		CHANNEL.registerMessage(networkId++,
//			S2CSetTerrainMeshGenerator.class,
//			S2CSetTerrainMeshGenerator::encode,
//			S2CSetTerrainMeshGenerator::decode,
//			S2CSetTerrainMeshGenerator::handle
//		);
	}

}
