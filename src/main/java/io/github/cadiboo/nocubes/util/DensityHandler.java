package io.github.cadiboo.nocubes.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

/**
 * @author Cadiboo
 */
public class DensityHandler {

	/**
	 * Snow and stuff is not a full block, don't treat it as such.
	 */
	public float densityF(BlockState state, IBlockReader world, BlockPos pos) {
		// Check the field, not the method because we ASM the method
		if (state.isSolid)
			return 1;
		if (!state.getBlock().canCollide)
			return 0;
		VoxelShape shape = state.getBlock().getShape(state, world, pos, ISelectionContext.dummy());
		return (float) shape.getEnd(Direction.Axis.Y);
	}

	public char density(BlockState state, IBlockReader world, BlockPos pos) {
		float density = densityF(state, world, pos);
		if (density < 0)
			return 0;
		return (char) density;
	}

}
