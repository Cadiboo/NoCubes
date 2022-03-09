package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.mesh.Mesher;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;

import javax.annotation.Nullable;

public class Area implements AutoCloseable {

	private static final ThreadLocalArrayCache<BlockState[]> BLOCKS_CACHE = new ThreadLocalArrayCache<>(BlockState[]::new, array -> array.length);

	public final BlockPos start;
	public final BlockPos size;
	private final IBlockReader world;
	// Arrays are indexed [z][y][x] for cache locality
	private BlockState[] blocks;

	public /* for testing */ Area(IBlockReader world, BlockPos startInclusive, BlockPos size) {
		this.world = world;
		this.start = startInclusive.immutable();
		this.size = size.immutable();
	}

	public Area(IBlockReader world, BlockPos startInclusive, BlockPos size, Mesher mesher) {
		this.world = world;
		Vector3i negativeExtension = mesher.getNegativeAreaExtension();
		Vector3i positiveExtension = mesher.getPositiveAreaExtension();
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
				traverse(start, endInclusive, new BlockPos.Mutable(), (IWorldReader) world, (state, pos, zyxIndex) -> {
					array[zyxIndex] = state;
					return true;
				});
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

	public interface Traverser {
		boolean accept(BlockState state, BlockPos.Mutable pos, int zyxIndex);
	}

	public static void traverse(Vector3i startInclusive, Vector3i endInclusive, BlockPos.Mutable currentPosition, IWorldReader world, Traverser func) {
		traverse(startInclusive.getX(), startInclusive.getY(), startInclusive.getZ(), endInclusive.getX(), endInclusive.getY(), endInclusive.getZ(), currentPosition, world, func);
	}

	public static void traverse(
		int startXInclusive, int startYInclusive, int startZInclusive,
		int endXInclusive, int endYInclusive, int endZInclusive,
		BlockPos.Mutable currentPosition, IWorldReader world, Traverser func
	) {
		traverse(startXInclusive, startYInclusive, startZInclusive, endXInclusive, endYInclusive, endZInclusive, currentPosition, (x, z) -> world.getChunk(x, z, ChunkStatus.EMPTY, false), func);
	}

	public interface ChunkGetter {
		@Nullable IChunk getChunk(int chunkX, int chunkY);
	}

	/** Copied and tweaked from "https://github.com/BiggerSeries/Phosphophyllite/blob/a5c07fa7a5fd52db4aadcadb4b1a9273c5d65cda/src/main/java/net/roguelogix/phosphophyllite/util/Util.java#L62-L94". */
	public static void traverse(
		int startXInclusive, int startYInclusive, int startZInclusive,
		int endXInclusive, int endYInclusive, int endZInclusive,
		BlockPos.Mutable currentPosition, ChunkGetter world, Traverser func
	) {
		final BlockState air = Blocks.AIR.defaultBlockState();
		int endXPlus1 = endXInclusive + 1;
		int endYPlus1 = endYInclusive + 1;
		int endZPlus1 = endZInclusive + 1;
		int maxX = (endXInclusive + 16) & 0xFFFFFFF0;
		int maxY = (endYInclusive + 16) & 0xFFFFFFF0;
		int maxZ = (endZInclusive + 16) & 0xFFFFFFF0;
		int width = endXPlus1 - startXInclusive;
		int height = endYPlus1 - startYInclusive;
		int widthMulHeight = width * height;
		// ChunkSource implementations are indexed [z][x] so iterate in that order (cache locality gain?)
		for (int blockChunkZ = startZInclusive; blockChunkZ < maxZ; blockChunkZ += 16) {
			int maskedBlockChunkZ = blockChunkZ & 0xFFFFFFF0;
			int maskedNextBlockChunkZ = (blockChunkZ + 16) & 0xFFFFFFF0;
			for (int blockChunkX = startXInclusive; blockChunkX < maxX; blockChunkX += 16) {
				int maskedBlockChunkX = blockChunkX & 0xFFFFFFF0;
				int maskedNextBlockChunkX = (blockChunkX + 16) & 0xFFFFFFF0;
				int chunkX = blockChunkX >> 4;
				int chunkZ = blockChunkZ >> 4;
				@Nullable IChunk chunk = world.getChunk(chunkX, chunkZ);
				@Nullable ChunkSection[] sections = chunk == null ? null : chunk.getSections();
				int chunkMinSection = 0;//chunk != null ? chunk.getMinSection() : 0;
				for (int blockChunkY = startYInclusive; blockChunkY < maxY; blockChunkY += 16) {
					int maskedBlockChunkY = blockChunkY & 0xFFFFFFF0;
					int maskedNextBlockChunkY = (blockChunkY + 16) & 0xFFFFFFF0;
					int sectionIndex = (blockChunkY >> 4) - chunkMinSection;
//					@Nullable var section = sections == null ? null : sections[sectionIndex];
					// If sectionIndex is out of range we want to continue supplying air to the func
					// No clue how this will work with cubic chunks...
					@Nullable ChunkSection section = sections == null || (sectionIndex < 0 || sectionIndex >= sections.length) ? null : sections[sectionIndex];
					int sectionMinX = Math.max(maskedBlockChunkX, startXInclusive);
					int sectionMinY = Math.max(maskedBlockChunkY, startYInclusive);
					int sectionMinZ = Math.max(maskedBlockChunkZ, startZInclusive);
					int sectionMaxX = Math.min(maskedNextBlockChunkX, endXPlus1);
					int sectionMaxY = Math.min(maskedNextBlockChunkY, endYPlus1);
					int sectionMaxZ = Math.min(maskedNextBlockChunkZ, endZPlus1);
					// PalettedContainers are indexed [y][z][x] so iterate in that order (cache locality gain?)
					for (int y = sectionMinY; y < sectionMaxY; ++y) {
						int maskedY = y & 15;
						for (int z = sectionMinZ; z < sectionMaxZ; ++z) {
							int maskedZ = z & 15;
							for (int x = sectionMinX; x < sectionMaxX; ++x) {
								BlockState state = section == null ? air : section.getBlockState(x & 15, maskedY, maskedZ);
								currentPosition.set(x, y, z);
								int zyxIndex = (z - startZInclusive) * widthMulHeight + (y - startYInclusive) * width + (x - startXInclusive);
								if (!func.accept(state, currentPosition, zyxIndex))
									return;
							}
						}
					}
				}
			}
		}
	}

}
