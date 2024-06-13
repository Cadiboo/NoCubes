package io.github.cadiboo.nocubes.platform;

import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.util.IBlockStateSerializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class FabricPlatform implements IPlatform {

	@Override
	public String getPlatformName() {
		return "Fabric";
	}

	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	@Override
	public Color parseColor(String color) {
		return new Color(0, 0, 0, 0.6f);
	}

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
