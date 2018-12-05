package io.github.cadiboo.nocubes;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.nocubes.config.ModConfig;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class EventSubscriber {

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		ModConfig.activeRenderingAlgorithm.renderBlock(event);

	}

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkBlockRenderInLayerEvent(final RebuildChunkBlockRenderInLayerEvent event) {
//		if (event.getBlockState().getBlock() instanceof BlockAir) {
//			event.setResult(Event.Result.ALLOW);
//			event.setCanceled(true);
//		}

//		event.setCanceled(true);

//
//		if (event.getBlockState().getBlock() == Blocks.GRASS) {
//			if (event.getBlockRenderLayer() == BlockRenderLayer.CUTOUT_MIPPED)
//				event.setResult(Event.Result.DENY);
//			else if (event.getBlockRenderLayer() == BlockRenderLayer.TRANSLUCENT)
//				event.setResult(Event.Result.ALLOW);
//			event.setCanceled(true);
//		}

	}

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkBlockRenderInTypeEvent(final RebuildChunkBlockRenderInTypeEvent event) {
//		event.setResult(Event.Result.ALLOW);
	}

}
