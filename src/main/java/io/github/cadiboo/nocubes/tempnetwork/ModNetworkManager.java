package io.github.cadiboo.nocubes.tempnetwork;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author Cadiboo
 */
public final class ModNetworkManager {

	public final SimpleNetworkWrapper NETWORK;

	public ModNetworkManager() {
		NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(NoCubes.MOD_ID);
		/* Server -> Client */
		NETWORK.registerMessage(S2CSyncConfig.class, S2CSyncConfig.class, 0, Side.CLIENT);
	}

}
