package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.network.NoCubesNetwork.REQUIRED_PERMISSION_LEVEL;

/**
 * @author Cadiboo
 */
public class C2SRequestUpdateSmoothable implements IMessage, IMessageHandler<C2SRequestUpdateSmoothable, IMessage> {
	final boolean newValue;
	final IBlockState[] states;

	public C2SRequestUpdateSmoothable(final boolean newValue, final IBlockState[] states) {
		this.newValue = newValue;
		this.states = states;
	}

	public static void encode(C2SRequestUpdateSmoothable msg, PacketBuffer buffer) {
		buffer.writeBoolean(msg.newValue);
		BlockStateConverter.writeBlockStatesTo(buffer, msg.states);
	}

	public static C2SRequestUpdateSmoothable decode(PacketBuffer buffer) {
		return new C2SRequestUpdateSmoothable(
			buffer.readBoolean(),
			BlockStateConverter.readBlockStatesFrom(buffer)
		);
	}


	public static void handle(C2SRequestUpdateSmoothable msg, Supplier<MessageContext> contextSupplier) {
		MessageContext ctx = contextSupplier.get();
		var sender = Objects.requireNonNull(ctx.getSender(), "Command sender was null");
		if (checkPermissionAndNotifyIfUnauthorised(sender, sender.server)) {
			boolean newValue = msg.newValue;
			var statesToUpdate = Arrays.stream(msg.states)
				.filter(s -> NoCubes.smoothableHandler.isSmoothable(s) != newValue)
				.toArray(BlockState[]::new);
			// Guards against useless config reload and/or someone spamming these packets to the server and the server spamming all clients
			if (statesToUpdate.length == 0)
				// Somehow the client is out of sync, just notify them
				NoCubesNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new S2CUpdateSmoothable(newValue, msg.states));
			else {
				ctx.enqueueWork(() -> NoCubesConfig.Server.updateSmoothable(newValue, statesToUpdate));
				// Send back update to all clients
				NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CUpdateSmoothable(newValue, statesToUpdate));
			}
		}
		ctx.setPacketHandled(true);
	}

	public static boolean checkPermissionAndNotifyIfUnauthorised(Player player, @Nullable MinecraftServer connectedToServer) {
		if (connectedToServer != null && connectedToServer.isSingleplayerOwner(player.getGameProfile()))
			return true;
		if (player.hasPermissions(REQUIRED_PERMISSION_LEVEL))
			return true;
		ModUtil.warnPlayer(player, NoCubes.MOD_ID + ".command.changeSmoothableNoPermission");
		return false;
	}

}
