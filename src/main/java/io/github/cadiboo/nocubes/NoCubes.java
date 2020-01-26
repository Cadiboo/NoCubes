package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
@Mod(MOD_ID)
public final class NoCubes {

	public static final String MOD_ID = "nocubes";
	private static final Logger LOGGER = LogManager.getLogger();

	public NoCubes() {
		preloadModifiedClasses();
		NoCubesConfig.register(ModLoadingContext.get());
		NoCubesNetwork.register();

		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener((FMLCommonSetupEvent event) -> ModList.get().getModContainerById(MOD_ID).ifPresent(ModUtil::launchUpdateDaemon));
//		modEventBus.addListener((FMLLoadCompleteEvent event) -> DistExecutor.runWhenOn(Dist.CLIENT, () -> ClientUtil::replaceFluidRenderer));

	}

	private void preloadModifiedClasses() {
		LOGGER.debug("Preloading patched classes...");
		preloadClass("net.minecraft.block.BlockState", "BlockState");
		preloadClass("net.minecraft.world.chunk.Chunk", "Chunk");
		LOGGER.debug("Finished preloading patched classes");
	}

	private void preloadClass(final String qualifiedName, final String simpleName) {
		try {
			LOGGER.info("Loading class \"{}\"...", simpleName);
			final ClassLoader classLoader = this.getClass().getClassLoader();
			final long startTime = System.nanoTime();
			Class.forName(qualifiedName, false, classLoader);
			final long timeElapsed = System.nanoTime() - startTime;
			LOGGER.info("Loaded class \"{}\" in {} nano seconds", simpleName, timeElapsed);
			LOGGER.info("Initialising class \"{}\"...", simpleName);
			Class.forName(qualifiedName, true, classLoader);
			LOGGER.info("Initialised \"{}\"", simpleName);
		} catch (final ClassNotFoundException e) {
			final CrashReport crashReport = CrashReport.makeCrashReport(e, "Failed to load class \"" + simpleName + "\". This should not be possible!");
			crashReport.makeCategory("Loading class");
			throw new ReportedException(crashReport);
		}
	}

}
