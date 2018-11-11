package cadiboo.nocubes;

import cadiboo.nocubes.config.ModConfig;
import cadiboo.nocubes.util.ModEnums.RenderType;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlocksEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class EventSubscriber {

	@SubscribeEvent
	public static void onRebuildChunkAllBlocksEvent(final RebuildChunkBlocksEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		if (ModConfig.renderType != RenderType.ALL_BLOCKS) {
			return;
		}

		event.setCanceled(true);

		ModConfig.activeRenderingAlgorithm.renderAllBlocks(event);

//		event.addRenderChunksUpdated(ModConfig.activeRenderingAlgorithm.renderAllBlocks(event));

	}

	@SubscribeEvent
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		if (ModConfig.renderType != RenderType.SINGLE_BLOCK) {
			return;
		}

		event.setCanceled(true);

		ModConfig.activeRenderingAlgorithm.renderBlock(event);

	}

}
