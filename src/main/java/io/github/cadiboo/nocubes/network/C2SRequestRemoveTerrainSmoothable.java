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
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

/**
 * @author Cadiboo
 */
public final class C2SRequestRemoveTerrainSmoothable {

	private final int blockStateId;

	public C2SRequestRemoveTerrainSmoothable(final int blockStateId) {
		this.blockStateId = blockStateId;
	}

	public static void encode(final C2SRequestRemoveTerrainSmoothable msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.blockStateId);
	}

	public static C2SRequestRemoveTerrainSmoothable decode(final PacketBuffer packetBuffer) {
		return new C2SRequestRemoveTerrainSmoothable(packetBuffer.readInt());
	}

	public static void handle(final C2SRequestRemoveTerrainSmoothable msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			final ServerPlayerEntity sender = context.getSender();
			if (sender == null) {
				return;
			}
			if (sender.hasPermissionLevel(COMMAND_PERMISSION_LEVEL)) {
				final int blockStateId = msg.blockStateId;
				final BlockState blockState = Block.getStateById(blockStateId);
				if (blockState == StateHolder.AIR_DEFAULT) {
					NoCubes.LOGGER.error("Trying to remove invalid terrain smoothable blockstate: " + blockStateId);
					return;
				}
				ConfigHelper.removeTerrainSmoothable(blockState);
				NoCubes.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CRemoveTerrainSmoothable(blockStateId));
			} else {
				sender.sendMessage(new TranslationTextComponent(MOD_ID + ".removeTerrainSmoothableNoPermission"));
			}
		});
		context.setPacketHandled(true);
	}

}
