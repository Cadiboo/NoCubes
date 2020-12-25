package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.gui.toast.BlockStateToast;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.util.DistExecutor;
import io.github.cadiboo.nocubes.util.INoCubesBlockState;
import io.github.cadiboo.nocubes.util.StateHolder;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Cadiboo
 */
public final class S2CRemoveTerrainSmoothable implements IMessage, IMessageHandler<S2CRemoveTerrainSmoothable, IMessage> {

	private /*final*/ int blockStateId;

	public S2CRemoveTerrainSmoothable(final int blockStateId) {
		this.blockStateId = blockStateId;
	}

	public S2CRemoveTerrainSmoothable() {
	}

	/**
	 * This method is not inlined because of the way lambdas work.
	 * If it were inlined it would be converted to a synthetic method in this class.
	 * Even though it will only ever be *called* on the client,
	 * because it is now a method in the class it will be checked when the class is loaded
	 * and will crash because it references client-only code.
	 */
	@SideOnly(Side.CLIENT)
	private static void handleOnClient(final S2CRemoveTerrainSmoothable msg) {
		final int blockStateId = msg.blockStateId;
		final IBlockState blockState = Block.getStateById(blockStateId);
		if (blockState == StateHolder.AIR_DEFAULT) {
			NoCubes.LOGGER.error("Trying to remove invalid terrain smoothable blockstate: " + blockStateId);
			return;
		}
		((INoCubesBlockState) blockState).nocubes_setTerrainSmoothable(false);
		Minecraft.getMinecraft().getToastGui().add(new BlockStateToast.RemoveTerrain(blockState, BlockPos.ORIGIN));
		if (Config.renderSmoothTerrain) {
			ClientUtil.tryReloadRenderers();
		}
	}

	@Override
	public IMessage onMessage(final S2CRemoveTerrainSmoothable msg, final MessageContext context) {
		Minecraft.getMinecraft().addScheduledTask(() -> DistExecutor.runWhenOn(Side.CLIENT, () -> () -> handleOnClient(msg)));
		return null;
	}

	@Override
	public void fromBytes(final ByteBuf buf) {
		this.blockStateId = new PacketBuffer(buf).readInt();
	}

	@Override
	public void toBytes(final ByteBuf buf) {
		new PacketBuffer(buf).writeInt(this.blockStateId);
	}

}
