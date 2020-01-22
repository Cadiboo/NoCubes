package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * Message from Server to Client to enable/disable TerrainCollisions.
 * <p>
 * Sets the TerrainCollisions to the new value and notifies the player.
 *
 * @author Cadiboo
 */
public final class S2CSetTerrainCollisions {

	private final boolean newValue;

	public S2CSetTerrainCollisions(final boolean newValue) {
		this.newValue = newValue;
	}

	public static void encode(final S2CSetTerrainCollisions msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeBoolean(msg.newValue);
	}

	public static S2CSetTerrainCollisions decode(final PacketBuffer packetBuffer) {
		return new S2CSetTerrainCollisions(packetBuffer.readBoolean());
	}

	/**
	 * Called on the Client.
	 */
	public static void handle(final S2CSetTerrainCollisions msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			final boolean newValue = msg.newValue;
			final String type = newValue ? "Enabled" : "Disabled";
			NoCubesConfig.Server.terrainCollisions = newValue;
			final ClientPlayerEntity player = Minecraft.getInstance().player;
			player.sendMessage(new TranslationTextComponent(MOD_ID + ".terrainCollisions" + type));
		}));
		context.setPacketHandled(true);
	}

}
