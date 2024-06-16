package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public record S2CUpdateSmoothable(
	boolean newValue,
	BlockState[] states
) implements FabricPacket {
	public static final PacketType<S2CUpdateSmoothable> TYPE = PacketType.create(
		new ResourceLocation(NoCubes.MOD_ID, "updatesmoothable"),
		buf -> NoCubesNetwork.Serializer.decodeUpdateSmoothable(buf, S2CUpdateSmoothable::new)
	);

	@Override
	public void write(FriendlyByteBuf buf) {
		NoCubesNetwork.Serializer.encodeUpdateSmoothable(buf, newValue, states);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
