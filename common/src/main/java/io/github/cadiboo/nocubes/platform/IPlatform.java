package io.github.cadiboo.nocubes.platform;

import io.github.cadiboo.nocubes.util.IBlockStateSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public interface IPlatform {
	IBlockStateSerializer blockStateSerializer();

	boolean isPlant(BlockState state);

	void updateClientVisuals(boolean render);

	boolean trySendC2SRequestUpdateSmoothable(Player player, boolean newValue, BlockState[] states);

	Component clientConfigComponent();
}
