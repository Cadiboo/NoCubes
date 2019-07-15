package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public final class C2SRequestChangeExtendFluidsRange {

	private final ExtendFluidsRange newRange;

	public C2SRequestChangeExtendFluidsRange(final ExtendFluidsRange extendFluidsRange) {
		this.newRange = extendFluidsRange;
	}

	public static void encode(final C2SRequestChangeExtendFluidsRange msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.newRange.ordinal());
	}

	public static C2SRequestChangeExtendFluidsRange decode(final PacketBuffer packetBuffer) {
		return new C2SRequestChangeExtendFluidsRange(ExtendFluidsRange.VALUES[packetBuffer.readInt()]);
	}

	public static void handle(final C2SRequestChangeExtendFluidsRange msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			final ServerPlayerEntity sender = context.getSender();
			if (sender != null && sender.server.getPlayerList().canSendCommands(sender.getGameProfile())) {
				final ExtendFluidsRange newRange = msg.newRange;
				// Config saving is async so set it now
				Config.extendFluidsRange = newRange;
				ConfigHelper.setExtendFluidsRange(newRange);
				NoCubes.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CChangeExtendFluidsRange(newRange));
			}
		});
	}

}
