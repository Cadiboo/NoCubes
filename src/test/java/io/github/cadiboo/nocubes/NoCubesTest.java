package io.github.cadiboo.nocubes;

import com.google.common.base.Preconditions;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
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
	public static void test(PlayerEvent.PlayerLoggedInEvent event) {
		TESTS.forEach(test -> {
			try {
				test.action.run();
			} catch (Exception e) {
				event.getPlayer().sendMessage(new StringTextComponent("TEST FAILED: " + test.name).applyTextStyle(TextFormatting.RED));
				e.printStackTrace();
				if (!ModUtil.IS_DEVELOPER_WORKSPACE.get())
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
