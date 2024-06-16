package io.github.cadiboo.nocubes.mixin.client;

import io.github.cadiboo.nocubes.network.NoCubesNetworkClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(
		method = "handleLogin",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;resetPos()V",
			shift = At.Shift.AFTER
		)
	)
	private void nocubes$onClientJoinedServer(ClientboundLoginPacket clientboundLoginPacket, CallbackInfo ci) {
		NoCubesNetworkClient.onJoinedServer(false);
	}
}
