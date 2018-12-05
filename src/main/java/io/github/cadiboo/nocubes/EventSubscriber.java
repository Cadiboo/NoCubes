package io.github.cadiboo.nocubes;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import io.github.cadiboo.nocubes.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class EventSubscriber {

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkPreEvent(final RebuildChunkPreEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderPre(event);
	}

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkBlockRenderInLayerEvent(final RebuildChunkBlockRenderInLayerEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderLayer(event);
	}

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkBlockRenderInTypeEvent(final RebuildChunkBlockRenderInTypeEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderType(event);
	}

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderBlock(event);
	}

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkPostEvent(final RebuildChunkPostEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderPost(event);
	}

}
