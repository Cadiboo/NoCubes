package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public final class S2CChangeExtendFluidsRange {

	private final ExtendFluidsRange newRange;

	public S2CChangeExtendFluidsRange(final ExtendFluidsRange extendFluidsRange) {
		this.newRange = extendFluidsRange;
	}

	public static void encode(final S2CChangeExtendFluidsRange msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.newRange.ordinal());
	}

	public static S2CChangeExtendFluidsRange decode(final PacketBuffer packetBuffer) {
		return new S2CChangeExtendFluidsRange(ExtendFluidsRange.VALUES[packetBuffer.readInt()]);
	}

	public static void handle(final S2CChangeExtendFluidsRange msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> Config.extendFluidsRange = msg.newRange);
		context.setPacketHandled(true);
	}

}
