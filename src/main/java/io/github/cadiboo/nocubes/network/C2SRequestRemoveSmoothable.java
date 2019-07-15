package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.gui.toast.BlockStateToast;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.util.StateHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public final class C2SRequestRemoveSmoothable {

	private final int blockStateId;

	public C2SRequestRemoveSmoothable(final int blockStateId) {
		this.blockStateId = blockStateId;
	}

	public static void encode(final C2SRequestRemoveSmoothable msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.blockStateId);
	}

	public static C2SRequestRemoveSmoothable decode(final PacketBuffer packetBuffer) {
		return new C2SRequestRemoveSmoothable(packetBuffer.readInt());
	}

	public static void handle(final C2SRequestRemoveSmoothable msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			final ServerPlayerEntity sender = context.getSender();
			if (sender != null && sender.server.getPlayerList().canSendCommands(sender.getGameProfile())) {
				final int blockStateId = msg.blockStateId;
				final BlockState blockState = Block.getStateById(blockStateId);
				if (blockState == StateHolder.AIR_DEFAULT) {
					NoCubes.LOGGER.error("Trying to remove invalid smoothable blockstate: " + blockStateId);
					return;
				}
				ConfigHelper.removeTerrainSmoothable(blockState);
				NoCubes.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CRemoveSmoothable(blockStateId));
			}
		});
	}

}
