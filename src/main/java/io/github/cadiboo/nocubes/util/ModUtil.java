package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.config.ModConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Random;

/**
 * Util that is used on BOTH physical sides
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class ModUtil {

	private static final Random RANDOM = new Random();

	/**
	 * Returns a random between the specified values;
	 *
	 * @param min the minimum value of the random number
	 * @param max the maximum value of the random number
	 * @return the random number
	 */
	public static double randomBetween(final int min, final int max) {
		return RANDOM.nextInt((max - min) + 1) + min;
	}

	/**
	 * Maps a value from one range to another range. Taken from https://stackoverflow.com/a/5732117
	 *
	 * @param input_start  the start of the input's range
	 * @param input_end    the end of the input's range
	 * @param output_start the start of the output's range
	 * @param output_end   the end of the output's range
	 * @param input        the input
	 * @return the newly mapped value
	 */
	public static double map(final double input_start, final double input_end, final double output_start, final double output_end, final double input) {
		final double input_range = input_end - input_start;
		final double output_range = output_end - output_start;

		return (((input - input_start) * output_range) / input_range) + output_start;
	}

	@Nonnull
	public static Side getLogicalSide(@Nonnull final World world) {
		if (world.isRemote) {
			return Side.CLIENT;
		} else {
			return Side.SERVER;
		}
	}

	public static void logLogicalSide(@Nonnull final Logger logger, @Nonnull final World world) {
		logger.info("Logical Side: " + getLogicalSide(world));
	}

	/**
	 * Logs all {@link Field Field}s and their values of an object with the {@link Level#INFO INFO} level.
	 *
	 * @param logger  the logger to dump on
	 * @param objects the objects to dump.
	 */
	public static void dump(@Nonnull final Logger logger, @Nonnull final Object... objects) {
		for (final Object object : objects) {
			final Field[] fields = object.getClass().getDeclaredFields();
			logger.info("Dump of " + object + ":");
			for (final Field field : fields) {
				try {
					field.setAccessible(true);
					logger.info(field.getName() + " - " + field.get(object));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.info("Error getting field " + field.getName());
					logger.info(e.getLocalizedMessage());
				}
			}
		}
	}

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
	 * @param pos   the position of the block
	 * @param cache the cache
	 * @return the density for the block
	 */
	public static float getBlockDensity(final BlockPos pos, final IBlockAccess cache) {

		float density = 0.0F;

		final MutableBlockPos mutablePos = new MutableBlockPos(pos);

		for (int x = 0; x < 2; ++x) {
			for (int y = 0; y < 2; ++y) {
				for (int z = 0; z < 2; ++z) {
					mutablePos.setPos(pos.getX() - x, pos.getY() - y, pos.getZ() - z);

					final IBlockState state = cache.getBlockState(mutablePos);

					if (ModUtil.shouldSmooth(state)) {
						density += 1;
						//					} else if (state.isNormalCube()) {
						//
						//					} else if (state.getMaterial() == Material.VINE) {
						//						density -= 0.75;
						// Thanks VoidWalker. I'm pretty embarrased.
						// Uncommenting 2 lines of code fixed the entire algorithm. (else density-=1)
						// I had been planning to uncomment and redo them after I fixed the algorithm.
						// If you hadn't taken the time to debug this, I might never have found the bug
					} else {
						density -= 1;
					}

					if (state.getBlock() == Blocks.BEDROCK) {
						density += 0.000000000000000000000000000000000000000000001f;
					}

				}
			}
		}

		return density;
	}

}
