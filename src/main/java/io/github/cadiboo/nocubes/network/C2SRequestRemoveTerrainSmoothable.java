package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.util.StateHolder;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
public final class C2SRequestRemoveTerrainSmoothable implements IMessage, IMessageHandler<C2SRequestRemoveTerrainSmoothable, IMessage> {

	private /*final*/ int blockStateId;

	public C2SRequestRemoveTerrainSmoothable() {
	}

	public C2SRequestRemoveTerrainSmoothable(final int blockStateId) {
		this.blockStateId = blockStateId;
	}

	@Override
	public IMessage onMessage(final C2SRequestRemoveTerrainSmoothable msg, final MessageContext context) {
		context.getServerHandler().player.server.addScheduledTask(() -> {
			final EntityPlayerMP sender = context.getServerHandler().player;
			if (sender == null) {
				return;
			}
			if (sender.canUseCommand(COMMAND_PERMISSION_LEVEL, COMMAND_PERMISSION_NAME)) {
				final int blockStateId = msg.blockStateId;
				final IBlockState blockState = Block.getStateById(blockStateId);
				if (blockState == StateHolder.AIR_DEFAULT) {
					NoCubes.LOGGER.error("Trying to remove invalid terrain smoothable blockstate: " + blockStateId);
					return;
				}
				ConfigHelper.removeTerrainSmoothable(blockState);
				NoCubes.CHANNEL.sendToAll(new S2CRemoveTerrainSmoothable(blockStateId));
			} else {
				sender.sendMessage(new TextComponentTranslation(MOD_ID + ".removeTerrainSmoothableNoPermission"));
			}
		});
		return null;
	}

	@Override
	public void fromBytes(final ByteBuf buf) {
		this.blockStateId = new PacketBuffer(buf).readInt();
	}

	@Override
	public void toBytes(final ByteBuf buf) {
		new PacketBuffer(buf).writeInt(this.blockStateId);
	}

}
