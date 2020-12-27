package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static io.github.cadiboo.nocubes.network.NoCubesNetwork.executeIfPlayerHasPermission;

/**
 * @author Cadiboo
 */
public final class C2SRequestSetTerrainCollisions implements IMessage, IMessageHandler<C2SRequestSetTerrainCollisions, IMessage> {

	private /*final*/ boolean newValue;

	public C2SRequestSetTerrainCollisions(boolean newValue) {
		this.newValue = newValue;
	}

	public C2SRequestSetTerrainCollisions() {
	}

	@Override
	public IMessage onMessage(C2SRequestSetTerrainCollisions msg, MessageContext context) {
		executeIfPlayerHasPermission(context, "setTerrainCollisions", () -> {
			boolean newValue = msg.newValue;
			NoCubesConfig.Server.setTerrainCollisions(newValue);
			NoCubesNetwork.CHANNEL.sendToAll(new S2CSetTerrainCollisions(newValue));
		});
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.newValue = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(this.newValue);
	}

}
