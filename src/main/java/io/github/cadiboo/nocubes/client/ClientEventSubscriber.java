package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModReference;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
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

		if (ModConfig.shouldExtendLiquids)
			ClientUtil.extendLiquids(event);

		ModConfig.activeStableRenderingAlgorithm.renderPre(event);
	}

	@SubscribeEvent
	public static void onRebuildChunkBlockRenderInLayerEvent(final RebuildChunkBlockRenderInLayerEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (ModConfig.debug.debugEnabled) {
			return;
		}

		ModConfig.activeStableRenderingAlgorithm.renderLayer(event);
	}

	@SubscribeEvent
	public static void onRebuildChunkBlockRenderInTypeEvent(final RebuildChunkBlockRenderInTypeEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (ModConfig.debug.debugEnabled) {
			return;
		}

		ModConfig.activeStableRenderingAlgorithm.renderType(event);
	}

	@SubscribeEvent
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (ModConfig.debug.debugEnabled) {
			return;
		}


		final BlockRenderLayer blockRenderLayer = BlockRenderLayer.SOLID;
		final BufferBuilder bufferBuilder = event.getGenerator().getRegionRenderCacheBuilder().getWorldRendererByLayer(blockRenderLayer);
		final CompiledChunk compiledChunk = event.getCompiledChunk();
		final BlockPos.MutableBlockPos renderChunkPos = event.getRenderChunkPosition();

		if (!compiledChunk.isLayerStarted(blockRenderLayer)) {
			compiledChunk.setLayerStarted(blockRenderLayer);
			ClientUtil.compiledChunk_setLayerUsed(compiledChunk, blockRenderLayer);
			//pre render blocks
			bufferBuilder.begin(7, DefaultVertexFormats.BLOCK);
			bufferBuilder.setTranslation((double) (-renderChunkPos.getX()), (double) (-renderChunkPos.getY()), (double) (-renderChunkPos.getZ()));

		}

//		if (new Random().nextInt(10) == 0)
//			event.getBlockRendererDispatcher().renderBlock(Blocks.LAVA.getDefaultState(), event.getBlockPos(), event.getChunkCache(), bufferBuilder);

		ModConfig.activeStableRenderingAlgorithm.renderBlock(event);
	}

	@SubscribeEvent
	public static void onRebuildChunkPostEvent(final RebuildChunkPostEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (ModConfig.debug.debugEnabled) {
			return;
		}

		ModConfig.activeStableRenderingAlgorithm.renderPost(event);
	}

}
