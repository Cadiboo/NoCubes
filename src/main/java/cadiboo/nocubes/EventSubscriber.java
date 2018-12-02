package cadiboo.nocubes;

import cadiboo.nocubes.config.ModConfig;
import cadiboo.nocubes.util.ModUtil;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.HashSet;

@Mod.EventBusSubscriber(Side.CLIENT)
public class EventSubscriber {

	// rebuildChunkPos -> {liquidBlockPositions}
	private static final HashMap<BlockPos, HashSet<BlockPos>> renderedLiquidBlocks = new HashMap<BlockPos, HashSet<BlockPos>>();

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {

		//		NoCubes.LOGGER.info("onRebuildChunkBlockEvent");

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (ModConfig.shouldSmoothLiquids) {
			final BlockPos blockPos = event.getBlockPos();
			final BlockPos renderChunkPos = event.getRenderChunkPosition();

			final IBlockState state = event.getBlockState();
			final Block block = state.getBlock();
			final ChunkCache chunkCache = event.getChunkCache();
			final BufferBuilder bufferBuilder = event.getBufferBuilder();
			if (block instanceof BlockLiquid) {

				for (EnumFacing facing : EnumFacing.VALUES) {
					final BlockPos facingPos = blockPos.offset(facing);
					final IBlockState facingState = chunkCache.getBlockState(facingPos);
					if (!ModUtil.shouldSmooth(facingState)) continue;

					event.getBlockRendererDispatcher().renderBlock(state, facingPos, chunkCache, bufferBuilder);

				}

			}

			// renderChunkPos == currentPos should only be happen once for every chunk
			// if new chunk (need to clear cache)

//			if (renderedLiquidBlocks.get(blockPos) == null) {
//				renderedLiquidBlocks.put(renderChunkPos, new HashSet<>());
//			} else {
//				// current chunk
//			}
//
//			final HashSet<BlockPos> liquidBlockPositions = renderedLiquidBlocks.get(renderChunkPos);
//
//			if (!liquidBlockPositions.contains(blockPos)) {
//				final IBlockState state = event.getBlockState();
//				final Block block = state.getBlock();
//
//				blockLiquidIf:
//				if (block instanceof BlockLiquid) {
//
//					final ChunkCache chunkCache = event.getChunkCache();
//
//					if (!RenderChunkRebuildChunkHooksHooks.canBlockRenderInLayer(event.getRenderChunk(), chunkCache, event.getGenerator(), event.getCompiledChunk(), event.getBlockRendererDispatcher(), event.getRenderChunkPosition(), event.getVisGraph(), event.getBlockPos(), block, state, event.getBlockRenderLayer()))
//						break blockLiquidIf;
//
//					final BufferBuilder bufferBuilder = event.getBufferBuilder();
//
//					for (EnumFacing facing : EnumFacing.VALUES) {
//
//						final BlockPos facingPos = blockPos.offset(facing);
//						if (liquidBlockPositions.contains(facingPos)) continue;
//
//						final IBlockState facingState = chunkCache.getBlockState(facingPos);
//						if (!ModUtil.shouldSmooth(facingState)) continue;
//
//						liquidBlockPositions.add(facingPos);
//						event.getBlockRendererDispatcher().renderBlock(state, facingPos, chunkCache, bufferBuilder);
//
//					}
//				}
//			}
		}

		event.setCanceled(true);

		ModConfig.activeRenderingAlgorithm.renderBlock(event);

	}

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkBlockRenderInLayerEvent(final RebuildChunkBlockRenderInLayerEvent event) {

	}

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public static void onRebuildChunkBlockRenderInTypeEvent(final RebuildChunkBlockRenderInTypeEvent event) {

		//		if(event.getBlockState().getMaterial()== Material.AIR) {
		event.setResult(Event.Result.ALLOW);
		//		}

	}

}
