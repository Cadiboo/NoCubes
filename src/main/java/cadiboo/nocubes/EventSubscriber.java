package cadiboo.nocubes;

import cadiboo.nocubes.config.ModConfig;
import cadiboo.nocubes.util.ModEnums.RenderType;
import cadiboo.nocubes.util.ModUtil;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class EventSubscriber {

	@SubscribeEvent
	public static void onRebuildChunkPreEvent(final RebuildChunkPreEvent event) {

//		NoCubes.LOGGER.info("onRebuildChunkPreEvent");

		if (! NoCubes.isEnabled()) {
			return;
		}
		if (ModConfig.renderType != RenderType.CHUNK) {
			return;
		}

		event.setCanceled(true);

		ModConfig.activeRenderingAlgorithm.renderChunk(event);

		//		event.addRenderChunksUpdated(ModConfig.activeRenderingAlgorithm.renderAllBlocks(event));

	}

	@SubscribeEvent
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {

//		NoCubes.LOGGER.info("onRebuildChunkBlockEvent");

		if (! NoCubes.isEnabled()) {
			return;
		}

		if (ModConfig.renderType == RenderType.CHUNK) {
			if (ModUtil.shouldSmooth(event.getBlockState())) {
				event.setCanceled(true);
			}
			return;
		}

		if (ModConfig.renderType != RenderType.BLOCK) {
			return;
		}

		event.setCanceled(true);

		ModConfig.activeRenderingAlgorithm.renderBlock(event);

	}

	@SubscribeEvent
	public static void onRebuildChunkBlockRenderInLayerEvent(final RebuildChunkBlockRenderInLayerEvent event) {

//		NoCubes.LOGGER.info("onRebuildChunkBlockRenderInLayerEvent");

		if (! NoCubes.isEnabled()) {
			return;
		}

		event.setResult(Event.Result.ALLOW);

	}

}
