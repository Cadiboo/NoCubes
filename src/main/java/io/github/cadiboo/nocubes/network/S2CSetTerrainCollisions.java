package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
public final class S2CSetTerrainCollisions implements IMessage, IMessageHandler<S2CSetTerrainCollisions, IMessage> {

	private  /*final*/ boolean newValue;

	public S2CSetTerrainCollisions(boolean newValue) {
		this.newValue = newValue;
	}

	public S2CSetTerrainCollisions() {
	}

		@Override
	public IMessage onMessage(S2CSetTerrainCollisions msg, MessageContext context) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			NoCubesConfig.Server.terrainCollisions = msg.newValue;
//			final EntityPlayerSP player = Minecraft.getMinecraft().player;
////			player.sendMessage(new TextComponentTranslation(MOD_ID + ".terrainCollisions114"));
//			player.sendMessage(new TextComponentTranslation(MOD_ID + ".terrainCollisionsEnabled"));
		});
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		newValue = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(newValue);
	}

}
