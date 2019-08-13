package io.github.cadiboo.nocubes.client.optifine;

import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * @author Cadiboo
 */
final class Config {

	private static final Class<?> CONFIG = OptiFineLocator.findConfigClass();
	private static final MethodHandle isShaders;
	static {
		if (!OptiFineCompatibility.OPTIFINE_INSTALLED) {
			isShaders = null;
		} else {
			try {
				isShaders = MethodHandles.publicLookup().unreflect(CONFIG.getMethod("isShaders"));
			} catch (NullPointerException | IllegalAccessException | NoSuchMethodException e) {
				final CrashReport crashReport = CrashReport.makeCrashReport(e, "Problem finding Config.isShaders");
				crashReport.makeCategory("NoCubes OptiFine Config");
				throw new ReportedException(crashReport);
			}
		}
	}

	public static boolean isShaders() {
		if (!OptiFineCompatibility.OPTIFINE_INSTALLED) {
			throw new OptiFineNotPresentException();
		}
		try {
			return (boolean) isShaders.invokeExact();
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

}
