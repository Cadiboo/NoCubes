package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.gui.toast.BlockStateToast;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.StateHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

/**
 * Message from Server to Client to set the smoothability of a BlockState
 * to a new value.
 * <p>
 * Validates that the BlockState is valid (not air). If so, sets the
 * smoothability of the BlockState to the new value and notifies the
 * player via a Toast.
 *
 * @author Cadiboo
 */
public final class S2CSetTerrainSmoothable {

	private static final Logger LOGGER = LogManager.getLogger();
	private final int blockStateId;
	private final boolean newSmoothability;

	public S2CSetTerrainSmoothable(final int blockStateId, final boolean newSmoothability) {
		this.blockStateId = blockStateId;
		this.newSmoothability = newSmoothability;
	}

	public S2CSetTerrainSmoothable(final BlockState blockState, final boolean newSmoothability) {
		this(Block.getStateId(blockState), newSmoothability);
	}

	public static void encode(final S2CSetTerrainSmoothable msg, final PacketBuffer packetBuffer) {
		packetBuffer
				.writeVarInt(msg.blockStateId)
				.writeBoolean(msg.newSmoothability);
	}

	public static S2CSetTerrainSmoothable decode(final PacketBuffer packetBuffer) {
		return new S2CSetTerrainSmoothable(packetBuffer.readVarInt(), packetBuffer.readBoolean());
	}

	/**
	 * Called on the Client.
	 */
	public static void handle(final S2CSetTerrainSmoothable msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> handleOnClient(msg)));
		context.setPacketHandled(true);
	}

	/**
	 * This method is not inlined because of the way lambdas work.
	 * If it were inlined it would be converted to a synthetic method in this class.
	 * Even though it will only ever be *called* on the client,
	 * because it is now a method in the class it will be checked when the class is loaded
	 * and will crash because it references client-only code.
	 */
	@OnlyIn(Dist.CLIENT)
	private static void handleOnClient(final S2CSetTerrainSmoothable msg) {
		final int blockStateId = msg.blockStateId;
		final boolean newSmoothability = msg.newSmoothability;
		final BlockState blockState = Block.getStateById(blockStateId);
		if (blockState == StateHolder.AIR_DEFAULT) {
			final String type = newSmoothability ? "add" : "remove";
			LOGGER.error("Trying to {} invalid terrain smoothable blockstate: {}", type, blockStateId);
			return;
		}
		blockState.nocubes_isTerrainSmoothable = newSmoothability;
		Minecraft.getInstance().getToastGui().add(new BlockStateToast.Terrain(blockState, newSmoothability));
		if (NoCubesConfig.Client.renderSmoothTerrain)
			ClientUtil.tryReloadRenderers();
	}

}
