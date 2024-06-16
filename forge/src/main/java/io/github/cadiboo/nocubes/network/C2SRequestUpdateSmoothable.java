package io.github.cadiboo.nocubes.network;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;
import java.util.function.Supplier;

public record C2SRequestUpdateSmoothable(
	boolean newValue,
	BlockState[] states
) {
	public static void handle(C2SRequestUpdateSmoothable msg, Supplier<NetworkEvent.Context> contextSupplier) {
		var ctx = contextSupplier.get();
		var sender = Objects.requireNonNull(ctx.getSender(), "Command sender was null");
		NoCubesNetwork.handleC2SRequestUpdateSmoothable(
			sender, msg.newValue, msg.states,
			ctx::enqueueWork,
			(playerIfNotNullElseEveryone, newValue, states) -> NoCubesNetworkForge.CHANNEL.send(
				playerIfNotNullElseEveryone == null ? PacketDistributor.ALL.noArg() : PacketDistributor.PLAYER.with(() -> sender),
				new S2CUpdateSmoothable(newValue, states)
			)
		);
		ctx.setPacketHandled(true);
	}
}
