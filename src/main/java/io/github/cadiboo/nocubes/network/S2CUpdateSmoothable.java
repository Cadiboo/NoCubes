package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public class S2CUpdateSmoothable {

	private final BlockState state;
	private final boolean newValue;

	public S2CUpdateSmoothable(BlockState state, boolean newValue) {
		this.state = state;
		this.newValue = newValue;
	}

	public static void encode(S2CUpdateSmoothable msg, FriendlyByteBuf buffer) {
		buffer.writeVarInt(BlockStateConverter.toId(msg.state));
		buffer.writeBoolean(msg.newValue);
	}

	public static S2CUpdateSmoothable decode(FriendlyByteBuf buffer) {
		BlockState state = BlockStateConverter.fromId(buffer.readVarInt());
		boolean newValue = buffer.readBoolean();
		return new S2CUpdateSmoothable(state, newValue);
	}

	public static void handle(S2CUpdateSmoothable msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		ctx.enqueueWork(() -> {
			NoCubes.smoothableHandler.setSmoothable(msg.newValue, msg.state);
			ClientUtil.reloadAllChunks();
		});
		ctx.setPacketHandled(true);
	}

}
