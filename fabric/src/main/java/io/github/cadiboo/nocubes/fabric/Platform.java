package io.github.cadiboo.nocubes.fabric;

import io.github.cadiboo.nocubes.platform.IPlatform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Platform implements IPlatform {
	@Override
	public ResourceLocation getRegistryName(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block);
	}

	@Override
	public boolean isPlant(BlockState state) {
		return state.getBlock() instanceof BushBlock;
	}
}
