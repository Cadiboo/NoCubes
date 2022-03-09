package io.github.cadiboo.nocubes.util;

import com.google.common.collect.ImmutableList;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;

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
	public static final float FULLY_SMOOTHABLE = 1;
	public static final float NOT_SMOOTHABLE = -FULLY_SMOOTHABLE;

	public static ImmutableList<BlockState> getStates(Block block) {
		return block.getStateDefinition().getPossibleStates();
	}

	public static int length(BlockPos size) {
		return size.getX() * size.getY() * size.getZ();
	}

	public static void warnPlayer(@Nullable PlayerEntity player, String translationKey, Object... formatArgs) {
		if (player != null)
			player.sendMessage(new TranslationTextComponent(translationKey, formatArgs).withStyle(TextFormatting.RED), Util.NIL_UUID);
		else
			LogManager.getLogger("NoCubes notification fallback").warn(I18n.get(translationKey, formatArgs));
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

	public static boolean isShortPlant(BlockState state) {
		Block block = state.getBlock();
		return block instanceof BushBlock && !(block instanceof DoublePlantBlock || block instanceof CropsBlock || block instanceof StemBlock);
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

	/**
	 * Searches neighbouring positions around a smooth block for a source fluid state.
	 * Makes the smooth block have the fluid "extended" into it.
	 *
	 * @return a fluid state that may not actually exist in the position
	 */
	public static FluidState getExtendedFluidState(World world, BlockPos pos) {
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
