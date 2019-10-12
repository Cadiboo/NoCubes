package io.github.cadiboo.nocubes.client.optifine;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.optifine.proxy.Dummy;
import io.github.cadiboo.nocubes.client.optifine.proxy.HD_U_F4;
import io.github.cadiboo.nocubes.client.optifine.proxy.OptiFine;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Cadiboo
 */
public final class OptiFineCompatibility {

	private static final OptiFineProxy[] proxies = {
			new OptiFineProxy("HD_U_F4", () -> HD_U_F4::new)
	};

	private static Class<?> configClass;

	private static boolean initialised;
	private static OptiFine optiFine;

	public static OptiFine get() {
		if (!initialised) {
			throw new IllegalStateException("Never initialised!");
		}
		return optiFine;
	}

	public static void init() {
		if (initialised) return;
		configClass = findConfigClass();
		initialised = true;

		final String optiFineVersion = getOptiFineVersion();
		if (optiFineVersion == null) {
			optiFine = new Dummy();
			return;
		}
		for (final OptiFineProxy proxy : proxies) {
			if (optiFineVersion.contains(proxy.version)) {
				optiFine = proxy.optiFine.get().get();
				return;
			}
		}
	}

	@Nullable
	private static Class<?> findConfigClass() {
		// Config was moved around in HD_U_F
		// 1. Try to find "net.optifine.Config"
		// 2. Try to find "Config"
		Class<?> config;
		try {
			config = Class.forName("net.optifine.Config");
		} catch (ClassNotFoundException failedToFindModernConfigClass) {
			try {
				config = Class.forName("Config");
			} catch (ClassNotFoundException failedToFindLegacyConfigClass) {
				NoCubes.LOGGER.info("OptiFineCompatibility: OptiFine not detected.");
				return null;
			}
		}
		NoCubes.LOGGER.info("OptiFineCompatibility: Found OptiFine!");
		return config;
	}

	@Nullable
	public static String getOptiFineVersion() {
		if (configClass == null)
			return null;
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
		return configClass != null;
	}

	public static boolean isOptiFineCompatible() {
		return !(optiFine instanceof Dummy);
	}

	public static String getSupportedVersions() {
		return Arrays.stream(proxies)
				.map(optiFineProxy -> optiFineProxy.version)
				.collect(Collectors.joining(", "));
	}

	public static void main(String... args) {
		System.out.println(getSupportedVersions());
	}

	private static class OptiFineProxy {

		private final String version;
		private final Supplier<Supplier<OptiFine>> optiFine;

		private OptiFineProxy(final String version, final Supplier<Supplier<OptiFine>> optiFine) {
			this.version = version;
			this.optiFine = optiFine;
		}

	}

}
