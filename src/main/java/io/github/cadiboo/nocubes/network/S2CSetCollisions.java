package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public class S2CSetCollisions {

	private final boolean newValue;

	public S2CSetCollisions(boolean newValue) {
		this.newValue = newValue;
	}

	public static S2CSetCollisions decode(PacketBuffer buffer) {
		boolean newValue = buffer.readBoolean();
		return new S2CSetCollisions(newValue);
	}

	public static void encode(S2CSetCollisions msg, PacketBuffer buffer) {
		buffer.writeBoolean(msg.newValue);
	}

	public static void handle(final S2CSetCollisions msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		NoCubesConfig.Server.collisionsEnabled = msg.newValue;
		ctx.setPacketHandled(true);
	}

}
