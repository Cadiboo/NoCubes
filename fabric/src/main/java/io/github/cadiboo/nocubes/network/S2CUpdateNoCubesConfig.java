package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class S2CUpdateNoCubesConfig implements FabricPacket {
	public static final PacketType<S2CUpdateNoCubesConfig> TYPE = PacketType.create(new ResourceLocation(NoCubes.MOD_ID, "syncconfig"), S2CUpdateNoCubesConfig::read);

	public S2CUpdateNoCubesConfig() {
	}

	public static S2CUpdateNoCubesConfig read(FriendlyByteBuf buf) {
		return new S2CUpdateNoCubesConfig();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
