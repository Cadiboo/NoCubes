package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.util.StateHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

/**
 * Message from Client to Server to request setting the smoothability of a BlockState
 * to a new value.
 * <p>
 * Validates that the sender has the permission to perform this action and that the
 * BlockState is valid (not air). If they do and the BlockState is valid sets the
 * smoothability of the BlockState to the new value and notifies all clients
 * (including the one that sent this packet) of the new value.
 *
 * @author Cadiboo
 */
public final class C2SRequestSetTerrainSmoothable {

	private static final Logger LOGGER = LogManager.getLogger();
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

	/**
	 * Called on the Server.
	 */
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
					LOGGER.error("Trying to {} invalid terrain smoothable blockstate: {}", type, blockState);
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
