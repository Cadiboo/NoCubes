package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record S2CUpdateServerConfig(
	byte[] data
) implements FabricPacket {
	public static final PacketType<S2CUpdateServerConfig> TYPE = PacketType.create(
		new ResourceLocation(NoCubes.MOD_ID, "syncconfig"),
		buf -> NoCubesNetwork.Serializer.decodeS2CUpdateServerConfig(buf, S2CUpdateServerConfig::new)
	);

	@Override
	public void write(FriendlyByteBuf buf) {
		NoCubesNetwork.Serializer.encodeS2CUpdateServerConfig(buf, data);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
