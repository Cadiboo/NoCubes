package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.util.StateHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
public final class C2SRequestAddTerrainSmoothable {

	private final int blockStateId;

	public C2SRequestAddTerrainSmoothable(final int blockStateId) {
		this.blockStateId = blockStateId;
	}

	public static void encode(final C2SRequestAddTerrainSmoothable msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.blockStateId);
	}

	public static C2SRequestAddTerrainSmoothable decode(final PacketBuffer packetBuffer) {
		return new C2SRequestAddTerrainSmoothable(packetBuffer.readInt());
	}

	public static void handle(final C2SRequestAddTerrainSmoothable msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			final ServerPlayerEntity sender = context.getSender();
			if (sender == null) {
				return;
			}
			if (sender.server.getPlayerList().canSendCommands(sender.getGameProfile())) {
				final int blockStateId = msg.blockStateId;
				final BlockState blockState = Block.getStateById(blockStateId);
				if (blockState == StateHolder.AIR_DEFAULT) {
					NoCubes.LOGGER.error("Trying to add invalid terrain smoothable blockstate: " + blockStateId);
					return;
				}
				ConfigHelper.addTerrainSmoothable(blockState);
				NoCubes.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CAddTerrainSmoothable(blockStateId));
			} else {
				sender.sendMessage(new TranslationTextComponent(MOD_ID + ".addTerrainSmoothableNoPermission"));
			}
		});
		context.setPacketHandled(true);
	}

}
