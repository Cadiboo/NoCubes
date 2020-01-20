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
public final class C2SRequestSetTerrainSmoothable {

	private final int blockStateId;
	private final boolean newSmoothability;

	public C2SRequestSetTerrainSmoothable(final int blockStateId, final boolean newSmoothability) {
		this.blockStateId = blockStateId;
		this.newSmoothability = newSmoothability;
	}

	public C2SRequestSetTerrainSmoothable(final BlockState state, final boolean newSmoothability) {
		this(Block.getStateId(state), newSmoothability);
	}

	public static void encode(final C2SRequestSetTerrainSmoothable msg, final PacketBuffer packetBuffer) {
		packetBuffer
				.writeVarInt(msg.blockStateId)
				.writeBoolean(msg.newSmoothability);
	}

	public static C2SRequestSetTerrainSmoothable decode(final PacketBuffer packetBuffer) {
		return new C2SRequestSetTerrainSmoothable(packetBuffer.readVarInt(), packetBuffer.readBoolean());
	}

	public static void handle(final C2SRequestSetTerrainSmoothable msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			final ServerPlayerEntity sender = context.getSender();
			if (sender == null)
				return;
			final boolean newSmoothability = msg.newSmoothability;
			final String type = newSmoothability ? "add" : "remove";
			if (sender.hasPermissionLevel(COMMAND_PERMISSION_LEVEL)) {
				final int blockStateId = msg.blockStateId;
				final BlockState blockState = Block.getStateById(blockStateId);
				if (blockState == StateHolder.AIR_DEFAULT) {
					NoCubes.LOGGER.error("Trying to " + type + " invalid terrain smoothable blockstate: " + blockState);
					return;
				}
				ConfigHelper.setTerrainSmoothable(blockState, newSmoothability);
				NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSetTerrainSmoothable(blockStateId, newSmoothability));
			} else
				sender.sendMessage(new TranslationTextComponent(MOD_ID + "." + type + "TerrainSmoothableNoPermission"));
		});
		context.setPacketHandled(true);
	}

}
