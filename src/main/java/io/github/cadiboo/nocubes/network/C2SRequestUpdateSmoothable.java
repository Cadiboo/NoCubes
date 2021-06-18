package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.network.NoCubesNetwork.REQUIRED_PERMISSION_LEVEL;

/**
 * @author Cadiboo
 */
public class C2SRequestUpdateSmoothable {

	private final BlockState state;
	private final boolean newValue;

	public C2SRequestUpdateSmoothable(final BlockState state, boolean newValue) {
		this.state = state;
		this.newValue = newValue;
	}

	public static C2SRequestUpdateSmoothable decode(PacketBuffer buffer) {
		BlockState state = BlockStateConverter.fromId(buffer.readVarInt());
		boolean newValue = buffer.readBoolean();
		return new C2SRequestUpdateSmoothable(state, newValue);
	}

	public static void encode(C2SRequestUpdateSmoothable msg, PacketBuffer buffer) {
		buffer.writeVarInt(BlockStateConverter.toId(msg.state));
		buffer.writeBoolean(msg.newValue);
	}

	public static void handle(C2SRequestUpdateSmoothable msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		ServerPlayerEntity sender = ctx.getSender();
		boolean hasPermission = sender.hasPermissions(REQUIRED_PERMISSION_LEVEL);
		if (hasPermission) {
			BlockState state = msg.state;
			boolean newValue = msg.newValue;
			// Guards against useless config reload and/or someone spamming these packets to the server and the server spamming all clients
			if (NoCubes.smoothableHandler.isSmoothable(state) != newValue) {
				ctx.enqueueWork(() -> NoCubesConfig.Server.updateSmoothable(newValue, state));
				// Send back update to all clients
				NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CUpdateSmoothable(state, newValue));
			} else
				// Somehow the client is out of sync, just notify them
				NoCubesNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new S2CUpdateSmoothable(state, newValue));
		} else
			sender.sendMessage(new TranslationTextComponent(NoCubes.MOD_ID + ".command.addSmoothableNoPermission"), Util.NIL_UUID);
		ctx.setPacketHandled(true);
	}

}
