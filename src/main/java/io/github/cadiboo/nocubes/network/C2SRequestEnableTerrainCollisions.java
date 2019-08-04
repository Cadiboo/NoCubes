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
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

/**
 * @author Cadiboo
 */
public final class C2SRequestEnableTerrainCollisions {

	public static void encode(final C2SRequestEnableTerrainCollisions msg, final PacketBuffer packetBuffer) {
	}

	public static C2SRequestEnableTerrainCollisions decode(final PacketBuffer packetBuffer) {
		return new C2SRequestEnableTerrainCollisions();
	}

	public static void handle(final C2SRequestEnableTerrainCollisions msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			final ServerPlayerEntity sender = context.getSender();
			if (sender == null) {
				return;
			}
			if (sender.hasPermissionLevel(COMMAND_PERMISSION_LEVEL)) {
				// Config saving is async so set it now
				Config.terrainCollisions = true;
				ConfigHelper.setTerrainCollisions(true);
				NoCubes.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CEnableTerrainCollisions());
			} else {
				sender.sendMessage(new TranslationTextComponent(MOD_ID + ".enableTerrainCollisionsNoPermission"));
			}
		});
		context.setPacketHandled(true);
	}

}
