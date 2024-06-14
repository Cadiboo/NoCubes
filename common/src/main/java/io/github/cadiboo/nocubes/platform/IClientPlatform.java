package io.github.cadiboo.nocubes.platform;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Consumer;

/**
 * Client-only version of {@link IPlatform} that contains references to classes that only exist on the minecraft client.
 */
public interface IClientPlatform {

	void updateClientVisuals(boolean render);

	boolean trySendC2SRequestUpdateSmoothable(LocalPlayer player, boolean newValue, BlockState[] states);

	Component clientConfigComponent();

	void forEachRenderLayer(BlockState state, Consumer<RenderType> action);

	List<BakedQuad> getQuads(BakedModel model, BlockState state, Direction direction, RandomSource random, Object modelData, RenderType layer);
}
