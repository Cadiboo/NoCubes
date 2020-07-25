package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public class S2CUpdateSmoothable {

	private final BlockState state;
	private final boolean newValue;

	public S2CUpdateSmoothable(final BlockState state, boolean newValue) {
		this.state = state;
		this.newValue = newValue;
	}

	public static S2CUpdateSmoothable decode(PacketBuffer buffer) {
		final BlockState state = BlockStateConverter.fromId(buffer.readVarInt());
		final boolean newValue = buffer.readBoolean();
		return new S2CUpdateSmoothable(state, newValue);
	}

	public static void encode(S2CUpdateSmoothable msg, PacketBuffer buffer) {
		buffer.writeVarInt(BlockStateConverter.toId(msg.state));
		buffer.writeBoolean(msg.newValue);
	}

	public static void handle(final S2CUpdateSmoothable msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context ctx = contextSupplier.get();
		if (msg.newValue)
			NoCubes.smoothableHandler.addSmoothable(msg.state);
		else
			NoCubes.smoothableHandler.removeSmoothable(msg.state);
		ctx.setPacketHandled(true);
	}

	public BlockState getState() {
		return state;
	}

}
