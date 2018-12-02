package cadiboo.nocubes;

import cadiboo.nocubes.config.ModConfig;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class EventSubscriber {

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {

		//		NoCubes.LOGGER.info("onRebuildChunkBlockEvent");

		if (! NoCubes.isEnabled()) {
			return;
		}

		event.setCanceled(true);

		ModConfig.activeRenderingAlgorithm.renderBlock(event);

	}

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkBlockRenderInLayerEvent(final RebuildChunkBlockRenderInLayerEvent event) {

	}

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkBlockRenderInTypeEvent(final RebuildChunkBlockRenderInTypeEvent event) {

		//		if(event.getBlockState().getMaterial()== Material.AIR) {
		event.setResult(Event.Result.ALLOW);
		//		}

	}

}
