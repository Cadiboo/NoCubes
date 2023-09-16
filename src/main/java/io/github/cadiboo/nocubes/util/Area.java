package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.mesh.Mesher;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import javax.annotation.Nullable;

public class Area implements AutoCloseable {

	private static final ThreadLocalArrayCache<IBlockState[]> BLOCKS_CACHE = new ThreadLocalArrayCache<>(IBlockState[]::new, array -> array.length);

	public final BlockPos start;
	public final BlockPos size;
	public final IBlockAccess world;
	// Arrays are indexed [z][y][x] for cache locality
	private IBlockState[] blocks;

	public /* for testing */ Area(IBlockAccess world, BlockPos startInclusive, BlockPos size) {
		this.world = world;
		this.start = startInclusive.toImmutable();
		this.size = size.toImmutable();
	}

	public Area(IBlockAccess world, BlockPos startInclusive, BlockPos size, Mesher mesher) {
		this.world = world;
		Vec3i negativeExtension = mesher.getNegativeAreaExtension();
		Vec3i positiveExtension = mesher.getPositiveAreaExtension();
		this.start = startInclusive.subtract(negativeExtension).toImmutable();
		this.size = new BlockPos(
			size.getX() + negativeExtension.getX() + positiveExtension.getX(),
			size.getY() + negativeExtension.getY() + positiveExtension.getY(),
			size.getZ() + negativeExtension.getZ() + positiveExtension.getZ()
		);
	}

	public IBlockState[] getAndCacheBlocks() {
		if (blocks == null) {
			IBlockState[] array = blocks = BLOCKS_CACHE.takeArray(this.numBlocks());
			BlockPos start = this.start;
			int startX = start.getX();
			int startY = start.getY();
			int startZ = start.getZ();
			BlockPos size = this.size;
			int endX = startX + size.getX();
			int endY = startY + size.getY();
			int endZ = startZ + size.getZ();
			IBlockAccess world = this.world;
			if (world instanceof World) {
				BlockPos endInclusive = new BlockPos(endX - 1, endY - 1, endZ - 1);
				traverseWorld(start, endInclusive, new BlockPos.MutableBlockPos(), (World) world, (state, pos, zyxIndex) -> {
					array[zyxIndex] = state;
					return true;
				});
			} else {
				BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
				int zyxIndex = 0;
				for (int z = startZ; z < endZ; ++z)
					for (int y = startY; y < endY; ++y)
						for (int x = startX; x < endX; ++x, ++zyxIndex)
							array[zyxIndex] = world.getBlockState(pos.setPos(x, y, z));
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

	public IBlockState getBlockState(BlockPos.MutableBlockPos relativePos) {
		return getBlockState(indexIfInsideCache(relativePos), relativePos);
	}

	public IBlockState getBlockState(int index, BlockPos.MutableBlockPos relativePos) {
		if (index == -1) {
			int x = relativePos.getX();
			int y = relativePos.getY();
			int z = relativePos.getZ();
			IBlockState state = world.getBlockState(relativePos.setPos(x + start.getX(), y + start.getY(), z + start.getZ()));
			relativePos.setPos(x, y, z);
			return state;
		}
		return getAndCacheBlocks()[index];
	}

	public interface AreaTraverser {

		boolean accept(int x, int y, int z, int index, IBlockState state);

	}

	public void traverse(Vec3i skipStart, Vec3i skipEnd, AreaTraverser func) {
		BlockPos size = this.size;
		int depth = size.getZ();
		int height = size.getY();
		int width = size.getX();

		int minX = skipStart.getX();
		int minY = skipStart.getY();
		int minZ = skipStart.getZ();
		int maxX = size.getX() - skipStart.getX() - skipEnd.getX();
		int maxY = size.getY() - skipStart.getY() - skipEnd.getY();
		int maxZ = size.getZ() - skipStart.getZ() - skipEnd.getZ();

		IBlockState[] blocks = this.getAndCacheBlocks();
		int index = 0;
		for (int z = 0; z < depth; ++z) {
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x, ++index) {
					if (x < minX || x >= maxX)
						continue;
					if (y < minY || y >= maxY)
						continue;
					if (z < minZ || z >= maxZ)
						continue;
					if (!func.accept(x, y, z, index, blocks[index]))
						return;
				}
			}
		}
	}

	public interface WorldTraverser {

		boolean accept(IBlockState state, BlockPos.MutableBlockPos pos, int zyxIndex);

	}

	public static void traverseWorld(Vec3i startInclusive, Vec3i endInclusive, BlockPos.MutableBlockPos currentPosition, World world, WorldTraverser func) {
		traverseWorld(startInclusive.getX(), startInclusive.getY(), startInclusive.getZ(), endInclusive.getX(), endInclusive.getY(), endInclusive.getZ(), currentPosition, world, func);
	}

	public static void traverseWorld(
		int startXInclusive, int startYInclusive, int startZInclusive,
		int endXInclusive, int endYInclusive, int endZInclusive,
		BlockPos.MutableBlockPos currentPosition, World world, WorldTraverser func
	) {
		traverseWorld(startXInclusive, startYInclusive, startZInclusive, endXInclusive, endYInclusive, endZInclusive, currentPosition, world::getChunk, func);
	}

	public interface ChunkGetter {

		@Nullable
		Chunk getChunk(int chunkX, int chunkY);

	}

	/**
	 * Copied and tweaked from "https://github.com/BiggerSeries/Phosphophyllite/blob/a5c07fa7a5fd52db4aadcadb4b1a9273c5d65cda/src/main/java/net/roguelogix/phosphophyllite/util/Util.java#L62-L94".
	 */
	public static void traverseWorld(
		int startXInclusive, int startYInclusive, int startZInclusive,
		int endXInclusive, int endYInclusive, int endZInclusive,
		BlockPos.MutableBlockPos currentPosition, ChunkGetter world, WorldTraverser func
	) {
		final IBlockState air = Blocks.AIR.getDefaultState();
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
				@Nullable Chunk chunk = world.getChunk(chunkX, chunkZ);
				@Nullable ExtendedBlockStorage[] sections = chunk == null ? null : chunk.getBlockStorageArray();
				int chunkMinSection = chunk != null ? 0 : 0;
				for (int blockChunkY = startYInclusive; blockChunkY < maxY; blockChunkY += 16) {
					int maskedBlockChunkY = blockChunkY & 0xFFFFFFF0;
					int maskedNextBlockChunkY = (blockChunkY + 16) & 0xFFFFFFF0;
					int sectionIndex = (blockChunkY >> 4) - chunkMinSection;
//					@Nullable var section = sections == null ? null : sections[sectionIndex];
					// If sectionIndex is out of range we want to continue supplying air to the func
					// No clue how this will work with cubic chunks...
					@Nullable ExtendedBlockStorage section = sections == null || (sectionIndex < 0 || sectionIndex >= sections.length) ? null : sections[sectionIndex];
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
								IBlockState state = section == null ? air : section.get(x & 15, maskedY, maskedZ);
								currentPosition.setPos(x, y, z);
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
