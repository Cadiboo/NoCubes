package io.github.cadiboo.nocubes.fabric;

import io.github.cadiboo.nocubes.platform.IPlatform;
import io.github.cadiboo.nocubes.util.IBlockStateSerializer;
import net.minecraft.world.level.block.state.BlockState;

public class Platform implements IPlatform {
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
}
