package io.github.cadiboo.nocubes.platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Implemented differently depending on what mod loader we are on
 * See also {@link IClientPlatform}
 * See also {@link IMixinPlatform}
 */
public interface IPlatform {
	ResourceLocation getRegistryName(Block block);
	boolean isPlant(BlockState state);
}
