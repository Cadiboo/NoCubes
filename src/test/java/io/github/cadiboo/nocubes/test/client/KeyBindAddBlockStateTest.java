package io.github.cadiboo.nocubes.test.client;

import io.github.cadiboo.nocubes.test.TestHandler.Test;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static net.minecraftforge.api.distmarker.Dist.CLIENT;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = MOD_ID, value = CLIENT)
public class KeyBindAddBlockStateTest extends Test {

	private static boolean running;

	@Override
	public String getName() {
		return "KeyBindAddBlockStateTest";
	}

	@Override
	public void startTasks() {
		running = true;
	}

	@Override
	public void stopTasks() {
		running = false;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST) // Run before normal NoCubes
	public static void onClientTickEvent(final TickEvent.ClientTickEvent event) {

	}

}
