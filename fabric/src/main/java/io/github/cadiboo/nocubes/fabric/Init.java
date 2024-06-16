package io.github.cadiboo.nocubes.fabric;

import io.github.cadiboo.nocubes.config.NoCubesConfigImpl;
import io.github.cadiboo.nocubes.network.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.Collections;

public class Init implements ModInitializer {
	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register((event) -> {
			NoCubesConfigImpl.loadServerConfig();
		});
		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
			synchronizer.waitFor(server.submit(() -> sender.sendPacket(NoCubesNetworkFabric.createS2CUpdateServerConfigDuringLogin(S2CUpdateServerConfig::new))));
		});
		ServerLoginNetworking.registerGlobalReceiver(S2CUpdateServerConfig.TYPE.getId(), (server, handler, understood, buf, synchronizer, responseSender) -> {
		});
		ServerPlayNetworking.registerGlobalReceiver(C2SRequestUpdateSmoothable.TYPE, (packet, player, responseSender) -> {
			NoCubesNetwork.handleC2SRequestUpdateSmoothable(
				player, packet.newValue(), packet.states(),
				player.server::execute,
				(playerIfNotNullElseEveryone, newValue, states) -> {
					var players = playerIfNotNullElseEveryone != null
						? Collections.singleton(playerIfNotNullElseEveryone)
						: player.server.getPlayerList().getPlayers();
					players.forEach(p -> ServerPlayNetworking.send(p, new S2CUpdateSmoothable(newValue, states)));
				}
			);
		});
	}
}
