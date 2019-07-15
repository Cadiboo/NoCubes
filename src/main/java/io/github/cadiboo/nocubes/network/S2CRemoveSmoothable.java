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
public final class S2CRemoveSmoothable {

	private final int blockStateId;

	S2CRemoveSmoothable(final int blockStateId) {
		this.blockStateId = blockStateId;
	}

	public static void encode(final S2CRemoveSmoothable msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.blockStateId);
	}

	public static S2CRemoveSmoothable decode(final PacketBuffer packetBuffer) {
		return new S2CRemoveSmoothable(packetBuffer.readInt());
	}

	public static void handle(final S2CRemoveSmoothable msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		contextSupplier.get().enqueueWork(() -> {
			final int blockStateId = msg.blockStateId;
			final BlockState blockState = Block.getStateById(blockStateId);
			if (blockState == StateHolder.AIR_DEFAULT) {
				NoCubes.LOGGER.error("Trying to remove invalid smoothable blockstate: " + blockStateId);
				return;
			}
			blockState.nocubes_isTerrainSmoothable = false;
			Minecraft.getInstance().getToastGui().add(new BlockStateToast.RemoveTerrain(blockState, BlockPos.ZERO));
			if (Config.renderSmoothTerrain) {
				ClientUtil.tryReloadRenderers();
			}
		});
	}

}
