package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.RenderDispatcher;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.nocubes.util.Vec3b;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreRenderEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static net.minecraft.util.math.RayTraceResult.Type.BLOCK;
import static net.minecraftforge.api.distmarker.Dist.CLIENT;

/**
 * Subscribe to events that should be handled on the PHYSICAL CLIENT in this class
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = MOD_ID, value = CLIENT)
public final class ClientEventSubscriber {

	@SubscribeEvent
	public static void onRebuildChunkPreEvent(final RebuildChunkPreRenderEvent event) {
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

	@SubscribeEvent
	public static void onClientTickEvent(final TickEvent.ClientTickEvent event) {

//		if (Minecraft.getInstance().world != null && Minecraft.getInstance().world.getGameTime() % 100 == 0) {
//			Logger logger = LogManager.getLogger("debug");
//			logger.info("Face " + Face.getInstances() + " " + Face.getPoolSize());
//			logger.info("FaceList " + FaceList.getInstances() + " " + FaceList.getPoolSize());
//			logger.info("Vec3 " + Vec3.getInstances() + " " + Vec3.getPoolSize());
//			logger.info("Vec3b " + Vec3b.getInstances() + " " + Vec3b.getPoolSize());
//		}

		if (!ClientProxy.toggleSmoothableBlockstate.isPressed()) {
			return;
		}
		final Minecraft minecraft = Minecraft.getInstance();
		final RayTraceResult objectMouseOver = minecraft.objectMouseOver;
		if (objectMouseOver.type != BLOCK) {
			return;
		}
		final IBlockState state = minecraft.world.getBlockState(objectMouseOver.getBlockPos());

		final HashSet<IBlockState> smoothableBlockStatesCache = ModConfig.getSmoothableBlockStatesCache();
		if (smoothableBlockStatesCache.contains(state)) {
			smoothableBlockStatesCache.remove(state);
		} else {
			smoothableBlockStatesCache.add(state);
		}

//		final String stateAsString = state.toString();
//		if (ModConfig.getSmoothableBlockStatesCache().contains(state)) {
//			ModConfig.smoothableBlockStates = ModConfig.getSmoothableBlockStatesCache().stream()
//					.filter(checkState -> checkState != state)
//					.map(IBlockState::toString)
//					.toArray(String[]::new);
//		} else {
//			final ArrayList<String> list = Lists.newArrayList(ModConfig.smoothableBlockStates);
//			list.add(stateAsString);
//			ModConfig.smoothableBlockStates = list.toArray(new String[0]);
//		}
//		// Copied from GuiConfig
//		if (Launcher.isModLoaded(MOD_ID)) {
//			ConfigChangedEvent configChangedEvent = new ConfigChangedEvent.OnConfigChangedEvent(MOD_ID, null, true, false);
//			MinecraftForge.EVENT_BUS.post(configChangedEvent);
//			if (!configChangedEvent.getResult().equals(Event.Result.DENY)) {
//				MinecraftForge.EVENT_BUS.post(new ConfigChangedEvent.PostConfigChangedEvent(MOD_ID, null, true, false));
//			}
//		}

		minecraft.renderGlobal.loadRenderers();
	}

//	@SubscribeEvent
//	public static void onForgeRenderChunkChunkCacheThingFor1_13(final EventThatDoesntExistIn1_12_2 event) {
//		if (!NoCubes.isEnabled()) {
//			return;
//		}
//	}

}
