package io.github.cadiboo.nocubes.util;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;

import static net.minecraft.block.BlockDirt.SNOWY;
import static net.minecraft.block.BlockDirt.VARIANT;
import static net.minecraft.init.Blocks.DIRT;
import static net.minecraft.init.Blocks.GRASS;
import static net.minecraft.init.Blocks.SNOW_LAYER;
import static net.minecraft.init.Blocks.TALLGRASS;

/**
 * @author Cadiboo
 */
public final class StateHolder {

	public static final IBlockState SNOW_LAYER_DEFAULT = SNOW_LAYER.getDefaultState();
	public static final IBlockState GRASS_BLOCK_SNOWY = GRASS.getDefaultState().withProperty(SNOWY, true);
	public static final IBlockState PODZOL_SNOWY = DIRT.getDefaultState().withProperty(VARIANT, BlockDirt.DirtType.PODZOL).withProperty(SNOWY, true);

	public static final IBlockState GRASS_BLOCK_DEFAULT = GRASS.getDefaultState();

	public static final IBlockState GRASS_PLANT_DEFAULT = TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS);

	static {
		// Some really basic prevention against premature access. Probably doesn't take modded blocks into account
		if (!Bootstrap.isRegistered()) {
			throw new IllegalStateException("StateHolder accessed before Bootstrap registered!");
		}
	}

}
