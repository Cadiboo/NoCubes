package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
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
public final class C2SRequestSetExtendFluidsRange implements IMessage, IMessageHandler<C2SRequestSetExtendFluidsRange, IMessage> {

	private /*final*/ ExtendFluidsRange newRange;

	public C2SRequestSetExtendFluidsRange(final ExtendFluidsRange extendFluidsRange) {
		this.newRange = extendFluidsRange;
	}

	public C2SRequestSetExtendFluidsRange() {
	}

	@Override
	public IMessage onMessage(final C2SRequestSetExtendFluidsRange msg, final MessageContext context) {
		context.getServerHandler().player.server.addScheduledTask(() -> {
			final EntityPlayerMP sender = context.getServerHandler().player;
			if (sender == null) {
				return;
			}
			if (sender.canUseCommand(COMMAND_PERMISSION_LEVEL, COMMAND_PERMISSION_NAME)) {
				final ExtendFluidsRange newRange = msg.newRange;
				// Config saving is async so set it now
				Config.extendFluidsRange = newRange;
				ConfigHelper.setExtendFluidsRange(newRange);
				NoCubes.CHANNEL.sendToAll(new S2CSetExtendFluidsRange(newRange));
			} else {
				sender.sendMessage(new TextComponentTranslation(MOD_ID + ".changeExtendFluidsRangeNoPermission"));
			}
		});
		return null;
	}

	@Override
	public void fromBytes(final ByteBuf buf) {
		this.newRange = ExtendFluidsRange.VALUES[new PacketBuffer(buf).readInt()];
	}

	@Override
	public void toBytes(final ByteBuf buf) {
		new PacketBuffer(buf).writeInt(this.newRange.ordinal());
	}

}
