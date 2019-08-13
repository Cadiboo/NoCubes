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
public final class C2SRequestAddTerrainSmoothable implements IMessage, IMessageHandler<C2SRequestAddTerrainSmoothable, IMessage> {

	private /*final*/ int blockStateId;

	public C2SRequestAddTerrainSmoothable() {
	}

	public C2SRequestAddTerrainSmoothable(final int blockStateId) {
		this.blockStateId = blockStateId;
	}

	public static void encode(final C2SRequestAddTerrainSmoothable msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.blockStateId);
	}

	@Override
	public void fromBytes(final ByteBuf buf) {
		this.blockStateId = new PacketBuffer(buf).readInt();
	}

	@Override
	public void toBytes(final ByteBuf buf) {
		new PacketBuffer(buf).writeInt(this.blockStateId);
	}

	@Override
	public IMessage onMessage(final C2SRequestAddTerrainSmoothable msg, final MessageContext context) {
		context.getServerHandler().player.server.addScheduledTask(() -> {
			final EntityPlayerMP sender = context.getServerHandler().player;
			if (sender == null) {
				return;
			}
			if (sender.canUseCommand(COMMAND_PERMISSION_LEVEL, COMMAND_PERMISSION_NAME)) {
				final int blockStateId = msg.blockStateId;
				final IBlockState blockState = Block.getStateById(blockStateId);
				if (blockState == StateHolder.AIR_DEFAULT) {
					NoCubes.LOGGER.error("Trying to add invalid terrain smoothable blockstate: " + blockStateId);
					return;
				}
				ConfigHelper.addTerrainSmoothable(blockState);
				NoCubes.CHANNEL.sendToAll(new S2CAddTerrainSmoothable(blockStateId));
			} else {
				sender.sendMessage(new TextComponentTranslation(MOD_ID + ".addTerrainSmoothableNoPermission"));
			}
		});
		return null;
	}

}
