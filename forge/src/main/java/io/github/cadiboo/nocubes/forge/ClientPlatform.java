package io.github.cadiboo.nocubes.forge;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfigImpl;
import io.github.cadiboo.nocubes.network.C2SRequestUpdateSmoothable;
import io.github.cadiboo.nocubes.network.NoCubesNetworkForge;
import io.github.cadiboo.nocubes.platform.IClientPlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.PacketDistributor;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class ClientPlatform implements IClientPlatform {
	@Override
	public void updateClientVisuals(boolean render) {
		NoCubesConfigImpl.Client.updateRender(render);
	}

	@Override
	public void sendC2SRequestUpdateSmoothable(boolean newValue, BlockState[] states) {
		NoCubesNetworkForge.CHANNEL.send(new C2SRequestUpdateSmoothable(newValue, states), PacketDistributor.SERVER.noArg());
	}

	@Override
	public void loadDefaultServerConfig() {
		NoCubesConfigImpl.Hacks.loadDefaultServerConfig();
	}

	@Override
	public void receiveSyncedServerConfig(byte[] configData) {
		NoCubesConfigImpl.Hacks.receiveSyncedServerConfig(configData);
	}

	@Override
	public Component clientConfigComponent() {
		var configFile = new File(ConfigTracker.INSTANCE.getConfigFileName(NoCubes.MOD_ID, ModConfig.Type.CLIENT));
		return Component.literal(configFile.getName())
			.withStyle(ChatFormatting.UNDERLINE)
			.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, configFile.getAbsolutePath())));
	}

	@Override
	public void forEachRenderLayer(BlockState state, Consumer<RenderType> action) {
		var layers = ItemBlockRenderTypes.getRenderLayers(state);
		for (var layer : layers) {
			action.accept(layer);
		}
	}

	@Override
	public List<BakedQuad> getQuads(BakedModel model, BlockState state, Direction direction, RandomSource random, Object modelData, RenderType layer) {
		return model.getQuads(state, direction, random, (ModelData) modelData, layer);
	}
}
