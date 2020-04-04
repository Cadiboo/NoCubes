package io.github.cadiboo.nocubes.test;

import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber
public final class TestRunner {

	@SubscribeEvent
	public static void runTests(FMLServerStartedEvent event) {
		final TestRepository testRepository = new TestRepository();
		final AtomicInteger fails = new AtomicInteger(0);
		testRepository.tests.forEach(test -> {
			try {
				test.action.run();
			} catch (Exception e) {
				event.getServer().sendMessage(new StringTextComponent("TEST FAILED: " + test.name).applyTextStyle(TextFormatting.RED));
				e.printStackTrace();
				fails.getAndIncrement();
			}
		});
		if (fails.get() > 0)
			event.getServer().sendMessage(new StringTextComponent(fails.get() + " TESTS FAILED").applyTextStyle(TextFormatting.RED));
		else
			event.getServer().sendMessage(new StringTextComponent("ALL TESTS PASSED").applyTextStyle(TextFormatting.GREEN));
//		if (ModUtil.IS_DEVELOPER_WORKSPACE.get())
//			return;
		// Assuming CI
		if (fails.get() > 0)
			throw new RuntimeException("had failed tests");
		else
			event.getServer().initiateShutdown(false);
	}

}
