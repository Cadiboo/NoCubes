package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.util.DistExecutor;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
public final class S2CSetExtendFluidsRange implements IMessage, IMessageHandler<S2CSetExtendFluidsRange, IMessage> {

	private /*final*/ ExtendFluidsRange newRange;

	public S2CSetExtendFluidsRange(final ExtendFluidsRange extendFluidsRange) {
		this.newRange = extendFluidsRange;
	}

	public S2CSetExtendFluidsRange() {
	}

	@Override
	public IMessage onMessage(final S2CSetExtendFluidsRange msg, final MessageContext context) {
		Minecraft.getMinecraft().addScheduledTask(() -> DistExecutor.runWhenOn(Side.CLIENT, () -> () -> {
			final ExtendFluidsRange newRange = msg.newRange;
			Config.extendFluidsRange = newRange;
			Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation(MOD_ID + ".setExtendFluidsRange", newRange));
			ClientUtil.tryReloadRenderers();
		}));
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
