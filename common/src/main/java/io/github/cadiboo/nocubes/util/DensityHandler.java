package io.github.cadiboo.nocubes.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * @author Cadiboo
 */
public class DensityHandler {

	/**
	 * Snow and stuff is not a full block, don't treat it as such.
	 */
	public float densityF(BlockState state, BlockGetter world, BlockPos pos) {
		// Check the field, not the method because we ASM the method
		if (state.canOcclude())
			return 1;
		VoxelShape shape = state.getShape(world, pos);
		return (float) shape.max(Direction.Axis.Y);
	}

	public char density(BlockState state, BlockGetter world, BlockPos pos) {
		float density = densityF(state, world, pos);
		if (density < 0)
			return 0;
		return (char) density;
	}

}
