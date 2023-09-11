package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

import static io.github.cadiboo.nocubes.network.NoCubesNetwork.REQUIRED_PERMISSION_LEVEL;

/**
 * @author Cadiboo
 */
public class C2SRequestUpdateSmoothable implements IMessage, IMessageHandler<C2SRequestUpdateSmoothable, IMessage> {

	boolean newValue;
	IBlockState[] states;

	public C2SRequestUpdateSmoothable() {
	}

	public C2SRequestUpdateSmoothable(boolean newValue, IBlockState[] states) {
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
	public IMessage onMessage(C2SRequestUpdateSmoothable msg, MessageContext context) {
		EntityPlayerMP sender = Objects.requireNonNull(context.getServerHandler().player, "Command sender was null");
		if (checkPermissionAndNotifyIfUnauthorised(sender, sender.server)) {
			boolean newValue = msg.newValue;
			IBlockState[] statesToUpdate = Arrays.stream(msg.states)
				.filter(s -> NoCubes.smoothableHandler.isSmoothable(s) != newValue)
				.toArray(IBlockState[]::new);
			// Guards against useless config reload and/or someone spamming these packets to the server and the server spamming all clients
			if (statesToUpdate.length == 0)
				// Somehow the client is out of sync, just notify them
				NoCubesNetwork.CHANNEL.sendTo(new S2CUpdateSmoothable(newValue, msg.states), sender);
			else {
				((WorldServer) sender.world).addScheduledTask(() -> NoCubesConfig.Server.updateSmoothable(newValue, statesToUpdate));
				// Send back update to all clients
				NoCubesNetwork.CHANNEL.sendToAll(new S2CUpdateSmoothable(newValue, statesToUpdate));
			}
		}
		return null;
	}

	public static boolean checkPermissionAndNotifyIfUnauthorised(EntityPlayer player, @Nullable MinecraftServer connectedToServer) {
		if (connectedToServer != null && Objects.equals(connectedToServer.getServerOwner(), player.getName()))
			return true;
		if (player.canUseCommand(REQUIRED_PERMISSION_LEVEL, "noCubesCommands"))
			return true;
		ModUtil.warnPlayer(player, NoCubes.MOD_ID + ".command.changeSmoothableNoPermission");
		return false;
	}

}
