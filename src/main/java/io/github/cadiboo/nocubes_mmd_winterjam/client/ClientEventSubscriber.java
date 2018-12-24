package io.github.cadiboo.nocubes_mmd_winterjam.client;

import io.github.cadiboo.nocubes_mmd_winterjam.NoCubes;
import io.github.cadiboo.nocubes_mmd_winterjam.client.render.OldNoCubes;
import io.github.cadiboo.nocubes_mmd_winterjam.util.ModReference;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
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

//	@SubscribeEvent
//	public static void onRebuildChunkPreEvent(final RebuildChunkPreEvent event) {
//		if (!NoCubes.isEnabled()) {
//			return;
//		}
//		OldNoCubes.renderPre(event);
//	}
//
//	@SubscribeEvent
//	public static void onRebuildChunkBlockRenderInLayerEvent(final RebuildChunkBlockRenderInLayerEvent event) {
//		if (!NoCubes.isEnabled()) {
//			return;
//		}
//		OldNoCubes.renderLayer(event);
//	}
//
//	@SubscribeEvent
//	public static void onRebuildChunkBlockRenderInTypeEvent(final RebuildChunkBlockRenderInTypeEvent event) {
//		if (!NoCubes.isEnabled()) {
//			return;
//		}
//		OldNoCubes.renderType(event);
//	}

	@SubscribeEvent
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		OldNoCubes.renderBlock(event);
	}

//	@SubscribeEvent
//	public static void onRebuildChunkPostEvent(final RebuildChunkPostEvent event) {
//		if (!NoCubes.isEnabled()) {
//			return;
//		}
//		OldNoCubes.renderPost(event);
//	}

}
