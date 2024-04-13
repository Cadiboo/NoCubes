package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.util.BlockStateConverter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

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
		C2SHandler.onC2SRequestUpdateSmoothable(msg.newValue, msg.states, ctx.getSender(), new C2SHandler.C2SRequestUpdateSmoothableReplier() {
			@Override
			public void senderClientIsOutOfSyncNotifyThemOfNewValue(ServerPlayer sender, boolean newValue, BlockState[] states) {
				NoCubesNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new S2CUpdateSmoothable(newValue, states));
			}
			@Override
			public void updateAllClients(boolean newValue, BlockState[] states) {
				NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CUpdateSmoothable(newValue, states));
			}
			@Override
			public void enqueueWork(Runnable work) {
				ctx.enqueueWork(work);
			}
		});
		ctx.setPacketHandled(true);
	}

}
