package io.github.cadiboo.nocubes.util;

import net.minecraft.block.BlockDirtSnowy;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;

import static net.minecraft.init.Blocks.GRASS;
import static net.minecraft.init.Blocks.GRASS_BLOCK;
import static net.minecraft.init.Blocks.PACKED_ICE;
import static net.minecraft.init.Blocks.PODZOL;
import static net.minecraft.init.Blocks.SNOW;

/**
 * @author Cadiboo
 */
public final class StateHolder {

	public static final IBlockState SNOW_LAYER_DEFAULT = SNOW.getDefaultState();
	public static final IBlockState GRASS_BLOCK_SNOWY = GRASS_BLOCK.getDefaultState().with(BlockDirtSnowy.SNOWY, true);
	public static final IBlockState PODZOL_SNOWY = PODZOL.getDefaultState().with(BlockDirtSnowy.SNOWY, true);

	public static final IBlockState GRASS_BLOCK_DEFAULT = GRASS_BLOCK.getDefaultState();

	public static final IBlockState GRASS_PLANT_DEFAULT = GRASS.getDefaultState();

	static {
		// Some really basic prevention against premature access. Probably doesn't take modded blocks into account
		if (!Bootstrap.isRegistered()) {
			throw new IllegalStateException("StateHolder accessed before Bootstrap registered!");
		}
	}

}
