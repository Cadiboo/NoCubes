package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
public final class S2CEnableTerrainCollisions {

	public static void encode(final S2CEnableTerrainCollisions msg, final PacketBuffer packetBuffer) {
	}

	public static S2CEnableTerrainCollisions decode(final PacketBuffer packetBuffer) {
		return new S2CEnableTerrainCollisions();
	}

	public static void handle(final S2CEnableTerrainCollisions msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			Config.terrainCollisions = true;
			Minecraft.getInstance().player.sendMessage(new TranslationTextComponent(MOD_ID + ".terrainCollisions114"));
			Minecraft.getInstance().player.sendMessage(new TranslationTextComponent(MOD_ID + ".terrainCollisionsEnabled"));
		});
		context.setPacketHandled(true);
	}

}
