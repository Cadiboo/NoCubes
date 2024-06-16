package io.github.cadiboo.nocubes.network;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;
import java.util.function.Supplier;

public record C2SRequestUpdateSmoothable(
	boolean newValue,
	BlockState[] states
) {
	public static void handle(C2SRequestUpdateSmoothable msg, Supplier<CustomPayloadEvent.Context> contextSupplier) {
		var ctx = contextSupplier.get();
		var sender = Objects.requireNonNull(ctx.getSender(), "Command sender was null");
		NoCubesNetwork.handleC2SRequestUpdateSmoothable(
			sender, msg.newValue, msg.states,
			ctx::enqueueWork,
			(playerIfNotNullElseEveryone, newValue, states) -> NoCubesNetworkForge.CHANNEL.send(
				new S2CUpdateSmoothable(newValue, states),
				playerIfNotNullElseEveryone == null ? PacketDistributor.ALL.noArg() : PacketDistributor.PLAYER.with(sender)
			)
		);
		ctx.setPacketHandled(true);
	}
}
