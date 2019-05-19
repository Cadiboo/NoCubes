package io.github.cadiboo.nocubes.util;

import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;

import javax.annotation.Nonnull;

import static io.github.cadiboo.nocubes.NoCubes.LOGGER;

/**
 * Some basic functions that differ depending on the physical side
 *
 * @author Cadiboo
 */
public interface Proxy {

	void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean updateImmediately);

	void replaceFluidRendererCauseImBored();

	default void preloadClasses() {
		preloadClass("net.minecraft.block.state.IBlockProperties", "IBlockProperties");
		preloadClass("net.minecraft.block.state.BlockStateContainer$StateImplementation", "StateImplementation");
	}

	default void preloadClass(@Nonnull final String qualifiedName, @Nonnull final String simpleName) {
		try {
			LOGGER.info("Loading class \"" + simpleName + "\"...");
			final ClassLoader classLoader = this.getClass().getClassLoader();
			final long startTime = System.nanoTime();
			Class.forName(qualifiedName, false, classLoader);
			LOGGER.info("Loaded class \"" + simpleName + "\" in " + (System.nanoTime() - startTime) + " nano seconds");
			LOGGER.info("Initialising class \"" + simpleName + "\"...");
			Class.forName(qualifiedName, true, classLoader);
			LOGGER.info("Initialised \"" + simpleName + "\"");
		} catch (final ClassNotFoundException e) {
			final CrashReport crashReport = new CrashReport("Failed to load class \"" + simpleName + "\". This should not be possible!", e);
			crashReport.makeCategory("Loading class");
			throw new ReportedException(crashReport);
		}
	}

}
