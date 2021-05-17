package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class Area implements AutoCloseable {

	private static final ThreadLocalArrayCache<BlockState[]> BLOCKS_CACHE = new ThreadLocalArrayCache<>(BlockState[]::new, array -> array.length);

	public final IBlockReader world;
	public final BlockPos start;
	public final BlockPos size;
	// Arrays are indexed [z][y][x] for cache locality
	private BlockState[] blocks;

	public /* for testing */ Area(IBlockReader world, BlockPos startInclusive, BlockPos size) {
		this.world = world;
		this.start = startInclusive.immutable();
		this.size = size.immutable();
	}

	public Area(IBlockReader world, BlockPos startInclusive, BlockPos size, MeshGenerator generator) {
		this.world = world;
		Vector3i negativeExtension = generator.getNegativeAreaExtension();
		Vector3i positiveExtension = generator.getPositiveAreaExtension();
		this.start = startInclusive.subtract(negativeExtension).immutable();
		this.size = new BlockPos(
			size.getX() + negativeExtension.getX() + positiveExtension.getX(),
			size.getY() + negativeExtension.getY() + positiveExtension.getY(),
			size.getZ() + negativeExtension.getZ() + positiveExtension.getZ()
		);
	}

	public BlockState[] getAndCacheBlocks() {
		if (blocks == null) {
			BlockState[] array = blocks = BLOCKS_CACHE.takeArray(this.numBlocks());
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

	public int numBlocks() {
		return size.getX() * size.getY() * size.getZ();
	}

	@Override
	public void close() {
	}

}
