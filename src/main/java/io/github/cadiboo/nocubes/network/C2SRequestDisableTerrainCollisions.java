package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
public final class C2SRequestDisableTerrainCollisions {

	public static void encode(final C2SRequestDisableTerrainCollisions msg, final PacketBuffer packetBuffer) {
	}

	public static C2SRequestDisableTerrainCollisions decode(final PacketBuffer packetBuffer) {
		return new C2SRequestDisableTerrainCollisions();
	}

	public static void handle(final C2SRequestDisableTerrainCollisions msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			final ServerPlayerEntity sender = context.getSender();
			if (sender == null) {
				return;
			}
			if (sender.server.getPlayerList().canSendCommands(sender.getGameProfile())) {
				// Config saving is async so set it now
				Config.terrainCollisions = false;
				ConfigHelper.setTerrainCollisions(false);
				NoCubes.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CDisableTerrainCollisions());
			} else {
				sender.sendMessage(new TranslationTextComponent(MOD_ID + ".disableTerrainCollisionsNoPermission"));
			}
		});
		context.setPacketHandled(true);
	}

}
