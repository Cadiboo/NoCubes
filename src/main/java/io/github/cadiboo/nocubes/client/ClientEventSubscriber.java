package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModReference;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

/**
 * Subscribe to events that should be handled on the PHYSICAL CLIENT in this class
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = ModReference.MOD_ID, value = CLIENT)
public final class ClientEventSubscriber {

	@SubscribeEvent
	public static void onRebuildChunkPreEvent(final RebuildChunkPreEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (ModConfig.debug.debugEnabled) {
			return;
		}

		Minecraft.getMinecraft().profiler.startSection("Rendering smooth world in Pre");

		if (ModConfig.shouldExtendLiquids)
			ClientUtil.calculateExtendedLiquids(event);

		ModConfig.activeStableRenderingAlgorithm.renderPre(event);

		Minecraft.getMinecraft().profiler.endSection();

	}

	@SubscribeEvent
	public static void onRebuildChunkBlockRenderInLayerEvent(final RebuildChunkBlockRenderInLayerEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (ModConfig.debug.debugEnabled) {
			return;
		}

		Minecraft.getMinecraft().profiler.startSection("Rendering smooth world in Layer");

		ModConfig.activeStableRenderingAlgorithm.renderLayer(event);

		Minecraft.getMinecraft().profiler.endSection();
	}

	@SubscribeEvent
	public static void onRebuildChunkBlockRenderInTypeEvent(final RebuildChunkBlockRenderInTypeEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (ModConfig.debug.debugEnabled) {
			return;
		}

		Minecraft.getMinecraft().profiler.startSection("Rendering smooth world in Type");

		ModConfig.activeStableRenderingAlgorithm.renderType(event);

		Minecraft.getMinecraft().profiler.endSection();

	}

	@SubscribeEvent
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (ModConfig.debug.debugEnabled) {
			return;
		}

		Minecraft.getMinecraft().profiler.startSection("Rendering smooth world in Block");

		ModConfig.activeStableRenderingAlgorithm.renderBlock(event);

		if (ModConfig.shouldExtendLiquids)
			ClientUtil.handleExtendedLiquidRender(event);

		Minecraft.getMinecraft().profiler.endSection();

	}

	@SubscribeEvent
	public static void onRebuildChunkPostEvent(final RebuildChunkPostEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (ModConfig.debug.debugEnabled) {
			return;
		}

		Minecraft.getMinecraft().profiler.startSection("Rendering smooth world in Post");

		if (ModConfig.shouldExtendLiquids)
			ClientUtil.cleanupExtendedLiquids(event);

		ModConfig.activeStableRenderingAlgorithm.renderPost(event);

		Minecraft.getMinecraft().profiler.endSection();

	}

}
