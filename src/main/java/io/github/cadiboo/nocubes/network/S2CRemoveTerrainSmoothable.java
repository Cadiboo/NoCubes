package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.gui.toast.BlockStateToast;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.util.StateHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public final class S2CRemoveTerrainSmoothable {

	private final int blockStateId;

	S2CRemoveTerrainSmoothable(final int blockStateId) {
		this.blockStateId = blockStateId;
	}

	public static void encode(final S2CRemoveTerrainSmoothable msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.blockStateId);
	}

	public static S2CRemoveTerrainSmoothable decode(final PacketBuffer packetBuffer) {
		return new S2CRemoveTerrainSmoothable(packetBuffer.readInt());
	}

	public static void handle(final S2CRemoveTerrainSmoothable msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			final int blockStateId = msg.blockStateId;
			final BlockState blockState = Block.getStateById(blockStateId);
			if (blockState == StateHolder.AIR_DEFAULT) {
				NoCubes.LOGGER.error("Trying to remove invalid terrain smoothable blockstate: " + blockStateId);
				return;
			}
			blockState.nocubes_isTerrainSmoothable = false;
			Minecraft.getInstance().getToastGui().add(new BlockStateToast.RemoveTerrain(blockState, BlockPos.ZERO));
			if (Config.renderSmoothTerrain) {
				ClientUtil.tryReloadRenderers();
			}
		});
		context.setPacketHandled(true);
	}

}
