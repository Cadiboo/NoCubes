package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.config.Config;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * @author Cadiboo
 */
@EventBusSubscriber(modid = NoCubes.MOD_ID)
public final class ForgeEventSubscriber {

	private static final Logger LOGGER = LogManager.getLogger();

	@SubscribeEvent
	public void onTickEvent(final TickEvent event) {
		Config.terrainCollisions = false;
	}

}
