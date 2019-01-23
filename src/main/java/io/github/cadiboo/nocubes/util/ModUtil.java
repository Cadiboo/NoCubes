package io.github.cadiboo.nocubes.util;

import com.google.common.collect.Lists;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.github.cadiboo.nocubes.NoCubes.NO_CUBES_LOG;
import static io.github.cadiboo.nocubes.util.ModReference.CONFIG_VERSION;
import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModReference.MOD_NAME;
import static net.minecraft.block.material.Material.VINE;
import static net.minecraft.init.Blocks.BEDROCK;

/**
 * Util that is used on BOTH physical sides
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class ModUtil {

	private static final Random RANDOM = new Random();

	private static final Field configuration_definedConfigVersion = ReflectionUtil.getFieldOrCrash(Configuration.class, "definedConfigVersion");

	private static final Field configManager_CONFIGS = ReflectionUtil.getFieldOrCrash(ConfigManager.class, "CONFIGS");

	/**
	 * If the state should be smoothed
	 *
	 * @param state the state
	 * @return If the state should be smoothed
	 */
	public static boolean shouldSmooth(final IBlockState state) {
		return ModConfig.getSmoothableBlockStatesCache().contains(state);
	}

	public static boolean shouldSmoothLeaves(final IBlockState state) {
		return ModConfig.smoothLeavesSeparate && state.getBlock() instanceof BlockLeaves;
	}

	public static float getIndividualBlockDensity(final boolean shouldSmooth, final IBlockState state, final IBlockAccess cache, final BlockPos pos) {
		float density = 0;

		if (shouldSmooth) {
			final AxisAlignedBB box = state.getBoundingBox(cache, pos);
			density += box.maxY - box.minY;
		} else if (/*ModConfig.debug.connectToNormal && */(state.isNormalCube() || state.isBlockNormalCube())) {
			// NO OP
			// OK OK OK OK OK LordPhrozen, I've done it (kinda)
		} else if (state.getMaterial() == VINE) {
			density -= 0.75;
		} else {
			// Thanks VoidWalker. I'm pretty embarrassed.
			// Uncommenting 2 lines of code fixed the entire algorithm. (else density-=1)
			// I had been planning to uncomment and redo them after I fixed the algorithm.
			// If you hadn't taken the time to debug this, I might never have found the bug
			density -= 1;
		}

		if (state.getBlock() == BEDROCK) {
			density += 0.000000000000000000000000000000000000000000001f;
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
	 * @param noCubesContainer the {@link ModContainer} for {@link NoCubes}
	 */
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
					// FIXME BIG TODO remove this once I'm done with beta releases and start doing actual releases
					// FIXME This is evil and not good
					{
						final String fuck9minecraft = "Your version of NoCubes (" + outdatedVersion + ") is outdated! Download the latest version from https://cadiboo.github.io/projects/nocubes/download/";
						for (int i = 0; i < 10; i++)
							NoCubes.NO_CUBES_LOG.error(fuck9minecraft);
						CrashReport crashReport = new CrashReport(fuck9minecraft, new RuntimeException(fuck9minecraft));
						FMLCommonHandler.instance().raiseException(new ReportedException(crashReport), fuck9minecraft, true);
						FMLCommonHandler.instance().exitJava(0, false);
					}
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

	public static void crashIfNotDev(final Exception e) {
		if (FMLLaunchHandler.isDeobfuscatedEnvironment()) {
			NO_CUBES_LOG.error("FIX THIS ERROR NOW!", e);
			return;
		}
		final CrashReport crashReport = new CrashReport("Error in mod " + MOD_ID, e);
		throw new ReportedException(crashReport);
	}

	public static List<Vec3> getBlankVertexList() {
		return Lists.newArrayList(getBlankVertexArray());
	}

	public static Vec3[] getBlankVertexArray() {
		return new Vec3[]{
				new Vec3(0, 0, 0),
				new Vec3(1, 0, 0),
				new Vec3(1, 0, 1),
				new Vec3(0, 0, 1),
				new Vec3(0, 1, 0),
				new Vec3(1, 1, 0),
				new Vec3(1, 1, 1),
				new Vec3(0, 1, 1)
		};
	}

}
