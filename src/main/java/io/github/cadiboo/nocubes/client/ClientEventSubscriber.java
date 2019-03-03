package io.github.cadiboo.nocubes.client;

import com.google.common.collect.Lists;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.RenderDispatcher;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.DensityCache;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.SmoothableCache;
import io.github.cadiboo.nocubes.util.StateCache;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.nocubes.util.Vec3b;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

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
		RenderDispatcher.renderChunk(event);
	}

	@SubscribeEvent
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		RenderDispatcher.renderBlock(event);
	}

	private static int counter = 20;

	private static String lightmapInfo = "";
	private static String packedLightCache = "";
	private static String densityCache = "";
	private static String face = "";
	private static String faceList = "";
	private static String smoothableCache = "";
	private static String stateCache = "";
	private static String vec3 = "";
	private static String vec3b = "";

	@SubscribeEvent
	public static void onClientTickEvent(final TickEvent.ClientTickEvent event) {
		--counter;
		if (counter == 0) {
			counter = 20;
			if (FaceList.getInstances() != 0) {
				lightmapInfo += "\t" + LightmapInfo.getInstances();
				packedLightCache += "\t" + PackedLightCache.getInstances();
				densityCache += "\t" + DensityCache.getInstances();
				face += "\t" + Face.getInstances();
				faceList += "\t" + FaceList.getInstances();
				smoothableCache += "\t" + SmoothableCache.getInstances();
				stateCache += "\t" + StateCache.getInstances();
				vec3 += "\t" + Vec3.getInstances();
				vec3b += "\t" + Vec3b.getInstances();

				Logger logger = LogManager.getLogger("debug pools");
				logger.info("EnablePools: " + ModConfig.enablePools);

//			logger.info("LightmapInfo " + LightmapInfo.getInstances());
//			logger.info("PackedLightCache " + PackedLightCache.getInstances());
//
//			logger.info("DensityCache " + DensityCache.getInstances());
//			logger.info("Face " + Face.getInstances() + " " + Face.getPoolSize());
//			logger.info("FaceList " + FaceList.getInstances() + " " + FaceList.getPoolSize());
//			logger.info("SmoothableCache " + SmoothableCache.getInstances());
//			logger.info("StateCache " + StateCache.getInstances());
//			logger.info("Vec3 " + Vec3.getInstances() + " " + Vec3.getPoolSize());
//			logger.info("Vec3b " + Vec3b.getInstances() + " " + Vec3b.getPoolSize());

				logger.info("LightmapInfo " + lightmapInfo);
				logger.info("PackedLightCache " + packedLightCache);

				logger.info("DensityCache " + densityCache);
				logger.info("Face " + face);
				logger.info("FaceList " + faceList);
				logger.info("SmoothableCache " + smoothableCache);
				logger.info("StateCache " + stateCache);
				logger.info("Vec3 " + vec3);
				logger.info("Vec3b " + vec3b);
			}
		}

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
			ModConfig.smoothableBlockStates = ModConfig.getSmoothableBlockStatesCache().stream()
					.filter(checkState -> checkState != state)
					.map(IBlockState::toString)
					.toArray(String[]::new);
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

//	@SubscribeEvent
//	public static void onForgeRenderChunkChunkCacheThingFor1_13(final EventThatDoesntExistIn1_12_2 event) {
//		if (!NoCubes.isEnabled()) {
//			return;
//		}
//	}

}
