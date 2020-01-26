package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.config.ConfigHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

/**
 * Message from Client to Server to request enabling/disabling TerrainCollisions.
 * <p>
 * Validates that the sender has the permission to perform this action and if they do
 * sets TerrainCollisions to the new value and notifies all clients (including the
 * one that sent this packet) of the new value.
 *
 * @author Cadiboo
 */
public final class C2SRequestSetTerrainCollisions {

	private final boolean newValue;

	public C2SRequestSetTerrainCollisions(final boolean newValue) {
		this.newValue = newValue;
	}

	public static void encode(final C2SRequestSetTerrainCollisions msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeBoolean(msg.newValue);
	}

	public static C2SRequestSetTerrainCollisions decode(final PacketBuffer packetBuffer) {
		return new C2SRequestSetTerrainCollisions(packetBuffer.readBoolean());
	}

	/**
	 * Called on the Server.
	 */
	public static void handle(final C2SRequestSetTerrainCollisions msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			final ServerPlayerEntity sender = context.getSender();
			if (sender == null)
				return;
			final boolean newValue = msg.newValue;
			final String type = newValue ? "enable" : "disable";
			if (sender.hasPermissionLevel(COMMAND_PERMISSION_LEVEL)) {
				ConfigHelper.setTerrainCollisions(newValue);
				NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSetTerrainCollisions(newValue));
			} else
				sender.sendMessage(new TranslationTextComponent(MOD_ID + "." + type + "TerrainCollisionsNoPermission"));
		});
		context.setPacketHandled(true);
	}

}
