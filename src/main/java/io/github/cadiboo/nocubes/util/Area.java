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
	private static final ThreadLocalArrayCache<FluidState[]> FLUIDS_CACHE = new ThreadLocalArrayCache<>(FluidState[]::new, array -> array.length);

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

	public FluidState[] getAndCacheFluids() {
		if (fluids == null) {
			int numBlocks = this.numBlocks();
			// TODO: This can be improved to use the blockstate[] and compute extended fluids efficiently
			FluidState[] array = fluids = FLUIDS_CACHE.takeArray(numBlocks);
//			BlockState[] andCacheBlocks = getAndCacheBlocks();
//			for (int i = 0; i < numBlocks; ++i)
//				array[i] = andCacheBlocks[i].getFluidState();
			BlockPos start = this.start;
			int startX = start.getX();
			int startY = start.getY();
			int startZ = start.getZ();
			BlockPos size = this.size;
			int endX = startX + size.getX();
			int endY = startY + size.getY();
			int endZ = startZ + size.getZ();
			IBlockReader world = this.world;
			BlockPos.Mutable pos = new BlockPos.Mutable();
			int zyxIndex = 0;
			for (int z = startZ; z < endZ; ++z)
				for (int y = startY; y < endY; ++y)
					for (int x = startX; x < endX; ++x, ++zyxIndex)
						array[zyxIndex] = world.getFluidState(pos.set(x, y, z));
		}
		return fluids;
	}

	public int numBlocks() {
		return size.getX() * size.getY() * size.getZ();
	}

	public int index(BlockPos relativePos) {
		return index(relativePos.getX(), relativePos.getY(), relativePos.getZ());
	}

	public int index(int relativeX, int relativeY, int relativeZ) {
		return ModUtil.get3dIndexInto1dArray(relativeX, relativeY, relativeZ, size.getX(), size.getY());
	}

	@Override
	public void close() {
	}

}
