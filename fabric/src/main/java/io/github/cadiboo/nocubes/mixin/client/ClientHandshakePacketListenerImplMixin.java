package io.github.cadiboo.nocubes.mixin.client;

import io.github.cadiboo.nocubes.mixin.PlayerListMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * See {@link PlayerListMixin} for documentation.
 */
@Mixin(ClientHandshakePacketListenerImpl.class)
public abstract class ClientHandshakePacketListenerImplMixin {

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void nocubes$setUpDefaultValuesInCaseTargetServerDoesNotHaveModInstalled(
		Connection connection, Minecraft minecraft,
		ServerData serverData, Screen screen,
		boolean bl, Duration duration, Consumer<Component> consumer,
		CallbackInfo ci
	) {
//		NoCubesNetwork.currentServerHasNoCubes = false;
	}

}
