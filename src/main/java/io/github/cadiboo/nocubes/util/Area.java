package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class Area implements AutoCloseable {

	private static final ThreadLocalArrayCache<BlockState[]> BLOCKS_CACHE = new ThreadLocalArrayCache<>(BlockState[]::new, array -> array.length);

	public final IBlockReader world;
	public final BlockPos start;
	public final BlockPos size;
	// Indexed [z][y][x] for cache locality
	private BlockState[] blocks;

	public Area(IBlockReader world, BlockPos startInclusive, BlockPos endExclusive) {
		this.world = world;
		this.start = startInclusive.immutable();
		this.size = endExclusive.subtract(startInclusive);
	}

	public Area(IBlockReader world, BlockPos startInclusive, BlockPos size, MeshGenerator generator) {
		this.world = world;
		this.start = startInclusive.subtract(generator.getNegativeAreaExtension()).immutable();
		this.size = size.offset(generator.getPositiveAreaExtension());
	}

	public BlockState[] getAndCacheBlocks() {
		if (blocks == null) {
			BlockState[] array = blocks = BLOCKS_CACHE.takeArray(this.getLength());
			int endX = start.getX() + size.getX();
			int endY = start.getY() + size.getY();
			int endZ = start.getZ() + size.getZ();
			IBlockReader world = this.world;
			if (world instanceof IWorldReader) {
				BlockPos endInclusive = new BlockPos(endX - 1, endY - 1, endZ - 1);
				ModUtil.traverseArea(start, endInclusive, new BlockPos.Mutable(), (IWorldReader) world, (state, pos, zyxIndex) -> array[zyxIndex] = state);
			} else {
				BlockPos.Mutable pos = new BlockPos.Mutable();
				int zyxIndex = 0;
				for (int z = start.getZ(); z < endZ; ++z)
					for (int y = start.getY(); y < endY; ++y)
						for (int x = start.getX(); x < endX; ++x, ++zyxIndex)
							array[zyxIndex] = world.getBlockState(pos.set(x, y, z));
			}
		}
		return blocks;
	}

	public int getLength() {
		return size.getZ() * size.getY() * size.getX();
	}

	@Override
	public void close() {
	}

}
