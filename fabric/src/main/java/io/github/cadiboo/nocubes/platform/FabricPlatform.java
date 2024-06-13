package io.github.cadiboo.nocubes.platform;

import io.github.cadiboo.nocubes.util.IBlockStateSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class FabricPlatform implements IPlatform {
	@Override
	public IBlockStateSerializer blockStateSerializer() {
		return new IBlockStateSerializer() {
			@Override
			public BlockState fromId(int id) {
				return null;
			}

			@Override
			public int toId(BlockState state) {
				return 0;
			}

			@Override
			public BlockState fromStringOrNull(String string) {
				return null;
			}

			@Override
			public String toString(BlockState state) {
				return null;
			}
		};
	}

	@Override
	public boolean isPlant(BlockState state) {
		return false;
	}

	@Override
	public void updateClientVisuals(boolean render) {
	}

	@Override
	public boolean trySendC2SRequestUpdateSmoothable(Player player, boolean newValue, BlockState[] states) {
		return false;
	}

	@Override
	public Component clientConfigComponent() {
		return Component.literal("client config");
	}
}
