package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.util.ModReference;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 * Subscribe to events that should be handled on both PHYSICAL sides in this class
 */
@Mod.EventBusSubscriber(modid = ModReference.MOD_ID)
public final class EventSubscriber {

	@SubscribeEvent
	public static void onGetCollisionBoxesEvent(@Nonnull final GetCollisionBoxesEvent event) {

		//return pre-calculated collision boxes

	}

}
