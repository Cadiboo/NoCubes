package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public record S2CUpdateSmoothable(
	boolean newValue,
	BlockState[] states
) {

	public static void encode(S2CUpdateSmoothable msg, FriendlyByteBuf buffer) {
		BlockStateConverter.writeBlockStatesTo(buffer, msg.states);
		buffer.writeBoolean(msg.newValue);
	}

	public static S2CUpdateSmoothable decode(FriendlyByteBuf buffer) {
		return new S2CUpdateSmoothable(
			buffer.readBoolean(),
			BlockStateConverter.readBlockStatesFrom(buffer)
		);
	}

	public static void handle(S2CUpdateSmoothable msg, Supplier<NetworkEvent.Context> contextSupplier) {
		var ctx = contextSupplier.get();
		ctx.enqueueWork(() -> {
			NoCubes.smoothableHandler.setSmoothable(msg.newValue, msg.states);
			ClientUtil.reloadAllChunks();
		});
		ctx.setPacketHandled(true);
	}

}
