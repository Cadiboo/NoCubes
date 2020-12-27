package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.util.IsSmoothable;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author Cadiboo
 */
public class S2CSetSmoothable extends SetSmoothableBase implements IMessageHandler<S2CSetSmoothable, IMessage> {

	public S2CSetSmoothable(IsSmoothable isSmoothable, IBlockState state, boolean newValue) {
		super(isSmoothable, state, newValue);
	}

	public S2CSetSmoothable() {
	}

	@Override
	public IMessage onMessage(S2CSetSmoothable msg, MessageContext context) {
		context.getServerHandler().player.server.addScheduledTask(() -> isSmoothable.set(msg.state, msg.newValue));
		return null;
	}

}
