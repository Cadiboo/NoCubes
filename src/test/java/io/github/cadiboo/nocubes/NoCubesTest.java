package io.github.cadiboo.nocubes;

import com.google.common.base.Preconditions;
import net.minecraft.block.Blocks;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.stream.Stream;

/**
 * TODO: Fix this up
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber
public class NoCubesTest {

	static final Lazy<Boolean> isDeveloperWorkspace = Lazy.of(() -> {
		final String target = System.getenv().get("target");
		if (target == null)
			return false;
		return target.contains("userdev");
	});
	static final Stream<Test> tests = Stream.of(
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
	public static void test(PlayerEvent.PlayerLoggedInEvent event) {
		tests.forEach(test -> {
			try {
				test.action.run();
			} catch (Exception e) {
				event.getPlayer().sendMessage(new StringTextComponent("TEST FAILED: " + test.name).applyTextStyle(TextFormatting.RED));
				e.printStackTrace();
				if (!isDeveloperWorkspace.get())
					throw new RuntimeException(e);
			}
		});
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
