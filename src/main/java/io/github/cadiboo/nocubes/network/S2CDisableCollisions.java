package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
public final class S2CDisableCollisions {

	public static void encode(final S2CDisableCollisions msg, final PacketBuffer packetBuffer) {
	}

	public static S2CDisableCollisions decode(final PacketBuffer packetBuffer) {
		return new S2CDisableCollisions();
	}

	public static void handle(final S2CDisableCollisions msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		contextSupplier.get().enqueueWork(() -> {
			Config.terrainCollisions = false;
			Minecraft.getInstance().player.sendMessage(new TranslationTextComponent(MOD_ID + ".collisionsDisabled"));
		});
	}

}
