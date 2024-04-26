package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import static io.github.cadiboo.nocubes.network.NoCubesNetwork.REQUIRED_PERMISSION_LEVEL;

/**
 * @author Cadiboo
 */
public record C2SRequestUpdateSmoothable(
	boolean newValue,
	BlockState[] states
) implements CustomPacketPayload {
	public static ResourceLocation ID = new ResourceLocation(NoCubes.MOD_ID, C2SRequestUpdateSmoothable.class.getSimpleName().toLowerCase());

	public static void encode(C2SRequestUpdateSmoothable msg, FriendlyByteBuf buffer) {
		buffer.writeBoolean(msg.newValue);
		BlockStateConverter.writeBlockStatesTo(buffer, msg.states);
	}

	public static C2SRequestUpdateSmoothable decode(FriendlyByteBuf buffer) {
		return new C2SRequestUpdateSmoothable(
			buffer.readBoolean(),
			BlockStateConverter.readBlockStatesFrom(buffer)
		);
	}

	public static void handle(C2SRequestUpdateSmoothable msg, PlayPayloadContext ctx) {
		var sender = ctx.player().orElse(null);
		handle(msg, sender instanceof ServerPlayer ? (ServerPlayer) sender : null, ctx.workHandler()::submitAsync);
	}

	private static void handle(C2SRequestUpdateSmoothable msg, ServerPlayer sender, Consumer<Runnable> queueToRunOnServerThread) {
		Objects.requireNonNull(sender, "Command sender was null");
		if (checkPermissionAndNotifyIfUnauthorised(sender, sender.server)) {
			var newValue = msg.newValue;
			var statesToUpdate = Arrays.stream(msg.states)
				.filter(s -> NoCubes.smoothableHandler.isSmoothable(s) != newValue)
				.toArray(BlockState[]::new);
			// Guards against useless config reload and/or someone spamming these packets to the server and the server spamming all clients
			if (statesToUpdate.length == 0)
				// Somehow the client is out of sync, just notify them
				PacketDistributor.PLAYER.with(sender).send(new S2CUpdateSmoothable(newValue, msg.states));
			else {
				queueToRunOnServerThread.accept(() -> NoCubesConfig.Server.updateSmoothable(newValue, statesToUpdate));
				// Send back update to all clients
				PacketDistributor.ALL.noArg().send(new S2CUpdateSmoothable(newValue, statesToUpdate));
			}
		}
	}

	public static boolean checkPermissionAndNotifyIfUnauthorised(Player player, @Nullable MinecraftServer connectedToServer) {
		if (connectedToServer != null && connectedToServer.isSingleplayerOwner(player.getGameProfile()))
			return true;
		if (player.hasPermissions(REQUIRED_PERMISSION_LEVEL))
			return true;
		ModUtil.warnPlayer(player, NoCubes.MOD_ID + ".command.changeSmoothableNoPermission");
		return false;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		encode(this, buffer);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

}
