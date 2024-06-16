package io.github.cadiboo.nocubes.network;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public record S2CUpdateSmoothable(
	boolean newValue,
	BlockState[] states
) {
	public static void handle(S2CUpdateSmoothable msg, Supplier<CustomPayloadEvent.Context> contextSupplier) {
		var ctx = contextSupplier.get();
		NoCubesNetworkClient.handleS2CUpdateSmoothable(ctx::enqueueWork, msg.newValue, msg.states);
		ctx.setPacketHandled(true);
	}
}
