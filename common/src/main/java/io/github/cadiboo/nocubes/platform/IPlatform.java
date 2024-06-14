package io.github.cadiboo.nocubes.platform;

import io.github.cadiboo.nocubes.util.IBlockStateSerializer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Implemented differently depending on what mod loader we are on
 * See also {@link IClientPlatform}
 * See also {@link IMixinPlatform}
 */
public interface IPlatform {
	IBlockStateSerializer blockStateSerializer();
	boolean isPlant(BlockState state);
}
