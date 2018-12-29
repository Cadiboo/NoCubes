package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Blocks;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import static io.github.cadiboo.nocubes.NoCubes.NO_CUBES_LOG;
import static io.github.cadiboo.nocubes.util.ModReference.CONFIG_VERSION;
import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModReference.MOD_NAME;

/**
 * Util that is used on BOTH physical sides
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class ModUtil {

	private static final Random RANDOM = new Random();
	//TODO: remove this backwards compatibility
	private static final Field configuration_definedConfigVersion = ReflectionHelper.findField(Configuration.class, "definedConfigVersion");
	private static final Field configManager_CONFIGS = ReflectionHelper.findField(ConfigManager.class, "CONFIGS");

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

		final PooledMutableBlockPos mutablePos = PooledMutableBlockPos.retain(pos);

		for (int x = 0; x < 2; ++x) {
			for (int y = 0; y < 2; ++y) {
				for (int z = 0; z < 2; ++z) {
					mutablePos.setPos(pos.getX() - x, pos.getY() - y, pos.getZ() - z);

					final IBlockState state = cache.getBlockState(mutablePos);

					if (ModUtil.shouldSmooth(state)) {
						density += state.getBoundingBox(cache, pos).maxY;
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

		mutablePos.release();

		return density;
	}

	/**
	 * Give the point some (pseudo) random offset based on its location
	 *
	 * @param point the point
	 * @return the point with offset applied
	 */
	public static void offsetVertex(Vec3 point) {
		// yay magic numbers
		/* Begin Click_Me's Code (Modified by Cadiboo) */
		long i = (long) (point.xCoord * 3129871.0D) ^ (long) point.yCoord * 116129781L ^ (long) point.zCoord;
		i = i * i * 42317861L + i * 11L;
		point.xCoord += (double) (((float) (i >> 16 & 15L) / 15.0F - 0.5F) * 0.5F);
		point.yCoord += (double) (((float) (i >> 20 & 15L) / 15.0F - 0.5F) * 0.5F);
		point.zCoord += (double) (((float) (i >> 24 & 15L) / 15.0F - 0.5F) * 0.5F);
		/* End Click_Me's Code (Modified by Cadiboo) */
	}

	public static double average(double... values) {
		if (values.length == 0) return 0;

		double total = 0L;

		for (double value : values) {
			total += value;
		}

		return total / values.length;
	}

	public static void offsetVertex(final float[] nv) {
		/* Begin Click_Me's Code (Modified by Cadiboo) */
		long i = (long) (nv[0] * 3129871.0D) ^ (long) nv[1] * 116129781L ^ (long) nv[2];
		i = i * i * 42317861L + i * 11L;
		nv[0] += (double) (((float) (i >> 16 & 15L) / 15.0F - 0.5F) * 0.5F);
		nv[1] += (double) (((float) (i >> 20 & 15L) / 15.0F - 0.5F) * 0.5F);
		nv[2] += (double) (((float) (i >> 24 & 15L) / 15.0F - 0.5F) * 0.5F);
		/* End Click_Me's Code (Modified by Cadiboo) */
	}

	public static void launchUpdateDaemon(ModContainer noCubesContainer) {

		new Thread(() -> {

			ComparableVersion outdatedVersion = null;
			boolean forceUpdate = false;

			WHILE:
			while (true) {

				final ForgeVersion.CheckResult checkResult = ForgeVersion.getResult(noCubesContainer);

				switch (checkResult.status) {
					default:
					case PENDING:
						break;
					case OUTDATED:
						outdatedVersion = checkResult.target;
						forceUpdate = ModConfig.shouldForceUpdate;
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

			final boolean developerEnvironment = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

			if (forceUpdate) {
				if (developerEnvironment) {
					NO_CUBES_LOG.info("Did not crash game because we're in a dev environment");
				} else {
					NoCubes.proxy.forceUpdate(outdatedVersion);
				}
			}

		}, MOD_NAME + " Update Daemon").start();

	}

	public static void fixConfig(final File configFile) {

		//Fix config file versioning while still using @Config
		final Map<String, Configuration> CONFIGS;
		try {
			//Map of full file path -> configuration
			CONFIGS = (Map<String, Configuration>) configManager_CONFIGS.get(null);
		} catch (IllegalAccessException e) {
			CrashReport crashReport = new CrashReport("Error getting field for ConfigManager.CONFIGS!", e);
			crashReport.makeCategory("Reflectively Accessing ConfigManager.CONFIGS");
			throw new ReportedException(crashReport);
		}

		//copied from ConfigManager
		Configuration config = CONFIGS.get(configFile.getAbsolutePath());
		if (config == null) {
			config = new Configuration(configFile, CONFIG_VERSION);
			config.load();
			CONFIGS.put(configFile.getAbsolutePath(), config);
		}

		try {
			configuration_definedConfigVersion.set(config, CONFIG_VERSION);
//			config.save();
//			config.load();
		} catch (IllegalAccessException | IllegalArgumentException e) {
			CrashReport crashReport = new CrashReport("Error setting value of field Configuration.definedConfigVersion!", e);
			crashReport.makeCategory("Reflectively Accessing Configuration.definedConfigVersion");
			throw new ReportedException(crashReport);
		}

		NO_CUBES_LOG.debug("fixing Config with version " + config.getDefinedConfigVersion() + ", current version is " + CONFIG_VERSION);
//		config.load();

		// reset config if old version
		if (!CONFIG_VERSION.equals(config.getLoadedConfigVersion())) {
			NO_CUBES_LOG.info("Resetting config file " + configFile.getName());
			//copied from Configuration
			File backupFile = new File(configFile.getAbsolutePath() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".version-" + config.getLoadedConfigVersion());
			try {
				FileUtils.copyFile(configFile, backupFile, true);
			} catch (IOException e) {
				NO_CUBES_LOG.error("We don't really care about this error", e);
			}
			configFile.delete();
			//refresh
			config.load();
			//save version
			config.save();
			//save default config
			ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
		}

		// fix Isosurface level (mod version 0.1.2?)
		{
			final double oldDefaultValue = 0.001D;
			Property isosurfaceLevel = config.get(Configuration.CATEGORY_GENERAL, "isosurfaceLevel", oldDefaultValue);
			if (isosurfaceLevel.isDefault())
				//edit in version 0.1.6: set to 1
//				isosurfaceLevel.set(0.0D);
				isosurfaceLevel.set(1.0D);
		}

		// fix Isosurface level (mod version 0.1.5?)
		{
			final double oldDefaultValue = 0.0D;
			Property isosurfaceLevel = config.get(Configuration.CATEGORY_GENERAL, "isosurfaceLevel", oldDefaultValue);
			if (isosurfaceLevel.isDefault())
				isosurfaceLevel.set(1.0D);
		}

		//save (Unnecessary?)
		config.save();
		//save
		ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
	}

}
