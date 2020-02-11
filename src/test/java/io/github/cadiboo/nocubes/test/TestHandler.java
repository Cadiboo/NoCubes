package io.github.cadiboo.nocubes.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.github.cadiboo.nocubes.test.client.KeyBindAddBlockStateTest;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = MOD_ID)
public final class TestHandler {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final Set<Test> TESTS = new HashSet<>();
	private static boolean running = false;
	static {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			TESTS.add(new KeyBindAddBlockStateTest());
		});
	}

	@SubscribeEvent
	public static void onFMLServerStartingEvent(final FMLServerStartingEvent event) {
		event.getCommandDispatcher().register(Commands.literal(MOD_ID)
				.then(Commands.literal("runTests"))
				.executes(TestHandler::runTestsCommand)
		);
	}

	private static int runTestsCommand(final CommandContext<CommandSource> context) {
		if (!running) {
			synchronized (TESTS) {
				if (!running) {
					new Thread(TestHandler::runTests, "Test Tracker")
							.start();
				}
			}
		}
		return Command.SINGLE_SUCCESS;
	}

	/**
	 * @return The amount of successful tests
	 */
	private static int runTests() {
		while (true) {
			if (TESTS.parallelStream().anyMatch(test -> test.getStatus() == Test.Status.RUNNING))
				Thread.yield();
			else
				break;
		}
		TESTS.stream().filter(test -> test.getStatus() == Test.Status.FAILURE).forEach(failedTest -> LOGGER.error("Test \"{}\" failed!", failedTest.getName()));
		return (int) TESTS.stream().filter(test -> test.getStatus() == Test.Status.SUCCESS).count();
	}

	public abstract static class Test {

		private Status status = Status.STOPPED;

		public String getName() {
			return getClass().getSimpleName();
		}

		public final Status getStatus() {
			return status;
		}

		public final void setStatus(Status newValue) {
			status = newValue;
		}

		public abstract void startTasks();

		public abstract void stopTasks();

		public final void start() {
			setStatus(Status.RUNNING);
			startTasks();
		}

		public final void stop() {
			setStatus(Status.STOPPED);
			stopTasks();
		}

		public enum Status {
			STOPPED,
			RUNNING,
			SUCCESS,
			FAILURE;
		}

	}

}
