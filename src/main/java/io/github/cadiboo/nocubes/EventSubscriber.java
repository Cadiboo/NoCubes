package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.util.ModReference;
import io.github.cadiboo.nocubes.world.ModWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Subscribe to events that should be handled on both PHYSICAL sides in this class
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = ModReference.MOD_ID)
public final class EventSubscriber {

	@SubscribeEvent
	public static void onWorldLoadEvent(final WorldEvent.Load event) {
		final World world = event.getWorld();
		world.addEventListener(new ModWorldEventListener());
	}

	//TODO: projectile impact event

}
