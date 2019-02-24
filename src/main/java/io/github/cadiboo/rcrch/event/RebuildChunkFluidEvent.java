//package io.github.cadiboo.rcrch.event;
//
//import net.minecraft.client.renderer.BlockRendererDispatcher;
//import net.minecraft.client.renderer.BufferBuilder;
//import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
//import net.minecraft.client.renderer.chunk.CompiledChunk;
//import net.minecraft.client.renderer.chunk.RenderChunk;
//import net.minecraft.client.renderer.chunk.VisGraph;
//import net.minecraft.fluid.IFluidState;
//import net.minecraft.util.BlockRenderLayer;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.ChunkCache;
//import net.minecraft.world.World;
//import net.minecraftforge.fml.common.eventhandler.Cancelable;
//import net.minecraftforge.fml.common.eventhandler.Event;
//
//import javax.annotation.Nonnull;
//import java.util.HashSet;
//import java.util.Random;
//
///**
// * @author Cadiboo
// */
//@Event.HasResult
//@Cancelable
//public class RebuildChunkFluidEvent extends RebuildChunkEvent {
//
//	@Nonnull
//	private final ChunkCache renderChunkCache;
//	@Nonnull
//	private final VisGraph visGraph;
//	@Nonnull
//	private final HashSet tileEntitiesWithGlobalRenderers;
//	@Nonnull
//	private final boolean[] usedBlockRenderLayers;
//	@Nonnull
//	private final Random random;
//	@Nonnull
//	private final BlockRendererDispatcher blockRendererDispatcher;
//	@Nonnull
//	private final BlockPos blockPos;
//	@Nonnull
//	private final IFluidState fluidState;
//	@Nonnull
//	private final BlockRenderLayer blockRenderLayer;
//	private final int blockRenderLayerOrdinal;
//	@Nonnull
//	private final BufferBuilder bufferBuilder;
//
//	public RebuildChunkFluidEvent(
//			@Nonnull final RenderChunk renderChunk,
//			final float x,
//			final float y,
//			final float z,
//			@Nonnull final ChunkCompileTaskGenerator generator,
//			@Nonnull final CompiledChunk compiledChunk,
//			@Nonnull final BlockPos renderChunkStartPosition,
//			@Nonnull final BlockPos renderChunkEndPosition,
//			@Nonnull final World world,
//			@Nonnull final ChunkCache renderChunkCache,
//			@Nonnull final VisGraph visGraph,
//			@Nonnull final HashSet tileEntitiesWithGlobalRenderers,
//			@Nonnull final boolean[] usedBlockRenderLayers,
//			@Nonnull final Random random,
//			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
//			@Nonnull final BlockPos blockPos,
//			@Nonnull final IFluidState fluidState,
//			@Nonnull final BlockRenderLayer blockRenderLayer,
//			final int blockRenderLayerOrdinal,
//			@Nonnull final BufferBuilder bufferBuilder
//	) {
//		super(renderChunk, x, y, z, generator, compiledChunk, renderChunkStartPosition, renderChunkEndPosition, world);
//		this.renderChunkCache = renderChunkCache;
//		this.visGraph = visGraph;
//		this.tileEntitiesWithGlobalRenderers = tileEntitiesWithGlobalRenderers;
//		this.usedBlockRenderLayers = usedBlockRenderLayers;
//		this.random = random;
//		this.blockRendererDispatcher = blockRendererDispatcher;
//		this.blockPos = blockPos;
//		this.fluidState = fluidState;
//		this.blockRenderLayer = blockRenderLayer;
//		this.blockRenderLayerOrdinal = blockRenderLayerOrdinal;
//		this.bufferBuilder = bufferBuilder;
//	}
//
//	@Nonnull
//	public ChunkCache getChunkCache() {
//		return renderChunkCache;
//	}
//
//	@Nonnull
//	public VisGraph getVisGraph() {
//		return visGraph;
//	}
//
//	@Nonnull
//	public HashSet getTileEntitiesWithGlobalRenderers() {
//		return tileEntitiesWithGlobalRenderers;
//	}
//
//	@Nonnull
//	public boolean[] getUsedBlockRenderLayers() {
//		return usedBlockRenderLayers;
//	}
//
//	@Nonnull
//	public Random getRandom() {
//		return random;
//	}
//
//	@Nonnull
//	public BlockRendererDispatcher getBlockRendererDispatcher() {
//		return blockRendererDispatcher;
//	}
//
//	@Nonnull
//	public BlockPos getBlockPos() {
//		return blockPos;
//	}
//
//	@Nonnull
//	public IFluidState getFluidState() {
//		return fluidState;
//	}
//
//	@Nonnull
//	public BlockRenderLayer getBlockRenderLayer() {
//		return blockRenderLayer;
//	}
//
//	public int getBlockRenderLayerOrdinal() {
//		return blockRenderLayerOrdinal;
//	}
//
//	@Nonnull
//	public BufferBuilder getBufferBuilder() {
//		return bufferBuilder;
//	}
//
//}
