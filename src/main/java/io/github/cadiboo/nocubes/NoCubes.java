package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.IProxy;
import net.minecraft.crash.CrashReport;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static io.github.cadiboo.nocubes.util.ModReference.ACCEPTED_MINECRAFT_VERSIONS;
import static io.github.cadiboo.nocubes.util.ModReference.CERTIFICATE_FINGERPRINT;
import static io.github.cadiboo.nocubes.util.ModReference.CLIENT_PROXY_CLASS;
import static io.github.cadiboo.nocubes.util.ModReference.CONFIG_VERSION;
import static io.github.cadiboo.nocubes.util.ModReference.DEPENDENCIES;
import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModReference.MOD_NAME;
import static io.github.cadiboo.nocubes.util.ModReference.SERVER_PROXY_CLASS;
import static io.github.cadiboo.nocubes.util.ModReference.VERSION;

/**
 * Our main mod class
 *
 * @author Cadiboo
 */
@Mod(
		modid = MOD_ID,
		name = MOD_NAME,
		version = VERSION,
		acceptedMinecraftVersions = ACCEPTED_MINECRAFT_VERSIONS,
		updateJSON = "https://raw.githubusercontent.com/Cadiboo/NoCubes/master/update.json",
		dependencies = DEPENDENCIES,
		clientSideOnly = true,
		acceptableRemoteVersions = VERSION,
		certificateFingerprint = CERTIFICATE_FINGERPRINT

)
public final class NoCubes {

	//FIXME TODO someone remind me to work on my memory management, I've got some massive memory leaks

	public static final Logger NO_CUBES_LOG = LogManager.getLogger(MOD_ID);
	private static final Logger LOGGER = LogManager.getLogger();

	@Instance(MOD_ID)
	public static NoCubes instance;

	@SidedProxy(serverSide = SERVER_PROXY_CLASS, clientSide = CLIENT_PROXY_CLASS)
	public static IProxy proxy;

	/**
	 * Run before anything else. <s>Read your config, create blocks, items, etc, and register them with the GameRegistry</s>
	 *
	 * @param event the event
	 * @see ForgeModContainer#preInit(FMLPreInitializationEvent)
	 */
	@EventHandler
	public void preInit(final FMLPreInitializationEvent event) {
		LOGGER.debug("preInit");
		fixConfig(event.getSuggestedConfigurationFile());
		proxy.logPhysicalSide(NO_CUBES_LOG);

		launchUpdateDaemon(Loader.instance().activeModContainer());

	}

	private void launchUpdateDaemon(ModContainer noCubesContainer) {

		new Thread(() -> {

			ComparableVersion outdatedVersion = null;
			boolean forceUpdate = false;

			WHILE:
			while (true) {

				final CheckResult checkResult = ForgeVersion.getResult(noCubesContainer);

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

	//TODO: remove this backwards compatibility
	private static final Field configuration_definedConfigVersion = ReflectionHelper.findField(Configuration.class, "definedConfigVersion");
	private static final Field configManager_CONFIGS = ReflectionHelper.findField(ConfigManager.class, "CONFIGS");

	private void fixConfig(final File configFile) {

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

		LOGGER.debug("fixing Config with version " + config.getDefinedConfigVersion() + ", current version is " + CONFIG_VERSION);
//		config.load();

		// reset config if old version
		if (!CONFIG_VERSION.equals(config.getLoadedConfigVersion())) {
			LOGGER.info("Resetting config file " + configFile.getName());
			//copied from Configuration
			File backupFile = new File(configFile.getAbsolutePath() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".version-" + config.getLoadedConfigVersion());
			try {
				FileUtils.copyFile(configFile, backupFile, true);
			} catch (IOException e) {
				LOGGER.error("We don't really care about this error", e);
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

	/**
	 * Do your mod setup. Build whatever data structures you care about. Register recipes, send FMLInterModComms messages to other mods.
	 *
	 * @param event the event
	 */
	@EventHandler
	public void init(final FMLInitializationEvent event) {
		LOGGER.debug("init");
	}

	/**
	 * Mod compatibility, or anything which depends on other mods’ init phases being finished.
	 *
	 * @param event the event
	 * @see ForgeModContainer#postInit(FMLPostInitializationEvent)
	 */
	@EventHandler
	public void postInit(final FMLPostInitializationEvent event) {
		LOGGER.debug("postInit");
//		SmoothLightingFluid.changeFluidRenderer();
	}

	//	private static boolean isEnabled;
	//
	//	public static void deactivate() {
	//		isEnabled = false;
	//	}
	//
	//	public static void reactivate() {
	//		isEnabled = true;
	//	}

	public static boolean isEnabled() {
		//		return isEnabled;
		return ModConfig.isEnabled;
	}

	// not implemented unfortunately, we use our config system instead
	@EventHandler
	public static void onDisableEvent(final FMLModDisabledEvent event) {
		LOGGER.fatal("DEBUG: " + MOD_NAME + " was disabled :o this is... impossible???");
		//			NoCubes.deactivate();
	}

}
