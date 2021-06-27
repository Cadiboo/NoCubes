package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class Area implements AutoCloseable {

	private static final ThreadLocalArrayCache<IBlockState[]> BLOCKS_CACHE = new ThreadLocalArrayCache<>(IBlockState[]::new, array -> array.length);

	public final IBlockAccess world;
	public final BlockPos start;
	public final BlockPos size;
	// Indexed [z][y][x] for cache locality
	private IBlockState[] blocks;

	public Area(IBlockAccess world, BlockPos startInclusive, BlockPos endExclusive) {
		this.world = world;
		this.start = startInclusive.toImmutable();
		this.size = endExclusive.subtract(startInclusive);
	}

	public Area(IBlockAccess world, BlockPos startInclusive, BlockPos size, MeshGenerator generator) {
		this.world = world;
		this.start = startInclusive.subtract(generator.getNegativeAreaExtension()).toImmutable();
		this.size = size.add(generator.getPositiveAreaExtension());
	}

	public IBlockState[] getAndCacheBlocks() {
		if (blocks == null) {
			IBlockState[] array = blocks = BLOCKS_CACHE.takeArray(this.getLength());
			int endX = start.getX() + size.getX();
			int endY = start.getY() + size.getY();
			int endZ = start.getZ() + size.getZ();
			IBlockAccess world = this.world;
			if (world instanceof World) {
				BlockPos endInclusive = new BlockPos(endX - 1, endY - 1, endZ - 1);
				ModUtil.traverseArea(start, endInclusive, new MutableBlockPos(), (World) world, (state, pos, zyxIndex) -> array[zyxIndex] = state);
			} else {
				MutableBlockPos pos = new MutableBlockPos();
				int zyxIndex = 0;
				for (int z = start.getZ(); z < endZ; ++z)
					for (int y = start.getY(); y < endY; ++y)
						for (int x = start.getX(); x < endX; ++x, ++zyxIndex)
							array[zyxIndex] = world.getBlockState(pos.setPos(x, y, z));
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
