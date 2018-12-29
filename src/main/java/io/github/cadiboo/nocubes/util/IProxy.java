package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

/**
 * Some basic functions that differ depending on the physical side
 *
 * @author Cadiboo
 */
public interface IProxy {

	String localize(String unlocalized);

	String localizeAndFormat(String unlocalized, Object... args);

	default void logPhysicalSide(Logger logger) {
		logger.debug("Physical Side: " + getPhysicalSide());
	}

	Side getPhysicalSide();

	// FIXME BIG TODO remove this once I'm done with beta releases and start doing actual releases
	// FIXME This is evil and not good
	default void forceUpdate(ComparableVersion outdatedVersion) {
		final String fuck9minecraft = "Your version of NoCubes (" + outdatedVersion + ") is outdated! Download the latest version from https://cadiboo.github.io/projects/nocubes/download/";
		for (int i = 0; i < 10; i++)
			NoCubes.NO_CUBES_LOG.error(fuck9minecraft);
		CrashReport crashReport = new CrashReport(fuck9minecraft, new RuntimeException(fuck9minecraft));
		FMLCommonHandler.instance().raiseException(new ReportedException(crashReport), fuck9minecraft, true);
		FMLCommonHandler.instance().exitJava(0, false);
	}

}
