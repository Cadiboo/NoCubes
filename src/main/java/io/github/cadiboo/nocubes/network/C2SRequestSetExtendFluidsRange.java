package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

/**
 * Message from Client to Server to request setting the ExtendFluidsRange to a new value.
 *
 * Validates that the sender has the permission to perform this action and if they do
 * sets the ExtendFluidsRange to the new value and notifies all clients (including the
 * one that sent this packet) of the new value.
 *
 * @author Cadiboo
 */
public final class C2SRequestSetExtendFluidsRange {

	private final ExtendFluidsRange newRange;

	public C2SRequestSetExtendFluidsRange(final ExtendFluidsRange extendFluidsRange) {
		this.newRange = extendFluidsRange;
	}

	public static void encode(final C2SRequestSetExtendFluidsRange msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeVarInt(msg.newRange.ordinal());
	}

	public static C2SRequestSetExtendFluidsRange decode(final PacketBuffer packetBuffer) {
		return new C2SRequestSetExtendFluidsRange(ExtendFluidsRange.VALUES[packetBuffer.readVarInt()]);
	}

	/**
	 * Called on the Server.
	 */
	public static void handle(final C2SRequestSetExtendFluidsRange msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			final ServerPlayerEntity sender = context.getSender();
			if (sender == null)
				return;
			if (sender.hasPermissionLevel(COMMAND_PERMISSION_LEVEL)) {
				final ExtendFluidsRange newRange = msg.newRange;
				ConfigHelper.setExtendFluidsRange(newRange);
				NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSetExtendFluidsRange(newRange));
			} else
				sender.sendMessage(new TranslationTextComponent(MOD_ID + ".changeExtendFluidsRangeNoPermission"));
		});
		context.setPacketHandled(true);
	}

}
