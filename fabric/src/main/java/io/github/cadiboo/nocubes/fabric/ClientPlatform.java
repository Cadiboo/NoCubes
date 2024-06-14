package io.github.cadiboo.nocubes.fabric;

import io.github.cadiboo.nocubes.platform.IClientPlatform;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Consumer;

public class ClientPlatform implements IClientPlatform {

	@Override
	public void updateClientVisuals(boolean render) {
	}

	@Override
	public boolean trySendC2SRequestUpdateSmoothable(LocalPlayer player, boolean newValue, BlockState[] states) {
		ModUtil.warnPlayer(player, "ERROR: FABRIC NETWORKING NOT IMPLEMENTED!");
		return false;
	}

	@Override
	public Component clientConfigComponent() {
		return Component.literal("client config (not implemented yet on Fabric)");
	}

	@Override
	public void forEachRenderLayer(BlockState state, Consumer<RenderType> action) {
		var layer = ItemBlockRenderTypes.getChunkRenderType(state);
		action.accept(layer);
	}

	@Override
	public List<BakedQuad> getQuads(BakedModel model, BlockState state, Direction direction, RandomSource random, Object modelData, RenderType layer) {
		return model.getQuads(state, direction, random);
	}
}
