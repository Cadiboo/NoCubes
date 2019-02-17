package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.util.ModReference;
import io.github.cadiboo.nocubes.world.ModWorldEventListener;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Subscribe to events that should be handled on both PHYSICAL sides in this class
 */
@Mod.EventBusSubscriber(modid = ModReference.MOD_ID)
public final class EventSubscriber {

	@SubscribeEvent
	public static void onWorldLoadEvent(final WorldEvent.Load event) {

		final IWorld iworld = event.getWorld();
		//WTF
		if (iworld instanceof World) {
			((World) iworld).addEventListener(new ModWorldEventListener());
		}

	}

}
