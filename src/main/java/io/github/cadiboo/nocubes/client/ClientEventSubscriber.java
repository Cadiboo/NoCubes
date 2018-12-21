package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModReference;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

/**
 * Subscribe to events that should be handled on the PHYSICAL CLIENT in this class
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = ModReference.MOD_ID, value = CLIENT)
public final class ClientEventSubscriber {

	public static void on(final RebuildChunkPreEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderPre(event);
	}

	public static void on(final RebuildChunkBlockRenderInLayerEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderLayer(event);
	}

	public static void on(final RebuildChunkBlockRenderInTypeEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderType(event);
	}

	public static void on(final RebuildChunkBlockEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderBlock(event);
	}

	public static void on(final RebuildChunkPostEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderPost(event);
	}

}
