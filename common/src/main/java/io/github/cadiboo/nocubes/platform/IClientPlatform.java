package io.github.cadiboo.nocubes.platform;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Client-only version of {@link IPlatform} that contains references to classes that only exist on the minecraft client.
 */
public interface IClientPlatform {

	void updateClientVisuals(boolean render);

	boolean trySendC2SRequestUpdateSmoothable(LocalPlayer player, boolean newValue, BlockState[] states);

	Component clientConfigComponent();
}
