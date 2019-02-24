package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.ModContainer;

import static net.minecraft.block.material.Material.VINE;
import static net.minecraft.init.Blocks.BEDROCK;

/**
 * Util that is used on BOTH physical sides
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class ModUtil {

	public static final IIsSmoothable TERRAIN_SMOOTHABLE = ModUtil::shouldSmooth;
	public static final IIsSmoothable LEAVES_SMOOTHABLE = ModUtil::shouldSmoothLeaves;

	/**
	 * If the state should be smoothed
	 *
	 * @param state the state
	 * @return If the state should be smoothed
	 */
	public static boolean shouldSmooth(final IBlockState state) {
		return ModConfig.getSmoothableBlockStatesCache().contains(state);
	}

	/**
	 * If the state should be smoothed
	 *
	 * @param state the state
	 * @return If the state should be smoothed
	 */
	public static boolean shouldSmoothLeaves(final IBlockState state) {
		return /*ModConfig.smoothLeavesSeparate &&*/ state.getBlock() instanceof BlockLeaves;
	}

	/**
	 * @return negative density if the block is smoothable (inside the isosurface), positive if it isn't
	 */
	public static float getIndividualBlockDensity(final boolean shouldSmooth, final IBlockState state, final IBlockAccess cache, final BlockPos pos) {
		float density = 0;

		if (shouldSmooth) {
			final AxisAlignedBB box = state.getBoundingBox(cache, pos);
			final double boxHeight = box.maxY - box.minY;
			if (boxHeight >= 1) {
				density += boxHeight;
			} else {
				density -= 1 - boxHeight;
			}

			if (state.getBlock() == BEDROCK) {
				density += 0.0005F;
			}

		} else if (/*ModConfig.debug.connectToNormal && */(state.isNormalCube() || state.isBlockNormalCube())) {
			// OK OK OK OK OK LordPhrozen, I've done it (kinda)
			density += (float) ModConfig.smoothOtherBlocksAmount;
		} else if (state.getMaterial() == VINE) {
			density -= 0.75;
		} else {
			// Thanks VoidWalker. I'm pretty embarrassed.
			// Uncommenting 2 lines of code fixed the entire algorithm. (else density-=1)
			// I had been planning to uncomment and redo them after I fixed the algorithm.
			// If you hadn't taken the time to debug this, I might never have found the bug
			density -= 1;
		}

//		return density;
		return -density;
	}

	/**
	 * Give the point some (pseudo) random offset based on its location
	 *
	 * @param point the point
	 */
	public static void offsetVertex(Vec3 point) {
		long rand = (long) (point.x * 3129871.0D) ^ (long) point.y * 116129781L ^ (long) point.z;
		rand = rand * rand * 42317861L + rand * 11L;
		final float offsetAmount = ModConfig.getoffsetAmount();
		point.x += (((rand >> 16 & 15L) / 15.0F - 0.5F) * offsetAmount);
		point.y += (((rand >> 20 & 15L) / 15.0F - 0.5F) * offsetAmount);
		point.z += (((rand >> 24 & 15L) / 15.0F - 0.5F) * offsetAmount);
	}

	/**
	 * Ew
	 *
	 * @param modContainer the {@link ModContainer} for {@link NoCubes}
	 */
	public static void launchUpdateDaemon(ModContainer modContainer) {

		//TODO

	}

	public static int max(int... ints) {
		int max = 0;
		for (final int anInt : ints) {
			if (max < anInt) max = anInt;
		}
		return max;
	}

}
