package io.github.cadiboo.nocubes.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import static net.minecraft.block.Blocks.AIR;
import static net.minecraft.block.Blocks.GRASS;
import static net.minecraft.block.Blocks.GRASS_BLOCK;
import static net.minecraft.block.Blocks.PODZOL;
import static net.minecraft.block.Blocks.SNOW;
import static net.minecraft.block.Blocks.TALL_GRASS;
import static net.minecraft.state.properties.DoubleBlockHalf.LOWER;

/**
 * Holds references to commonly used BlockState instances in the mod for a tiny bit of optimisation
 * so that direct field access is used rather than getting the block and then the state.
 *
 * @author Cadiboo
 */
public final class StateHolder {

	public static final BlockState AIR_DEFAULT = AIR.getDefaultState();

	public static final BlockState SNOW_LAYER_DEFAULT = SNOW.getDefaultState();
	public static final BlockState GRASS_BLOCK_SNOWY = GRASS_BLOCK.getDefaultState().with(SnowyDirtBlock.SNOWY, true);
	public static final BlockState PODZOL_SNOWY = PODZOL.getDefaultState().with(SnowyDirtBlock.SNOWY, true);

	public static final BlockState GRASS_BLOCK_DEFAULT = GRASS_BLOCK.getDefaultState();

	public static final BlockState GRASS_PLANT_DEFAULT = GRASS.getDefaultState();
	public static final BlockState TALL_GRASS_PLANT_BOTTOM = TALL_GRASS.getDefaultState().with(DoublePlantBlock.HALF, LOWER);

	static {
		if (!((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).isLocked())
			throw new IllegalStateException("StateHolder accessed before all Blocks registered!");
	}

}
