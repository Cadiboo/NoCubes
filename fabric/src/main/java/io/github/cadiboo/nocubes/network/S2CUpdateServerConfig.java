package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

record S2CUpdateServerConfig(
	byte[] data
) implements FabricPacket {
	public static final PacketType<S2CUpdateServerConfig> TYPE = PacketType.create(new ResourceLocation(NoCubes.MOD_ID, "updateconfig"), S2CUpdateServerConfig::read);

	public static S2CUpdateServerConfig read(FriendlyByteBuf buf) {
		return new S2CUpdateServerConfig(buf.readByteArray());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeByteArray(data);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
