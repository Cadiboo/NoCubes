package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static io.github.cadiboo.nocubes.client.RenderHelper.reloadAllChunks;

/**
 * @author Cadiboo
 */
public record S2CUpdateSmoothable(
	boolean newValue,
	BlockState[] states
) implements CustomPacketPayload {
	public static ResourceLocation ID = new ResourceLocation(NoCubes.MOD_ID, S2CUpdateSmoothable.class.getSimpleName().toLowerCase());

	public static void encode(S2CUpdateSmoothable msg, FriendlyByteBuf buffer) {
		buffer.writeBoolean(msg.newValue);
		BlockStateConverter.writeBlockStatesTo(buffer, msg.states);
	}

	public static S2CUpdateSmoothable decode(FriendlyByteBuf buffer) {
		return new S2CUpdateSmoothable(
			buffer.readBoolean(),
			BlockStateConverter.readBlockStatesFrom(buffer)
		);
	}

	public static void handle(S2CUpdateSmoothable msg, PlayPayloadContext ctx) {
		ctx.workHandler().submitAsync(() -> {
			handleOnServerThread(msg);
		});
	}

	static void handleOnServerThread(S2CUpdateSmoothable msg) {
		NoCubes.smoothableHandler.setSmoothable(msg.newValue, msg.states);
		reloadAllChunks("the server told us that the smoothness of some states changed");
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		encode(this, buffer);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

}
