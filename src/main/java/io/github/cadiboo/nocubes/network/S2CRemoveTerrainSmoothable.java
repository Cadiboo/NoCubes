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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
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
	private static void handleOnClient(final S2CRemoveTerrainSmoothable msg) {
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
	}

}
