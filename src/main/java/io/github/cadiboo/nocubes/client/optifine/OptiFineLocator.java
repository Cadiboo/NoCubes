package io.github.cadiboo.nocubes.client.optifine;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

/**
 * @author Cadiboo
 */
public final class OptiFineLocator {

	@Nullable
	static Class<?> findConfigClass() {
		// Config was moved around in HD_U_F
		// 1. Try to find "net.optifine.Config"
		// 2. Try to find "Config"
		Class<?> config = null;
//		try {
//			config = Class.forName("net.optifine.Config");
//		} catch (ClassNotFoundException failedToFindModernConfigClass) {
//			try {
//				config = Class.forName("Config");
//			} catch (ClassNotFoundException failedToFindLegacyConfigClass) {
//				NoCubes.LOGGER.info("OptiFineCompatibility: OptiFine not detected.");
//				return null;
//			}
//		}
//		NoCubes.LOGGER.info("OptiFineCompatibility: Found OptiFine!");
		return config;
	}

	private static boolean isCompatibleOptiFineVersion(final String version) {
		return version.contains("HD_U_F");
	}

	static boolean isOptiFineInstalledAndCompatible() {
		return isOptiFineInstalled() && isOptiFineCompatible();
	}

	public static boolean isOptiFineCompatible() {
		final Class<?> configClass = findConfigClass();
		if (configClass == null) {
			return false;
		}
		return isCompatibleOptiFineVersion(getOptiFineVersion(configClass));
	}

	@Nullable
	public static String getOptiFineVersion() {
		final Class<?> configClass = findConfigClass();
		return configClass == null ? null : getOptiFineVersion(configClass);
	}

	@Nonnull
	private static String getOptiFineVersion(final Class<?> configClass) {
		try {
			final Field versionField = configClass.getField("VERSION");
			versionField.setAccessible(true);
			return (String) versionField.get(null);
		} catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
			final CrashReport crashReport = CrashReport.makeCrashReport(e, "Problem getting OptiFine version");
			crashReport.makeCategory("NoCubes OptiFine Locator");
			throw new ReportedException(crashReport);
		}
	}

	public static boolean isOptiFineInstalled() {
		return findConfigClass() != null;
	}

}
