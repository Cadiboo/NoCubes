package io.github.cadiboo.nocubes.client;

import com.google.common.collect.Lists;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.RenderDispatcher;
import io.github.cadiboo.nocubes.config.ModConfig;
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

}
