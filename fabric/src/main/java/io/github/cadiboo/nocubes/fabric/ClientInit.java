package io.github.cadiboo.nocubes.fabric;


import io.github.cadiboo.nocubes.client.KeyMappings;
import io.github.cadiboo.nocubes.network.S2CUpdateNoCubesConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;


public class ClientInit implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(S2CUpdateNoCubesConfig.TYPE, (player, packet, responseSender) -> {
//			NoCubesNetwork.currentServerHasNoCubes = true;
		});
		KeyMappings.register(
			KeyBindingHelper::registerKeyBinding,
			keyBindingsOnTick -> ClientTickEvents.START_CLIENT_TICK.register(client -> keyBindingsOnTick.run())
		);
	}
}
