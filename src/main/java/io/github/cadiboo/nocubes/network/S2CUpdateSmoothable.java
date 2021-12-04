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
	BlockState state,
	boolean newValue
) {

	public static void encode(S2CUpdateSmoothable msg, FriendlyByteBuf buffer) {
		buffer.writeVarInt(BlockStateConverter.toId(msg.state));
		buffer.writeBoolean(msg.newValue);
	}

	public static S2CUpdateSmoothable decode(FriendlyByteBuf buffer) {
		var state = BlockStateConverter.fromId(buffer.readVarInt());
		var newValue = buffer.readBoolean();
		return new S2CUpdateSmoothable(state, newValue);
	}

	public static void handle(S2CUpdateSmoothable msg, Supplier<NetworkEvent.Context> contextSupplier) {
		var ctx = contextSupplier.get();
		ctx.enqueueWork(() -> {
			NoCubes.smoothableHandler.setSmoothable(msg.newValue, msg.state);
			ClientUtil.reloadAllChunks();
		});
		ctx.setPacketHandled(true);
	}

}
