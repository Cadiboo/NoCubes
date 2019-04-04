package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.block.BlockSnowLayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.VersionChecker;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_NAME;
import static net.minecraft.init.Blocks.BEDROCK;
import static net.minecraft.init.Blocks.SNOW;
import static net.minecraftforge.fml.VersionChecker.getResult;

/**
 * Util that is used on BOTH physical sides
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class ModUtil {

	public static final IIsSmoothable TERRAIN_SMOOTHABLE = ModUtil::shouldSmoothTerrain;
	public static final IIsSmoothable LEAVES_SMOOTHABLE = ModUtil::shouldSmoothLeaves;
//	private static final Random RANDOM = new Random();
//	private static final Field configuration_definedConfigVersion = ReflectionUtil.getFieldOrCrash(Configuration.class, "definedConfigVersion");
//	private static final Field configManager_CONFIGS = ReflectionUtil.getFieldOrCrash(ConfigManager.class, "CONFIGS");

	/**
	 * If the state should be smoothed
	 *
	 * @param state the state
	 * @return If the state should be smoothed
	 */
	public static boolean shouldSmoothTerrain(final IBlockState state) {
		return ModConfig.getTerrainSmoothableBlockStatesCache().contains(state);
	}

	/**
	 * If the state should be smoothed
	 *
	 * @param state the state
	 * @return If the state should be smoothed
	 */
	public static boolean shouldSmoothLeaves(final IBlockState state) {
		return ModConfig.getLeavesSmoothableBlockStatesCache().contains(state);
	}

	/**
	 * @return negative density if the block is smoothable (inside the isosurface), positive if it isn't
	 */
	public static float getIndividualBlockDensity(final boolean shouldSmooth, final IBlockState state, final IBlockReader cache, final BlockPos pos) {
		if (state.getBlock() == SNOW && shouldSmooth) {
			final int value = state.get(BlockSnowLayer.LAYERS);
			if (value == 1) { // zero-height snow layer
				return 1;
			} else { // snow height between 0-8 to between -0.25F and -1
				return -((value - 1) * 0.125F);
			}
		} else if (shouldSmooth) {
			return state.getBlock() == BEDROCK ? -1.0005F : -1;
		} else if (state.isNormalCube() || state.isBlockNormalCube()) {
			return (float) ModConfig.smoothOtherBlocksAmount;
		} else {
			return 1;
		}
	}

	/**
	 * Give the vec3 some (pseudo) random offset based on its location.
	 * This code is from {link MathHelper#getCoordinateRandom} and Block#getOffset
	 *
	 * @param vec3 the vec3
	 */
	public static Vec3 offsetVertex(Vec3 vec3) {
		long rand = (long) (vec3.x * 3129871.0D) ^ (long) vec3.z * 116129781L ^ (long) vec3.y;
		rand = rand * rand * 42317861L + rand * 11;
		vec3.x += ((double) ((float) (rand >> 16 & 15L) / 15.0F) - 0.5D) * 0.5D;
		vec3.y += ((double) ((float) (rand >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D;
		vec3.z += ((double) ((float) (rand >> 24 & 15L) / 15.0F) - 0.5D) * 0.5D;
		return vec3;
	}

	@Deprecated
	public static double average(double... values) {
		if (values.length == 0) return 0;

		double total = 0L;

		for (double value : values) {
			total += value;
		}

		return total / values.length;
	}

	/**
	 * Ew
	 *
	 * @param modContainer the {@link ModContainer} for {@link NoCubes}
	 */
	public static void launchUpdateDaemon(ModContainer modContainer) {

		new Thread(() -> {
			WHILE:
			while (true) {

				final VersionChecker.CheckResult checkResult = getResult(modContainer.getModInfo());

				switch (checkResult.status) {
					default:
					case PENDING:
						break;
					case OUTDATED:
						try {
							BadAutoUpdater.update(modContainer, checkResult.target.toString(), "Cadiboo");
						} catch (Exception e) {
							throw new RuntimeException("Unable to update Mod", e);
						}
						break WHILE;
					case FAILED:
					case UP_TO_DATE:
					case AHEAD:
					case BETA:
					case BETA_OUTDATED:
						break WHILE;
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

		}, MOD_NAME + " Update Daemon").start();

	}

	public static int max(int... ints) {
		int max = 0;
		for (final int anInt : ints) {
			if (max < anInt) max = anInt;
		}
		return max;
	}

	//TODO: inline
	public static boolean isLiquidSource(final IFluidState state) {
		return state.isSource();
	}

	public static boolean isDeveloperWorkspace() {
		final String target = System.getenv().get("target");
		if (target == null) {
			return false;
		}
		return target.contains("userdev");
	}

}
