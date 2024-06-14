package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.network.S2CUpdateNoCubesConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NoCubes needs to know if the server it is connecting to has NoCubes installed.
 * This is because some features (collisions) require the mod to be installed on the server as well as the client.
 * This packet lets us know that the mod is installed on the server - if we don't receive it, the mod isn't installed.
 */
@Mixin(PlayerList.class)
public class PlayerListMixin {

	@Inject(
		method = "placeNewPlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;)V"
		)
	)
	public void nocubes$sendServerHasNoCubesPacket(
		Connection connection, ServerPlayer serverPlayer,
		CallbackInfo ci
	) {
		ServerPlayNetworking.send(serverPlayer, new S2CUpdateNoCubesConfig());
	}

}
