package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Arrays;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static net.minecraft.util.math.RayTraceResult.Type.BLOCK;
import static net.minecraftforge.fml.relauncher.Side.CLIENT;

/**
 * Subscribe to events that should be handled on the PHYSICAL CLIENT in this class
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = MOD_ID, value = CLIENT)
public final class ClientEventSubscriber {

	@SubscribeEvent
	public static void onRebuildChunkPreEvent(final RebuildChunkPreEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (ModConfig.shouldExtendLiquids) {
			ClientUtil.calculateExtendedLiquids(event);
		}

		if (ModConfig.debug.debugEnabled) {
			return;
		}

		Minecraft.getMinecraft().profiler.startSection("Rendering smooth world in Pre");

		try {
			ModConfig.activeStableRenderingAlgorithm.renderPre(event);
		} catch (Exception e) {
			CrashReport crashReport = new CrashReport("Error rendering smooth chunk in Pre event!", e);
			crashReport.makeCategory("Rendering smooth chunk");
			throw new ReportedException(crashReport);
		}
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

		try {
			Minecraft.getMinecraft().profiler.startSection("Rendering smooth world in Layer");
		} catch (Exception e) {
			CrashReport crashReport = new CrashReport("Error rendering smooth chunk in Layer event!", e);
			crashReport.makeCategory("Rendering smooth chunk");
			throw new ReportedException(crashReport);
		}
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

		try {
			ModConfig.activeStableRenderingAlgorithm.renderType(event);
		} catch (Exception e) {
			CrashReport crashReport = new CrashReport("Error rendering smooth chunk in Type event!", e);
			crashReport.makeCategory("Rendering smooth chunk");
			throw new ReportedException(crashReport);
		}
		Minecraft.getMinecraft().profiler.endSection();

	}

	@SubscribeEvent
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (ModConfig.shouldExtendLiquids) {
			ClientUtil.handleExtendedLiquidRender(event);
		}

		if (ModConfig.debug.debugEnabled) {
			return;
		}

		Minecraft.getMinecraft().profiler.startSection("Rendering smooth world in Block");

		try {
			ModConfig.activeStableRenderingAlgorithm.renderBlock(event);
		} catch (Exception e) {
			CrashReport crashReport = new CrashReport("Error rendering smooth chunk in Block event!", e);
			crashReport.makeCategory("Rendering smooth chunk");
			throw new ReportedException(crashReport);
		}

		Minecraft.getMinecraft().profiler.endSection();

	}

	@SubscribeEvent
	public static void onRebuildChunkPostEvent(final RebuildChunkPostEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (ModConfig.shouldExtendLiquids) {
			ClientUtil.cleanupExtendedLiquids(event);
		}

		if (ModConfig.debug.debugEnabled) {
			return;
		}

		Minecraft.getMinecraft().profiler.startSection("Rendering smooth world in Post");

		try {
			ModConfig.activeStableRenderingAlgorithm.renderPost(event);
		} catch (Exception e) {
			CrashReport crashReport = new CrashReport("Error rendering smooth chunk in Post event!", e);
			crashReport.makeCategory("Rendering smooth chunk");
			throw new ReportedException(crashReport);
		}

		Minecraft.getMinecraft().profiler.endSection();

	}

	@SubscribeEvent
	public static void onClientTickEvent(final TickEvent.ClientTickEvent event) {

		if (!ClientProxy.toggleSmoothableBlockstate.isPressed()) {
			return;
		}

		final Minecraft minecraft = Minecraft.getMinecraft();

		final RayTraceResult objectMouseOver = minecraft.objectMouseOver;

		if (objectMouseOver.typeOfHit != BLOCK) {
			return;
		}

		final IBlockState state = minecraft.world.getBlockState(objectMouseOver.getBlockPos());

		final String stateAsString = state.toString();

		if (ModConfig.getSmoothableBlockStatesCache().contains(state)) {
			ModConfig.smoothableBlockStates = Arrays.stream(ModConfig.smoothableBlockStates).filter(string -> !string.equals(stateAsString)).toArray(String[]::new);
		} else {
			final ArrayList<String> list = Lists.newArrayList(ModConfig.smoothableBlockStates);
			list.add(stateAsString);
			ModConfig.smoothableBlockStates = list.toArray(new String[0]);
		}

		// Copied from GuiConfig
		if (Loader.isModLoaded(MOD_ID)) {
			ConfigChangedEvent configChangedEvent = new ConfigChangedEvent.OnConfigChangedEvent(MOD_ID, null, true, false);
			MinecraftForge.EVENT_BUS.post(configChangedEvent);
			if (!configChangedEvent.getResult().equals(Event.Result.DENY)) {
				MinecraftForge.EVENT_BUS.post(new ConfigChangedEvent.PostConfigChangedEvent(MOD_ID, null, true, false));
			}
		}

		minecraft.renderGlobal.loadRenderers();

	}

}
