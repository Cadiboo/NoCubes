package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_NAME;

/**
 * @author Cadiboo
 */
public final class C2SRequestEnableTerrainCollisions implements IMessage, IMessageHandler<C2SRequestEnableTerrainCollisions, IMessage> {

	@Override
	public IMessage onMessage(final C2SRequestEnableTerrainCollisions msg, final MessageContext context) {
		context.getServerHandler().player.server.addScheduledTask(() -> {
			final EntityPlayerMP sender = context.getServerHandler().player;
			if (sender == null) {
				return;
			}
			if (sender.canUseCommand(COMMAND_PERMISSION_LEVEL, COMMAND_PERMISSION_NAME)) {
				// Config saving is async so set it now
				Config.terrainCollisions = true;
				ConfigHelper.setTerrainCollisions(true);
				NoCubes.CHANNEL.sendToAll(new S2CEnableTerrainCollisions());
			} else {
				sender.sendMessage(new TextComponentTranslation(MOD_ID + ".enableTerrainCollisionsNoPermission"));
			}
		});
		return null;
	}

	@Override
	public void fromBytes(final ByteBuf buf) {
	}

	@Override
	public void toBytes(final ByteBuf buf) {
	}

}
