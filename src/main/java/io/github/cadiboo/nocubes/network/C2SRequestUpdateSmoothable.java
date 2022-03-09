package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.network.NoCubesNetwork.REQUIRED_PERMISSION_LEVEL;

/**
 * @author Cadiboo
 */
public class C2SRequestUpdateSmoothable {

	public final boolean newValue;
	public final BlockState[] states;

	public C2SRequestUpdateSmoothable(boolean newValue, BlockState[] states) {
		this.newValue = newValue;
		this.states = states;
	}

	public static void encode(C2SRequestUpdateSmoothable msg, PacketBuffer buffer) {
		buffer.writeBoolean(msg.newValue);
		BlockStateConverter.writeBlockStatesTo(buffer, msg.states);
	}

	public static C2SRequestUpdateSmoothable decode(PacketBuffer buffer) {
		return new C2SRequestUpdateSmoothable(
			buffer.readBoolean(),
			BlockStateConverter.readBlockStatesFrom(buffer)
		);
	}

	public static void handle(C2SRequestUpdateSmoothable msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		ServerPlayerEntity sender = Objects.requireNonNull(ctx.getSender(), "Command sender was null");
		if (checkPermissionAndNotifyIfUnauthorised(sender, sender.server)) {
			boolean newValue = msg.newValue;
			BlockState[] statesToUpdate = Arrays.stream(msg.states)
				.filter(s -> NoCubes.smoothableHandler.isSmoothable(s) != newValue)
				.toArray(BlockState[]::new);
			// Guards against useless config reload and/or someone spamming these packets to the server and the server spamming all clients
			if (statesToUpdate.length == 0)
				// Somehow the client is out of sync, just notify them
				NoCubesNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new S2CUpdateSmoothable(newValue, msg.states));
			else {
				ctx.enqueueWork(() -> NoCubesConfig.Server.updateSmoothable(newValue, statesToUpdate));
				// Send back update to all clients
				NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CUpdateSmoothable(newValue, statesToUpdate));
			}
		}
		ctx.setPacketHandled(true);
	}

	public static boolean checkPermissionAndNotifyIfUnauthorised(PlayerEntity player, @Nullable MinecraftServer connectedToServer) {
		if (connectedToServer != null && connectedToServer.isSingleplayerOwner(player.getGameProfile()))
			return true;
		if (player.hasPermissions(REQUIRED_PERMISSION_LEVEL))
			return true;
		ModUtil.warnPlayer(player, NoCubes.MOD_ID + ".command.changeSmoothableNoPermission");
		return false;
	}

}
