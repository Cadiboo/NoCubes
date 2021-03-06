package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class Area implements AutoCloseable {

	private static final ThreadLocalArrayCache<BlockState[]> BLOCKS_CACHE = new ThreadLocalArrayCache<>(BlockState[]::new, array -> array.length);

	public final BlockPos start;
	public final BlockPos size;
	private final IBlockReader world;
	// Arrays are indexed [z][y][x] for cache locality
	private BlockState[] blocks;
	private FluidState[] fluids;

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
			BlockPos start = this.start;
			int startX = start.getX();
			int startY = start.getY();
			int startZ = start.getZ();
			BlockPos size = this.size;
			int endX = startX + size.getX();
			int endY = startY + size.getY();
			int endZ = startZ + size.getZ();
			IBlockReader world = this.world;
			if (world instanceof IWorldReader) {
				BlockPos endInclusive = new BlockPos(endX - 1, endY - 1, endZ - 1);
				ModUtil.traverseArea(start, endInclusive, new BlockPos.Mutable(), (IWorldReader) world, (state, pos, zyxIndex) -> array[zyxIndex] = state);
			} else {
				BlockPos.Mutable pos = new BlockPos.Mutable();
				int zyxIndex = 0;
				for (int z = startZ; z < endZ; ++z)
					for (int y = startY; y < endY; ++y)
						for (int x = startX; x < endX; ++x, ++zyxIndex)
							array[zyxIndex] = world.getBlockState(pos.set(x, y, z));
			}
		}
		return blocks;
	}

	public int numBlocks() {
		return ModUtil.length(size);
	}

	public int index(BlockPos relativePos) {
		int index = indexIfInsideCache(relativePos);
		if (index == -1)
			throw new IndexOutOfBoundsException("relativePos was " + relativePos + " but should have been within " + ModUtil.VEC_ZERO + " and " + size);
		return index;
	}

	public int indexIfInsideCache(BlockPos relativePos) {
		return indexIfInsideCache(relativePos.getX(), relativePos.getY(), relativePos.getZ());
	}

	public int indexIfInsideCache(int relativeX, int relativeY, int relativeZ) {
		BlockPos size = this.size;
		int sizeX = size.getX();
		int sizeY = size.getY();
		if (relativeX < 0 || relativeX >= sizeX || relativeY < 0 || relativeY >= sizeY || relativeZ < 0 || relativeZ >= size.getZ())
			return -1; // Outside cache
		return ModUtil.get3dIndexInto1dArray(relativeX, relativeY, relativeZ, sizeX, sizeY);
	}

	@Override
	public void close() {
	}

	public BlockState getBlockState(BlockPos.Mutable relativePos) {
		int index = indexIfInsideCache(relativePos);
		if (index == -1) {
			int x = relativePos.getX();
			int y = relativePos.getY();
			int z = relativePos.getZ();
			BlockState state = world.getBlockState(relativePos.move(start));
			relativePos.set(x, y, z);
			return state;
		}
		return getAndCacheBlocks()[index];
	}

//	public BlockState getBlockState(BlockPos.Mutable relativePos) {
//		return getBlockState(relativePos, relativePos.getX(), relativePos.getY(), relativePos.getZ());
//	}
//
//	public BlockState getBlockState(BlockPos.Mutable relativePos, Direction direction) {
//		return getBlockState(relativePos, relativePos.getX() + direction.getStepX(), relativePos.getY() + direction.getStepY(), relativePos.getZ() + direction.getStepZ());
//	}
//
//	private BlockState getBlockState(BlockPos.Mutable pos, int relativeX, int relativeY, int relativeZ) {
//		int index = indexIfInsideCache(relativeX, relativeY, relativeZ);
//		if (index == -1) {
//			int x = pos.getX();
//			int y = pos.getY();
//			int z = pos.getZ();
//			pos.set(relativeX, relativeY, relativeZ).move(start);
//			BlockState state = world.getBlockState(pos);
//			pos.set(x, y, z);
//			return state;
//		}
//		return getAndCacheBlocks()[index];
//	}

}
