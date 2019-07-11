package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.config.Config;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * @author Cadiboo
 */
@EventBusSubscriber(modid = NoCubes.MOD_ID)
public final class ForgeEventSubscriber {

	@SubscribeEvent
	public static void onTickEvent(final TickEvent event) {
		// TODO: Remove once collisions work
		Config.terrainCollisions = false;
	}

}
