package io.github.cadiboo.nocubes.util;

import com.google.common.collect.ImmutableList;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * @author Cadiboo
 */
public class ModUtil {

	public static final BlockPos VEC_ZERO = new BlockPos(0, 0, 0);
	public static final BlockPos VEC_ONE = new BlockPos(1, 1, 1);
	public static final BlockPos VEC_TWO = new BlockPos(2, 2, 2);
	public static final BlockPos CHUNK_SIZE = new BlockPos(16, 16, 16);
	public static final Direction[] DIRECTIONS = Direction.values();
	public static final Lazy<Boolean> IS_DEVELOPER_WORKSPACE = Lazy.concurrentOf(() -> {
		final String target = System.getenv().get("target");
		if (target == null)
			return false;
		return target.contains("userdev");
	});
	public static final float FULLY_SMOOTHABLE = 1;
	public static final float NOT_SMOOTHABLE = -FULLY_SMOOTHABLE;

	public static ImmutableList<BlockState> getStates(Block block) {
		return block.getStateDefinition().getPossibleStates();
	}

	public interface Traverser {
		void accept(BlockState state, BlockPos.Mutable pos, int zyxIndex);
	}

	public static void traverseArea(Vector3i startInclusive, Vector3i endInclusive, BlockPos.Mutable currentPosition, IWorldReader world, Traverser func) {
		traverseArea(startInclusive.getX(), startInclusive.getY(), startInclusive.getZ(), endInclusive.getX(), endInclusive.getY(), endInclusive.getZ(), currentPosition, world, func);
	}

	/** Copied and tweaked from "https://github.com/Cadiboo/BiggerReactors/blob/1f0e0c48cdd16b8ecc0d2bc5f6c41db272dd8b7c/Phosphophyllite/src/main/java/net/roguelogix/phosphophyllite/util/Util.java#L76-L104". */
	public static void traverseArea(
		int startXInclusive, int startYInclusive, int startZInclusive,
		int endXInclusive, int endYInclusive, int endZInclusive,
		BlockPos.Mutable currentPosition, IWorldReader world, Traverser func
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
					// PalettedContainers are indexed [y][z][x] so lets iterate in that order (cache locality gain?)
					for (int y = sectionMinY; y < sectionMaxY; ++y) {
						int maskedY = y & 15;
						for (int z = sectionMinZ; z < sectionMaxZ; ++z) {
							int maskedZ = z & 15;
							for (int x = sectionMinX; x < sectionMaxX; ++x) {
								BlockState blockState = chunkSection == null ? air : chunkSection.getBlockState(x & 15, maskedY, maskedZ);
								currentPosition.set(x, y, z);
								int zyxIndex = (z - startZInclusive) * widthMulHeight + (y - startYInclusive) * width + (x - startXInclusive);
								func.accept(blockState, currentPosition, zyxIndex);
							}
						}
					}
				}
			}
		}
	}

	public static float getBlockDensity(Predicate<BlockState> isSmoothable, BlockState state) {
		return getBlockDensity(isSmoothable.test(state), state);
	}

	/**
	 * @return Positive density if the block is smoothable (and will be at least partially inside the isosurface)
	 */
	public static float getBlockDensity(boolean shouldSmooth, BlockState state) {
		if (!shouldSmooth)
			return NOT_SMOOTHABLE;
		if (isSnowLayer(state))
			// Snow layer, not the actual whole snow block
			return mapSnowHeight(state.getValue(SnowBlock.LAYERS));
		return FULLY_SMOOTHABLE;
	}

	/** Map snow height between 1-8 to between -1 and 1. */
	private static float mapSnowHeight(int value) {
		return -1 + (value - 1) * 0.25F;
	}

	public static boolean isSnowLayer(BlockState state) {
		return state.hasProperty(SnowBlock.LAYERS);
	}

	public static boolean isPlant(BlockState state) {
		Material material = state.getMaterial();
		return material == Material.PLANT ||
			material == Material.WATER_PLANT ||
			material == Material.REPLACEABLE_PLANT ||
			material == Material.REPLACEABLE_FIREPROOF_PLANT ||
			material == Material.REPLACEABLE_WATER_PLANT ||
			material == Material.BAMBOO_SAPLING ||
			material == Material.BAMBOO ||
			material == Material.VEGETABLE;
	}

	/**
	 * Assumes the array is indexed [z][y][x].
	 */
	public static int get3dIndexInto1dArray(int x, int y, int z, int xSize, int ySize) {
		return (xSize * ySize * z) + (xSize * y) + x;
	}

	public static FluidState getExtendedFluidState(World world, BlockPos pos) {
		// Check NoCubesConfig.Server.extendFluidsRange fluid states around pos and return a fluid state if there is one
		int extendRange = NoCubesConfig.Server.extendFluidsRange;
		assert extendRange > 0;

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		Chunk chunk = world.getChunk(chunkX, chunkZ);

		FluidState fluid = chunk.getFluidState(x, y, z);
		if (!fluid.isEmpty() || !NoCubes.smoothableHandler.isSmoothable(chunk.getBlockState(pos)))
			return fluid;

		// Check up
		fluid = chunk.getFluidState(x, y + 1, z);
		if (fluid.isSource())
			return fluid;

		// Check around
		for (int extendZ = z - extendRange; extendZ <= z + extendRange; ++extendZ) {
			for (int extendX = x - extendRange; extendX <= x + extendRange; ++extendX) {
				if (extendZ == z && extendX == x)
					continue; // We already checked ourself above

				if (chunkX != extendZ >> 4 || chunkZ != extendX >> 4) {
					chunkZ = extendZ >> 4;
					chunkX = extendX >> 4;
					chunk = world.getChunk(chunkX, chunkZ);
				}

				fluid = chunk.getFluidState(extendX, y, extendZ);
				if (fluid.isSource())
					return fluid;
			}
		}
		return Fluids.EMPTY.defaultFluidState();
	}

}
