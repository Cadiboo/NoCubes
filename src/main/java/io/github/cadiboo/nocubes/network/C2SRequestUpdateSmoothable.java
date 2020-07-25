package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.smoothable.ServerSmoothableChangeHandler;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

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
		final BlockState state = BlockStateConverter.fromId(buffer.readVarInt());
		final boolean newValue = buffer.readBoolean();
		return new C2SRequestUpdateSmoothable(state, newValue);
	}

	public static void encode(C2SRequestUpdateSmoothable msg, PacketBuffer buffer) {
		buffer.writeVarInt(BlockStateConverter.toId(msg.state));
		buffer.writeBoolean(msg.newValue);
	}

	public static void handle(final C2SRequestUpdateSmoothable msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		ServerPlayerEntity sender = ctx.getSender();
		boolean hasPermission = sender.hasPermissionLevel(ServerSmoothableChangeHandler.REQUIRED_PERMISSION_LEVEL);
		if (hasPermission) {
			BlockState state = msg.state;
			boolean newValue = msg.newValue;
			if (NoCubes.smoothableHandler.isSmoothable(state) != newValue) {
				NoCubesConfig.Server.updateSmoothable(newValue, state);
				// Send back update to all clients
				NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CUpdateSmoothable(state, newValue));
			}
		} else
			sender.sendMessage(new TranslationTextComponent(NoCubes.MOD_ID + ".addSmoothableNoPermission"), Util.DUMMY_UUID);
		ctx.setPacketHandled(true);
	}

	public BlockState getState() {
		return state;
	}

}
