package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.client.RenderHelper.reloadAllChunks;

/**
 * @author Cadiboo
 */
public class S2CUpdateSmoothable {

	public final boolean newValue;
	public final BlockState[] states;

	public S2CUpdateSmoothable(boolean newValue, BlockState[] states) {
		this.newValue = newValue;
		this.states = states;
	}

	public static void encode(S2CUpdateSmoothable msg, PacketBuffer buffer) {
		buffer.writeBoolean(msg.newValue);
		BlockStateConverter.writeBlockStatesTo(buffer, msg.states);
	}

	public static S2CUpdateSmoothable decode(PacketBuffer buffer) {
		return new S2CUpdateSmoothable(
			buffer.readBoolean(),
			BlockStateConverter.readBlockStatesFrom(buffer)
		);
	}

	public static void handle(S2CUpdateSmoothable msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		ctx.enqueueWork(() -> {
			NoCubes.smoothableHandler.setSmoothable(msg.newValue, msg.states);
			reloadAllChunks("the server told us that the smoothness of some states changed");
		});
		ctx.setPacketHandled(true);
	}

}
