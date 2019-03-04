package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.gui.toast.BlockStateToast;
import io.github.cadiboo.nocubes.client.render.RenderDispatcher;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModReference.MOD_NAME;
import static io.github.cadiboo.nocubes.util.ModReference.VERSION;
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

	@SubscribeEvent
	public static void onClientTickEvent(final TickEvent.ClientTickEvent event) {

		if (false) {
			ObjectPoolingProfiler.onTick();
		}

		if (ClientProxy.toggleSmoothableBlockstate.isPressed()) {
			if (addBlockstateToSmoothable()) {
				if (NoCubes.isEnabled()) {
					ClientUtil.tryReloadRenderers();
				}
			}
		}
	}

	private static boolean addBlockstateToSmoothable() {
		final Minecraft minecraft = Minecraft.getMinecraft();
		final RayTraceResult objectMouseOver = minecraft.objectMouseOver;
		if (objectMouseOver.typeOfHit != BLOCK) {
			return false;
		}

		final IBlockState state = minecraft.world.getBlockState(objectMouseOver.getBlockPos());

		final BlockStateToast toast;
		final HashSet<IBlockState> smoothableBlockStatesCache = ModConfig.getSmoothableBlockStatesCache();
		if (!smoothableBlockStatesCache.remove(state)) {
			smoothableBlockStatesCache.add(state);
			toast = new BlockStateToast.Add(state);
		} else {
			toast = new BlockStateToast.Remove(state);
		}
		minecraft.getToastGui().add(toast);

		syncSmoothableBlockstatesWithCache();

		// Copied from GuiConfig
		{
			if (Loader.isModLoaded(MOD_ID)) {
				ConfigChangedEvent configChangedEvent = new ConfigChangedEvent.OnConfigChangedEvent(MOD_ID, null, true, false);
				MinecraftForge.EVENT_BUS.post(configChangedEvent);
				if (!configChangedEvent.getResult().equals(Event.Result.DENY)) {
					MinecraftForge.EVENT_BUS.post(new ConfigChangedEvent.PostConfigChangedEvent(MOD_ID, null, true, false));
				}
			}
		}
		return true;
	}

	private static void syncSmoothableBlockstatesWithCache() {
		ModConfig.smoothableBlockStates = ModConfig.getSmoothableBlockStatesCache().stream()
				.map(IBlockState::toString)
				.toArray(String[]::new);
	}

	//	@SubscribeEvent
//	public static void onForgeRenderChunkChunkCacheThingFor1_13(final EventThatDoesntExistIn1_12_2 event) {
//		if (!NoCubes.isEnabled()) {
//			return;
//		}
//	}

	@SubscribeEvent
	public static void onEntityJoinWorldEvent(final EntityJoinWorldEvent event) {
		final Entity entity = event.getEntity();
		if (entity instanceof EntityPlayerSP) {
			EntityPlayerSP player = (EntityPlayerSP) entity;
			player.sendChatMessage(MOD_NAME + " " + VERSION + ": The code for Collisions is 90% copied/ported/stolen from the Repose Mod's code. More accurate collisions with less exactly similar code are being worked on. Until then, enjoy and go give Repose some love at https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2076319-repose-walkable-soil-slopes-give-your-spacebar-a");
		}

	}

}
