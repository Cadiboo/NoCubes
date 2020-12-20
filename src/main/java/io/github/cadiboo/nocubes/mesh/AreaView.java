package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class AreaView {

	public final int xSize;
	public final int ySize;
	private final BlockState[] blockStates;
	private final FluidState[] fluidStates;

	public AreaView(World world, Vector3i start, Vector3i end, BlockPos.Mutable mutablePos) {
		this(world, start, end, mutablePos, false);
	}

	public AreaView(World world, Vector3i start, Vector3i end, BlockPos.Mutable mutablePos, boolean needsFluids) {
		xSize = end.getX() - start.getX();
		ySize = end.getY() - start.getY();
		int zSize = end.getZ() - start.getZ();
		int arraySize = (xSize + 1) * (ySize + 1) * (zSize + 1);
		blockStates = new BlockState[arraySize];
		traverseArea(start, end, mutablePos, world, (state, pos) -> {
			int x = pos.getX() - start.getX();
			int y = pos.getY() - start.getY();
			int z = pos.getZ() - start.getZ();
			blockStates[getIndex(x, y, z)] = state;
		});
		if (needsFluids) {
			FluidState[] fluidStates = this.fluidStates = new FluidState[arraySize];
			int index = 0;
			int maxX = start.getX() + xSize;
			int maxY = start.getY() + ySize;
			int maxZ = start.getZ() + zSize;
			for (int z = start.getZ(); z < maxZ; ++z) {
				for (int y = start.getY(); y < maxY; ++y) {
					for (int x = start.getX(); x < maxX; ++x, ++index) {
						mutablePos.setPos(x, y, z);
						FluidState state = world.getFluidState(mutablePos);
						fluidStates[index] = state;
					}
				}
			}
		} else
			fluidStates = null;
	}

	public BlockState[] getBlockStates() {
		return blockStates;
	}

	public FluidState[] getFluidStates() {
		return fluidStates;
	}

	public static void traverseArea(Vector3i start, Vector3i end, BlockPos.Mutable currentPosition, World world, BiConsumer<BlockState, BlockPos.Mutable> func) {
		traverseArea(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ(), currentPosition, world, func);
	}

	/** Copied and tweaked from "https://github.com/Cadiboo/BiggerReactors/blob/1f0e0c48cdd16b8ecc0d2bc5f6c41db272dd8b7c/Phosphophyllite/src/main/java/net/roguelogix/phosphophyllite/util/Util.java#L76-L104". */
	public static void traverseArea(
		int startX, int startY, int startZ,
		int endX, int endY, int endZ,
		BlockPos.Mutable currentPosition, World world, BiConsumer<BlockState, BlockPos.Mutable> func
	) {
		final BlockState air = Blocks.AIR.getDefaultState();
		int endXPlus1 = endX + 1;
		int endYPlus1 = endY + 1;
		int endZPlus1 = endZ + 1;
		int maxX = (endX + 16) & 0xFFFFFFF0;
		int maxY = (endY + 16) & 0xFFFFFFF0;
		int maxZ = (endZ + 16) & 0xFFFFFFF0;
		for (int blockChunkX = startX; blockChunkX < maxX; blockChunkX += 16) {
			int maskedBlockChunkX = blockChunkX & 0xFFFFFFF0;
			int maskedNextBlockChunkX = (blockChunkX + 16) & 0xFFFFFFF0;
			for (int blockChunkZ = startZ; blockChunkZ < maxZ; blockChunkZ += 16) {
				int maskedBlockChunkZ = blockChunkZ & 0xFFFFFFF0;
				int maskedNextBlockChunkZ = (blockChunkZ + 16) & 0xFFFFFFF0;
				int chunkX = blockChunkX >> 4;
				int chunkZ = blockChunkZ >> 4;
				@Nullable
				IChunk chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);
				@Nullable
				ChunkSection[] chunkSections = chunk == null ? null : chunk.getSections();
				for (int blockChunkY = startY; blockChunkY < maxY; blockChunkY += 16) {
					int maskedBlockChunkY = blockChunkY & 0xFFFFFFF0;
					int maskedNextBlockChunkY = (blockChunkY + 16) & 0xFFFFFFF0;
					int chunkSectionIndex = blockChunkY >> 4;
					@Nullable
					ChunkSection chunkSection = chunkSections == null ? null : chunkSections[chunkSectionIndex];
					int sectionMinX = Math.max(maskedBlockChunkX, startX);
					int sectionMinY = Math.max(maskedBlockChunkY, startY);
					int sectionMinZ = Math.max(maskedBlockChunkZ, startZ);
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

	public int getIndex(int x, int y, int z) {
		return ModUtil.get3dIndexInto1dArray(x, y, z, xSize, ySize);
	}

}
