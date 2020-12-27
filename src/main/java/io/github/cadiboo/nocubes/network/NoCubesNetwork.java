package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.future.S2CConfigData;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * Network handler.
 * Stores the mod's channel and registers the messages.
 *
 * @author Cadiboo
 */
public final class NoCubesNetwork {

	/**
	 * Only valid when connected to a server on the client.
	 * Contains random values from the most recently pinged server otherwise.
	 */
	public static boolean currentServerHasNoCubes = false;

	private static final Side SERVER = Side.SERVER;
	private static final Side CLIENT = Side.CLIENT;

	private static final String NETWORK_PROTOCOL_VERSION = "1";
//	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
//		new ResourceLocation(MOD_ID, "main"),
//		() -> NETWORK_PROTOCOL_VERSION,
//		// Clients can run on servers with the same NoCubes version and servers without NoCubes (including vanilla)
//		serverVersion -> {
//			if (NetworkRegistry.ABSENT.equals(serverVersion) || NetworkRegistry.ACCEPTVANILLA.equals(serverVersion)) {
//				currentServerHasNoCubes = false;
//				return true;
//			}
//			currentServerHasNoCubes = true;
//			return NETWORK_PROTOCOL_VERSION.equals(serverVersion);
//		},
//		// Clients must have the same version as the server
//		NETWORK_PROTOCOL_VERSION::equals
//	);
	public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);

	/**
	 * Called from inside the mod constructor.
	 */
	public static void register() {
		int networkId = 0;
		CHANNEL.registerMessage(
				C2SRequestSetSmoothable.class,
				C2SRequestSetSmoothable.class,
				networkId++,
				SERVER
		);
		CHANNEL.registerMessage(
				C2SRequestSetTerrainCollisions.class,
				C2SRequestSetTerrainCollisions.class,
				networkId++,
				SERVER
		);
//		CHANNEL.registerMessage(
//				C2SRequestSetTerrainMeshGenerator.class,
//				C2SRequestSetTerrainMeshGenerator.class,
//				networkId++,
//				SERVER
//		);

		/* Server -> Client */
		CHANNEL.registerMessage(
				S2CSetSmoothable.class,
				S2CSetSmoothable.class,
				networkId++,
				CLIENT
		);
		CHANNEL.registerMessage(
				S2CSetTerrainCollisions.class,
				S2CSetTerrainCollisions.class,
				networkId++,
				CLIENT
		);
//		CHANNEL.registerMessage(
//				S2CSetTerrainMeshGenerator.class,
//				S2CSetTerrainMeshGenerator.class,
//				networkId++,
//				CLIENT
//		);


		CHANNEL.registerMessage(
				S2CConfigData.class,
				S2CConfigData.class,
				networkId++,
				CLIENT
		);
	}

	static void executeIfPlayerHasPermission(MessageContext context, String actionName, Runnable action) {
		context.getServerHandler().player.server.addScheduledTask(() -> {
			EntityPlayerMP sender = context.getServerHandler().player;
			if (sender == null)
				return;
			if (ModUtil.doesPlayerHavePermission(sender))
				action.run();
			else
				sender.sendMessage(new TextComponentTranslation(MOD_ID + "." + actionName + "NoPermission"));
		});
	}

}
