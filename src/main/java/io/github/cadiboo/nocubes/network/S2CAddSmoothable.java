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
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public final class S2CAddSmoothable {

	private final int blockStateId;

	S2CAddSmoothable(final int blockStateId) {
		this.blockStateId = blockStateId;
	}

	public static void encode(final S2CAddSmoothable msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.blockStateId);
	}

	public static S2CAddSmoothable decode(final PacketBuffer packetBuffer) {
		return new S2CAddSmoothable(packetBuffer.readInt());
	}

	public static void handle(final S2CAddSmoothable msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		contextSupplier.get().enqueueWork(() -> {
			final int blockStateId = msg.blockStateId;
			final BlockState blockState = Block.getStateById(blockStateId);
			if (blockState == StateHolder.AIR_DEFAULT) {
				NoCubes.LOGGER.error("Trying to add invalid smoothable blockstate: " + blockStateId);
				return;
			}
			blockState.nocubes_isTerrainSmoothable = true;
			Minecraft.getInstance().getToastGui().add(new BlockStateToast.AddTerrain(blockState, BlockPos.ZERO));
			if (Config.renderSmoothTerrain) {
				ClientUtil.tryReloadRenderers();
			}
		});
	}

}
