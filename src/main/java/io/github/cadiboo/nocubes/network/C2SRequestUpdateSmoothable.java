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

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.network.NoCubesNetwork.REQUIRED_PERMISSION_LEVEL;

/**
 * @author Cadiboo
 */
public record C2SRequestUpdateSmoothable(
	BlockState state,
	boolean newValue
) {

	public static void encode(C2SRequestUpdateSmoothable msg, FriendlyByteBuf buffer) {
		buffer.writeVarInt(BlockStateConverter.toId(msg.state));
		buffer.writeBoolean(msg.newValue);
	}

	public static C2SRequestUpdateSmoothable decode(FriendlyByteBuf buffer) {
		var state = BlockStateConverter.fromId(buffer.readVarInt());
		var newValue = buffer.readBoolean();
		return new C2SRequestUpdateSmoothable(state, newValue);
	}

	public static void handle(C2SRequestUpdateSmoothable msg, Supplier<NetworkEvent.Context> contextSupplier) {
		var ctx = contextSupplier.get();
		var sender = ctx.getSender();
		boolean hasPermission = sender.hasPermissions(REQUIRED_PERMISSION_LEVEL);
		if (hasPermission) {
			BlockState state = msg.state;
			boolean newValue = msg.newValue;
			// Guards against useless config reload and/or someone spamming these packets to the server and the server spamming all clients
			if (NoCubes.smoothableHandler.isSmoothable(state) != newValue) {
				ctx.enqueueWork(() -> NoCubesConfig.Server.updateSmoothable(newValue, state));
				// Send back update to all clients
				NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CUpdateSmoothable(state, newValue));
			} else
				// Somehow the client is out of sync, just notify them
				NoCubesNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new S2CUpdateSmoothable(state, newValue));
		} else
			sender.sendMessage(new TranslatableComponent(NoCubes.MOD_ID + ".command.addSmoothableNoPermission"), Util.NIL_UUID);
		ctx.setPacketHandled(true);
	}

}
