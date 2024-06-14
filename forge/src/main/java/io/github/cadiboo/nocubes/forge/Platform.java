package io.github.cadiboo.nocubes.forge;

import io.github.cadiboo.nocubes.config.NoCubesConfigImpl;
import io.github.cadiboo.nocubes.platform.IPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.registries.ForgeRegistries;

public class Platform implements IPlatform {

	@Override
	public ResourceLocation getRegistryName(Block block) {
		return ForgeRegistries.BLOCKS.getKey(block);
	}

	@Override
	public boolean isPlant(BlockState state) {
		return state.getBlock() instanceof IPlantable;
	}

	@Override
	public void updateServerConfigSmoothable(boolean newValue, BlockState... states) {
		NoCubesConfigImpl.Server.updateSmoothable(newValue, states);
	}
}
