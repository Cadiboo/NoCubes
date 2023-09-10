package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.client.RenderHelper.reloadAllChunks;

/**
 * @author Cadiboo
 */
public class S2CUpdateSmoothable implements IMessage, IMessageHandler<S2CUpdateSmoothable, IMessage> {
	final boolean newValue;
	final IBlockState[] states;

	public S2CUpdateSmoothable(final boolean newValue, final IBlockState[] states) {
		this.newValue = newValue;
		this.states = states;
	}

	public static void encode(S2CUpdateSmoothable msg, FriendlyByteBuf buffer) {
		buffer.writeBoolean(msg.newValue);
		BlockStateConverter.writeBlockStatesTo(buffer, msg.states);
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
			reloadAllChunks("the server told us that the smoothness of some states changed");
		});
		ctx.setPacketHandled(true);
	}

}
