package io.github.cadiboo.nocubes.integrationtesting;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.ChatFormatting.GREEN;
import static net.minecraft.ChatFormatting.RED;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber
public final class TestRunner {

	private static final Logger LOGGER = LogManager.getLogger();

	@SubscribeEvent
	public static void runTests(ServerStartedEvent event) {
		var testRepository = new TestRepository();
		var server = event.getServer();
		var fails = testRepository.tests.parallelStream()
			.filter(test -> runTestWithCatch(test, server))
			.count();
		if (fails > 0)
			log(server, new TextComponent(fails + " TESTS FAILED").withStyle(RED));
		else
			log(server, new TextComponent("ALL TESTS PASSED").withStyle(GREEN));
		if (!TestUtil.IS_CI_ENVIRONMENT.get())
			return;
		if (fails > 0)
			throw new RuntimeException("Had failed tests");
		else
			event.getServer().halt(false);
	}

	private static void log(MinecraftServer server, Component component) {
		server.sendMessage(component, Util.NIL_UUID);
		LOGGER.info(component.getString());
	}

	/**
	 * @return if the test FAILED
	 */
	private static boolean runTestWithCatch(Test test, MinecraftServer server) {
		try {
			test.action.run();
		} catch (OutOfMemoryError | InternalError e) {
			throw e;
		} catch (Throwable t) {
			log(server, new TextComponent("TEST FAILED: " + test.name).withStyle(RED));
			t.printStackTrace();
			return true;
		}
		return false;
	}

}
