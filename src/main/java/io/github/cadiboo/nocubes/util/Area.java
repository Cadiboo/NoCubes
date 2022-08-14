package io.github.cadiboo.nocubes.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class Area implements AutoCloseable {

	private static final ThreadLocalArrayCache<IBlockState[]> BLOCKS_CACHE = new ThreadLocalArrayCache<>(IBlockState[]::new, array -> array.length);

	public final IBlockAccess world;
	public final BlockPos start;
	public final BlockPos end;
	// Arrays are indexed [z][y][x] for cache locality
	private IBlockState[] blocks;

	public Area(IBlockAccess world, BlockPos startInclusive, BlockPos endExclusive) {
		this.world = world;
		this.start = startInclusive.toImmutable();
		this.end = endExclusive.toImmutable();
	}

	public IBlockState[] getAndCacheBlocks() {
		if (blocks == null) {
			IBlockState[] array = blocks = BLOCKS_CACHE.takeArray(this.getLength());
			if (world instanceof World)
				ModUtil.traverseArea(start, end.add(-1, -1, -1), new MutableBlockPos(), (World) world, (state, pos, zyxIndex) -> array[zyxIndex] = state);
			else {
				MutableBlockPos pos = new MutableBlockPos();
				IBlockAccess world = this.world;
				int maxZ = end.getZ();
				int maxY = end.getY();
				int maxX = end.getX();
				int zyxIndex = 0;
				for (int z = start.getZ(); z < maxZ; ++z)
					for (int y = start.getY(); y < maxY; ++y)
						for (int x = start.getX(); x < maxX; ++x, ++zyxIndex)
							array[zyxIndex] = world.getBlockState(pos.setPos(x, y, z));
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
