package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.client.RenderHelper.reloadAllChunks;

/**
 * @author Cadiboo
 */
public record S2CUpdateSmoothable(
	boolean newValue,
	BlockState[] states
) {

	public static void encode(S2CUpdateSmoothable msg, FriendlyByteBuf buffer) {
		buffer.writeBoolean(msg.newValue);
		NoCubes.platform.blockStateSerializer().writeBlockStatesTo(buffer, msg.states);
	}

	public static S2CUpdateSmoothable decode(FriendlyByteBuf buffer) {
		return new S2CUpdateSmoothable(
			buffer.readBoolean(),
			NoCubes.platform.blockStateSerializer().readBlockStatesFrom(buffer)
		);
	}

	public static void handle(S2CUpdateSmoothable msg, Supplier<NetworkEvent.Context> contextSupplier) {
		var ctx = contextSupplier.get();
		ctx.enqueueWork(() -> {
			NoCubes.smoothableHandler.setSmoothable(msg.newValue, msg.states);
			reloadAllChunks("the server told us that the smoothness of some states changed");
		});
		ctx.setPacketHandled(true);
	}

}
