package io.github.cadiboo.nocubes.fabric;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.KeyMappings;
import io.github.cadiboo.nocubes.config.NoCubesConfigImpl;
import io.github.cadiboo.nocubes.network.C2SRequestUpdateSmoothable;
import io.github.cadiboo.nocubes.platform.IClientPlatform;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
		warnClientPlayerThatConfigIsNotImplemented();
		NoCubesConfigImpl.Client.updateRender(render);
	}

	@Override
	public void sendC2SRequestUpdateSmoothable(boolean newValue, BlockState[] states) {
		warnClientPlayerThatConfigIsNotImplemented();
		ClientPlayNetworking.send(new C2SRequestUpdateSmoothable(newValue, states));
	}

	static void warnClientPlayerThatConfigIsNotImplemented() {
		// Copied and tweaked from the bit of NoCubesNetworkClient.onJoinedServer that sends the notification.notInstalledOnServer message
		var message = "!!! The Fabric version of NoCubes does not have a config system yet - any changes you make will not be saved";
		ClientUtil.warnPlayer(message, KeyMappings.translate(KeyMappings.TOGGLE_SMOOTHABLE_BLOCK_TYPE));
	}

	@Override
	public void loadDefaultServerConfig() {
		NoCubesConfigImpl.loadDefaultServerConfig();
	}

	@Override
	public void receiveSyncedServerConfig(byte[] configData) {
		NoCubesConfigImpl.receiveSyncedServerConfig(configData);
	}

	@Override
	public Component clientConfigComponent() {
		return Component.literal("not implemented yet on Fabric");
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
