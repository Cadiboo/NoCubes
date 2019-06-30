package io.github.cadiboo.nocubes.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.SnowyDirtBlock;

import static net.minecraft.block.Blocks.GRASS;
import static net.minecraft.block.Blocks.GRASS_BLOCK;
import static net.minecraft.block.Blocks.PODZOL;
import static net.minecraft.block.Blocks.SNOW;
import static net.minecraft.block.Blocks.TALL_GRASS;
import static net.minecraft.state.properties.DoubleBlockHalf.LOWER;

/**
 * @author Cadiboo
 */
public final class StateHolder {

	public static final BlockState SNOW_LAYER_DEFAULT = SNOW.getDefaultState();
	public static final BlockState GRASS_BLOCK_SNOWY = GRASS_BLOCK.getDefaultState().with(SnowyDirtBlock.SNOWY, true);
	public static final BlockState PODZOL_SNOWY = PODZOL.getDefaultState().with(SnowyDirtBlock.SNOWY, true);

	public static final BlockState GRASS_BLOCK_DEFAULT = GRASS_BLOCK.getDefaultState();

	public static final BlockState GRASS_PLANT_DEFAULT = GRASS.getDefaultState();
	public static final BlockState TALL_GRASS_PLANT_BOTTOM = TALL_GRASS.getDefaultState().with(DoublePlantBlock.HALF, LOWER);

	static {
		// Some really basic prevention against premature access. Probably doesn't take modded blocks into account
//		if (!Bootstrap.isRegistered()) {
//			throw new IllegalStateException("StateHolder accessed before Bootstrap registered!");
//		}
	}

}
