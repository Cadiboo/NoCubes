package io.github.cadiboo.nocubes;

import com.google.common.base.Preconditions;
import net.minecraft.block.Blocks;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * TODO: Fix this up
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber
public class NoCubesTest {

	static final Stream<Test> TESTS = Stream.of(
		makeTest("stone should be smoothable", () -> assertTrue(NoCubes.isStateSmoothable(Blocks.STONE.getDefaultState()))),
		makeTest("dirt should be smoothable", () -> assertTrue(NoCubes.isStateSmoothable(Blocks.DIRT.getDefaultState())))
	);

	private static void assertTrue(final boolean b) {
		Preconditions.checkArgument(b);
	}

	private static Test makeTest(final String name, final Runnable action) {
		return new Test(name, action);
	}

	@SubscribeEvent
	public static void test(FMLServerStartedEvent event) {
		final AtomicBoolean hadFails = new AtomicBoolean(false);
		TESTS.forEach(test -> {
			try {
				test.action.run();
			} catch (Exception e) {
				event.getServer().sendMessage(new StringTextComponent("TEST FAILED: " + test.name).applyTextStyle(TextFormatting.RED));
				e.printStackTrace();
				hadFails.set(true);
			}
		});
		if (hadFails.get())
			throw new RuntimeException("had failed tests");
		// Assuming CI
		event.getServer().close();
	}

	static class Test {

		final String name;
		final Runnable action;

		Test(final String name, final Runnable action) {
			this.name = name;
			this.action = action;
		}

	}

}
