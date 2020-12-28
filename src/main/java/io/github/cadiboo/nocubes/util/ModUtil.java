package io.github.cadiboo.nocubes.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

/**
 * @author Cadiboo
 */
public class ModUtil {

	public static final Lazy<Boolean> IS_DEVELOPER_WORKSPACE = Lazy.concurrentOf(() -> {
		final String target = System.getenv().get("target");
		if (target == null)
			return false;
		return target.contains("userdev");
	});

	public static void traverseArea(Vector3i startInclusive, Vector3i endInclusive, BlockPos.Mutable currentPosition, World world, BiConsumer<BlockState, BlockPos.Mutable> func) {
		traverseArea(startInclusive.getX(), startInclusive.getY(), startInclusive.getZ(), endInclusive.getX(), endInclusive.getY(), endInclusive.getZ(), currentPosition, world, func);
	}

	/** Copied and tweaked from "https://github.com/Cadiboo/BiggerReactors/blob/1f0e0c48cdd16b8ecc0d2bc5f6c41db272dd8b7c/Phosphophyllite/src/main/java/net/roguelogix/phosphophyllite/util/Util.java#L76-L104". */
	public static void traverseArea(
		int startXInclusive, int startYInclusive, int startZInclusive,
		int endXInclusive, int endYInclusive, int endZInclusive,
		BlockPos.Mutable currentPosition, World world, BiConsumer<BlockState, BlockPos.Mutable> func
	) {
		final BlockState air = Blocks.AIR.getDefaultState();
		int endXPlus1 = endXInclusive + 1;
		int endYPlus1 = endYInclusive + 1;
		int endZPlus1 = endZInclusive + 1;
		int maxX = (endXInclusive + 16) & 0xFFFFFFF0;
		int maxY = (endYInclusive + 16) & 0xFFFFFFF0;
		int maxZ = (endZInclusive + 16) & 0xFFFFFFF0;
		for (int blockChunkX = startXInclusive; blockChunkX < maxX; blockChunkX += 16) {
			int maskedBlockChunkX = blockChunkX & 0xFFFFFFF0;
			int maskedNextBlockChunkX = (blockChunkX + 16) & 0xFFFFFFF0;
			for (int blockChunkZ = startZInclusive; blockChunkZ < maxZ; blockChunkZ += 16) {
				int maskedBlockChunkZ = blockChunkZ & 0xFFFFFFF0;
				int maskedNextBlockChunkZ = (blockChunkZ + 16) & 0xFFFFFFF0;
				int chunkX = blockChunkX >> 4;
				int chunkZ = blockChunkZ >> 4;
				@Nullable
				IChunk chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);
				@Nullable
				ChunkSection[] chunkSections = chunk == null ? null : chunk.getSections();
				for (int blockChunkY = startYInclusive; blockChunkY < maxY; blockChunkY += 16) {
					int maskedBlockChunkY = blockChunkY & 0xFFFFFFF0;
					int maskedNextBlockChunkY = (blockChunkY + 16) & 0xFFFFFFF0;
					int chunkSectionIndex = blockChunkY >> 4;
//					@Nullable
//					ChunkSection chunkSection = chunkSections == null ? null : chunkSections[chunkSectionIndex];
					// If chunkSectionIndex is out of range we want to continue supplying air to the func
					// No clue how this will work with cubic chunks...
					@Nullable
					ChunkSection chunkSection = chunkSections == null || (chunkSectionIndex < 0 || chunkSectionIndex >= chunkSections.length) ? null : chunkSections[chunkSectionIndex];
					int sectionMinX = Math.max(maskedBlockChunkX, startXInclusive);
					int sectionMinY = Math.max(maskedBlockChunkY, startYInclusive);
					int sectionMinZ = Math.max(maskedBlockChunkZ, startZInclusive);
					int sectionMaxX = Math.min(maskedNextBlockChunkX, endXPlus1);
					int sectionMaxY = Math.min(maskedNextBlockChunkY, endYPlus1);
					int sectionMaxZ = Math.min(maskedNextBlockChunkZ, endZPlus1);
					for (int x = sectionMinX; x < sectionMaxX; ++x) {
						int maskedX = x & 15;
						for (int y = sectionMinY; y < sectionMaxY; ++y) {
							int maskedY = y & 15;
							for (int z = sectionMinZ; z < sectionMaxZ; ++z) {
								currentPosition.setPos(x, y, z);
								BlockState blockState = chunkSection == null ? air : chunkSection.getBlockState(maskedX, maskedY, z & 15);
								func.accept(blockState, currentPosition);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @return Positive density if the block is smoothable (and will be at least partially inside the isosurface)
	 */
	public static float getBlockDensity(boolean shouldSmooth, BlockState state) {
		if (!shouldSmooth)
			return -1;
		if (state.getBlock() == Blocks.SNOW) { // Snow layer
			int value = state.get(SnowBlock.LAYERS);
			// Map snow height between 0-8 to between -0.25F and -1
			return -((value - 1) * 0.125F);
		}
		return 1;
	}

	/**
	 * Assumes the array is indexed [z][y][x].
	 */
	public static int get3dIndexInto1dArray(int x, int y, int z, int xSize, int ySize) {
		return (xSize * ySize * z) + (xSize * y) + x;
	}

}
