package io.github.cadiboo.nocubes.fabric;

import io.github.cadiboo.nocubes.platform.IClientPlatform;
import io.github.cadiboo.nocubes.platform.IPlatform;
import io.github.cadiboo.nocubes.util.IBlockStateSerializer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class ClientPlatform implements IClientPlatform {

	@Override
	public void updateClientVisuals(boolean render) {
	}

	@Override
	public boolean trySendC2SRequestUpdateSmoothable(LocalPlayer player, boolean newValue, BlockState[] states) {
		return false;
	}

	@Override
	public Component clientConfigComponent() {
		return Component.literal("client config");
	}
}
