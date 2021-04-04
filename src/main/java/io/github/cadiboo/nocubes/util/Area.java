package io.github.cadiboo.nocubes.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class Area {

	public final IWorldReader world;
	public final BlockPos start;
	public final BlockPos end;
	// Arrays are indexed [z][y][x] for cache locality
	private BlockState[] blocks;

	public Area(IWorldReader world, BlockPos start, BlockPos end) {
		this.world = world;
		this.start = start.immutable();
		this.end = end.immutable();
	}

	public BlockState[] getAndCacheBlocks() {
		if (blocks == null) {
			blocks = new BlockState[this.getLength()];
			ModUtil.traverseArea(start, end, new BlockPos.Mutable(), world, (state, pos, index) -> blocks[index] = state);
		}
		return blocks;
	}

	public int getLength() {
		// I could do Math.abs but I don't think I'll ever have the params passed in reversed
		int depth = end.getZ() - start.getZ();
		int height = end.getY() - start.getY();
		int width = end.getX() - start.getX();
		return depth * height * width;
	}

}
