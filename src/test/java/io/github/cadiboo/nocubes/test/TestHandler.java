package io.github.cadiboo.nocubes.test;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * Tests for NoCubes.
 *
 * @author Cadiboo
 */
public final class TestHandler {

	@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static final class ModEventSubscriber {

		@SubscribeEvent
		public static void onFMLLoadCompleteEvent(FMLLoadCompleteEvent event) {

		}

	}

	@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static final class ForgeEventSubscriber {

	}

}
