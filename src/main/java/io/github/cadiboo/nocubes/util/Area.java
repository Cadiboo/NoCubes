package io.github.cadiboo.nocubes.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class Area implements AutoCloseable {

	private static final ThreadLocalArrayCache<BlockState[]> BLOCKS_CACHE = new ThreadLocalArrayCache<>(BlockState[]::new, array -> array.length);

	public final IBlockReader world;
	public final BlockPos start;
	public final BlockPos end;
	// Arrays are indexed [z][y][x] for cache locality
	private BlockState[] blocks;

	public Area(IBlockReader world, BlockPos startInclusive, BlockPos endExclusive) {
		this.world = world;
		this.start = startInclusive.immutable();
		this.end = endExclusive.immutable();
	}

	public BlockState[] getAndCacheBlocks() {
		if (blocks == null) {
			BlockState[] array = blocks = BLOCKS_CACHE.takeArray(this.getLength());
			if (world instanceof IWorldReader)
				ModUtil.traverseArea(start, end.offset(-1, -1, -1), new BlockPos.Mutable(), (IWorldReader) world, (state, pos, zyxIndex) -> array[zyxIndex] = state);
			else {
				BlockPos.Mutable pos = new BlockPos.Mutable();
				IBlockReader world = this.world;
				int maxZ = end.getZ();
				int maxY = end.getY();
				int maxX = end.getX();
				int zyxIndex = 0;
				for (int z = start.getZ(); z < maxZ; ++z)
					for (int y = start.getY(); y < maxY; ++y)
						for (int x = start.getX(); x < maxX; ++x, ++zyxIndex)
							array[zyxIndex] = world.getBlockState(pos.set(x, y, z));
			}
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

	@Override
	public void close() {
	}

}
