package net.minecraft.block.state;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.state.AbstractStateHolder;
import net.minecraft.state.IProperty;

public class BlockState extends AbstractStateHolder<Block, IBlockState> implements IBlockState {

	public BlockState(Block blockIn, ImmutableMap<IProperty<?>, Comparable<?>> properties) {
		super(blockIn, properties);
	}

	public Block getBlock() {
		return this.object;
	}

	// ******** NoCubes Start ******** //

	private boolean nocubes_isTerrainSmoothable = false;

	/**
	 * does NOT take into account whether NoCubes is enabled or not
	 */
	@Override
	public boolean nocubes_isTerrainSmoothable() {
		return this.nocubes_isTerrainSmoothable;
	}

	@Override
	public void nocubes_setTerrainSmoothable(final boolean newIsTerrainSmoothable) {
		this.nocubes_isTerrainSmoothable = newIsTerrainSmoothable;
	}

}
