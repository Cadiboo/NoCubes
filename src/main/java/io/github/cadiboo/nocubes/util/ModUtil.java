package io.github.cadiboo.nocubes.util;

import com.google.common.collect.ImmutableList;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
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

	public static int length(BlockPos size) {
		return size.getX() * size.getY() * size.getZ();
	}

	public interface Traverser {
		void accept(BlockState state, MutableBlockPos pos, int zyxIndex);
	}

	public static void traverseArea(Vec3i startInclusive, Vec3i endInclusive, MutableBlockPos currentPosition, LevelReader world, Traverser func) {
		traverseArea(startInclusive.getX(), startInclusive.getY(), startInclusive.getZ(), endInclusive.getX(), endInclusive.getY(), endInclusive.getZ(), currentPosition, world, func);
	}

	/** Copied and tweaked from "https://github.com/BiggerSeries/Phosphophyllite/blob/a5c07fa7a5fd52db4aadcadb4b1a9273c5d65cda/src/main/java/net/roguelogix/phosphophyllite/util/Util.java#L62-L94". */
	public static void traverseArea(
		int startXInclusive, int startYInclusive, int startZInclusive,
		int endXInclusive, int endYInclusive, int endZInclusive,
		MutableBlockPos currentPosition, LevelReader world, Traverser func
	) {
		final var air = Blocks.AIR.defaultBlockState();
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
				@Nullable var chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);
				@Nullable var sections = chunk == null ? null : chunk.getSections();
				int chunkMinSection = chunk != null ? chunk.getMinSection() : 0;
				for (int blockChunkY = startYInclusive; blockChunkY < maxY; blockChunkY += 16) {
					int maskedBlockChunkY = blockChunkY & 0xFFFFFFF0;
					int maskedNextBlockChunkY = (blockChunkY + 16) & 0xFFFFFFF0;
					int sectionIndex = (blockChunkY >> 4) - chunkMinSection;
//					@Nullable var section = sections == null ? null : sections[sectionIndex];
					// If sectionIndex is out of range we want to continue supplying air to the func
					// No clue how this will work with cubic chunks...
					@Nullable var section = sections == null || (sectionIndex < 0 || sectionIndex >= sections.length) ? null : sections[sectionIndex];
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
								var state = section == null ? air : section.getBlockState(x & 15, maskedY, maskedZ);
								currentPosition.set(x, y, z);
								int zyxIndex = (z - startZInclusive) * widthMulHeight + (y - startYInclusive) * width + (x - startXInclusive);
								func.accept(state, currentPosition, zyxIndex);
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
			return mapSnowHeight(state.getValue(SnowLayerBlock.LAYERS));
		return FULLY_SMOOTHABLE;
	}

	/** Map snow height between 1-8 to between -1 and 1. */
	private static float mapSnowHeight(int value) {
		return -1 + (value - 1) * 0.25F;
	}

	public static boolean isSnowLayer(BlockState state) {
		return state.hasProperty(SnowLayerBlock.LAYERS);
	}

	public static boolean isShortPlant(BlockState state) {
		Block block = state.getBlock();
		return block instanceof BushBlock && !(block instanceof DoublePlantBlock || block instanceof CropBlock || block instanceof StemBlock);
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

	public static FluidState getExtendedFluidState(Level world, BlockPos pos) {
		// Check NoCubesConfig.Server.extendFluidsRange fluid states around pos and return a fluid state if there is one
		int extendRange = NoCubesConfig.Server.extendFluidsRange;
		assert extendRange > 0;

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		LevelChunk chunk = world.getChunk(chunkX, chunkZ);

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
