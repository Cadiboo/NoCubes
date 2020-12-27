package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.IsSmoothable;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.NotImplementedException;

import static io.github.cadiboo.nocubes.network.NoCubesNetwork.executeIfPlayerHasPermission;

/**
 * @author Cadiboo
 */
public class C2SRequestSetSmoothable extends SetSmoothableBase implements IMessageHandler<C2SRequestSetSmoothable, IMessage> {

	public C2SRequestSetSmoothable(IsSmoothable isSmoothable, IBlockState state, boolean newValue) {
		super(isSmoothable, state, newValue);
	}

	public C2SRequestSetSmoothable() {
	}

	@Override
	public IMessage onMessage(C2SRequestSetSmoothable msg, MessageContext context) {
		executeIfPlayerHasPermission(context, "setSmoothable", () -> {
			IsSmoothable isSmoothable = msg.isSmoothable;
			IBlockState state = msg.state;
			boolean newValue = msg.newValue;
			if (isSmoothable.test(state) == newValue)
				return;

			if (isSmoothable != IsSmoothable.TERRAIN)
				throw new NotImplementedException("lazy");
			NoCubesConfig.Server.updateTerrainSmoothable(newValue, state);
			// Send back update to all clients
//			NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSetSmoothable(state, newValue));
			NoCubesNetwork.CHANNEL.sendToAll(new S2CSetSmoothable(isSmoothable, state, newValue));
		});
		return null;
	}

}
