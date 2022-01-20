package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.network.NoCubesNetwork.REQUIRED_PERMISSION_LEVEL;

/**
 * @author Cadiboo
 */
public record C2SRequestUpdateSmoothable(
	boolean newValue,
	BlockState[] states
) {

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

	public static void handle(C2SRequestUpdateSmoothable msg, Supplier<NetworkEvent.Context> contextSupplier) {
		var ctx = contextSupplier.get();
		var sender = ctx.getSender();
		var hasPermission = sender.hasPermissions(REQUIRED_PERMISSION_LEVEL);
		if (hasPermission) {
			var newValue = msg.newValue;
			var statesToUpdate = Arrays.stream(msg.states)
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
		} else
			sender.sendMessage(new TranslatableComponent(NoCubes.MOD_ID + ".command.addSmoothableNoPermission"), Util.NIL_UUID);
		ctx.setPacketHandled(true);
	}

}
