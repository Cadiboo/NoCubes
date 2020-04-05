package io.github.cadiboo.nocubes.test;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber
public final class TestRunner {

	@SubscribeEvent
	public static void runTests(FMLServerStartedEvent event) {
		final TestRepository testRepository = new TestRepository();
		final MinecraftServer server = event.getServer();
		final long fails = testRepository.tests.parallelStream()
			.filter(test -> runTestWithCatch(test, server))
			.count();
		if (fails > 0)
			server.sendMessage(new StringTextComponent(fails + " TESTS FAILED").applyTextStyle(TextFormatting.RED));
		else
			server.sendMessage(new StringTextComponent("ALL TESTS PASSED").applyTextStyle(TextFormatting.GREEN));
		if (!TestUtil.IS_CI_ENVIRONMENT.get())
			return;
		if (fails > 0)
			throw new RuntimeException("Had failed tests");
		else
			event.getServer().initiateShutdown(false);
	}

	/**
	 * @return if the test FAILED
	 */
	private static boolean runTestWithCatch(final Test test, final MinecraftServer server) {
		try {
			test.action.run();
		} catch (Exception e) {
			server.sendMessage(new StringTextComponent("TEST FAILED: " + test.name).applyTextStyle(TextFormatting.RED));
			e.printStackTrace();
			return true;
		}
		return false;
	}

}
