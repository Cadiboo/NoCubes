package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import io.github.cadiboo.nocubes.util.DistExecutor;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import static io.github.cadiboo.nocubes.client.RenderHelper.reloadAllChunks;

/**
 * @author Cadiboo
 */
public class S2CUpdateSmoothable implements IMessage, IMessageHandler<S2CUpdateSmoothable, IMessage> {
	boolean newValue;
	IBlockState[] states;

	public S2CUpdateSmoothable() {
	}

	public S2CUpdateSmoothable(final boolean newValue, final IBlockState[] states) {
		this.newValue = newValue;
		this.states = states;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		buffer.writeBoolean(this.newValue);
		BlockStateConverter.writeBlockStatesTo(buffer, this.states);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		this.newValue = buffer.readBoolean();
		this.states = BlockStateConverter.readBlockStatesFrom(buffer);
	}

	@Override
	public IMessage onMessage(S2CUpdateSmoothable msg, MessageContext context) {
		DistExecutor.runWhenOn(Side.CLIENT, () -> () -> Minecraft.getMinecraft().addScheduledTask(() -> {
			NoCubes.smoothableHandler.setSmoothable(msg.newValue, msg.states);
			reloadAllChunks("the server told us that the smoothness of some states changed");
		}));
		return null;
	}

}
