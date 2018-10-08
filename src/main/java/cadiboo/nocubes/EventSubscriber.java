package cadiboo.nocubes;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;

@Mod.EventBusSubscriber
public final class EventSubscriber {

	public static void onRenderWorldLastEvent(final RenderWorldLastEvent event) {

		event.setCanceled(true);

		event.setResult(Event.Result.DENY);

	}

}
