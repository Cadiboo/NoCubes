package io.github.cadiboo.nocubes.integrationtesting;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber
public final class TestRunner {

	private static final Logger LOGGER = LogManager.getLogger();

	@SubscribeEvent
	public static void runTests(FMLServerStartedEvent event) {
		final TestRepository testRepository = new TestRepository();
		final MinecraftServer server = event.getServer();
		final long fails = testRepository.tests.parallelStream()
			.filter(test -> runTestWithCatch(test, server))
			.count();
		if (fails > 0)
			log(server, new StringTextComponent(fails + " TESTS FAILED").mergeStyle(TextFormatting.RED));
		else
			log(server, new StringTextComponent("ALL TESTS PASSED").mergeStyle(TextFormatting.GREEN));
		if (!TestUtil.IS_CI_ENVIRONMENT.get())
			return;
		if (fails > 0)
			throw new RuntimeException("Had failed tests");
		else
			event.getServer().initiateShutdown(false);
	}

	private static void log(final MinecraftServer server, final ITextComponent component) {
		server.sendMessage(component, Util.DUMMY_UUID);
		LOGGER.info(component.getString());
	}

	/**
	 * @return if the test FAILED
	 */
	private static boolean runTestWithCatch(final Test test, final MinecraftServer server) {
		try {
			test.action.run();
		} catch (Exception e) {
			log(server, new StringTextComponent("TEST FAILED: " + test.name).mergeStyle(TextFormatting.RED));
			e.printStackTrace();
			return true;
		}
		return false;
	}

}
